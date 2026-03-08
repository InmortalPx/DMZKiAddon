package com.dmzkiaddon.mixin;

import com.dmzkiaddon.client.gui.KiAttackMenuSlot;
import com.dragonminez.client.gui.UtilityMenuScreen;
import com.dragonminez.client.gui.utilitymenu.menuslots.EmptyMenuSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.dragonminez.client.gui.utilitymenu.IUtilityMenuSlot;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Injects our KiAttackMenuSlot into the DragonMineZ X-menu.
 *
 * Addon slot indices (in order): {0, 4, 5, 8, 9, 13}
 * We want index 13 (bottom-right corner) for compatibility with other addons
 * that may use the first slots. We register 5 empty slots first, then ours.
 *
 * The HEAD inject ensures ADDON_SLOTS is populated before the grid is built.
 */
@Mixin(value = UtilityMenuScreen.class, remap = false)
public class UtilityMenuScreenMixin {
    @Shadow
    @Final
    private static List<IUtilityMenuSlot> ADDON_SLOTS;

    @Inject(method = "initMenuSlots", at = @At("HEAD"))
    private static void injectAddonSlot(CallbackInfo ci) {
        boolean addonSlotRegistered = ADDON_SLOTS.stream()
                .anyMatch(slot -> slot instanceof KiAttackMenuSlot);
        if (!addonSlotRegistered) {
            // Pad slots 0,4,5,8,9 with empty so our slot lands at index 13
            int targetIndex = 5; // sería el slot 13
            while (ADDON_SLOTS.size() < targetIndex) {
                ADDON_SLOTS.add(new EmptyMenuSlot());
            }
            ADDON_SLOTS.add(new KiAttackMenuSlot()); // index 13
        }
    }
}
