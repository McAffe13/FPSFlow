package dev.fpsflow.mixin.render;

import dev.fpsflow.entities.EntityCullingManager;
import dev.fpsflow.optimization.OptimizationManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    /**
     * Drives the optimization manager's per-tick logic from the world render tick,
     * which fires once per game tick on the render thread.
     * This keeps entity culling cache eviction and HUD throttle counters in sync
     * with the actual game tick rate rather than the frame rate.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void fpsflow$onTick(CallbackInfo ci) {
        OptimizationManager.getInstance().tick();
    }
}
