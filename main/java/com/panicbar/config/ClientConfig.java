package com.panicbar.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue BAR_X;
    public static final ForgeConfigSpec.IntValue BAR_Y;
    public static final ForgeConfigSpec.EnumValue<AnchorPoint> BAR_ANCHOR;
    public static final ForgeConfigSpec.IntValue BAR_WIDTH;
    public static final ForgeConfigSpec.IntValue BAR_HEIGHT;
    public static final ForgeConfigSpec.BooleanValue HIDE_WHEN_EMPTY;

    public enum AnchorPoint {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    static {
        BUILDER.push("Bar Placement & Appearance");
        BAR_X = BUILDER
                .comment("X pixel offset of the bar from its anchor point.")
                .defineInRange("barX", 0, -4096, 4096);

        BAR_Y = BUILDER
                .comment("Y pixel offset of the bar from its anchor point.")
                .defineInRange("barY", 40, -4096, 4096);

        BAR_ANCHOR = BUILDER
                .comment("Which corner/edge of the screen barX/barY are measured from.")
                .defineEnum("barAnchor", AnchorPoint.BOTTOM_CENTER);

        BAR_WIDTH = BUILDER
                .comment("On-screen width of the bar in pixels. Source texture is 182px wide; this stretches/compresses it to fit.")
                .defineInRange("barWidth", 140, 20, 400);

        BAR_HEIGHT = BUILDER
                .comment("On-screen height of the bar in pixels. Source texture is 5px tall; this stretches/compresses it to fit.")
                .defineInRange("barHeight", 6, 2, 60);

        HIDE_WHEN_EMPTY = BUILDER
                .comment("If true, the bar is hidden entirely when panic is 0.")
                .define("hideWhenEmpty", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
