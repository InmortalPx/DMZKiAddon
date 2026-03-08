package com.dmzkiaddon.command;

import com.dmzkiaddon.config.AddonConfig;
import com.dmzkiaddon.DMZKiAddon;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class KiAddonCommand {

    private static final Map<String, String> ATTACK_MAP = Map.ofEntries(
            Map.entry("ki_laser",          "addon_ki_laser"),
            Map.entry("dodompa",           "addon_dodompa"),
            Map.entry("ki_volley",         "addon_ki_volley"),
            Map.entry("masenko",           "addon_masenko"),
            Map.entry("galick_gun",        "addon_galick_gun"),
            Map.entry("kamehameha",        "addon_kamehameha"),
            Map.entry("ki_disc",           "addon_ki_disc"),
            Map.entry("makankosappo",      "addon_makankosappo"),
            Map.entry("taiyoken",          "addon_taiyoken"),
            Map.entry("big_bang",          "addon_big_bang"),
            Map.entry("spirit_bomb",       "addon_spirit_bomb"),
            Map.entry("death_ball",        "addon_death_ball"),
            Map.entry("hellzone",          "addon_hellzone"),
            Map.entry("final_flash",       "addon_final_flash"),
            Map.entry("final_kamehameha",  "addon_final_kamehameha"),
            Map.entry("hakai",             "addon_hakai")
    );

    private static final SuggestionProvider<CommandSourceStack> ATTACK_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(
                    java.util.stream.Stream.concat(
                            ATTACK_MAP.keySet().stream(),
                            java.util.stream.Stream.of("all")
                    ).toList(),
                    builder
            );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dmzkiaddon")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("give")
                                .then(Commands.argument("attack", StringArgumentType.string())
                                        .suggests(ATTACK_SUGGESTIONS)
                                        .executes(ctx -> executeGive(ctx,
                                                List.of(ctx.getSource().getPlayerOrException()),
                                                StringArgumentType.getString(ctx, "attack"), true))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> executeGive(ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "attack"), true))
                                        )
                                )
                        )

                        .then(Commands.literal("remove")
                                .then(Commands.argument("attack", StringArgumentType.string())
                                        .suggests(ATTACK_SUGGESTIONS)
                                        .executes(ctx -> executeGive(ctx,
                                                List.of(ctx.getSource().getPlayerOrException()),
                                                StringArgumentType.getString(ctx, "attack"), false))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> executeGive(ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "attack"), false))
                                        )
                                )
                        )

                        .then(Commands.literal("reload")
                                .executes(KiAddonCommand::executeReload)
                        )
        );
    }

    private static int executeGive(CommandContext<CommandSourceStack> ctx,
                                   Collection<ServerPlayer> targets,
                                   String attack, boolean give) {

        boolean all = attack.equalsIgnoreCase("all");
        List<String> skillIds;

        if (all) {
            skillIds = List.copyOf(ATTACK_MAP.values());
        } else {
            String id = resolveSkillId(attack);
            if (id == null) {
                ctx.getSource().sendFailure(
                        Component.translatable("command.dmzkiaddon.attack.unknown", attack));
                return 0;
            }
            skillIds = List.of(id);
        }

        for (ServerPlayer player : targets) {
            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(data -> {
                for (String id : skillIds) {
                    if (give) {
                        data.getSkills().setSkillLevel(id, 1);
                    } else {
                        removeSkillCompletely(data.getSkills(), id);
                    }
                }
                NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(player), player);
            });
        }

        String attackLabel = all
                ? Component.translatable("command.dmzkiaddon.attack.all").getString()
                : attack;

        if (targets.size() == 1) {
            ServerPlayer target = targets.iterator().next();
            String finalLabel = attackLabel;
            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            give ? "command.dmzkiaddon.give.success"
                                    : "command.dmzkiaddon.remove.success",
                            finalLabel,
                            target.getName().getString()
                    ),
                    true
            );
            DMZKiAddon.LOGGER.info("[DMZKiAddon] {} {} for {}",
                    give ? "gave" : "removed", attackLabel, target.getName().getString());
        } else {
            String finalLabel = attackLabel;
            ctx.getSource().sendSuccess(
                    () -> Component.translatable(
                            give ? "command.dmzkiaddon.give.success_multiple"
                                    : "command.dmzkiaddon.remove.success_multiple",
                            finalLabel,
                            targets.size()
                    ),
                    true
            );
            DMZKiAddon.LOGGER.info("[DMZKiAddon] {} {} for {} players",
                    give ? "gave" : "removed", attackLabel, targets.size());
        }
        return targets.size();
    }

    /**
     * Attempts to fully remove a skill from the player's skill data so it
     * disappears from menus. Strategy (in order):
     *
     *  1. Call removeSkill(String) if DMZ exposes it publicly.
     *  2. Find the internal Map field by reflection and remove the key.
     *  3. Fall back to setSkillLevel(id, 0) with a warning.
     */
    private static void removeSkillCompletely(Object skillsObj, String skillId) {
        // Strategy 1: public removeSkill method
        try {
            Method removeMethod = skillsObj.getClass().getMethod("removeSkill", String.class);
            removeMethod.invoke(skillsObj, skillId);
            return;
        } catch (NoSuchMethodException ignored) {
        } catch (Exception e) {
            DMZKiAddon.LOGGER.warn("[DMZKiAddon] removeSkill({}) threw: {}", skillId, e.getMessage());
        }

        // Strategy 2: find the backing Map field by reflection
        String[] candidateFields = {"skills", "skillLevels", "learnedSkills", "skillMap", "data"};
        for (String fieldName : candidateFields) {
            try {
                Field f = getFieldFromHierarchy(skillsObj.getClass(), fieldName);
                if (f == null) continue;
                f.setAccessible(true);
                Object val = f.get(skillsObj);
                if (val instanceof Map<?, ?> map) {
                    map.remove(skillId);
                    DMZKiAddon.LOGGER.debug("[DMZKiAddon] Removed '{}' via field '{}'", skillId, fieldName);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // Strategy 3: last resort
        try {
            skillsObj.getClass().getMethod("setSkillLevel", String.class, int.class)
                    .invoke(skillsObj, skillId, 0);
            DMZKiAddon.LOGGER.warn("[DMZKiAddon] Could not fully remove '{}'; set to level 0 as fallback.", skillId);
        } catch (Exception e) {
            DMZKiAddon.LOGGER.error("[DMZKiAddon] All removal strategies failed for '{}'", skillId, e);
        }
    }

    private static Field getFieldFromHierarchy(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try { return clazz.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) {}
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static int executeReload(CommandContext<CommandSourceStack> ctx) {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get().resolve("DMZKiAddon/config.toml");
            try (CommentedFileConfig fileConfig = CommentedFileConfig.of(configPath.toFile())) {
                fileConfig.load();
                AddonConfig.SPEC.setConfig(fileConfig);
            }
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.dmzkiaddon.reload.success"),
                    true
            );
            DMZKiAddon.LOGGER.info("[DMZKiAddon] Config reloaded by {}", ctx.getSource().getTextName());
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(
                    Component.translatable("command.dmzkiaddon.reload.fail",
                            e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
            );
            DMZKiAddon.LOGGER.error("[DMZKiAddon] Failed to reload config", e);
            return 0;
        }
    }

    private static String resolveSkillId(String input) {
        String lower = input.toLowerCase();
        if (ATTACK_MAP.containsKey(lower)) return ATTACK_MAP.get(lower);
        if (ATTACK_MAP.containsValue(lower)) return lower;
        return null;
    }
}