package dev.fpsflow;

import dev.fpsflow.rendering.ResourcePackReloadTracker;
import dev.fpsflow.rendering.SmartRenderScheduler;
import dev.fpsflow.updates.UpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public class FPSFlowClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SmartRenderScheduler.getInstance().initialize();
        ResourcePackReloadTracker.register();
        UpdateChecker.getInstance().checkAsync();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                UpdateChecker.getInstance().showPendingIfAny(client));
        FPSFlow.LOGGER.info("[FPSFlow] Client subsystems ready");
    }
}
