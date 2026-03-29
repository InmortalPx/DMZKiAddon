package com.dmzkiaddon.mixin;

import com.dmzkiaddon.config.AddonConfig;
import com.dragonminez.common.config.SkillsConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(value = SkillsConfig.class, remap = false)
public class SkillsConfigMixin {

    private static final List<String> ADDON_SKILL_IDS = List.of(
            "addon_ki_laser",
            "addon_dodompa",
            "addon_ki_volley",
            "addon_masenko",
            "addon_galick_gun",
            "addon_kamehameha",
            "addon_ki_disc",
            "addon_makankosappo",
            "addon_taiyoken",
            "addon_big_bang",
            "addon_spirit_bomb",
            "addon_death_ball",
            "addon_hellzone",
            "addon_final_flash",
            "addon_final_kamehameha",
            "addon_hakai",
            "addon_time_skip",
            "addon_point_pressure",
            "addon_final_explosion",
            "addon_kikoho",
            "addon_neo_kikoho"
    );

    @Inject(method = "getSkills", at = @At("RETURN"), cancellable = true)
    private void injectAddonSkillsIntoMap(CallbackInfoReturnable<Map<String, SkillsConfig.SkillCosts>> cir) {
        Map<String, SkillsConfig.SkillCosts> original = cir.getReturnValue();

        boolean needsInjection = false;
        for (String id : ADDON_SKILL_IDS) {
            if (!original.containsKey(id)) {
                needsInjection = true;
                break;
            }
        }
        if (!needsInjection) return;

        Map<String, SkillsConfig.SkillCosts> extended = new HashMap<>(original);
        Map<String, Integer> tpCosts = AddonConfig.getAllTpCosts();

        for (String skillId : ADDON_SKILL_IDS) {
            if (!extended.containsKey(skillId)) {
                Integer cost = tpCosts.get(skillId);
                if (cost != null) {
                    extended.put(skillId, new SkillsConfig.SkillCosts(
                            Collections.singletonList(cost)));
                }
            }
        }

        cir.setReturnValue(extended);
    }

    @Inject(method = "getSkillCosts", at = @At("RETURN"), cancellable = true)
    private void injectAddonSkillCosts(String skillName,
                                       CallbackInfoReturnable<SkillsConfig.SkillCosts> cir) {
        String lower = skillName.toLowerCase();
        if (!ADDON_SKILL_IDS.contains(lower)) return;

        SkillsConfig.SkillCosts existing = cir.getReturnValue();
        if (existing != null && !existing.getCosts().isEmpty()) return;

        Map<String, Integer> tpCosts = AddonConfig.getAllTpCosts();
        Integer cost = tpCosts.get(lower);
        if (cost != null) {
            cir.setReturnValue(new SkillsConfig.SkillCosts(
                    Collections.singletonList(cost)));
        }
    }

    @Inject(method = "getSkillOfferings", at = @At("RETURN"), cancellable = true)
    private void injectSkillOfferings(CallbackInfoReturnable<Map<String, List<String>>> cir) {
        Map<String, List<String>> offerings = new LinkedHashMap<>(cir.getReturnValue());
        boolean changed = false;

        // NOTA: las claves deben coincidir exactamente con los IDs de maestro de DMZ.
        // Se usa un Map normal (no Map.of) para poder tener más de 10 entradas en el futuro.
        Map<String, List<String>> addonOfferings = new LinkedHashMap<>();
        addonOfferings.put("goku",    List.of("addon_kamehameha", "addon_spirit_bomb", "addon_final_kamehameha"));
        addonOfferings.put("kingkai", List.of("addon_dodompa", "addon_taiyoken"));
        addonOfferings.put("roshi",   List.of("addon_ki_disc", "addon_ki_laser", "addon_ki_volley"));
        addonOfferings.put("vegeta",  List.of("addon_galick_gun", "addon_big_bang", "addon_final_flash", "addon_hakai", "addon_final_explosion"));
        addonOfferings.put("piccolo", List.of("addon_makankosappo", "addon_hellzone", "addon_masenko"));
        addonOfferings.put("frieza",  List.of("addon_death_ball", "addon_ki_laser"));
        // Hit enseña el Time-Skip y el Point Pressure
        addonOfferings.put("hit",     List.of("addon_time_skip", "addon_point_pressure"));

        for (Map.Entry<String, List<String>> entry : addonOfferings.entrySet()) {
            String master = entry.getKey();
            List<String> masterList = offerings.containsKey(master)
                    ? new ArrayList<>(offerings.get(master))
                    : new ArrayList<>();

            for (String skill : entry.getValue()) {
                if (!masterList.contains(skill)) {
                    masterList.add(skill);
                    changed = true;
                }
            }
            offerings.put(master, masterList);
        }

        if (changed) cir.setReturnValue(offerings);
    }
}