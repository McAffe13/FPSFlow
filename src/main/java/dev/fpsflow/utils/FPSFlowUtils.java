package dev.fpsflow.utils;

import net.minecraft.item.ItemStack;

public final class FPSFlowUtils {

    private FPSFlowUtils() {}

    /**
     * Produces a deterministic hash for an ItemStack suitable for hotbar cache comparison.
     * Considers item type, count, and damage only – not full NBT – for performance.
     */
    public static long itemStackHash(ItemStack stack) {
        if (stack.isEmpty()) return 0L;
        long id = stack.getItem().hashCode();
        long count = stack.getCount();
        long damage = stack.getDamage();
        // Include enchantment presence as a flag
        long enchanted = stack.hasEnchantments() ? 1L : 0L;
        return MathUtils.combineHash(id, count, damage, enchanted);
    }

    /** Returns the current UNIX timestamp in milliseconds. */
    public static long nowMs() {
        return System.currentTimeMillis();
    }
}
