package com.jeh.common.events;
 
import com.jeh.common.data.HydrationData;
import com.jeh.common.utils.HydrationUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
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
        HydrationData data = HydrationUtils.getHydrationData(player);

        data.tick();

        if (player.tickCount % 100 == 0) {
            System.out.println("[JEFAH] Hydration: " + data.getHydrationLevel() + ", Timer: " + data.getHydrationTickTimer());
        }

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

        // FOOD: track use and detect by food level increase
        if (player.isUsingItem()) {
            data.setLastUseItem(player.getUseItem().copy());
        } else if (!data.getLastUseItem().isEmpty()) {
            int currentFood = player.getFoodData().getFoodLevel();
            if (currentFood > data.getLastFoodLevel()) {
                FoodProperties food = data.getLastUseItem().get(DataComponents.FOOD);
                if (food != null) {
                    HydrationUtils.addHydration(player, HydrationUtils.getFoodHydrationCost(data.getLastUseItem()));
                }
            }
            data.setLastFoodLevel(currentFood);
            data.setLastUseItem(ItemStack.EMPTY);
        } else {
            data.setLastFoodLevel(player.getFoodData().getFoodLevel());
        }

        // POTION: detect by glass bottle appearing in hands
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        boolean hasGlassBottle = mainHand.getItem() == Items.GLASS_BOTTLE || offHand.getItem() == Items.GLASS_BOTTLE;

        if (hasGlassBottle && !data.getHadGlassBottle()) {
            HydrationUtils.addHydration(player, 20);
        }
        data.setHadGlassBottle(hasGlassBottle);

        int hydrationLevel = data.getHydrationLevel();

        if (hydrationLevel <= 0) {
            if (!player.hasEffect(MobEffects.HUNGER)) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.HUNGER,
                    200,
                    1,
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
                        player.sendSystemMessage(Component.translatable("message.JEH.critical"));
                    }
                }
            } else {
                if (player.tickCount % 1200 == 0) {
                    player.sendSystemMessage(Component.translatable("message.JEH.dehydrated"));
                }
            }
        } else if (hydrationLevel <= 5) {
            if (player.tickCount % 2400 == 0) {
                player.sendSystemMessage(Component.translatable("message.JEH.thirsty"));
            }
        }

        // Health regen: hunger first, then hydration
        if (player.tickCount % 80 == 0 && player.getHealth() < player.getMaxHealth()) {
            int foodLevel = player.getFoodData().getFoodLevel();
            float saturation = player.getFoodData().getSaturationLevel();

            if (foodLevel < 18 || saturation <= 0) {
                if (foodLevel >= 6) {
                    player.heal(1.0f);
                    player.getFoodData().setFoodLevel(foodLevel - 1);
                } else if (hydrationLevel > 0) {
                    player.heal(1.0f);
                    data.setHydrationLevel(hydrationLevel - 1);
                }
            }
        }

        HydrationUtils.setHydrationData(player, data);
        HydrationUtils.syncToClient(player);
    }
}
