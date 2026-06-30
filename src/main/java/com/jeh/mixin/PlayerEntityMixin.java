package com.jeh.mixin;

import com.jeh.common.data.HydrationAccess;
import com.jeh.common.data.HydrationData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Player.class)
public class PlayerEntityMixin implements HydrationAccess {
    @Unique
    private HydrationData hydrationData = new HydrationData();

    @Override
    public HydrationData getHydrationData() {
        return hydrationData;
    }

    @Override
    public void setHydrationData(HydrationData data) {
        this.hydrationData = data;
    }
}
