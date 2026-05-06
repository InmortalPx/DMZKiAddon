package com.dmzkiaddon.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import static com.dmzkiaddon.DMZKiAddon.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class FinalExplosionRingRenderer {

    private static final ResourceLocation RING_TEX =
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/entity/final_explosion_ring.png");

    private static final int TOTAL_FRAMES = 47;
    private static final int PHASE_1_END = 60;
    private static final int PHASE_2_END = 80;
    private static final int NUM_PLANES = 12;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!ScreenEffects.isFinalExplosionCharging()) return;

        int tick = ScreenEffects.getFinalExplosionChargeTick();
        float progress = Math.min(1.0f, tick / 80.0f);

        float alpha, radius, spriteH;
        if (tick <= PHASE_1_END) {
            alpha = 0.4f + progress * 0.5f;
            radius = 1.2f + progress * 0.8f;
            spriteH = 1.2f + progress * 0.6f;
        } else {
            float t = (tick - PHASE_1_END) / (float)(PHASE_2_END - PHASE_1_END);
            alpha = 0.9f + t * 0.1f;
            radius = 2.0f + t * 1.0f;
            spriteH = 1.8f + t * 0.8f;
        }

        if (tick >= PHASE_2_END - 3 && tick < PHASE_2_END) {
            float shrink = 1.0f - ((tick - (PHASE_2_END - 3)) / 3.0f) * 0.5f;
            radius *= shrink;
            spriteH *= shrink;
        }

        long ms = System.currentTimeMillis();
        int frame = (int)((ms / 50) % TOTAL_FRAMES);
        float frameH = 1.0f / TOTAL_FRAMES;
        float vMin = frame * frameH;
        float vMax = vMin + frameH;

        Player player = mc.player;
        float partial = event.getPartialTick();
        double px = player.getX(partial);
        double py = player.getY(partial);
        double pz = player.getZ(partial);

        Vec3 cam = event.getCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, RING_TEX);

        int r = 255, g = 220, b = 50;
        int a = (int)(alpha * 255);

        for (int i = 0; i < NUM_PLANES; i++) {
            double angle = (Math.PI * 2.0 / NUM_PLANES) * i + (ms * 0.001);
            double cx = px + Math.cos(angle) * radius;
            double cz = pz + Math.sin(angle) * radius;

            PoseStack ps = event.getPoseStack();
            double arcWidth = (2.0 * Math.PI * radius / NUM_PLANES) * 1.6f;

            ps.pushPose();
            ps.translate(cx - cam.x, py - cam.y, cz - cam.z);

            float yawRad = (float)(angle + Math.PI / 2.0);
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees((float) Math.toDegrees(yawRad)));

            ps.scale((float)arcWidth, spriteH, 1.0f);

            BufferBuilder buf = Tesselator.getInstance().getBuilder();
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            Matrix4f mat = ps.last().pose();

            buf.vertex(mat, -0.5f, 0f, 0f).uv(0f, vMax).color(r, g, b, a).endVertex();
            buf.vertex(mat, 0.5f, 0f, 0f).uv(1f, vMax).color(r, g, b, a).endVertex();
            buf.vertex(mat, 0.5f, 1f, 0f).uv(1f, vMin).color(r, g, b, a).endVertex();
            buf.vertex(mat, -0.5f, 1f, 0f).uv(0f, vMin).color(r, g, b, a).endVertex();

            Tesselator.getInstance().end();
            ps.popPose();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}