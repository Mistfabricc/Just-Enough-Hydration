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
        Item item = stack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return -1;
        String path = id.getPath();

        if (path.equals("mushroom_stew") || path.equals("beetroot_soup")
            || path.equals("rabbit_stew") || path.equals("suspicious_stew")) {
            return 3;
        }
        if (path.equals("milk_bucket")) return 4;
        if (path.equals("honey_bottle")) return 6;
        if (path.equals("golden_apple") || path.equals("enchanted_golden_apple")) return 5;

        if (path.equals("pufferfish")) return -6;
        if (path.equals("rotten_flesh")) return -5;

        if (path.equals("beef") || path.equals("chicken") || path.equals("porkchop")
            || path.equals("rabbit") || path.equals("mutton")) {
            return -4;
        }

        if (path.equals("cooked_beef") || path.equals("cooked_chicken")
            || path.equals("cooked_porkchop") || path.equals("cooked_rabbit")
            || path.equals("cooked_mutton")) {
            return -1;
        }

        if (path.equals("cod") || path.equals("salmon")) return -3;
        if (path.equals("cooked_cod") || path.equals("cooked_salmon")) return -1;
        if (path.equals("spider_eye")) return -3;

        if (path.equals("potato") || path.equals("baked_potato") || path.equals("poisonous_potato")
            || path.equals("carrot") || path.equals("beetroot") || path.equals("dried_kelp")
            || path.equals("sweet_berries") || path.equals("glow_berries")
            || path.equals("chorus_fruit")) {
            return -2;
        }

        return -1;
    }
}