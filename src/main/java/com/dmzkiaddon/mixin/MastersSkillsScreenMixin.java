package com.dmzkiaddon.mixin;

import com.dmzkiaddon.config.AddonConfig;
import com.dragonminez.client.gui.MastersSkillsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = MastersSkillsScreen.class, remap = false)
public class MastersSkillsScreenMixin {

    @Shadow
    private String masterName;

    private static final Map<String, List<String>> ADDON_MASTER_ATTACKS = new LinkedHashMap<>();

    static {
        ADDON_MASTER_ATTACKS.put("goku",    Arrays.asList(
                "addon_ki_laser", "addon_ki_volley", "addon_kamehameha",
                "addon_spirit_bomb", "addon_final_kamehameha", "addon_hakai"));
        ADDON_MASTER_ATTACKS.put("kingkai", Arrays.asList(
                "addon_dodompa", "addon_masenko", "addon_taiyoken"));
        ADDON_MASTER_ATTACKS.put("roshi",   Arrays.asList(
                "addon_ki_disc", "addon_galick_gun", "addon_makankosappo",
                "addon_big_bang", "addon_death_ball", "addon_hellzone", "addon_final_flash"));
    }

    @Inject(method = "getMasterSkills", at = @At("RETURN"), cancellable = true)
    private void injectKiAddonSkills(CallbackInfoReturnable<List<String>> cir) {
        List<String> addonSkills = ADDON_MASTER_ATTACKS.get(masterName.toLowerCase());
        if (addonSkills == null) return;

        List<String> result = new ArrayList<>(cir.getReturnValue());
        for (String attack : addonSkills) {
            if (!result.contains(attack)) result.add(attack);
        }
        cir.setReturnValue(result);
    }

    /**
     * Returns the TP cost for addon skills, reading live from AddonConfig
     * so that /dmzkiaddon reload reflects immediately in the master screen.
     *
     * Injected at HEAD to short-circuit before DMZ's own cost lookup,
     * which would return Integer.MAX_VALUE for unknown skill IDs.
     */
    @Inject(method = "getUpgradeCost", at = @At("HEAD"), cancellable = true)
    private void injectAddonSkillCost(String skillName, int currentLevel,
                                      CallbackInfoReturnable<Integer> cir) {
        if (!skillName.startsWith("addon_")) return;

        Integer cost = AddonConfig.getAllTpCosts().get(skillName.toLowerCase());
        if (cost != null) {
            cir.setReturnValue(cost);
        }
    }
}