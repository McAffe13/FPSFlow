package dev.fpsflow.compatibility;

/**
 * Known mods that overlap with FPSFlow features.
 * When these mods are detected, the corresponding FPSFlow subsystem is disabled
 * to avoid redundant work and potential conflicts.
 */
public enum ModCompatibility {

    ENTITY_CULLING(
            "entityculling",
            "tr7zw's EntityCulling",
            "Provides entity occlusion culling – FPSFlow's built-in culling is disabled when present."
    ),

    SODIUM(
            "sodium",
            "Sodium",
            "Replaces the chunk rendering engine. Fully compatible; FPSFlow adds entity/GUI optimizations on top."
    ),

    LITHIUM(
            "lithium",
            "Lithium",
            "Optimises game logic and physics. No overlap with FPSFlow features."
    ),

    FERRITE_CORE(
            "ferritecore",
            "FerriteCore",
            "Reduces memory usage of block states. No overlap with FPSFlow features."
    ),

    IMMEDIATELY_FAST(
            "immediatelyfast",
            "ImmediatelyFast",
            "Optimises immediate-mode rendering. FPSFlow's HUD caching defers to ImmediatelyFast when present."
    ),

    MODERN_FIX(
            "modernfix",
            "ModernFix",
            "Various performance fixes. No direct overlap with FPSFlow."
    );

    public final String modId;
    public final String displayName;
    public final String note;

    ModCompatibility(String modId, String displayName, String note) {
        this.modId = modId;
        this.displayName = displayName;
        this.note = note;
    }
}
