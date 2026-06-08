package dev.fpsflow.gui;

/**
 * Tracks the last-known values of HUD elements so mixins can detect
 * when a re-render is actually necessary.
 */
public final class HUDCache {

    private static final HUDCache INSTANCE = new HUDCache();

    // Player stats
    private int health = -1;
    private int maxHealth = -1;
    private float absorption = -1f;
    private int food = -1;
    private float saturation = -1f;
    private int armor = -1;
    private float xpProgress = -1f;
    private int xpLevel = -1;
    private int air = -1;

    // Hotbar
    private int selectedSlot = -1;
    private final long[] slotHashes = new long[9];

    private HUDCache() {}

    public static HUDCache getInstance() {
        return INSTANCE;
    }

    public void reset() {
        health = maxHealth = food = armor = xpLevel = air = selectedSlot = -1;
        absorption = saturation = xpProgress = -1f;
        java.util.Arrays.fill(slotHashes, -1L);
    }

    // --- health ---

    public boolean isHealthDirty(int newHealth, int newMax, float newAbsorption) {
        if (health != newHealth || maxHealth != newMax || absorption != newAbsorption) {
            health = newHealth;
            maxHealth = newMax;
            absorption = newAbsorption;
            return true;
        }
        return false;
    }

    // --- food ---

    public boolean isFoodDirty(int newFood, float newSaturation) {
        if (food != newFood || saturation != newSaturation) {
            food = newFood;
            saturation = newSaturation;
            return true;
        }
        return false;
    }

    // --- armor ---

    public boolean isArmorDirty(int newArmor) {
        if (armor != newArmor) {
            armor = newArmor;
            return true;
        }
        return false;
    }

    // --- XP ---

    public boolean isXpDirty(float newProgress, int newLevel) {
        if (xpProgress != newProgress || xpLevel != newLevel) {
            xpProgress = newProgress;
            xpLevel = newLevel;
            return true;
        }
        return false;
    }

    // --- air ---

    public boolean isAirDirty(int newAir) {
        if (air != newAir) {
            air = newAir;
            return true;
        }
        return false;
    }

    // --- hotbar ---

    public boolean isHotbarSlotDirty(int slot, long hash) {
        if (slotHashes[slot] != hash) {
            slotHashes[slot] = hash;
            return true;
        }
        return false;
    }

    public boolean isSelectedSlotDirty(int slot) {
        if (selectedSlot != slot) {
            selectedSlot = slot;
            return true;
        }
        return false;
    }
}
