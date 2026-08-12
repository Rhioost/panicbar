package com.panicbar.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.panicbar.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class PanicBarOverlay implements IGuiOverlay {

    public static final ResourceLocation TEXTURE =
            new ResourceLocation("panicbar", "textures/gui/panic_bar.png");
    private static final int TEX_WIDTH = 182;
    private static final int TEX_HEIGHT = 15;
    private static final int SRC_BAR_WIDTH = 182;
    private static final int SRC_BAR_HEIGHT = 5;
    private static final int BG_V = 0;
    private static final int FILL_V = 5;
    private static final int FULL_V = 10;

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics guiGraphics,
                        float partialTick, int screenWidth, int screenHeight) {

        float percent = ClientPanicState.getPanicPercent();
        boolean lockedOut = ClientPanicState.isLockedOut();

        if (percent <= 0f && !lockedOut && ClientConfig.HIDE_WHEN_EMPTY.get()) {
            return;
        }

        int drawWidth = ClientConfig.BAR_WIDTH.get();
        int drawHeight = ClientConfig.BAR_HEIGHT.get();

        int[] pos = resolveAnchoredPosition(screenWidth, screenHeight, drawWidth, drawHeight);
        int x = pos[0];
        int y = pos[1];

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(TEXTURE, x, y, drawWidth, drawHeight,
                0, BG_V, SRC_BAR_WIDTH, SRC_BAR_HEIGHT, TEX_WIDTH, TEX_HEIGHT);

        if (lockedOut) {
            guiGraphics.blit(TEXTURE, x, y, drawWidth, drawHeight,
                    0, FULL_V, SRC_BAR_WIDTH, SRC_BAR_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
        } else {
            int filledSrcWidth = Math.max(0, Math.round(SRC_BAR_WIDTH * percent));
            int filledDrawWidth = Math.max(0, Math.round(drawWidth * percent));
            if (filledSrcWidth > 0) {
                guiGraphics.blit(TEXTURE, x, y, filledDrawWidth, drawHeight,
                        0, FILL_V, filledSrcWidth, SRC_BAR_HEIGHT, TEX_WIDTH, TEX_HEIGHT);
            }
        }

        RenderSystem.disableBlend();
    }

    private int[] resolveAnchoredPosition(int screenWidth, int screenHeight, int drawWidth, int drawHeight) {
        int offX = ClientConfig.BAR_X.get();
        int offY = ClientConfig.BAR_Y.get();
        ClientConfig.AnchorPoint anchor = ClientConfig.BAR_ANCHOR.get();

        int x;
        int y;

        switch (anchor) {
            case TOP_LEFT -> { x = 0 + offX; y = 0 + offY; }
            case TOP_CENTER -> { x = (screenWidth - drawWidth) / 2 + offX; y = 0 + offY; }
            case TOP_RIGHT -> { x = screenWidth - drawWidth - offX; y = 0 + offY; }
            case CENTER -> { x = (screenWidth - drawWidth) / 2 + offX; y = (screenHeight - drawHeight) / 2 + offY; }
            case BOTTOM_LEFT -> { x = 0 + offX; y = screenHeight - drawHeight - offY; }
            case BOTTOM_RIGHT -> { x = screenWidth - drawWidth - offX; y = screenHeight - drawHeight - offY; }
            case BOTTOM_CENTER -> { x = (screenWidth - drawWidth) / 2 + offX; y = screenHeight - drawHeight - offY; }
            default -> { x = offX; y = offY; }
        }

        return new int[] { x, y };
    }
}
// time wasted here 3 hours I fucking hate doing gui's please fucking kill me this should've taken 15 minutes