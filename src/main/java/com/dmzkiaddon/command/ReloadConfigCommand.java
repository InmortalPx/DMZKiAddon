package com.dmzkiaddon.command;

import com.dmzkiaddon.AddonConfig;
import com.dmzkiaddon.DMZKiAddon;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ReloadConfigCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("dmzkiaddon")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload")
                                .executes(ReloadConfigCommand::executeReload)
                        )
        );
    }

    // Público para que GiveKiAttackCommand pueda reutilizarlo
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
}