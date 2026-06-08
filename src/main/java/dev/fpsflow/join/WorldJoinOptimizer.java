package dev.fpsflow.join;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.optimization.OptimizationModule;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class WorldJoinOptimizer implements OptimizationModule {

    private static final WorldJoinOptimizer INSTANCE = new WorldJoinOptimizer();

    /** Fraction of normal cull distances applied at the very first tick after join. */
    private static final float MIN_FRACTION = 0.35f;
    /** How many extra async raycasts to process per tick during the grace period. */
    private static final int ASYNC_BATCH_MULTIPLIER = 3;

    private int graceTicksTotal = 200;
    private int graceTicksRemaining = 0;

    private WorldJoinOptimizer() {}

    public static WorldJoinOptimizer getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "world-join-optimizer";
    }

    @Override
    public void initialize() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!isEnabled()) return;
            FPSFlowConfig.WorldJoinOptimizerConfig cfg =
                    ConfigManager.getInstance().getConfig().worldJoinOptimizer;
            graceTicksTotal = Math.max(1, cfg.gracePeriodTicks);
            graceTicksRemaining = graceTicksTotal;
            FPSFlow.LOGGER.debug("[FPSFlow] World join detected – grace period active ({} ticks)", graceTicksTotal);
        });
    }

    @Override
    public void shutdown() {
        graceTicksRemaining = 0;
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().worldJoinOptimizer.enabled;
    }

    @Override
    public void onTick() {
        if (graceTicksRemaining > 0) {
            graceTicksRemaining--;
        }
    }

    public boolean isInGracePeriod() {
        return isEnabled() && graceTicksRemaining > 0;
    }

    /**
     * Returns a fraction in [MIN_FRACTION, 1.0] that scales all cull distances.
     * Starts at MIN_FRACTION immediately after join and eases linearly to 1.0.
     * Returns 1.0 when no grace period is active.
     */
    public float getDistanceFraction() {
        if (!isInGracePeriod()) return 1.0f;
        float progress = 1.0f - (float) graceTicksRemaining / graceTicksTotal;
        return MIN_FRACTION + progress * (1.0f - MIN_FRACTION);
    }

    /** During the grace period, process more async raycasts per tick to drain the backlog faster. */
    public int getAsyncBatchMultiplier() {
        return isInGracePeriod() ? ASYNC_BATCH_MULTIPLIER : 1;
    }
}
