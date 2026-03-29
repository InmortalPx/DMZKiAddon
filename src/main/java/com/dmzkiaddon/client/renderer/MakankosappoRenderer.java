package com.dmzkiaddon.client.renderer;

import com.dmzkiaddon.entity.MakankosappoEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class MakankosappoRenderer extends EntityRenderer<MakankosappoEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    private static final int SEGMENTS = 8;

    public MakankosappoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(MakankosappoEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(MakankosappoEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(MakankosappoEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {

        Vec3 vel = entity.getDeltaMovement();
        double speed = vel.length();
        if (speed < 1e-6) return;

        Vec3 dir     = vel.scale(1.0 / speed);
        Vec3 worldUp = Math.abs(dir.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 perp1   = cross(dir, worldUp).normalize();
        Vec3 perp2   = cross(dir, perp1).normalize();

        float coreRadius  = 0.08f;
        float outerRadius = 0.18f;
        float beamLength  = 1.8f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();

        Matrix4f mat = poseStack.last().pose();

        BufferBuilder buf = Tesselator.getInstance().getBuilder();

        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / SEGMENTS;
            float nx = (float)(perp1.x * Math.cos(angle) + perp2.x * Math.sin(angle));
            float ny = (float)(perp1.y * Math.cos(angle) + perp2.y * Math.sin(angle));
            float nz = (float)(perp1.z * Math.cos(angle) + perp2.z * Math.sin(angle));

            buf.vertex(mat,
                    nx * outerRadius,
                    ny * outerRadius,
                    nz * outerRadius)
                    .color(0.55f, 0.0f, 1.0f, 0.45f).endVertex();

            buf.vertex(mat,
                    nx * outerRadius + (float)dir.x * beamLength,
                    ny * outerRadius + (float)dir.y * beamLength,
                    nz * outerRadius + (float)dir.z * beamLength)
                    .color(0.55f, 0.0f, 1.0f, 0.45f).endVertex();
        }
        Tesselator.getInstance().end();

        buf.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = Math.PI * 2.0 * i / SEGMENTS;
            float nx = (float)(perp1.x * Math.cos(angle) + perp2.x * Math.sin(angle));
            float ny = (float)(perp1.y * Math.cos(angle) + perp2.y * Math.sin(angle));
            float nz = (float)(perp1.z * Math.cos(angle) + perp2.z * Math.sin(angle));

            buf.vertex(mat,
                    nx * coreRadius,
                    ny * coreRadius,
                    nz * coreRadius)
                    .color(0.9f, 0.8f, 1.0f, 0.95f).endVertex();

            buf.vertex(mat,
                    nx * coreRadius + (float)dir.x * beamLength,
                    ny * coreRadius + (float)dir.y * beamLength,
                    nz * coreRadius + (float)dir.z * beamLength)
                    .color(0.9f, 0.8f, 1.0f, 0.95f).endVertex();
        }
        Tesselator.getInstance().end();

        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x);
    }
}