package com.jeh.mixin;

import com.jeh.common.utils.HydrationUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class DamageHydrationMixin {
    @Inject(
        method = "hurt",
        at = @At("HEAD"),
        require = 0
    )
    private void onHurt(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player) {
            int cost = Math.max(1, (int) (amount / 2));
            HydrationUtils.addHydration(player, -cost);
        }
    }
}