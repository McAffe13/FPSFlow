package dev.fpsflow.optimization;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.blockentity.BlockEntityCullingManager;
import dev.fpsflow.entities.EntityCullingManager;
import dev.fpsflow.gui.GUIOptimizer;
import dev.fpsflow.join.WorldJoinOptimizer;
import dev.fpsflow.particles.ParticleOptimizer;
import dev.fpsflow.rendering.AdaptiveRenderer;

import java.util.ArrayList;
import java.util.List;

public final class OptimizationManager {

    private static final OptimizationManager INSTANCE = new OptimizationManager();

    private final List<OptimizationModule> modules = new ArrayList<>();
    private boolean initialized = false;

    private OptimizationManager() {}

    public static OptimizationManager getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        if (initialized) return;

        register(WorldJoinOptimizer.getInstance());
        register(EntityCullingManager.getInstance());
        register(BlockEntityCullingManager.getInstance());
        register(ParticleOptimizer.getInstance());
        register(GUIOptimizer.getInstance());
        register(AdaptiveRenderer.getInstance());

        for (OptimizationModule module : modules) {
            try {
                module.initialize();
                FPSFlow.LOGGER.info("[FPSFlow] Module '{}' initialized", module.getId());
            } catch (Exception e) {
                FPSFlow.LOGGER.error("[FPSFlow] Module '{}' failed to initialize", module.getId(), e);
            }
        }

        initialized = true;
    }

    public void tick() {
        for (OptimizationModule module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    FPSFlow.LOGGER.error("[FPSFlow] Module '{}' threw during tick", module.getId(), e);
                }
            }
        }
    }

    public void shutdown() {
        for (OptimizationModule module : modules) {
            try {
                module.shutdown();
            } catch (Exception e) {
                FPSFlow.LOGGER.error("[FPSFlow] Module '{}' failed to shutdown cleanly", module.getId(), e);
            }
        }
        modules.clear();
        initialized = false;
    }

    private void register(OptimizationModule module) {
        modules.add(module);
    }
}
