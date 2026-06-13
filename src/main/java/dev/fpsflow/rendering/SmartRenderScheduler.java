package dev.fpsflow.rendering;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.optimization.OptimizationManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class SmartRenderScheduler {

    private static final SmartRenderScheduler INSTANCE = new SmartRenderScheduler();

    private long lastFrameTime = System.nanoTime();
    private double smoothedFps = 60.0;
    private static final double FPS_SMOOTHING = 0.05;

    private SmartRenderScheduler() {}

    public static SmartRenderScheduler getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world != null) {
                onClientTick();
            }
        });
        FPSFlow.LOGGER.debug("[FPSFlow] SmartRenderScheduler registered");
    }

    private void onClientTick() {
        long now = System.nanoTime();
        double frameMs = (now - lastFrameTime) / 1_000_000.0;
        lastFrameTime = now;
        if (frameMs > 0) {
            double fps = 1000.0 / frameMs;
            smoothedFps = smoothedFps * (1 - FPS_SMOOTHING) + fps * FPS_SMOOTHING;
        }
        ResourcePackReloadTracker.getInstance().onTick();
        OptimizationManager.getInstance().tick();
    }

    /** Returns the exponentially-smoothed FPS estimate. */
    public double getSmoothedFps() {
        return smoothedFps;
    }

    /** True when the game is running below the given fps target. */
    public boolean isUnderFpsTarget(double target) {
        return smoothedFps < target;
    }

    /** Whether render caching is active for this frame. */
    public boolean isRenderCachingEnabled() {
        return ConfigManager.getInstance().getConfig().renderCaching.enabled;
    }
}
