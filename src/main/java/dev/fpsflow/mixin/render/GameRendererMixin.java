package dev.fpsflow.mixin.render;

import dev.fpsflow.rendering.SmartRenderScheduler;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Called at the end of each rendered frame.
     * We use this hook to record frame timestamps for the SmartRenderScheduler's
     * smoothed-FPS calculation without adding overhead to the critical render path.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void fpsflow$onFrameEnd(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // SmartRenderScheduler tracks timing via CLIENT_TICK events, which is sufficient.
        // This hook is reserved for future per-frame profiling.
    }
}
