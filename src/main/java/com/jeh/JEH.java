package com.jeh;
 
import com.jeh.common.events.HydrationEvents;
import com.jeh.common.events.PlayerTickHandler;
import com.jeh.common.networking.HydrationSyncPayload;
import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JEH implements ModInitializer {
    public static final String MOD_ID = "jeh";
    public static final Identifier HYDRATION_SYNC_ID = Identifier.fromNamespaceAndPath(MOD_ID, "sync_hydration");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Just Enough Food And Hydration - Loading...");

        PayloadTypeRegistry.clientboundPlay().register(HydrationSyncPayload.TYPE, HydrationSyncPayload.CODEC);

        HydrationEvents.register();
        PlayerTickHandler.register();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("hydrate")
                .executes(context -> {
                    var player = context.getSource().getPlayer();
                    if (player != null) {
                        HydrationUtils.addHydration(player, 5);
                        player.sendSystemMessage(Component.literal("§aAdded 5 hydration!"));
                        return 1;
                    }
                    return 0;
                })
            );
        });

        LOGGER.info("JEH has been successfully loaded!");
        LOGGER.info("Use /hydrate to add hydration for testing.");
    }
}
