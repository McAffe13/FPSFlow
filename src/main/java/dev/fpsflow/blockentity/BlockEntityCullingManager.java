package dev.fpsflow.blockentity;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.join.WorldJoinOptimizer;
import dev.fpsflow.optimization.OptimizationModule;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BlockEntityCullingManager implements OptimizationModule {

    private static final BlockEntityCullingManager INSTANCE = new BlockEntityCullingManager();

    private BlockEntityCullingManager() {}

    public static BlockEntityCullingManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "block-entity-culling";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] BlockEntityCullingManager ready");
    }

    @Override
    public void shutdown() {}

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().blockEntityCulling.enabled;
    }

    @Override
    public void onTick() {}

    public boolean shouldCull(BlockEntityRenderState state, CameraRenderState camera) {
        if (!isEnabled()) return false;

        double maxDist = ConfigManager.getInstance().getConfig().blockEntityCulling.maxDistance
                * WorldJoinOptimizer.getInstance().getDistanceFraction();
        Vec3d camPos = camera.pos;
        if (camPos == null) return false;

        BlockPos pos = state.pos;
        double dx = pos.getX() + 0.5 - camPos.x;
        double dy = pos.getY() + 0.5 - camPos.y;
        double dz = pos.getZ() + 0.5 - camPos.z;
        return (dx * dx + dy * dy + dz * dz) > maxDist * maxDist;
    }
}
