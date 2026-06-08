package dev.fpsflow.mixin.entity;

import dev.fpsflow.entities.EntityCullingManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderManagerMixin {

    @Inject(
        method = "shouldRender",
        at = @At("RETURN"),
        cancellable = true
    )
    private <E extends Entity> void fpsflow$cullEntity(
            E entity,
            Frustum frustum,
            double cameraX, double cameraY, double cameraZ,
            CallbackInfoReturnable<Boolean> cir) {

        if (!cir.getReturnValueZ()) return;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        if (camera == null) return;

        if (EntityCullingManager.getInstance().shouldCullEntity(entity, camera)) {
            cir.setReturnValue(false);
        }
    }
}
