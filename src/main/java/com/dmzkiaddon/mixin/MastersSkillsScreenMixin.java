package com.dmzkiaddon.mixin;

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
    private static final Map<String, Integer> ADDON_COSTS = new LinkedHashMap<>();

    static {
        ADDON_MASTER_ATTACKS.put("goku",    Arrays.asList("addon_ki_laser", "addon_ki_volley", "addon_kamehameha", "addon_spirit_bomb", "addon_final_kamehameha", "addon_hakai"));
        ADDON_MASTER_ATTACKS.put("kingkai", Arrays.asList("addon_dodompa",  "addon_masenko",   "addon_taiyoken"));
        ADDON_MASTER_ATTACKS.put("roshi",   Arrays.asList("addon_ki_disc",  "addon_galick_gun","addon_makankosappo","addon_big_bang","addon_death_ball","addon_hellzone","addon_final_flash"));

        ADDON_COSTS.put("addon_ki_laser",         10);
        ADDON_COSTS.put("addon_dodompa",          10);
        ADDON_COSTS.put("addon_ki_volley",        20);
        ADDON_COSTS.put("addon_masenko",          30);
        ADDON_COSTS.put("addon_galick_gun",       40);
        ADDON_COSTS.put("addon_kamehameha",       40);
        ADDON_COSTS.put("addon_ki_disc",          30);
        ADDON_COSTS.put("addon_makankosappo",     50);
        ADDON_COSTS.put("addon_taiyoken",         20);
        ADDON_COSTS.put("addon_big_bang",         60);
        ADDON_COSTS.put("addon_spirit_bomb",      80);
        ADDON_COSTS.put("addon_death_ball",       70);
        ADDON_COSTS.put("addon_hellzone",         50);
        ADDON_COSTS.put("addon_final_flash",      70);
        ADDON_COSTS.put("addon_final_kamehameha", 100);
        ADDON_COSTS.put("addon_hakai",            100);
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

    @Inject(method = "getUpgradeCost", at = @At("HEAD"), cancellable = true)
    private void injectAddonSkillCost(String skillName, int currentLevel, CallbackInfoReturnable<Integer> cir) {
        if (skillName.startsWith("addon_") && ADDON_COSTS.containsKey(skillName)) {
            Integer cost = ADDON_COSTS.get(skillName);
            cir.setReturnValue(cost != null ? cost : 30);
        }
    }
}