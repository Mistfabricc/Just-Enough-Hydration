package com.jeh.mixin;
 
import com.jeh.common.utils.HydrationUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class FoodHydrationMixin {
    @Inject(
        method = "eat",
        at = @At("HEAD")
    )
    private void onEat(Level world, ItemStack stack, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player) {
            HydrationUtils.addHydration(player, getFoodHydrationCost(stack));
        }
    }

    private static int getFoodHydrationCost(ItemStack stack) {
        Item item = stack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return -1;
        String path = id.getPath();

        // Stews/soups and milk - hydrating
        if (path.equals("mushroom_stew") || path.equals("beetroot_soup")
            || path.equals("rabbit_stew") || path.equals("suspicious_stew")) {
            return 3;
        }
        if (path.equals("milk_bucket")) return 4;
        if (path.equals("honey_bottle")) return 6;
        if (path.equals("golden_apple") || path.equals("enchanted_golden_apple")) return 5;

        // Pufferfish - severely dehydrating
        if (path.equals("pufferfish")) return -6;
        // Rotten flesh
        if (path.equals("rotten_flesh")) return -5;

        // Raw meat - high dehydration
        if (path.equals("beef") || path.equals("chicken") || path.equals("porkchop")
            || path.equals("rabbit") || path.equals("mutton")) {
            return -4;
        }

        // Cooked meat - low dehydration (digestion cost)
        if (path.equals("cooked_beef") || path.equals("cooked_chicken")
            || path.equals("cooked_porkchop") || path.equals("cooked_rabbit")
            || path.equals("cooked_mutton")) {
            return -1;
        }

        // Raw fish - medium dehydration
        if (path.equals("cod") || path.equals("salmon")) return -3;
        // Cooked fish - low
        if (path.equals("cooked_cod") || path.equals("cooked_salmon")) return -1;

        // Spider eye
        if (path.equals("spider_eye")) return -3;

        // Vegetables / plant-based - medium dehydration
        if (path.equals("potato") || path.equals("baked_potato") || path.equals("poisonous_potato")
            || path.equals("carrot") || path.equals("beetroot") || path.equals("dried_kelp")
            || path.equals("sweet_berries") || path.equals("glow_berries")
            || path.equals("chorus_fruit")) {
            return -2;
        }

        // Default digestion cost
        return -1;
    }
}