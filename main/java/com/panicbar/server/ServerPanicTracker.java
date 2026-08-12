package com.panicbar.server;

import com.panicbar.config.ServerConfig;
import com.panicbar.network.PanicNetwork;
import com.panicbar.network.PanicSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Server calculation process for panic value and lockout state.
@Mod.EventBusSubscriber(modid = "panicbar")
public class ServerPanicTracker {

    private static class State {
        double panicValue = 0.0D;
        int ticksSinceLastThreatOrDamage = Integer.MAX_VALUE;
        int lockoutTicksRemaining = 0;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    public static boolean isLockedOut(ServerPlayer player) {
        if (!ServerConfig.BLOCK_LOCK_ENABLED.get()) return false;
        State state = STATES.get(player.getUUID());
        return state != null && state.lockoutTicksRemaining > 0;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        state.panicValue += event.getAmount() * ServerConfig.DAMAGE_MULTIPLIER.get();
        state.ticksSinceLastThreatOrDamage = 0;
    }

    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (!ServerConfig.SATURATION_REDUCTION_ENABLED.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();
        FoodProperties food = stack.getItem().getFoodProperties(stack, player);
        if (food == null) return;

        // Mirrors the vanilla FoodData.eat() saturation formula.
        float saturationRestored = Math.min(food.getNutrition() * food.getSaturationModifier() * 2.0F, 20.0F);
        if (saturationRestored <= 0f) return;

        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        double reduction = saturationRestored * ServerConfig.SATURATION_REDUCTION_PER_POINT.get();
        state.panicValue = Math.max(0.0D, state.panicValue - reduction);
    }

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!ServerConfig.ABSORPTION_REDUCTION_ENABLED.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getEffectInstance() == null || event.getEffectInstance().getEffect() != MobEffects.ABSORPTION) return;

        int amplifier = event.getEffectInstance().getAmplifier();
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        double reduction = ServerConfig.ABSORPTION_REDUCTION_PER_LEVEL.get() * (amplifier + 1);
        state.panicValue = Math.max(0.0D, state.panicValue - reduction);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        boolean contributedThisTick = false;

        double radius = ServerConfig.DETECTION_RADIUS.get();
        AABB scanBox = player.getBoundingBox().inflate(radius);
        boolean losRequired = ServerConfig.LINE_OF_SIGHT_ENABLED.get();
        int maxCounted = ServerConfig.MAX_MOBS_COUNTED.get();

        List<Mob> nearby = player.level().getEntitiesOfClass(Mob.class, scanBox,
                mob -> mob.isAlive() && mob instanceof Enemy && mob.getTarget() == player);

        int qualifying = 0;
        double weightedContribution = 0.0D;
        boolean hpScaleEnabled = ServerConfig.HP_SCALE_ENABLED.get();
        double referenceHealth = ServerConfig.HP_SCALE_REFERENCE_HEALTH.get();
        double minMultiplier = ServerConfig.HP_SCALE_MIN_MULTIPLIER.get();
        double maxMultiplier = ServerConfig.HP_SCALE_MAX_MULTIPLIER.get();

        for (Mob mob : nearby) {
            if (qualifying >= maxCounted) break;
            if (!losRequired || mob.hasLineOfSight(player)) {
                qualifying++;
                double weight = 1.0D;
                if (hpScaleEnabled && referenceHealth > 0) {
                    weight = mob.getMaxHealth() / referenceHealth;
                    weight = Math.max(minMultiplier, Math.min(maxMultiplier, weight));
                }
                weightedContribution += weight;
            }
        }

        if (qualifying > 0) {
            state.panicValue += weightedContribution * ServerConfig.FILL_PER_MOB_PER_TICK.get();
            contributedThisTick = true;
        }

        if (contributedThisTick) {
            state.ticksSinceLastThreatOrDamage = 0;
        } else {
            state.ticksSinceLastThreatOrDamage++;
            if (state.ticksSinceLastThreatOrDamage >= ServerConfig.DECAY_DELAY_TICKS.get()) {
                state.panicValue -= ServerConfig.DECAY_PER_TICK.get();
            }
        }

        double max = ServerConfig.MAX_PANIC_VALUE.get();
        state.panicValue = Math.max(0.0D, Math.min(max, state.panicValue));

        double thresholdPercent = ServerConfig.BLOCK_LOCK_THRESHOLD_PERCENT.get();
        boolean atOrAboveThreshold = (max > 0) && (state.panicValue / max) >= thresholdPercent;

        if (atOrAboveThreshold) {
            int cooldown = ServerConfig.BLOCK_LOCK_COOLDOWN_TICKS.get();
            if (state.lockoutTicksRemaining <= 0 || ServerConfig.BLOCK_LOCK_RESET_ON_RETRIGGER.get()) {
                state.lockoutTicksRemaining = cooldown;
            }
        } else if (state.lockoutTicksRemaining > 0) {
            state.lockoutTicksRemaining--;
        }

        boolean lockedOutNow = state.lockoutTicksRemaining > 0 && ServerConfig.BLOCK_LOCK_ENABLED.get();
        float percentNow = max > 0 ? (float) Math.max(0.0D, Math.min(1.0D, state.panicValue / max)) : 0f;
        PanicNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new PanicSyncPacket(percentNow, lockedOutNow));
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }
}
