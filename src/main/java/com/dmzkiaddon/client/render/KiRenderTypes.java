package com.dmzkiaddon.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class KiRenderTypes extends RenderType {

    public KiRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode,
                         int bufferSize, boolean affectsCrumbling, boolean sortOnUpload,
                         Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    // ── Núcleo brillante — usa el shader de ojos (full-bright, sin iluminación) ──
    private static final Function<ResourceLocation, RenderType> KI_CORE = Util.memoize(texture ->
            create("ki_core",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256, false, false,
                    CompositeState.builder()
                            .setShaderState(RENDERTYPE_EYES_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setOverlayState(NO_OVERLAY)
                            .createCompositeState(false)));

    // ── Aura exterior — shader de energy swirl (rota la textura automáticamente) ──
    private static final Function<ResourceLocation, RenderType> KI_AURA = Util.memoize(texture ->
            create("ki_aura",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256, false, false,
                    CompositeState.builder()
                            .setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setOverlayState(NO_OVERLAY)
                            .createCompositeState(false)));

    // ── Beam — translúcido aditivo ─────────────────────────────────────────────
    private static final Function<ResourceLocation, RenderType> KI_BEAM = Util.memoize(texture ->
            create("ki_beam",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256, false, false,
                    CompositeState.builder()
                            .setShaderState(RENDERTYPE_EYES_SHADER)
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setLightmapState(NO_LIGHTMAP)
                            .setOverlayState(NO_OVERLAY)
                            .createCompositeState(false)));

    public static RenderType kiCore(ResourceLocation texture) { return KI_CORE.apply(texture); }
    public static RenderType kiAura(ResourceLocation texture) { return KI_AURA.apply(texture); }
    public static RenderType kiBeam(ResourceLocation texture) { return KI_BEAM.apply(texture); }
}
