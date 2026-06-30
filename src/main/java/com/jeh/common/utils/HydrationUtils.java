package com.jeh.common.utils;
 
import com.jeh.common.data.HydrationAccess;
import com.jeh.common.data.HydrationData;
import com.jeh.common.networking.HydrationSyncPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HydrationUtils {
    public static HydrationData getHydrationData(Player player) {
        return ((HydrationAccess) player).getHydrationData();
    }

    public static void setHydrationData(Player player, HydrationData data) {
        ((HydrationAccess) player).setHydrationData(data);
    }

    public static void addHydration(Player player, int amount) {
        HydrationData data = getHydrationData(player);
        data.addHydration(amount);
        setHydrationData(player, data);
        syncToClient(player);
    }

    public static int getHydrationLevel(Player player) {
        return getHydrationData(player).getHydrationLevel();
    }

    public static void setHydrationLevel(Player player, int level) {
        HydrationData data = getHydrationData(player);
        data.setHydrationLevel(level);
        setHydrationData(player, data);
        syncToClient(player);
    }

    public static boolean isDehydrated(Player player) {
        return getHydrationLevel(player) <= 0;
    }

    public static boolean isThirsty(Player player) {
        return getHydrationLevel(player) <= 5;
    }

    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new HydrationSyncPayload(getHydrationLevel(player)));
        }
    }

    public static int getFoodHydrationCost(ItemStack stack) {
        return 0;
    }
}