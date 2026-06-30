package com.jeh.common.data;
 
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class HydrationData {
    private int hydrationLevel = 20;
    private float hydrationExhaustion = 0.0f;
    private int hydrationTickTimer = 0;
    private static final int MAX_HYDRATION = 20;
    private static final int TICKS_PER_DRAIN = 9600;

    private ItemStack lastUseItem = ItemStack.EMPTY;
    private int glassBottlesBefore = 0;
    private int lastFoodLevel = -1;
    private boolean dehydrationAlertSent = false;

    public int getHydrationLevel() {
        return hydrationLevel;
    }

    public void setHydrationLevel(int level) {
        this.hydrationLevel = Math.max(0, Math.min(MAX_HYDRATION, level));
    }

    public float getHydrationExhaustion() {
        return hydrationExhaustion;
    }

    public void setHydrationExhaustion(float exhaustion) {
        this.hydrationExhaustion = exhaustion;
    }

    public int getHydrationTickTimer() {
        return hydrationTickTimer;
    }

    public void addExhaustion(float exhaustion) {
        this.hydrationExhaustion += exhaustion;
        if (this.hydrationExhaustion >= 4.0f) {
            this.hydrationExhaustion = 0.0f;
            if (this.hydrationLevel > 0) {
                this.hydrationLevel--;
            }
        }
    }

    public void tick() {
        hydrationTickTimer++;
        if (hydrationTickTimer >= TICKS_PER_DRAIN) {
            hydrationTickTimer = 0;
            if (hydrationLevel > 0) {
                hydrationLevel--;
            }
        }
    }

    public void addHydration(int amount) {
        this.hydrationLevel = Math.max(0, Math.min(MAX_HYDRATION, this.hydrationLevel + amount));
    }

    public boolean isHydrated() {
        return hydrationLevel > 0;
    }

    public boolean isFullyHydrated() {
        return hydrationLevel >= MAX_HYDRATION;
    }

    public float getHydrationPercentage() {
        return (float) Math.max(0, hydrationLevel) / MAX_HYDRATION;
    }

    public ItemStack getLastUseItem() {
        return lastUseItem;
    }

    public void setLastUseItem(ItemStack stack) {
        this.lastUseItem = stack;
    }

    public int getGlassBottlesBefore() {
        return glassBottlesBefore;
    }

    public void setGlassBottlesBefore(int count) {
        this.glassBottlesBefore = count;
    }

    public int getLastFoodLevel() {
        return lastFoodLevel;
    }

    public void setLastFoodLevel(int level) {
        this.lastFoodLevel = level;
    }

    public boolean isDehydrationAlertSent() {
        return dehydrationAlertSent;
    }

    public void setDehydrationAlertSent(boolean sent) {
        this.dehydrationAlertSent = sent;
    }

    public void readNbt(CompoundTag nbt) {
        if (nbt.contains("HydrationLevel")) {
            hydrationLevel = nbt.getIntOr("HydrationLevel", 20);
        }
        if (nbt.contains("HydrationExhaustion")) {
            hydrationExhaustion = nbt.getFloatOr("HydrationExhaustion", 0.0f);
        }
        if (nbt.contains("HydrationTickTimer")) {
            hydrationTickTimer = nbt.getIntOr("HydrationTickTimer", 0);
        }
    }

    public void writeNbt(CompoundTag nbt) {
        nbt.putInt("HydrationLevel", hydrationLevel);
        nbt.putFloat("HydrationExhaustion", hydrationExhaustion);
        nbt.putInt("HydrationTickTimer", hydrationTickTimer);
    }

    public HydrationData copy() {
        HydrationData copy = new HydrationData();
        copy.hydrationLevel = this.hydrationLevel;
        copy.hydrationExhaustion = this.hydrationExhaustion;
        copy.hydrationTickTimer = this.hydrationTickTimer;
        return copy;
    }
}
