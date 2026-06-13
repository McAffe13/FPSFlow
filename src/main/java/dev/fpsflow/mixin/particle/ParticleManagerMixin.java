package dev.fpsflow.mixin.particle;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.particles.ParticleOptimizer;
import dev.fpsflow.rendering.AdaptiveRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Queue;
import java.util.function.BiConsumer;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow @Final private Map<ParticleTextureSheet, Queue<Particle>> particles;

    private static volatile long fpsflow$lastParticleErrorMs = 0L;
    // Cached once per tick; avoids reading smoothed FPS on every particle spawn attempt.
    private double fpsflow$cachedDistMult = 1.0;

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

        if (optimizer.shouldBlockParticleWithMultiplier(x, y, z, fpsflow$cachedDistMult)) {
            cir.setReturnValue(null);
            return;
        }

        optimizer.incrementCount();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void fpsflow$onTickStart(CallbackInfo ci) {
        // Seed the cap from the real live-particle count so maxParticles applies to
        // total alive particles, not just new spawns within a single tick.
        int live = 0;
        for (Queue<Particle> q : particles.values()) live += q.size();
        ParticleOptimizer optimizer = ParticleOptimizer.getInstance();
        optimizer.setActiveParticleCount(live);

        // Cache once per tick – value changes at most once per tick via SmartRenderScheduler.
        fpsflow$cachedDistMult = AdaptiveRenderer.getInstance().getParticleDistanceMultiplier();
    }

    // particles field in ParticleManager is declared as Map<> (interface), so the bytecode
    // emits invokeinterface Map.forEach – not invokevirtual IdentityHashMap.forEach.
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"
        )
    )
    private <K, V> void fpsflow$safeParticleForEach(Map<K, V> map, BiConsumer<K, V> consumer) {
        map.forEach((k, v) -> {
            try {
                consumer.accept(k, v);
            } catch (Exception e) {
                long now = System.currentTimeMillis();
                if (now - fpsflow$lastParticleErrorMs > 5000L) {
                    fpsflow$lastParticleErrorMs = now;
                    FPSFlow.LOGGER.warn("[FPSFlow] Particle tick exception caught – crash prevented: {}", e.toString());
                }
            }
        });
    }
}
