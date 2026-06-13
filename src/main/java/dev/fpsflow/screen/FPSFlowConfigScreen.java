package dev.fpsflow.screen;

import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.config.FPSFlowConfig;
import dev.fpsflow.config.PerformanceProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class FPSFlowConfigScreen extends Screen {

    private static final int BTN_W = 150;
    private static final int BTN_H = 20;
    private static final int SPACING = 26;

    private enum Tab {
        GENERAL("General"),
        CULLING("Culling"),
        LOD("LOD & Labels"),
        BACKGROUND_FPS("Background FPS");

        final String label;
        Tab(String label) { this.label = label; }
    }

    private final Screen parent;
    private FPSFlowConfig cfg;
    private Tab currentTab = Tab.GENERAL;

    public FPSFlowConfigScreen(Screen parent) {
        super(Text.translatable("fpsflow.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        cfg = ConfigManager.getInstance().getConfig();

        Tab[] tabs = Tab.values();
        int tabW = (width - 20) / tabs.length;
        for (int i = 0; i < tabs.length; i++) {
            Tab tab = tabs[i];
            String label = (tab == currentTab ? "[ " : "") + tab.label + (tab == currentTab ? " ]" : "");
            int tx = 10 + i * tabW;
            addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> {
                currentTab = tab;
                clearAndInit();
            }).dimensions(tx, 8, tabW - 2, BTN_H).build());
        }

        int cx = width / 2;
        int y = 40;
        int lx = cx - BTN_W - 5;
        int rx = cx + 5;

        switch (currentTab) {
            case GENERAL        -> initGeneralTab(cx, lx, rx, y);
            case CULLING        -> initCullingTab(lx, rx, y);
            case LOD            -> initLODTab(lx, rx, y);
            case BACKGROUND_FPS -> initBackgroundFpsTab(cx, lx, rx, y);
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), btn -> close())
                .dimensions(cx - 75, height - 30, 150, BTN_H).build());
    }

    // ── tab content ───────────────────────────────────────────────────────────

    private void initGeneralTab(int cx, int lx, int rx, int y) {
        addDrawableChild(ButtonWidget.builder(profileText(), btn -> {
            cycleProfile();
            btn.setMessage(profileText());
        }).dimensions(cx - 100, y, 200, BTN_H)
          .tooltip(Tooltip.of(Text.literal("Switch between built-in and custom performance profiles")))
          .build());
        y += SPACING + 6;

        addDrawableChild(ButtonWidget.builder(Text.literal("Save Custom Profile"), btn -> {
            saveCurrentAsCustomProfile();
            btn.setMessage(Text.literal("Saved as " + cfg.selectedProfile));
        }).dimensions(cx - 100, y, 200, BTN_H)
          .tooltip(Tooltip.of(Text.literal("Save current settings as a new custom profile")))
          .build());
        y += SPACING + 6;

        addDrawableChild(toggleBtn(lx, y, "Update Checker",
                "Check Modrinth for FPSFlow updates on startup",
                () -> cfg.updateChecker.enabled,
                v -> cfg.updateChecker.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Join Optimizer",
                "Tighten culling for ~10 s after joining a world to reduce the initial FPS spike",
                () -> cfg.worldJoinOptimizer.enabled,
                v -> cfg.worldJoinOptimizer.enabled = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y, "GUI Optimizer",
                "Skip redundant hotbar and HUD redraws when nothing changed",
                () -> cfg.guiOptimization.enabled,
                v -> cfg.guiOptimization.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Particle Optimizer",
                "Cap particle count and thin out distant particles",
                () -> cfg.particleOptimization.enabled,
                v -> cfg.particleOptimization.enabled = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y, "Singleplayer Boost",
                "Apply extra-aggressive culling in singleplayer to free CPU for chunk generation",
                () -> cfg.singleplayerOpt.enabled,
                v -> cfg.singleplayerOpt.enabled = v));
    }

    private void initCullingTab(int lx, int rx, int y) {
        addDrawableChild(toggleBtn(lx, y, "Entity Culling",
                "Skip frustum, distance, and occlusion-hidden entities",
                () -> cfg.entityCulling.enabled,
                v -> cfg.entityCulling.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Block Entity Culling",
                "Skip distant chests, furnaces, signs, and banners",
                () -> cfg.blockEntityCulling.enabled,
                v -> cfg.blockEntityCulling.enabled = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y, "Occlusion Culling",
                "Raycast check: skip entities fully hidden behind solid blocks",
                () -> cfg.entityCulling.occlusionCulling,
                v -> cfg.entityCulling.occlusionCulling = v));
        addDrawableChild(toggleBtn(rx, y, "Async Occlusion",
                "Spread raycasts over multiple ticks to avoid frame spikes",
                () -> cfg.entityCulling.asyncOcclusion,
                v -> cfg.entityCulling.asyncOcclusion = v));
        y += SPACING;

        addDrawableChild(toggleBtn(lx, y, "Painting Backface Cull",
                "Skip paintings when the camera is behind them (they are never visible from behind)",
                () -> cfg.entityCulling.paintingBackfaceCulling,
                v -> cfg.entityCulling.paintingBackfaceCulling = v));
    }

    private void initLODTab(int lx, int rx, int y) {
        addDrawableChild(toggleBtn(lx, y, "Entity LOD",
                "Entities beyond the LOD distance render every 2nd tick instead of every frame — nearly invisible at that range",
                () -> cfg.entityLOD.enabled,
                v -> cfg.entityLOD.enabled = v));
        addDrawableChild(toggleBtn(rx, y, "Nameplate Culling",
                "Hide name tags beyond the configured distance",
                () -> cfg.nameplateCulling.enabled,
                v -> cfg.nameplateCulling.enabled = v));
        y += SPACING;

        addDrawableChild(createSlider(lx, y, "LOD distance",
                "Entities beyond this distance (blocks) render every 2nd tick — at this range detail is not visible anyway",
                () -> cfg.entityLOD.farLODDistance,
                v -> cfg.entityLOD.farLODDistance = v,
                16, 320));
        y += SPACING;

        addDrawableChild(createSlider(lx, y, "Nameplate dist",
                "Name tags are hidden beyond this distance (blocks)",
                () -> cfg.nameplateCulling.maxDistance,
                v -> cfg.nameplateCulling.maxDistance = v,
                8, 128));
        addDrawableChild(toggleBtn(rx, y, "Map Frame Throttle",
                "Update map item frames only every few ticks instead of every frame",
                () -> cfg.itemFrame.enabled,
                v -> cfg.itemFrame.enabled = v));
    }

    private void initBackgroundFpsTab(int cx, int lx, int rx, int y) {
        addDrawableChild(toggleBtn(cx - BTN_W / 2, y, "Background FPS Limit",
                "Cap frame rate when the Minecraft window loses focus or is minimised",
                () -> cfg.backgroundFps.enabled,
                v -> cfg.backgroundFps.enabled = v));
        y += SPACING + 6;

        addDrawableChild(createFpsCapSlider(lx, y, "Unfocused FPS cap",
                "FPS limit when the Minecraft window loses focus",
                () -> cfg.backgroundFps.unfocusedFpsCap,
                v -> cfg.backgroundFps.unfocusedFpsCap = v));
        addDrawableChild(createFpsCapSlider(rx, y, "Minimized FPS cap",
                "FPS limit when the Minecraft window is minimised/iconified",
                () -> cfg.backgroundFps.minimizedFpsCap,
                v -> cfg.backgroundFps.minimizedFpsCap = v));
        y += SPACING;

        addDrawableChild(createFpsCapSlider(lx, y, "Menu/Load FPS cap",
                "FPS limit during loading screens, title screen, and server menus — reduces GPU spinning so loading threads finish faster. 0 = no cap.",
                () -> cfg.backgroundFps.titleScreenFpsCap,
                v -> cfg.backgroundFps.titleScreenFpsCap = v));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ButtonWidget toggleBtn(int x, int y, String label, String description,
                                   BoolSupplier getter, BoolConsumer setter) {
        return ButtonWidget.builder(toggleText(label, getter.get()), btn -> {
            boolean next = !getter.get();
            setter.accept(next);
            btn.setMessage(toggleText(label, next));
            ConfigManager.getInstance().save();
        }).dimensions(x, y, BTN_W, BTN_H)
          .tooltip(Tooltip.of(Text.literal(description)))
          .build();
    }

    private static Text toggleText(String label, boolean on) {
        return Text.literal(label + ": " + (on ? "ON" : "OFF"));
    }

    private Text profileText() {
        String selected = cfg.selectedProfile != null ? cfg.selectedProfile : PerformanceProfile.BALANCED.name();
        return Text.literal("Profile: " + selected);
    }

    private void cycleProfile() {
        List<String> profileNames = new ArrayList<>();
        for (PerformanceProfile profile : PerformanceProfile.values()) {
            profileNames.add(profile.name());
        }
        profileNames.addAll(cfg.customProfiles.keySet());

        if (profileNames.isEmpty()) {
            cfg.selectedProfile = PerformanceProfile.BALANCED.name();
            PerformanceProfile.BALANCED.apply(cfg);
            ConfigManager.getInstance().save();
            clearAndInit();
            return;
        }

        String current = cfg.selectedProfile != null ? cfg.selectedProfile : profileNames.get(0);
        int index = profileNames.indexOf(current);
        if (index < 0) index = 0;
        int next = (index + 1) % profileNames.size();
        String nextName = profileNames.get(next);
        cfg.selectedProfile = nextName;

        if (isBuiltInProfile(nextName)) {
            PerformanceProfile.valueOf(nextName).apply(cfg);
            cfg.profile = PerformanceProfile.valueOf(nextName);
        } else {
            FPSFlowConfig.CustomProfile custom = cfg.customProfiles.get(nextName);
            if (custom != null) {
                custom.applyTo(cfg);
            } else {
                PerformanceProfile.BALANCED.apply(cfg);
                cfg.profile = PerformanceProfile.BALANCED;
                cfg.selectedProfile = PerformanceProfile.BALANCED.name();
            }
        }

        ConfigManager.getInstance().save();
        clearAndInit();
    }

    private void saveCurrentAsCustomProfile() {
        String baseName = "Custom";
        int suffix = 1;
        String candidate = baseName + " " + suffix;
        while (cfg.customProfiles.containsKey(candidate)) {
            suffix++;
            candidate = baseName + " " + suffix;
        }
        cfg.customProfiles.put(candidate, new FPSFlowConfig.CustomProfile(cfg));
        cfg.selectedProfile = candidate;
        ConfigManager.getInstance().save();
    }

    private boolean isBuiltInProfile(String profileName) {
        try {
            PerformanceProfile.valueOf(profileName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private SliderWidget createSlider(int x, int y, String label, String description,
                                      IntSupplier getter, IntConsumer setter,
                                      int min, int max) {
        double initialValue = (double)(getter.getAsInt() - min) / (max - min);
        SliderWidget slider = new SliderWidget(x, y, BTN_W, BTN_H,
                Text.literal(label + ": " + getter.getAsInt()), initialValue) {
            @Override
            protected void updateMessage() {
                int value = min + (int) Math.round(this.value * (max - min));
                setMessage(Text.literal(label + ": " + value));
            }
            @Override
            protected void applyValue() {
                int value = min + (int) Math.round(this.value * (max - min));
                setter.accept(Math.max(min, Math.min(max, value)));
                ConfigManager.getInstance().save();
            }
        };
        slider.setTooltip(Tooltip.of(Text.literal(description)));
        return slider;
    }

    private SliderWidget createFpsCapSlider(int x, int y, String label, String description,
                                            IntSupplier getter, IntConsumer setter) {
        final int MAX_FPS = 480;
        double initial = (double) Math.max(0, getter.getAsInt()) / MAX_FPS;
        SliderWidget slider = new SliderWidget(x, y, BTN_W, BTN_H,
                Text.literal(fpsCapLabel(label, getter.getAsInt())), initial) {
            @Override
            protected void updateMessage() {
                int v = (int) Math.round(this.value * MAX_FPS);
                setMessage(Text.literal(fpsCapLabel(label, v)));
            }
            @Override
            protected void applyValue() {
                int v = (int) Math.round(this.value * MAX_FPS);
                setter.accept(v);
                ConfigManager.getInstance().save();
            }
        };
        slider.setTooltip(Tooltip.of(Text.literal(description)));
        return slider;
    }

    private static String fpsCapLabel(String label, int fps) {
        return label + ": " + (fps <= 0 ? "Unlimited" : fps);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 32, 0xFFFFFF);
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
