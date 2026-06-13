package dev.fpsflow.entities;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.compatibility.CompatibilityChecker;
import dev.fpsflow.join.WorldJoinOptimizer;
import dev.fpsflow.optimization.OptimizationModule;
import dev.fpsflow.rendering.AdaptiveRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class EntityCullingManager implements OptimizationModule {

    private static final EntityCullingManager INSTANCE = new EntityCullingManager();
    private static final int MAX_ASYNC_RAYCASTS_PER_TICK = 8;

    // Entities closer than this are never occluded — raycast precision errors and stale
    // async data are unacceptable this close to the camera.
    private static final double NEAR_SAFE_DIST_SQ = 64.0;   // 8 blocks
    // If the camera has moved this far since the last raycast, the cached result is
    // spatially invalid and must not be used even if it is not time-stale.
    private static final double CAMERA_MOVE_THRESHOLD_SQ = 2.25; // 1.5 blocks
    // Within this distance we prefer "visible" over stale "occluded" while an async
    // re-check is pending, to avoid entities popping out when the player moves.
    private static final double ASYNC_NEAR_BIAS_SQ = 576.0;  // 24 blocks

    private final Map<Integer, CullingEntry> cache = new ConcurrentHashMap<>();
    private final Queue<PendingCheck> pendingChecks = new ConcurrentLinkedQueue<>();
    private final Set<Integer> pendingIds = ConcurrentHashMap.newKeySet();
    private final Map<EntityType<?>, Optional<Boolean>> typeOverrideCache = new ConcurrentHashMap<>();
    private int currentTick = 0;
    // Tracks the last-seen entityTypeOverrides reference; if it changes (profile switch or
    // config reload) all cached results are stale and the cache is cleared.
    private Map<String, Boolean> lastSeenOverrides = null;

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
            while (processed < batchLimit) {
                PendingCheck pending = pendingChecks.poll();
                if (pending == null) break;
                pendingIds.remove(pending.entityId());
                boolean occluded = rayCast(pending.camPos(), pending.samplePoint(), mc);
                cache.put(pending.entityId(), new CullingEntry(occluded, currentTick, pending.camPos()));
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
        if (entity instanceof PlayerEntity) return false;

        // Entities whose nameplate is forced always-visible by the server are cosmetically
        // important server objects (NPCs, display minecarts, cosmetic entities).
        // Culling them races against server-side metadata updates and causes persistent flicker,
        // exactly as it did for nameplate visibility before 1.6.0.
        if (entity.isCustomNameVisible()) return false;

        FPSFlowConfig.EntityCullingConfig cfg = ConfigManager.getInstance().getConfig().entityCulling;

        Boolean typeOverride = getTypeOverride(entity.getType(), cfg);
        if (typeOverride != null && !typeOverride) return false;

        Vec3d camPos = camera.getCameraPos();
        double distSq = entity.squaredDistanceTo(camPos.x, camPos.y, camPos.z);
        double maxDist = cfg.maxDistance * WorldJoinOptimizer.getInstance().getDistanceFraction();
        maxDist *= AdaptiveRenderer.getInstance().getEntityDistanceMultiplier();
        if (distSq > maxDist * maxDist) return true;

        // Within 8 blocks occlusion culling is skipped entirely: raycast precision errors
        // and stale async data at this range cause clearly visible pop-out artefacts.
        if (distSq < NEAR_SAFE_DIST_SQ) return false;

        if (!cfg.occlusionCulling) return false;

        int id = entity.getId();
        CullingEntry entry = cache.get(id);

        // A cached result is only valid if (a) it is not time-stale and (b) the camera has
        // not moved significantly since the raycast was taken.  Camera movement invalidates
        // directional occlusion data even when the tick counter has not expired.
        boolean cameraMovedFar = entry != null
                && entry.cachedCamPos().squaredDistanceTo(camPos) > CAMERA_MOVE_THRESHOLD_SQ;

        boolean cacheValid = entry != null
                && !cameraMovedFar
                && (currentTick - entry.lastCheckedTick()) < cfg.cacheUpdateIntervalTicks;

        if (cacheValid) return entry.occluded();

        // Use the entity's eye position rather than the AABB centre.
        Vec3d samplePoint = entity.getEyePos();

        if (cfg.asyncOcclusion) {
            if (pendingIds.size() < 256 && pendingIds.add(id)) {
                pendingChecks.offer(new PendingCheck(id, camPos, samplePoint));
            }
            // If the camera has moved far enough to invalidate the old result, or the entity
            // is close enough that a wrong answer is very noticeable, prefer "visible" while
            // the new check is still in the queue.  For distant entities the brief stale
            // "occluded" flash is acceptable and prevents distant pop-in.
            if (cameraMovedFar || distSq < ASYNC_NEAR_BIAS_SQ) return false;
            return entry != null && entry.occluded();
        }

        boolean occluded = rayCast(camPos, samplePoint, mc);
        cache.put(id, new CullingEntry(occluded, currentTick, camPos));
        return occluded;
    }

    private Boolean getTypeOverride(EntityType<?> type, FPSFlowConfig.EntityCullingConfig cfg) {
        // Profile switches replace the entityTypeOverrides map reference; detecting that
        // change here avoids a cross-package dependency on ConfigManager change events.
        if (cfg.entityTypeOverrides != lastSeenOverrides) {
            typeOverrideCache.clear();
            lastSeenOverrides = cfg.entityTypeOverrides;
        }
        return typeOverrideCache.computeIfAbsent(type, key -> {
            Identifier id = Registries.ENTITY_TYPE.getId(key);
            return id == null ? Optional.empty() : Optional.ofNullable(cfg.entityTypeOverrides.get(id.toString()));
        }).orElse(null);
    }

    private boolean rayCast(Vec3d from, Vec3d to, MinecraftClient mc) {
        Vec3d origin = from;
        // Retry up to 4 times, stepping past visually-invisible blocks (barrier, light,
        // structure void) that would wrongly count as occlusion.
        for (int attempt = 0; attempt < 4; attempt++) {
            RaycastContext ctx = new RaycastContext(
                    origin, to,
                    RaycastContext.ShapeType.VISUAL,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            );
            BlockHitResult result = mc.world.raycast(ctx);
            if (result.getType() == HitResult.Type.MISS) return false;

            net.minecraft.block.BlockState hit = mc.world.getBlockState(result.getBlockPos());
            if (isVisuallyPassable(hit)) {
                // Step slightly past this block and retry so visually-invisible blocks
                // (barrier, light, structure void) are treated as transparent.
                Vec3d dir = to.subtract(origin).normalize();
                origin = result.getPos().add(dir.multiply(0.02));
                continue;
            }
            return true;
        }
        return false;
    }

    private static boolean isVisuallyPassable(net.minecraft.block.BlockState state) {
        net.minecraft.block.Block b = state.getBlock();
        return b == net.minecraft.block.Blocks.BARRIER
                || b == net.minecraft.block.Blocks.STRUCTURE_VOID
                || b == net.minecraft.block.Blocks.LIGHT;
    }

    public void invalidate(int entityId) {
        cache.remove(entityId);
    }

    private record CullingEntry(boolean occluded, int lastCheckedTick, Vec3d cachedCamPos) {}
    private record PendingCheck(int entityId, Vec3d camPos, Vec3d samplePoint) {}
}
