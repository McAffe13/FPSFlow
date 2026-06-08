package dev.fpsflow.compatibility;

import dev.fpsflow.FPSFlow;
import net.fabricmc.loader.api.FabricLoader;

import java.util.EnumSet;
import java.util.Set;

public final class CompatibilityChecker {

    private static final CompatibilityChecker INSTANCE = new CompatibilityChecker();

    private final Set<ModCompatibility> presentMods = EnumSet.noneOf(ModCompatibility.class);

    private CompatibilityChecker() {}

    public static CompatibilityChecker getInstance() {
        return INSTANCE;
    }

    public void check() {
        FabricLoader loader = FabricLoader.getInstance();
        for (ModCompatibility mod : ModCompatibility.values()) {
            if (loader.isModLoaded(mod.modId)) {
                presentMods.add(mod);
                FPSFlow.LOGGER.info("[FPSFlow] Detected mod: {} – {}", mod.displayName, mod.note);
            }
        }
    }

    public boolean isPresent(ModCompatibility mod) {
        return presentMods.contains(mod);
    }

    /** True when tr7zw's EntityCulling is installed – disables FPSFlow's built-in entity culling. */
    public boolean isEntityCullingModPresent() {
        return isPresent(ModCompatibility.ENTITY_CULLING);
    }

    /** True when ImmediatelyFast is installed – disables FPSFlow's HUD caching layer. */
    public boolean isImmediatelyFastPresent() {
        return isPresent(ModCompatibility.IMMEDIATELY_FAST);
    }

    public boolean isSodiumPresent() {
        return isPresent(ModCompatibility.SODIUM);
    }
}
