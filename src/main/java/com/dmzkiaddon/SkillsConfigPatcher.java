package com.dmzkiaddon;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * Patches the DragonMineZ skills_config.json to inject addon Ki skills,
 * costs, and master offerings on startup.
 */
public class SkillsConfigPatcher {

    private static final Logger LOGGER = LogManager.getLogger("DmzKiAddon");
    private static final String CONFIG_PATH = "config/dragonminez/skills_config.json";

    /** Ordered list of all skill IDs this addon adds */
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

    /** Ki cost per skill (in ki points) */
    private static final Map<String, Integer> SKILL_COSTS = new LinkedHashMap<>();

    /** Which master teaches which skills */
    private static final Map<String, List<String>> MASTER_OFFERINGS = new LinkedHashMap<>();

    static {
        // Costs
        SKILL_COSTS.put("addon_ki_laser", 10);
        SKILL_COSTS.put("addon_dodompa", 10);
        SKILL_COSTS.put("addon_ki_volley", 20);
        SKILL_COSTS.put("addon_masenko", 30);
        SKILL_COSTS.put("addon_galick_gun", 40);
        SKILL_COSTS.put("addon_kamehameha", 40);
        SKILL_COSTS.put("addon_ki_disc", 30);
        SKILL_COSTS.put("addon_makankosappo", 50);
        SKILL_COSTS.put("addon_taiyoken", 20);
        SKILL_COSTS.put("addon_big_bang", 60);
        SKILL_COSTS.put("addon_spirit_bomb", 80);
        SKILL_COSTS.put("addon_death_ball", 70);
        SKILL_COSTS.put("addon_hellzone", 50);
        SKILL_COSTS.put("addon_final_flash", 70);
        SKILL_COSTS.put("addon_final_kamehameha", 100);
        SKILL_COSTS.put("addon_hakai", 100);

        // Master offerings
        MASTER_OFFERINGS.put("goku",    Arrays.asList("addon_ki_laser", "addon_ki_volley", "addon_kamehameha", "addon_spirit_bomb", "addon_final_kamehameha", "addon_hakai"));
        MASTER_OFFERINGS.put("kingkai", Arrays.asList("addon_dodompa", "addon_masenko", "addon_taiyoken"));
        MASTER_OFFERINGS.put("roshi",   Arrays.asList("addon_ki_disc", "addon_galick_gun", "addon_makankosappo", "addon_big_bang", "addon_death_ball", "addon_hellzone", "addon_final_flash"));
    }

    public static void patch() {
        try {
            Path configFile = FMLPaths.GAMEDIR.get().resolve(CONFIG_PATH);
            if (!Files.exists(configFile)) {
                LOGGER.warn("[DmzKiAddon] No se encontro skills_config.json del DragonMinZ en: {}", configFile);
                return;
            }

            String content = new String(Files.readAllBytes(configFile));
            var gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            boolean modified = false;

            // --- Inject kiSkills list ---
            if (root.has("kiSkills")) {
                JsonArray kiSkillsArray = root.getAsJsonArray("kiSkills");
                Set<String> existingKiSkills = new HashSet<>();
                kiSkillsArray.forEach(e -> existingKiSkills.add(e.getAsString()));

                for (String skillId : KI_SKILLS_ORDER) {
                    if (!existingKiSkills.contains(skillId)) {
                        LOGGER.info("[DmzKiAddon] Agregando kiSkill: {}", skillId);
                        kiSkillsArray.add(skillId);
                        modified = true;
                    }
                }
            }

            // --- Inject skill definitions (costs) ---
            if (root.has("skills")) {
                JsonObject skills = root.getAsJsonObject("skills");
                for (Map.Entry<String, Integer> entry : SKILL_COSTS.entrySet()) {
                    String skillId = entry.getKey();
                    if (!skills.has(skillId)) {
                        JsonObject skillEntry = new JsonObject();
                        skillEntry.addProperty("cost", entry.getValue());
                        skills.add(skillId, skillEntry);
                        modified = true;
                    }
                }
            }

            // --- Inject master skill offerings ---
            if (root.has("costs")) {
                JsonObject costs = root.getAsJsonObject("costs");
                for (Map.Entry<String, List<String>> entry : MASTER_OFFERINGS.entrySet()) {
                    String masterName = entry.getKey();
                    if (costs.has(masterName)) {
                        JsonObject masterObj = costs.getAsJsonObject(masterName);
                        if (masterObj.has("skillOfferings")) {
                            JsonArray offerings = masterObj.getAsJsonArray("skillOfferings");
                            Set<String> existingOfferings = new HashSet<>();
                            offerings.forEach(e -> existingOfferings.add(e.getAsString()));
                            for (String skill : entry.getValue()) {
                                if (!existingOfferings.contains(skill)) {
                                    offerings.add(skill);
                                    modified = true;
                                }
                            }
                        }
                    }
                }
            }

            if (modified) {
                String output = gson.toJson(root);
                Files.write(configFile, output.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                LOGGER.info("[DmzKiAddon] skills_config.json actualizado con los ataques Ki del addon.");
            } else {
                LOGGER.info("[DmzKiAddon] skills_config.json ya tiene los ataques Ki sin cambios.");
            }

        } catch (Exception e) {
            LOGGER.error("[DmzKiAddon] Error al parchear skills_config.json: {}", e.getMessage());
        }
    }
}
