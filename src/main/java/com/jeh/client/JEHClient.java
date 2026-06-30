package com.jeh.client;
 
import com.jeh.JEH;
import com.jeh.client.hud.HydrationHud;
import com.jeh.common.networking.HydrationSyncPayload;
import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class JEHClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.addFirst(
            Identifier.fromNamespaceAndPath(JEH.MOD_ID, "hydration"),
            new HydrationHud()
        );

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(HydrationSyncPayload.TYPE, (payload, context) -> {
                int level = payload.level();
                context.client().execute(() -> {
                    if (context.client().player != null) {
                        HydrationUtils.setHydrationLevel(context.client().player, level);
                    }
                });
            });
        });

        JEH.LOGGER.info("[JEH] Client initialized!");
        System.out.println("[JEH] Client initialized!");
    }
}