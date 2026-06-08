package dev.fpsflow;

import dev.fpsflow.rendering.SmartRenderScheduler;
import dev.fpsflow.updates.UpdateChecker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FPSFlowClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        SmartRenderScheduler.getInstance().initialize();
        UpdateChecker.getInstance().checkAsync();
        FPSFlow.LOGGER.info("[FPSFlow] Client subsystems ready");
    }
}
