package dev.fpsflow;

import dev.fpsflow.compatibility.CompatibilityChecker;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.optimization.OptimizationManager;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FPSFlow implements ModInitializer {

    public static final String MOD_ID = "fpsflow";
    public static final String MOD_NAME = "FPSFlow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[FPSFlow] Initializing {}", MOD_NAME);

        ConfigManager.getInstance().load();
        CompatibilityChecker.getInstance().check();
        OptimizationManager.getInstance().initialize();

        // Update checker is initialised in FPSFlowClient (client-only)
        LOGGER.info("[FPSFlow] {} initialized – profile: {}",
                MOD_NAME, ConfigManager.getInstance().getConfig().profile);
    }
}
