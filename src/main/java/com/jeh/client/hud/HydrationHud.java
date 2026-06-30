package com.jeh.client.hud;

import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HydrationHud implements HudElement {
    private static final int BAR_SIZE = 9;
    private static final int MAX_HYDRATION = 20;
    private static final int COLOR_FULL = 0xFF3399FF;
    private static final int COLOR_EMPTY = 0xFF222222;

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

            if (filled && !half) {
                context.fill(barX, y, barX + BAR_SIZE, y + BAR_SIZE, COLOR_FULL);
            } else if (half) {
                context.fill(barX, y, barX + 4, y + BAR_SIZE, COLOR_FULL);
                context.fill(barX + 4, y, barX + BAR_SIZE, y + BAR_SIZE, COLOR_EMPTY);
            } else {
                context.fill(barX, y, barX + BAR_SIZE, y + BAR_SIZE, COLOR_EMPTY);
            }
        }
    }
}
