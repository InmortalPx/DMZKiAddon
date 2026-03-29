package com.dmzkiaddon;

import com.google.gson.*;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Parchea el skills_config.json del DragonMinZ automaticamente al iniciar.
 * Agrega los ataques Ki del addon si no estan presentes — sin tocar nada existente.
 */
public class SkillsConfigPatcher {

    private static final Logger LOGGER = LogManager.getLogger("DmzKiAddon");

    // Ruta del config del mod base
    private static final String CONFIG_PATH = "config/dragonminez/skills_config.json";

    // ===== DEFINICION DE ATAQUES (orden: debil → fuerte) ====================
    private static final List<String> KI_SKILLS_ORDER = Arrays.asList(
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
        "addon_hakai"
    );

    private static final Map<String, Integer> SKILL_COSTS = new LinkedHashMap<>();
    static {
        SKILL_COSTS.put("addon_ki_laser",         5000);
        SKILL_COSTS.put("addon_dodompa",           8000);
        SKILL_COSTS.put("addon_ki_volley",         12000);
        SKILL_COSTS.put("addon_masenko",           18000);
        SKILL_COSTS.put("addon_galick_gun",        25000);
        SKILL_COSTS.put("addon_kamehameha",        35000);
        SKILL_COSTS.put("addon_ki_disc",           45000);
        SKILL_COSTS.put("addon_makankosappo",      60000);
        SKILL_COSTS.put("addon_taiyoken",          80000);
        SKILL_COSTS.put("addon_big_bang",          100000);
        SKILL_COSTS.put("addon_spirit_bomb",       130000);
        SKILL_COSTS.put("addon_death_ball",        160000);
        SKILL_COSTS.put("addon_hellzone",          200000);
        SKILL_COSTS.put("addon_final_flash",       350000);
        SKILL_COSTS.put("addon_final_kamehameha",  600000);
        SKILL_COSTS.put("addon_hakai",             2400000);
    }

    // Que ataques ofrece cada master
    private static final Map<String, List<String>> MASTER_OFFERINGS = new LinkedHashMap<>();
    static {
        MASTER_OFFERINGS.put("goku", Arrays.asList(
            "addon_ki_laser", "addon_ki_volley", "addon_kamehameha",
            "addon_spirit_bomb", "addon_final_kamehameha", "addon_hakai"
        ));
        MASTER_OFFERINGS.put("kingkai", Arrays.asList(
            "addon_dodompa", "addon_masenko", "addon_ki_laser", "addon_taiyoken"
        ));
        MASTER_OFFERINGS.put("roshi", Arrays.asList(
            "addon_ki_volley", "addon_ki_disc", "addon_dodompa"
        ));
    }

    // ========================================================================

    public static void patch() {
        Path configFile = FMLPaths.GAMEDIR.get().resolve(CONFIG_PATH);

        if (!Files.exists(configFile)) {
            LOGGER.warn("[DmzKiAddon] No se encontro skills_config.json del DragonMinZ en: {}", configFile);
            return;
        }

        try {
            // Leer el config existente
            String content = new String(Files.readAllBytes(configFile));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            boolean modified = false;

            // ── 1. kiSkills ─────────────────────────────────────────────────
            JsonArray kiSkills = root.has("kiSkills")
                ? root.getAsJsonArray("kiSkills")
                : new JsonArray();

            Set<String> existingKiSkills = new HashSet<>();
            kiSkills.forEach(e -> existingKiSkills.add(e.getAsString()));

            for (String skillId : KI_SKILLS_ORDER) {
                if (!existingKiSkills.contains(skillId)) {
                    kiSkills.add(skillId);
                    modified = true;
                    LOGGER.info("[DmzKiAddon] Agregando kiSkill: {}", skillId);
                }
            }
            root.add("kiSkills", kiSkills);

            // ── 2. skills (costos) ───────────────────────────────────────────
            JsonObject skills = root.has("skills")
                ? root.getAsJsonObject("skills")
                : new JsonObject();

            for (Map.Entry<String, Integer> entry : SKILL_COSTS.entrySet()) {
                if (!skills.has(entry.getKey())) {
                    JsonObject skillEntry = new JsonObject();
                    JsonArray costs = new JsonArray();
                    costs.add(entry.getValue());
                    skillEntry.add("costs", costs);
                    skills.add(entry.getKey(), skillEntry);
                    modified = true;
                }
            }
            root.add("skills", skills);

            // ── 3. skillOfferings ────────────────────────────────────────────
            JsonObject offerings = root.has("skillOfferings")
                ? root.getAsJsonObject("skillOfferings")
                : new JsonObject();

            for (Map.Entry<String, List<String>> master : MASTER_OFFERINGS.entrySet()) {
                String masterName = master.getKey();
                JsonArray masterList = offerings.has(masterName)
                    ? offerings.getAsJsonArray(masterName)
                    : new JsonArray();

                Set<String> existingOfferings = new HashSet<>();
                masterList.forEach(e -> existingOfferings.add(e.getAsString()));

                for (String skillId : master.getValue()) {
                    if (!existingOfferings.contains(skillId)) {
                        masterList.add(skillId);
                        modified = true;
                    }
                }
                offerings.add(masterName, masterList);
            }
            root.add("skillOfferings", offerings);

            // ── Guardar si hubo cambios ──────────────────────────────────────
            if (modified) {
                String output = gson.toJson(root);
                Files.write(configFile, output.getBytes(),
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                LOGGER.info("[DmzKiAddon] skills_config.json actualizado con los ataques Ki del addon.");
            } else {
                LOGGER.info("[DmzKiAddon] skills_config.json ya tiene los ataques Ki — sin cambios.");
            }

        } catch (Exception e) {
            LOGGER.error("[DmzKiAddon] Error al parchear skills_config.json: {}", e.getMessage());
        }
    }
}
