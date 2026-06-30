package com.jeh.common.events;

import com.jeh.common.data.HydrationData;
import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PlayerTickHandler {
    private static final int DEHYDRATION_DAMAGE_INTERVAL = 6000;
    private static final float DEHYDRATION_DAMAGE_AMOUNT = 0.1f;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickPlayer(player);
            }
        });
    }

    private static void tickPlayer(Player player) {
        if (player.isCreative() || player.isSpectator()) return;

        HydrationData data = HydrationUtils.getHydrationData(player);

        data.tick();

        if (player.tickCount % 100 == 0) {
            System.out.println("[JEH] Hydration: " + data.getHydrationLevel() + ", Timer: " + data.getHydrationTickTimer());
        }

        // Exhaustion from actions
        if (player.isSprinting()) {
            data.addExhaustion(0.01f);
        }
        if (!player.onGround() && player.fallDistance > 0) {
            data.addExhaustion(0.005f);
        }
        if (player.isSwimming()) {
            data.addExhaustion(0.008f);
        }
        if (player.isInLava()) {
            data.addExhaustion(0.02f);
        }
        if (player.isOnFire()) {
            data.addExhaustion(0.015f);
        }

        // Detect potion and food consumption
        if (player.isUsingItem()) {
            data.setLastUseItem(player.getUseItem().copy());
            if (player.getUseItem().getItem() == Items.POTION) {
                data.setGlassBottlesBefore(countGlassBottles(player));
            }
        } else if (!data.getLastUseItem().isEmpty()) {
            ItemStack used = data.getLastUseItem();
            data.setLastUseItem(ItemStack.EMPTY);

            if (used.getItem() == Items.POTION) {
                int currentBottles = countGlassBottles(player);
                if (currentBottles > data.getGlassBottlesBefore()) {
                    HydrationUtils.addHydration(player, 20);
                }
            }
        }

        // Food detection via food level increase (reliable detection)
        int currentFood = player.getFoodData().getFoodLevel();
        if (data.getLastFoodLevel() >= 0 && currentFood > data.getLastFoodLevel()) {
            ItemStack used = data.getLastUseItem();
            if (used.getItem() != Items.POTION) {
                int cost = getFoodHydrationCost(used);
                if (cost != 0) {
                    HydrationUtils.addHydration(player, cost);
                }
            }
        }
        data.setLastFoodLevel(currentFood);

        int hydrationLevel = data.getHydrationLevel();

        // Immediate alert when hydration first hits 0
        if (hydrationLevel <= 0 && !data.isDehydrationAlertSent()) {
            player.sendSystemMessage(Component.translatable("message.jeh.dehydrated"));
            data.setDehydrationAlertSent(true);
        } else if (hydrationLevel > 0) {
            data.setDehydrationAlertSent(false);
        }

        // Hunger effect every 4 minutes for 25 seconds
        if (hydrationLevel <= 0) {
            if (player.tickCount % 4800 == 0) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.HUNGER,
                    500,
                    0,
                    false,
                    false,
                    true
                ));
            }

            int foodLevel = player.getFoodData().getFoodLevel();
            if (foodLevel <= 3) {
                if (player.tickCount % DEHYDRATION_DAMAGE_INTERVAL == 0) {
                    player.hurt(player.damageSources().starve(), DEHYDRATION_DAMAGE_AMOUNT);

                    if (player.tickCount % (DEHYDRATION_DAMAGE_INTERVAL * 2) == 0) {
                        player.sendSystemMessage(Component.translatable("message.jeh.critical"));
                    }
                }
            }
        } else if (hydrationLevel <= 5) {
            if (player.tickCount % 2400 == 0) {
                player.sendSystemMessage(Component.translatable("message.jeh.thirsty"));
            }
        }

        HydrationUtils.setHydrationData(player, data);
        HydrationUtils.syncToClient(player);
    }

    private static int countGlassBottles(Player player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == Items.GLASS_BOTTLE) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int getFoodHydrationCost(ItemStack stack) {
        Item item = stack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return 0;
        String path = id.getPath();

        // Hydrating foods (positive)
        if (path.equals("apple")) return 2;
        if (path.equals("golden_apple") || path.equals("enchanted_golden_apple")) return 4;
        if (path.equals("sweet_berries") || path.equals("glow_berries")) return 2;
        if (path.equals("melon_slice")) return 2;
        if (path.equals("chorus_fruit")) return 1;
        if (path.equals("mushroom_stew") || path.equals("beetroot_soup")
            || path.equals("rabbit_stew") || path.equals("suspicious_stew")) return 4;
        if (path.equals("milk_bucket")) return 5;
        if (path.equals("honey_bottle")) return 6;
        if (path.equals("carrot") || path.equals("golden_carrot")) return 2;
        if (path.equals("potato") || path.equals("baked_potato")) return 1;
        if (path.equals("beetroot") || path.equals("dried_kelp")) return 1;

        // Dehydrating foods (negative)
        if (path.equals("beef") || path.equals("chicken") || path.equals("porkchop")
            || path.equals("rabbit") || path.equals("mutton")) return -3;
        if (path.equals("cod") || path.equals("salmon")) return -2;
        if (path.equals("rotten_flesh")) return -5;
        if (path.equals("spider_eye")) return -4;
        if (path.equals("pufferfish")) return -8;

        if (path.equals("cooked_beef") || path.equals("cooked_chicken")
            || path.equals("cooked_porkchop") || path.equals("cooked_rabbit")
            || path.equals("cooked_mutton")) return -1;
        if (path.equals("cooked_cod") || path.equals("cooked_salmon")) return -1;

        if (path.equals("bread") || path.equals("cookie") || path.equals("cake")
            || path.equals("pumpkin_pie") || path.equals("sugar") || path.equals("tropical_fish")) return 0;

        // Default digestion cost for any other edible item not listed (e.g. suspicious stew)
        return -1;
    }
}