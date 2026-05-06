package com.dmzkiaddon.client.renderer;

import com.dmzkiaddon.client.model.KiBallModel;
import com.dmzkiaddon.client.render.KiRenderTypes;
import com.dmzkiaddon.entity.KiBlastAddon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.awt.Color;

public class KiBlastRenderer extends EntityRenderer<KiBlastAddon> {

    private static final ResourceLocation TEX_AURA =
            new ResourceLocation("dmzkiaddon", "textures/entity/ki/blast_aura.png");
    private static final ResourceLocation TEX_CORE =
            new ResourceLocation("dmzkiaddon", "textures/entity/ki/blast_core.png");

    private final KiBallModel<KiBlastAddon> model;

    public KiBlastRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new KiBallModel<>(ctx.bakeLayer(KiBallModel.LAYER_LOCATION));
    }

    @Override
    public void render(KiBlastAddon entity, float entityYaw, float partialTick,
                       PoseStack ps, MultiBufferSource buffer, int packedLight) {

        ps.pushPose();

        // Billboard — siempre mira a la cámara
        ps.mulPose(this.entityRenderDispatcher.cameraOrientation());
        ps.mulPose(Axis.YP.rotationDegrees(180.0F));

        float size  = entity.getSize();
        float age   = entity.tickCount + partialTick;
        float pulse = 1.0F + (float) Math.sin(age * 0.25F) * 0.06F;
        ps.scale(size * pulse, size * pulse, size * pulse);

        // Extraer RGB del color del ataque
        Color color = new Color(entity.getColor());
        float r = color.getRed()   / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue()  / 255f;

        model.setupAnim(entity, 0, 0, age, 0, 0);

        // Capa 1 — aura exterior (color del ataque, semitransparente)
        ps.pushPose();
        ps.scale(1.35F, 1.35F, 1.35F);
        VertexConsumer aura = buffer.getBuffer(KiRenderTypes.kiAura(TEX_AURA));
        model.renderToBuffer(ps, aura, 15728880, OverlayTexture.NO_OVERLAY, r, g, b, 0.55F);
        ps.popPose();

        // Capa 2 — núcleo (blanco brillante, más pequeño)
        ps.pushPose();
        ps.scale(0.7F, 0.7F, 0.7F);
        float cr = Math.min(r + 0.5f, 1f), cg = Math.min(g + 0.5f, 1f), cb = Math.min(b + 0.5f, 1f);
        VertexConsumer core = buffer.getBuffer(KiRenderTypes.kiCore(TEX_CORE));
        model.renderToBuffer(ps, core, 15728880, OverlayTexture.NO_OVERLAY, cr, cg, cb, 0.95F);
        ps.popPose();

        ps.popPose();

        super.render(entity, entityYaw, partialTick, ps, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(KiBlastAddon entity) {
        return TEX_CORE;
    }
}
