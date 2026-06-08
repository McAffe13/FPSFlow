package dev.fpsflow.gui;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.optimization.OptimizationModule;

public final class GUIOptimizer implements OptimizationModule {

    private static final GUIOptimizer INSTANCE = new GUIOptimizer();

    private int tick = 0;

    private GUIOptimizer() {}

    public static GUIOptimizer getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "gui-optimizer";
    }

    @Override
    public void initialize() {
        HUDCache.getInstance().reset();
        FPSFlow.LOGGER.debug("[FPSFlow] GUIOptimizer ready");
    }

    @Override
    public void shutdown() {
        HUDCache.getInstance().reset();
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().guiOptimization.enabled;
    }

    @Override
    public void onTick() {
        tick++;
    }

    /**
     * Returns true if non-critical HUD elements should be re-evaluated this tick.
     * When throttling is on, only every other tick is considered an update tick.
     */
    public boolean isHUDUpdateTick() {
        if (!ConfigManager.getInstance().getConfig().guiOptimization.hudUpdateThrottling) {
            return true;
        }
        return (tick & 1) == 0;
    }

    public HUDCache getCache() {
        return HUDCache.getInstance();
    }
}
