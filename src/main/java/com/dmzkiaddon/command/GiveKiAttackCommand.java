package com.dmzkiaddon.command;

import com.dmzkiaddon.DMZKiAddon;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GiveKiAttackCommand {

    // Nombre corto → skill ID completo
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
            (ctx, builder) -> SharedSuggestionProvider.suggest(ATTACK_MAP.keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dmzkiaddon")
                        .requires(source -> source.hasPermission(2))

                        // /dmzkiaddon give <attack> [targets]
                        .then(Commands.literal("give")
                                .then(Commands.argument("attack", StringArgumentType.string())
                                        .suggests(ATTACK_SUGGESTIONS)
                                        .executes(ctx -> executeGive(
                                                ctx,
                                                List.of(ctx.getSource().getPlayerOrException()),
                                                StringArgumentType.getString(ctx, "attack")
                                        ))
                                        .then(Commands.argument("targets", EntityArgument.players())
                                                .executes(ctx -> executeGive(
                                                        ctx,
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "attack")
                                                ))
                                        )
                                )
                        )

                        // /dmzkiaddon set <attack> <level> [targets]
                        .then(Commands.literal("set")
                                .then(Commands.argument("attack", StringArgumentType.string())
                                        .suggests(ATTACK_SUGGESTIONS)
                                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 10))
                                                .executes(ctx -> executeSet(
                                                        ctx,
                                                        List.of(ctx.getSource().getPlayerOrException()),
                                                        StringArgumentType.getString(ctx, "attack"),
                                                        IntegerArgumentType.getInteger(ctx, "level")
                                                ))
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                        .executes(ctx -> executeSet(
                                                                ctx,
                                                                EntityArgument.getPlayers(ctx, "targets"),
                                                                StringArgumentType.getString(ctx, "attack"),
                                                                IntegerArgumentType.getInteger(ctx, "level")
                                                        ))
                                                )
                                        )
                                )
                        )

                        // /dmzkiaddon reload
                        .then(Commands.literal("reload")
                                .executes(ReloadConfigCommand::executeReload)
                        )
        );
    }

    private static int executeGive(CommandContext<CommandSourceStack> ctx,
                                   Collection<ServerPlayer> targets, String attack) {
        String skillId = resolveSkillId(attack);
        if (skillId == null) {
            ctx.getSource().sendFailure(
                    Component.translatable("command.dmzkiaddon.attack.unknown", attack)
            );
            return 0;
        }

        for (ServerPlayer player : targets) {
            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(data -> {
                data.getSkills().setSkillLevel(skillId, 1);
                NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(player), player);
            });
        }

        if (targets.size() == 1) {
            ServerPlayer target = targets.iterator().next();
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.dmzkiaddon.give.success",
                            attack, target.getName().getString()),
                    true
            );
            DMZKiAddon.LOGGER.info("[DMZKiAddon] Gave {} to {}", attack, target.getName().getString());
        } else {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.dmzkiaddon.give.success_multiple",
                            attack, targets.size()),
                    true
            );
            DMZKiAddon.LOGGER.info("[DMZKiAddon] Gave {} to {} players", attack, targets.size());
        }
        return targets.size();
    }

    private static int executeSet(CommandContext<CommandSourceStack> ctx,
                                  Collection<ServerPlayer> targets, String attack, int level) {
        String skillId = resolveSkillId(attack);
        if (skillId == null) {
            ctx.getSource().sendFailure(
                    Component.translatable("command.dmzkiaddon.attack.unknown", attack)
            );
            return 0;
        }

        for (ServerPlayer player : targets) {
            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(data -> {
                data.getSkills().setSkillLevel(skillId, level);
                NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(player), player);
            });
        }

        if (targets.size() == 1) {
            ServerPlayer target = targets.iterator().next();
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.dmzkiaddon.set.success",
                            attack, level, target.getName().getString()),
                    true
            );
            DMZKiAddon.LOGGER.info("[DMZKiAddon] Set {} level {} for {}", attack, level, target.getName().getString());
        } else {
            ctx.getSource().sendSuccess(
                    () -> Component.translatable("command.dmzkiaddon.set.success_multiple",
                            attack, level, targets.size()),
                    true
            );
            DMZKiAddon.LOGGER.info("[DMZKiAddon] Set {} level {} for {} players", attack, level, targets.size());
        }
        return targets.size();
    }

    /**
     * Resuelve el nombre corto al skill ID completo.
     * Acepta tanto "kamehameha" como "addon_kamehameha" por si acaso.
     */
    private static String resolveSkillId(String input) {
        String lower = input.toLowerCase();
        if (ATTACK_MAP.containsKey(lower)) return ATTACK_MAP.get(lower);
        // También acepta el nombre completo directamente
        if (ATTACK_MAP.containsValue(lower)) return lower;
        return null;
    }
}