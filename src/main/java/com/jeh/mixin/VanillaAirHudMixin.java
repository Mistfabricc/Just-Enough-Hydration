package com.jeh.mixin;

import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(Hud.class)
public class VanillaAirHudMixin {

    /**
     * Safely targets the Y-coordinate specifically for the air bubbles.
     * The @Slice ensures we ONLY modify the Y variable that is generated 
     * near the player.getAir() check, leaving all other HUD elements untouched.
     */
    @ModifyVariable(
        method = "extractRenderState", 
        at = @At("STORE"),
        slice = @Slice(
            // Start looking for variables immediately after the game gets the player's air level
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAir()I")
        ),
        ordinal = 0 // Targets the first integer stored after the getAir() check
    )
    private int shiftAirBubblesUp(int originalY) {
        // Push the vanilla air bubbles physically higher on the screen by 10 pixels
        // (9px for your hydration bar + 1px spacing)
        return originalY - 10;
    }
}