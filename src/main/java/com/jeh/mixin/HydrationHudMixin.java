package com.jeh.mixin;

import com.jeh.common.utils.HydrationUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HydrationHudMixin {
    private static final int BAR_SIZE = 9;
    private static final int MAX_HYDRATION = 20;

    private static final Identifier TEX_FULL = Identifier.fromNamespaceAndPath("jeh", "textures/gui/hydration_full.png");
    private static final Identifier TEX_HALF = Identifier.fromNamespaceAndPath("jeh", "textures/gui/hydration_half.png");
    private static final Identifier TEX_EMPTY = Identifier.fromNamespaceAndPath("jeh", "textures/gui/hydration_empty.png");

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onExtractRenderState(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.player.isCreative() || client.player.isSpectator()) return;

        int hydration = HydrationUtils.getHydrationLevel(client.player);
        int hydrationBars = (int) Math.ceil(Math.max(0, Math.min(MAX_HYDRATION, hydration)) / 2.0);
        int maxBars = MAX_HYDRATION / 2;

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();

        // 1px gap above the hunger bar
        int y = screenHeight - 49; 

        // Safely check if air bubbles are rendering (player is submerged/losing air)
        // If they are, shift the hydration HUD up another 10 pixels to make room for them!
        if (client.player.getAirSupply() < client.player.getMaxAirSupply()) {
            y -= 10; // Becomes screenHeight - 59
        }

        // Shift one icon to the left (8px) for alignment
        int startX = screenWidth / 2 + 91 - 1 - 8;
        int spacing = BAR_SIZE - 1;

        for (int i = 0; i < maxBars; i++) {
            int barX = startX - i * spacing;
            boolean filled = i < hydrationBars;
            boolean half = i == hydrationBars - 1 && hydration % 2 != 0;

            Identifier tex = TEX_EMPTY;
            if (filled && !half) {
                tex = TEX_FULL;
            } else if (half) {
                tex = TEX_HALF;
            }

            // Note: If textures still look stretched/weird, try removing RenderPipelines.GUI_TEXTURED 
            // or ensure your .png files are strictly 9x9 pixels on disk.
            context.blit(RenderPipelines.GUI_TEXTURED, tex, barX, y, 0f, 0f, BAR_SIZE, BAR_SIZE, BAR_SIZE, BAR_SIZE);
        }
    }
}