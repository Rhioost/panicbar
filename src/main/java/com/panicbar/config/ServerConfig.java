package com.panicbar.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Damage settings
    public static final ForgeConfigSpec.DoubleValue DAMAGE_MULTIPLIER;

    // Mob detection settings
    public static final ForgeConfigSpec.BooleanValue LINE_OF_SIGHT_ENABLED;
    public static final ForgeConfigSpec.DoubleValue DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue FILL_PER_MOB_PER_TICK;
    public static final ForgeConfigSpec.IntValue MAX_MOBS_COUNTED;

    // Mob HP scaling (tougher mobs are scarier)
    public static final ForgeConfigSpec.BooleanValue HP_SCALE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue HP_SCALE_REFERENCE_HEALTH;
    public static final ForgeConfigSpec.DoubleValue HP_SCALE_MIN_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue HP_SCALE_MAX_MULTIPLIER;

    // Food-based panic reduction
    public static final ForgeConfigSpec.BooleanValue SATURATION_REDUCTION_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SATURATION_REDUCTION_PER_POINT;
    public static final ForgeConfigSpec.BooleanValue ABSORPTION_REDUCTION_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ABSORPTION_REDUCTION_PER_LEVEL;

    // Bar fill/decay tuning
    public static final ForgeConfigSpec.DoubleValue DECAY_PER_TICK;
    public static final ForgeConfigSpec.DoubleValue DECAY_DELAY_TICKS;
    public static final ForgeConfigSpec.DoubleValue MAX_PANIC_VALUE;

    // Block placement lock
    public static final ForgeConfigSpec.BooleanValue BLOCK_LOCK_ENABLED;
    public static final ForgeConfigSpec.DoubleValue BLOCK_LOCK_THRESHOLD_PERCENT;
    public static final ForgeConfigSpec.IntValue BLOCK_LOCK_COOLDOWN_TICKS;
    public static final ForgeConfigSpec.BooleanValue BLOCK_LOCK_RESET_ON_RETRIGGER;
    public static final ForgeConfigSpec.BooleanValue BLOCK_LOCK_SHOW_MESSAGE;

    static {
        BUILDER.push("Damage");
        DAMAGE_MULTIPLIER = BUILDER
                .comment("How much panic is added per point of damage taken. panic += damage * this value.")
                .defineInRange("damageMultiplier", 0.08D, 0.0D, 10.0D);
        BUILDER.pop();

        BUILDER.push("Mob Detection");
        LINE_OF_SIGHT_ENABLED = BUILDER
                .comment("If true, a targeting mob only counts toward panic when it has direct line of sight to you.",
                          "If false, any hostile mob currently targeting/chasing you counts, LOS or not.")
                .define("lineOfSightEnabled", true);

        DETECTION_RADIUS = BUILDER
                .comment("Radius (in blocks) around the player to scan for hostile mobs.")
                .defineInRange("detectionRadius", 24.0D, 1.0D, 128.0D);

        FILL_PER_MOB_PER_TICK = BUILDER
                .comment("How much panic is added per qualifying mob, per game tick.",
                          "Lower = slower/gentler fill; higher = fills up fast with just a couple mobs.")
                .defineInRange("fillPerMobPerTick", 0.0015D, 0.0D, 1.0D);

        MAX_MOBS_COUNTED = BUILDER
                .comment("Maximum number of qualifying mobs counted per tick (prevents huge hordes from instantly maxing the bar).")
                .defineInRange("maxMobsCounted", 10, 1, 200);
        BUILDER.pop();

        BUILDER.push("Mob HP Scaling");
        HP_SCALE_ENABLED = BUILDER
                .comment("If true, tougher (higher max HP) mobs contribute more panic per tick than weaker ones.")
                .define("hpScaleEnabled", true);

        HP_SCALE_REFERENCE_HEALTH = BUILDER
                .comment("Max HP treated as the '1x' baseline for fright (a zombie/skeleton is 20). ",
                          "A mob with double this HP contributes ~2x as much panic per tick, half as much HP contributes ~0.5x.")
                .defineInRange("hpScaleReferenceHealth", 20.0D, 1.0D, 10000.0D);

        HP_SCALE_MIN_MULTIPLIER = BUILDER
                .comment("Floor on the HP-based multiplier, so very weak mobs still contribute something.")
                .defineInRange("hpScaleMinMultiplier", 0.25D, 0.0D, 10.0D);

        HP_SCALE_MAX_MULTIPLIER = BUILDER
                .comment("Cap on the HP-based multiplier, so a single very tanky mob (bosses, modded mobs) can't instantly max the bar.")
                .defineInRange("hpScaleMaxMultiplier", 5.0D, 1.0D, 100.0D);
        BUILDER.pop();

        BUILDER.push("Food-Based Panic Reduction");
        SATURATION_REDUCTION_ENABLED = BUILDER
                .comment("If true, eating food reduces panic based on how much saturation it restores.")
                .define("saturationReductionEnabled", true);

        SATURATION_REDUCTION_PER_POINT = BUILDER
                .comment("Panic removed per point of saturation restored by food eaten (e.g. steak restores a lot, an apple restores little).")
                .defineInRange("saturationReductionPerPoint", 0.02D, 0.0D, 5.0D);

        ABSORPTION_REDUCTION_ENABLED = BUILDER
                .comment("If true, gaining the Absorption effect (golden apples, etc.) reduces panic - more than plain saturation does.")
                .define("absorptionReductionEnabled", true);

        ABSORPTION_REDUCTION_PER_LEVEL = BUILDER
                .comment("Flat panic removed per Absorption effect application, multiplied by (amplifier + 1) - so Absorption II removes 2x this.")
                .defineInRange("absorptionReductionPerLevel", 0.15D, 0.0D, 5.0D);
        BUILDER.pop();

        BUILDER.push("Fill & Decay Tuning");
        DECAY_PER_TICK = BUILDER
                .comment("How much panic drains per tick once decay starts (no threats and no recent damage).")
                .defineInRange("decayPerTick", 0.0025D, 0.0D, 1.0D);

        DECAY_DELAY_TICKS = BUILDER
                .comment("Ticks to wait after the last threat/damage before the bar starts decaying.")
                .defineInRange("decayDelayTicks", 40.0D, 0.0D, 2000.0D);

        MAX_PANIC_VALUE = BUILDER
                .comment("Internal cap for the panic value (bar is always displayed as a 0-1 percentage of this).")
                .defineInRange("maxPanicValue", 1.0D, 0.01D, 100.0D);
        BUILDER.pop();

        BUILDER.push("Block Placement Lock");
        BLOCK_LOCK_ENABLED = BUILDER
                .comment("If true, the player cannot place blocks while panicking at/above the threshold, and during the cool-off period after.")
                .define("blockLockEnabled", true);

        BLOCK_LOCK_THRESHOLD_PERCENT = BUILDER
                .comment("Panic percentage (0.0-1.0) at which block placement locks and the alternate full/locked sprite (red with white outline) is shown.")
                .defineInRange("blockLockThresholdPercent", 1.0D, 0.01D, 1.0D);

        BLOCK_LOCK_COOLDOWN_TICKS = BUILDER
                .comment("How long (in ticks, 20 = 1 second) the block placement lock and full/locked sprite persist after triggering, ",
                          "even if panic drops back below the threshold before the cooldown finishes.")
                .defineInRange("blockLockCooldownTicks", 100, 0, 24000);

        BLOCK_LOCK_RESET_ON_RETRIGGER = BUILDER
                .comment("If true, hitting the threshold again while already locked out restarts the cooldown timer from its full value.")
                .define("blockLockResetOnRetrigger", true);

        BLOCK_LOCK_SHOW_MESSAGE = BUILDER
                .comment("If true, shows a brief action-bar message when a block placement attempt is blocked.")
                .define("blockLockShowMessage", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
