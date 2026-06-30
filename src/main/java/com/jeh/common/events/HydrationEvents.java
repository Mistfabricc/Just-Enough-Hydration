package com.jeh.common.events;

import com.jeh.common.data.HydrationCache;
import com.jeh.common.data.HydrationData;
import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class HydrationEvents {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var player = handler.getPlayer();
            HydrationData cached = HydrationCache.load(player.getUUID());
            if (cached != null) {
                HydrationUtils.setHydrationData(player, cached);
            }
            HydrationUtils.syncToClient(player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            HydrationData data = HydrationUtils.getHydrationData(handler.getPlayer());
            HydrationCache.save(handler.getPlayer().getUUID(), data);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                HydrationData oldData = HydrationUtils.getHydrationData(oldPlayer);
                HydrationUtils.setHydrationData(newPlayer, oldData.copy());
            }
        });
    }
}
