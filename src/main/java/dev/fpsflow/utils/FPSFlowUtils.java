package dev.fpsflow.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

public final class FPSFlowUtils {

    private FPSFlowUtils() {}

    /**
     * Produces a deterministic hash for an ItemStack suitable for hotbar cache comparison.
     * Considers item type, count, damage, enchantment presence, and CustomModelData.
     * CustomModelData is included because texture-pack servers use it to select custom
     * models; without it, visually distinct items with the same type/count/damage would
     * hash identically and cause incorrect cache hits.
     */
    public static long itemStackHash(ItemStack stack) {
        if (stack.isEmpty()) return 0L;
        long id = stack.getItem().hashCode();
        long count = stack.getCount();
        long damage = stack.getDamage();
        long enchanted = stack.hasEnchantments() ? 1L : 0L;
        var cmd = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        long customModel = cmd != null ? (long) cmd.hashCode() : 0L;
        return MathUtils.combineHash(id, count, damage, enchanted, customModel);
    }

    /** Returns the current UNIX timestamp in milliseconds. */
    public static long nowMs() {
        return System.currentTimeMillis();
    }
}
