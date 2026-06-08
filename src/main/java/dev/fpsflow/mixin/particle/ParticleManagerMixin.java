package dev.fpsflow.mixin.particle;

import dev.fpsflow.particles.ParticleOptimizer;
import dev.fpsflow.rendering.AdaptiveRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    /**
     * Intercepts addParticle before the particle object is created.
     * Blocking at this stage avoids both the allocation and the rendering cost.
     *
     * The particle count maintained by ParticleOptimizer is incremented and
     * decremented via the tick injection below, avoiding a fragile @Shadow on
     * the internal particles map.
     */
    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
        at = @At("HEAD"),
        cancellable = true
    )
    private <T extends ParticleEffect> void fpsflow$limitParticles(
            T parameters,
            double x, double y, double z,
            double vx, double vy, double vz,
            CallbackInfoReturnable<Particle> cir) {

        ParticleOptimizer optimizer = ParticleOptimizer.getInstance();
        if (!optimizer.isEnabled()) return;

        double distMult = AdaptiveRenderer.getInstance().getParticleDistanceMultiplier();

        if (optimizer.shouldBlockParticleWithMultiplier(x, y, z, distMult)) {
            cir.setReturnValue(null);
            return;
        }

        // Track the count ourselves so subsequent calls in the same tick see the updated value
        optimizer.incrementCount();
    }

    /**
     * After each tick, reset the per-tick particle count so the cap applies
     * per tick rather than accumulating forever.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void fpsflow$onTickStart(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ParticleOptimizer.getInstance().resetTickCount();
    }
}
