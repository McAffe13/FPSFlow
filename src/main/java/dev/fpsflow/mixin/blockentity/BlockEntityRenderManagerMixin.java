package dev.fpsflow.mixin.blockentity;

import dev.fpsflow.blockentity.BlockEntityCullingManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderManager.class)
public abstract class BlockEntityRenderManagerMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <S extends BlockEntityRenderState> void fpsflow$cullBlockEntity(
            S state,
            MatrixStack matrices,
            OrderedRenderCommandQueue renderQueue,
            CameraRenderState cameraState,
            CallbackInfo ci) {

        if (BlockEntityCullingManager.getInstance().shouldCull(state, cameraState)) {
            ci.cancel();
        }
    }
}
