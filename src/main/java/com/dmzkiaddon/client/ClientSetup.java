package com.dmzkiaddon.client;

import com.dmzkiaddon.client.model.KiBallModel;
import com.dmzkiaddon.client.model.KiBeamModel;
import com.dmzkiaddon.client.renderer.KiBlastRenderer;
import com.dmzkiaddon.client.renderer.KiWaveRenderer;
import com.dmzkiaddon.client.renderer.MasterFriezaRenderer;
import com.dmzkiaddon.client.renderer.MasterHitRenderer;
import com.dmzkiaddon.client.renderer.MasterPiccoloRenderer;
import com.dmzkiaddon.client.renderer.MasterTenshinhanRenderer;
import com.dmzkiaddon.client.renderer.MasterVegetaRenderer;
import com.dmzkiaddon.entity.KiWaveAddon;
import com.dmzkiaddon.registry.ModEntities;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.dmzkiaddon.DMZKiAddon.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    public static final String CATEGORY = "key.categories.dmzkiaddon";

    public static final KeyMapping KEY_FIRE = registerKey("fire", InputConstants.KEY_R);
    public static final KeyMapping KEY_ATTACK_PREV = registerKey("attack_prev", InputConstants.KEY_LBRACKET);
    public static final KeyMapping KEY_ATTACK_NEXT = registerKey("attack_next", InputConstants.KEY_RBRACKET);
    public static final KeyMapping KEY_KI_SHIELD = registerKey("ki_shield", -1);
    public static final KeyMapping KEY_HELLZONE = registerKey("hellzone", -1);
    public static final KeyMapping KEY_HAKAI_SPAM = registerKey("hakai_spam", InputConstants.KEY_SPACE);
    public static final KeyMapping KEY_HAKAI = registerKey("hakai", -1);
    public static final KeyMapping KEY_TAIYOKEN = registerKey("taiyoken", -1);
    public static final KeyMapping KEY_TIME_SKIP = registerKey("time_skip", -1);
    public static final KeyMapping KEY_POINT_PRESSURE = registerKey("point_pressure", -1);
    public static final KeyMapping KEY_FINAL_EXPLOSION = registerKey("final_explosion", -1);
    public static final KeyMapping KEY_KIKOHO = registerKey("kikoho", InputConstants.KEY_X);
    public static final KeyMapping KEY_NEO_KIKOHO = registerKey("neo_kikoho", InputConstants.KEY_Z);

    private static KeyMapping registerKey(String name, int defaultKeyCode) {
        return new KeyMapping(
                "key.dmzkiaddon." + name,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                defaultKeyCode,
                CATEGORY
        );
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KEY_FIRE);
        event.register(KEY_ATTACK_PREV);
        event.register(KEY_ATTACK_NEXT);
        event.register(KEY_KI_SHIELD);
        event.register(KEY_HELLZONE);
        event.register(KEY_HAKAI_SPAM);
        event.register(KEY_HAKAI);
        event.register(KEY_TAIYOKEN);
        event.register(KEY_TIME_SKIP);
        event.register(KEY_POINT_PRESSURE);
        event.register(KEY_FINAL_EXPLOSION);
        event.register(KEY_KIKOHO);
        event.register(KEY_NEO_KIKOHO);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(KiBallModel.LAYER_LOCATION, KiBallModel::createBodyLayer);
        event.registerLayerDefinition(KiBeamModel.LAYER_LOCATION, KiBeamModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        KiWaveAddon.prewarmAccessors();
        event.registerEntityRenderer(ModEntities.KI_BLAST_ADDON.get(), KiBlastRenderer::new);
        event.registerEntityRenderer(ModEntities.KI_WAVE_ADDON.get(),  KiWaveRenderer::new);
        event.registerEntityRenderer(ModEntities.MASTER_VEGETA.get(), MasterVegetaRenderer::new);
        event.registerEntityRenderer(ModEntities.MASTER_PICCOLO.get(), MasterPiccoloRenderer::new);
        event.registerEntityRenderer(ModEntities.MASTER_FRIEZA.get(), MasterFriezaRenderer::new);
        event.registerEntityRenderer(ModEntities.MASTER_TENSHINHAN.get(), MasterTenshinhanRenderer::new);
        event.registerEntityRenderer(ModEntities.MASTER_HIT.get(), MasterHitRenderer::new);
    }
}