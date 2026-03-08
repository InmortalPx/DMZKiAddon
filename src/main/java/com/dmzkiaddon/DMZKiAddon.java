package com.dmzkiaddon;

import com.dmzkiaddon.client.KeyHandler;
import com.dmzkiaddon.client.ScreenEffects;
import com.dmzkiaddon.command.GiveKiAttackCommand;
import com.dmzkiaddon.network.AddonNetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
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

        LOGGER.info(" >> DMZKiAddon");
        LOGGER.info("");
        LOGGER.info(" >> Mod hecho por: carlosn2300K");
        LOGGER.info(" >> Con ayuda de:  Facub8  |  InmortalPx");
        LOGGER.info(" >> Version: 1.0.0 para DragonMineZ 2.0.2 | Forge 1.20.1");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AddonNetworkHandler.register();
            SkillsConfigPatcher.patch();
        });
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        // GiveKiAttackCommand incluye /give, /set y /reload en un solo árbol
        GiveKiAttackCommand.register(event.getDispatcher());
    }
}