package com.dmzkiaddon.mixin;

import com.dragonminez.common.config.SkillsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

/**
 * Injects the addon's skill IDs and costs into DragonMineZ's SkillsConfig
 * so they are recognized as valid Ki skills in the base mod's systems.
 */
@Mixin(value = SkillsConfig.class, remap = false)
public class SkillsConfigMixin {

    private static final Map<String, Integer> ADDON_COSTS = new LinkedHashMap<>();

    static {
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

    @Inject(method = "getSkillCosts", at = @At("RETURN"), cancellable = true)
    private void injectAddonSkillCosts(String skillName,
                                        CallbackInfoReturnable<SkillsConfig.SkillCosts> cir) {
        String lower = skillName.toLowerCase();
        if (ADDON_COSTS.containsKey(lower)) {
            SkillsConfig.SkillCosts existing = cir.getReturnValue();
            if (existing == null || existing.getCosts().isEmpty()) {
                Integer cost = ADDON_COSTS.get(lower);
                cir.setReturnValue(new SkillsConfig.SkillCosts(
                        Collections.singletonList(cost)));
            }
        }
    }

    @Inject(method = "getKiSkills", at = @At("RETURN"), cancellable = true)
    private void injectKiSkillIds(CallbackInfoReturnable<List<String>> cir) {
        List<String> list = new ArrayList<>(cir.getReturnValue());
        boolean changed = false;
        for (String key : ADDON_COSTS.keySet()) {
            if (!list.contains(key)) {
                list.add(key);
                changed = true;
            }
        }
        if (changed) cir.setReturnValue(list);
    }
}
