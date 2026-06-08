package dev.fpsflow.mixin.gui;

import dev.fpsflow.compatibility.CompatibilityChecker;
import dev.fpsflow.config.ConfigManager;
import dev.fpsflow.gui.GUIOptimizer;
import dev.fpsflow.gui.HUDCache;
import dev.fpsflow.utils.FPSFlowUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    /**
     * At the start of each HUD render frame, update the hotbar cache.
     * If ImmediatelyFast is present its own batching already handles draw-call
     * reduction, so we skip the hotbar hash check to avoid double-work.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void fpsflow$onRenderHead(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ConfigManager.getInstance().getConfig().guiOptimization.enabled) return;
        if (CompatibilityChecker.getInstance().isImmediatelyFastPresent()) return;
        if (!ConfigManager.getInstance().getConfig().guiOptimization.hotbarCaching) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        PlayerInventory inv = mc.player.getInventory();
        HUDCache cache = HUDCache.getInstance();

        // Update per-slot hashes so we know which slots actually changed this frame
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            long hash = FPSFlowUtils.itemStackHash(stack);
            cache.isHotbarSlotDirty(i, hash); // side-effect: updates stored hash
        }
        cache.isSelectedSlotDirty(inv.getSelectedSlot());
    }

    /**
     * Throttle non-critical player stat updates (health, food, armor, XP)
     * by tracking dirty flags. The render itself always runs, but downstream
     * code can query {@link GUIOptimizer#isHUDUpdateTick()} to decide whether
     * to perform expensive recalculations.
     *
     * This injection is used to keep HUDCache in sync with the player's state
     * every render frame so dirty checks are always accurate.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void fpsflow$onRenderTail(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!ConfigManager.getInstance().getConfig().guiOptimization.enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (player == null) return;

        HUDCache cache = HUDCache.getInstance();

        cache.isHealthDirty(
                (int) player.getHealth(),
                (int) player.getMaxHealth(),
                player.getAbsorptionAmount()
        );
        cache.isFoodDirty(
                player.getHungerManager().getFoodLevel(),
                player.getHungerManager().getSaturationLevel()
        );
        cache.isArmorDirty(player.getArmor());
        cache.isXpDirty(player.experienceProgress, player.experienceLevel);
        cache.isAirDirty(player.getAir());
    }
}
