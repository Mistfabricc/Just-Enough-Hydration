package com.jeh.common.events;

import com.jeh.common.data.HydrationData;
import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class HydrationEvents {
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            HydrationUtils.syncToClient(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                HydrationData oldData = HydrationUtils.getHydrationData(oldPlayer);
                HydrationUtils.setHydrationData(newPlayer, oldData.copy());
            }
        });
    }
}
