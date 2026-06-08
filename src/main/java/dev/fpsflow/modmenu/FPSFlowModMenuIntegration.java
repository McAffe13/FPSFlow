package dev.fpsflow.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.fpsflow.screen.FPSFlowConfigScreen;

public class FPSFlowModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FPSFlowConfigScreen::new;
    }
}
