package com.dmzkiaddon.registry;

import com.dmzkiaddon.DMZKiAddon;
import com.dmzkiaddon.network.AddonNetworkHandler;
import com.dmzkiaddon.network.packets.SyncAttackRegistryS2C;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.PacketDistributor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CustomAttackManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("dmzkiaddon");
    private static final Path ATTACKS_FILE = CONFIG_DIR.resolve("custom_attacks.json");

    private static final List<AttackRegistry.KiAttackEntry> CUSTOM_ATTACKS = new ArrayList<>();

    public static void load() {
        if (!Files.exists(ATTACKS_FILE)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(ATTACKS_FILE)) {
            List<AttackRegistry.KiAttackEntry> loaded = GSON.fromJson(reader,
                    new TypeToken<List<AttackRegistry.KiAttackEntry>>() {
                    }.getType());

            if (loaded != null) {
                CUSTOM_ATTACKS.clear();
                CUSTOM_ATTACKS.addAll(loaded);
                for (AttackRegistry.KiAttackEntry entry : loaded) {
                    AttackRegistry.register(entry);
                }
                DMZKiAddon.LOGGER.info("Loaded {} custom attacks from JSON", loaded.size());
            }
        } catch (Exception e) {
            DMZKiAddon.LOGGER.error("Failed to load custom attacks: {}", e.getMessage());
        }
    }

    public static void save(AttackRegistry.KiAttackEntry entry) {

        if (!entry.id().startsWith("custom_"))
            return;


        CUSTOM_ATTACKS.removeIf(e -> e.id().equals(entry.id()));
        CUSTOM_ATTACKS.add(entry);

        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }

            try (Writer writer = Files.newBufferedWriter(ATTACKS_FILE)) {
                GSON.toJson(CUSTOM_ATTACKS, writer);
            }
        } catch (IOException e) {
            DMZKiAddon.LOGGER.error("Failed to save custom attacks: {}", e.getMessage());
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        for (AttackRegistry.KiAttackEntry entry : AttackRegistry.ALL) {
            if (entry.id().startsWith("custom_")) {
                AddonNetworkHandler.sendToPlayer(
                        new SyncAttackRegistryS2C(entry), player);
            }
        }
    }
}
