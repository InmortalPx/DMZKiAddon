package com.dmzkiaddon;

import com.dmzkiaddon.client.KeyHandler;
import com.dmzkiaddon.client.ScreenEffects;
import com.dmzkiaddon.command.KiAddonCommand;
import com.dmzkiaddon.config.AddonConfig;
import com.dmzkiaddon.network.AddonNetworkHandler;
import com.dmzkiaddon.registry.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DMZKiAddon.MOD_ID)
public class DMZKiAddon {

    public static final String MOD_ID = "dmzkiaddon";
    public static final Logger LOGGER = LogManager.getLogger("DmzKiAddon");

    @SuppressWarnings("removal")
    public DMZKiAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AddonConfig.SPEC, "DMZKiAddon/config.toml");

        modEventBus.addListener(this::commonSetup);

        ModSounds.SOUNDS.register(modEventBus);

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(KeyHandler.class);
            MinecraftForge.EVENT_BUS.register(ScreenEffects.class);
        });

        String addonVersion = ModList.get().getModFileById(MOD_ID)
                .getMods().get(0).getVersion().toString();
        String dmzVersion = ModList.get().isLoaded("dragonminez")
                ? ModList.get().getModFileById("dragonminez").getMods().get(0).getVersion().toString()
                : "unknown";
        String forgeVersion = net.minecraftforge.versions.forge.ForgeVersion.getVersion();

        LOGGER.info("  ╔══════════════════════════════════════╗");
        LOGGER.info("  ║       DMZ Ki Addon  v{}           ║", addonVersion);
        LOGGER.info("  ╠══════════════════════════════════════╣");
        LOGGER.info("  ║  DragonMineZ : {}                ║", dmzVersion);
        LOGGER.info("  ║  Forge       : {}            ║", forgeVersion);
        LOGGER.info("  ╠══════════════════════════════════════╣");
        LOGGER.info("  ║  By: carlosn2300K                    ║");
        LOGGER.info("  ║  With: Facub8  |  InmortalPx         ║");
        LOGGER.info("  ╚══════════════════════════════════════╝");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AddonNetworkHandler::register);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        KiAddonCommand.register(event.getDispatcher());
    }
}