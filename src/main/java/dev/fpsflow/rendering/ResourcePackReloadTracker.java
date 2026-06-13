package dev.fpsflow.rendering;

import dev.fpsflow.FPSFlow;
import dev.fpsflow.gui.HUDCache;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;

/**
 * Detects server resource pack reloads and briefly suspends throttled render
 * optimizations that would otherwise produce stale visuals while new textures
 * and baked models are being applied.
 *
 * Concrete problems prevented:
 *  - Item-frame map texture flickering: updateRenderState throttle references
 *    a MapRenderState whose texture handle is invalidated by the reload.
 *  - Hotbar stale-cache: HUDCache hashes are reset so all slots are treated as
 *    dirty on the first post-reload render frame.
 *
 * The cooldown window (COOLDOWN_TICKS) covers the async model-baking phase that
 * continues after the synchronous reload listener returns.
 */
public final class ResourcePackReloadTracker implements SynchronousResourceReloader, IdentifiableResourceReloadListener {

    private static final ResourcePackReloadTracker INSTANCE = new ResourcePackReloadTracker();

    /** Ticks to keep the "reload active" window open after the reload listener fires. */
    private static final int COOLDOWN_TICKS = 60;

    private volatile int cooldownTicks = 0;

    private ResourcePackReloadTracker() {}

    public static ResourcePackReloadTracker getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("deprecation")
    public static void register() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(INSTANCE);
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of("fpsflow", "resource_reload_tracker");
    }

    @Override
    public void reload(ResourceManager manager) {
        cooldownTicks = COOLDOWN_TICKS;
        HUDCache.getInstance().reset();
        FPSFlow.LOGGER.debug("[FPSFlow] Resource pack reload detected – suspending throttle for {} ticks", COOLDOWN_TICKS);
    }

    /** Called once per client tick; decrements the post-reload cooldown. */
    public void onTick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        }
    }

    /**
     * Returns true during and for {@value #COOLDOWN_TICKS} ticks after a resource
     * pack reload. Throttled render passes should be bypassed while this is true.
     */
    public boolean isReloadActive() {
        return cooldownTicks > 0;
    }
}
