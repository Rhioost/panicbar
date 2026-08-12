package com.panicbar.server;

import com.panicbar.config.ServerConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// it make you place no block and no use right click 
@Mod.EventBusSubscriber(modid = "panicbar")
public class BlockPlacementLock {

    private static final int MESSAGE_COOLDOWN_TICKS = 20; // once per second max, I probably should make this a config option for this. 

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ServerConfig.BLOCK_LOCK_ENABLED.get()) return;
        if (!ServerPanicTracker.isLockedOut(player)) return;

        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);

        if (ServerConfig.BLOCK_LOCK_SHOW_MESSAGE.get()) {
            long now = player.level().getGameTime();
            Long last = lastMessageTick.get(player.getUUID());
            if (last == null || now - last >= MESSAGE_COOLDOWN_TICKS) {
                player.displayClientMessage(
                        Component.literal("You're too panicked to place blocks right now!"), true);
                lastMessageTick.put(player.getUUID(), now);
            }
        }
    }

    private static final java.util.Map<java.util.UUID, Long> lastMessageTick = new java.util.HashMap<>();
}
