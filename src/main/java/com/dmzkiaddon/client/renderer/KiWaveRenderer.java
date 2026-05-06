package com.dmzkiaddon.client.renderer;

import com.dmzkiaddon.client.model.KiBeamModel;
import com.dmzkiaddon.client.model.KiBallModel;
import com.dmzkiaddon.client.render.KiRenderTypes;
import com.dmzkiaddon.entity.KiWaveAddon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.Color;
import java.lang.reflect.Field;

public class KiWaveRenderer extends EntityRenderer<KiWaveAddon> {

    private static final ResourceLocation TEX_BEAM =
            new ResourceLocation("dmzkiaddon", "textures/entity/ki/wave_beam.png");
    private static final ResourceLocation TEX_CORE =
            new ResourceLocation("dmzkiaddon", "textures/entity/ki/wave_core.png");
    private static final ResourceLocation TEX_BALL =
            new ResourceLocation("dmzkiaddon", "textures/entity/ki/blast_core.png");

    private final KiBeamModel<KiWaveAddon> beamModel;
    private final KiBallModel<KiWaveAddon> ballModel;

    public KiWaveRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.beamModel = new KiBeamModel<>(ctx.bakeLayer(KiBeamModel.LAYER_LOCATION));
        this.ballModel = new KiBallModel<>(ctx.bakeLayer(KiBallModel.LAYER_LOCATION));
    }

    @Override
    public void render(KiWaveAddon entity, float entityYaw, float partialTick,
                       PoseStack ps, MultiBufferSource buffer, int packedLight) {

        float size = entity.getSize();
        float age  = entity.tickCount + partialTick;

        Color color = new Color(entity.getColor());
        float r = color.getRed()   / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue()  / 255f;
        float cr = Math.min(r + 0.5f, 1f), cg = Math.min(g + 0.5f, 1f), cb = Math.min(b + 0.5f, 1f);

        float yaw   = entity.getYRot();
        float pitch = entity.getXRot();

        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(-yaw));
        ps.mulPose(Axis.XP.rotationDegrees(pitch));

        beamModel.setupAnim(entity, 0, 0, age, 0, 0);

        // ── Aura del beam ────────────────────────────────────────────────────
        ps.pushPose();
        ps.scale(size, size, size * 2.0F);
        VertexConsumer beamAura = buffer.getBuffer(KiRenderTypes.kiAura(TEX_BEAM));
        beamModel.renderToBuffer(ps, beamAura, 15728880, OverlayTexture.NO_OVERLAY, r, g, b, 0.6F);
        ps.popPose();

        // ── Núcleo del beam ──────────────────────────────────────────────────
        ps.pushPose();
        ps.scale(size * 0.55F, size * 0.55F, size * 2.0F);
        VertexConsumer beamCore = buffer.getBuffer(KiRenderTypes.kiBeam(TEX_CORE));
        beamModel.renderToBuffer(ps, beamCore, 15728880, OverlayTexture.NO_OVERLAY, cr, cg, cb, 0.85F);
        ps.popPose();

        // ── Bola frontal del beam ────────────────────────────────────────────
        ps.pushPose();
        ps.translate(0.0D, 0.0D, size * 2.5D);
        ps.mulPose(Axis.XP.rotationDegrees(-pitch));
        ps.mulPose(Axis.YP.rotationDegrees(yaw));
        ps.mulPose(this.entityRenderDispatcher.cameraOrientation());
        ps.mulPose(Axis.YP.rotationDegrees(180.0F));
        float ballScale = size * 1.2F;
        ps.scale(ballScale, ballScale, ballScale);

        ballModel.setupAnim(entity, 0, 0, age, 0, 0);
        VertexConsumer ball = buffer.getBuffer(KiRenderTypes.kiAura(TEX_BALL));
        ballModel.renderToBuffer(ps, ball, 15728880, OverlayTexture.NO_OVERLAY, r, g, b, 0.7F);
        ps.popPose();

        ps.popPose();

        super.render(entity, entityYaw, partialTick, ps, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(KiWaveAddon entity) {
        return TEX_BEAM;
    }
}
