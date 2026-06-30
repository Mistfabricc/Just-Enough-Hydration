package com.jeh.client.hud;

import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class HydrationHud implements HudElement {
    private static final int BAR_SIZE = 9;
    private static final int MAX_HYDRATION = 20;

    private static final Identifier TEX_FULL = Identifier.fromNamespaceAndPath("jeh", "textures/gui/hydration_full.png");
    private static final Identifier TEX_HALF = Identifier.fromNamespaceAndPath("jeh", "textures/gui/hydration_half.png");
    private static final Identifier TEX_EMPTY = Identifier.fromNamespaceAndPath("jeh", "textures/gui/hydration_empty.png");

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        int hydration = HydrationUtils.getHydrationLevel(client.player);
        int hydrationBars = (int) Math.ceil(Math.max(0, Math.min(MAX_HYDRATION, hydration)) / 2.0);
        int maxBars = MAX_HYDRATION / 2;

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        int y = screenHeight - 39 - BAR_SIZE - 1;

        for (int i = 0; i < maxBars; i++) {
            int barX = (screenWidth / 2 + 82) - (i * 8);
            boolean filled = i < hydrationBars;
            boolean half = i == hydrationBars - 1 && hydration % 2 != 0;

            Identifier tex = TEX_EMPTY;
            if (filled && !half) {
                tex = TEX_FULL;
            } else if (half) {
                tex = TEX_HALF;
            }

            context.blit(RenderPipelines.GUI_TEXTURED, tex, barX, y, 0f, 0f, BAR_SIZE, BAR_SIZE, BAR_SIZE, BAR_SIZE);
        }
    }
}
