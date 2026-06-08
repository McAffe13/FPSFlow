package dev.fpsflow.screen;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.config.PerformanceProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class FPSFlowConfigScreen extends Screen {

    private static final int BTN_W = 150;
    private static final int BTN_H = 20;
    private static final int SPACING = 26;

    private final Screen parent;
    private FPSFlowConfig cfg;

    public FPSFlowConfigScreen(Screen parent) {
        super(Text.literal("FPSFlow Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cfg = ConfigManager.getInstance().getConfig();

        int cx = width / 2;
        int y = 40;

        // Profile cycling button (full width)
        addDrawableChild(ButtonWidget.builder(profileText(), btn -> {
            cycleProfile();
            btn.setMessage(profileText());
        }).dimensions(cx - 100, y, 200, BTN_H).build());

        y += SPACING + 6;

        int lx = cx - BTN_W - 5;
        int rx = cx + 5;

        // Row 1
        addDrawableChild(toggleBtn(lx, y, "Entity Culling",
                () -> cfg.entityCulling.enabled,
                v -> cfg.entityCulling.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Block Entity Culling",
                () -> cfg.blockEntityCulling.enabled,
                v -> cfg.blockEntityCulling.enabled = v));
        y += SPACING;

        // Row 2
        addDrawableChild(toggleBtn(lx, y, "Occlusion Culling",
                () -> cfg.entityCulling.occlusionCulling,
                v -> cfg.entityCulling.occlusionCulling = v));
        addDrawableChild(toggleBtn(rx, y, "Async Occlusion",
                () -> cfg.entityCulling.asyncOcclusion,
                v -> cfg.entityCulling.asyncOcclusion = v));
        y += SPACING;

        // Row 3
        addDrawableChild(toggleBtn(lx, y, "Particle Optimizer",
                () -> cfg.particleOptimization.enabled,
                v -> cfg.particleOptimization.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "GUI Optimizer",
                () -> cfg.guiOptimization.enabled,
                v -> cfg.guiOptimization.enabled = v));
        y += SPACING;

        // Row 4
        addDrawableChild(toggleBtn(lx, y, "Update Checker",
                () -> cfg.updateChecker.enabled,
                v -> cfg.updateChecker.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Join Optimizer",
                () -> cfg.worldJoinOptimizer.enabled,
                v -> cfg.worldJoinOptimizer.enabled = v));

        // Done
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), btn -> close())
                .dimensions(cx - 75, height - 30, 150, BTN_H).build());
    }

    private ButtonWidget toggleBtn(int x, int y, String label,
                                   BoolSupplier getter, BoolConsumer setter) {
        return ButtonWidget.builder(toggleText(label, getter.get()), btn -> {
            boolean next = !getter.get();
            setter.accept(next);
            btn.setMessage(toggleText(label, next));
            ConfigManager.getInstance().save();
        }).dimensions(x, y, BTN_W, BTN_H).build();
    }

    private static Text toggleText(String label, boolean on) {
        return Text.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private Text profileText() {
        PerformanceProfile p = cfg.profile != null ? cfg.profile : PerformanceProfile.BALANCED;
        return Text.literal("Profile: " + p.name());
    }

    private void cycleProfile() {
        PerformanceProfile[] values = PerformanceProfile.values();
        PerformanceProfile current = cfg.profile != null ? cfg.profile : PerformanceProfile.BALANCED;
        int next = (current.ordinal() + 1) % values.length;
        cfg.profile = values[next];
        cfg.profile.apply(cfg);
        ConfigManager.getInstance().save();
        // Rebuild buttons to reflect new toggle states
        clearAndInit();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        ConfigManager.getInstance().save();
        assert client != null;
        client.setScreen(parent);
    }

    @FunctionalInterface
    private interface BoolSupplier {
        boolean get();
    }

    @FunctionalInterface
    private interface BoolConsumer {
        void accept(boolean value);
    }
}
