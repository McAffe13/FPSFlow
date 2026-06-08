package dev.fpsflow.entities;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.compatibility.CompatibilityChecker;
import dev.fpsflow.join.WorldJoinOptimizer;
import dev.fpsflow.optimization.OptimizationModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class EntityCullingManager implements OptimizationModule {

    private static final EntityCullingManager INSTANCE = new EntityCullingManager();
    private static final int MAX_ASYNC_RAYCASTS_PER_TICK = 8;

    private final Map<Integer, CullingEntry> cache = new ConcurrentHashMap<>();
    private final Queue<PendingCheck> pendingChecks = new ConcurrentLinkedQueue<>();
    private int currentTick = 0;

    private EntityCullingManager() {}

    public static EntityCullingManager getInstance() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        return "entity-culling";
    }

    @Override
    public void initialize() {
        FPSFlow.LOGGER.debug("[FPSFlow] EntityCullingManager ready");
    }

    @Override
    public void shutdown() {
        cache.clear();
        pendingChecks.clear();
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().getConfig().entityCulling.enabled
                && !CompatibilityChecker.getInstance().isEntityCullingModPresent();
    }

    @Override
    public void onTick() {
        currentTick++;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world != null && mc.player != null) {
            int batchLimit = MAX_ASYNC_RAYCASTS_PER_TICK * WorldJoinOptimizer.getInstance().getAsyncBatchMultiplier();
            int processed = 0;
            while (!pendingChecks.isEmpty() && processed < batchLimit) {
                PendingCheck pending = pendingChecks.poll();
                boolean occluded = computeOcclusionDirect(pending.entityCenter(), pending.camPos(), mc);
                cache.put(pending.entityId(), new CullingEntry(occluded, currentTick));
                processed++;
            }
        }

        if (currentTick % 100 == 0) {
            int threshold = currentTick - 200;
            Iterator<Map.Entry<Integer, CullingEntry>> it = cache.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().lastCheckedTick() < threshold) {
                    it.remove();
                }
            }
        }
    }

    public boolean shouldCullEntity(Entity entity, Camera camera) {
        if (!isEnabled()) return false;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;

        if (entity == mc.player) return false;
        if (mc.player.getVehicle() == entity) return false;

        FPSFlowConfig.EntityCullingConfig cfg = ConfigManager.getInstance().getConfig().entityCulling;

        // Per-entity-type override check
        String typeId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        Boolean typeOverride = cfg.entityTypeOverrides.get(typeId);
        if (typeOverride != null && !typeOverride) return false; // this type is exempt

        Vec3d camPos = camera.getCameraPos();
        double distSq = entity.squaredDistanceTo(camPos.x, camPos.y, camPos.z);
        double maxDist = cfg.maxDistance * WorldJoinOptimizer.getInstance().getDistanceFraction();
        if (distSq > maxDist * maxDist) return true;
        if (distSq < 16.0) return false;

        if (!cfg.occlusionCulling) return false;

        int id = entity.getId();
        CullingEntry entry = cache.get(id);
        boolean cacheValid = entry != null && (currentTick - entry.lastCheckedTick()) < cfg.cacheUpdateIntervalTicks;

        if (cacheValid) return entry.occluded();

        if (cfg.asyncOcclusion) {
            Box box = entity.getBoundingBox();
            pendingChecks.offer(new PendingCheck(id, camPos, box.getCenter()));
            return entry != null && entry.occluded();
        }

        boolean occluded = computeOcclusionDirect(entity.getBoundingBox().getCenter(), camPos, mc);
        cache.put(id, new CullingEntry(occluded, currentTick));
        return occluded;
    }

    private boolean computeOcclusionDirect(Vec3d entityCenter, Vec3d camPos, MinecraftClient mc) {
        RaycastContext ctx = new RaycastContext(
                camPos,
                entityCenter,
                RaycastContext.ShapeType.VISUAL,
                RaycastContext.FluidHandling.NONE,
                mc.player
        );
        BlockHitResult result = mc.world.raycast(ctx);
        return result.getType() != HitResult.Type.MISS;
    }

    public void invalidate(int entityId) {
        cache.remove(entityId);
    }

    private record CullingEntry(boolean occluded, int lastCheckedTick) {}
    private record PendingCheck(int entityId, Vec3d camPos, Vec3d entityCenter) {}
}
