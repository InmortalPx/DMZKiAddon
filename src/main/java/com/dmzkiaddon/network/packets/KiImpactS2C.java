package com.dmzkiaddon.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;

import java.awt.Color;
import java.util.function.Supplier;

public class KiImpactS2C {

    public enum ImpactType { BLAST, WAVE, LASER }

    private final double     x, y, z;
    private final int        colorInt;
    private final float      size;
    private final ImpactType impactType;

    public KiImpactS2C(double x, double y, double z, int colorInt, float size, ImpactType impactType) {
        this.x          = x;
        this.y          = y;
        this.z          = z;
        this.colorInt   = colorInt;
        this.size       = size;
        this.impactType = impactType;
    }

    public KiImpactS2C(FriendlyByteBuf buf) {
        this.x          = buf.readDouble();
        this.y          = buf.readDouble();
        this.z          = buf.readDouble();
        this.colorInt   = buf.readInt();
        this.size       = buf.readFloat();
        this.impactType = buf.readEnum(ImpactType.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(colorInt);
        buf.writeFloat(size);
        buf.writeEnum(impactType);
    }

    public static void handle(KiImpactS2C pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> spawnImpact(pkt))
        );
        ctx.get().setPacketHandled(true);
    }

    private static void spawnImpact(KiImpactS2C pkt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Level level = mc.level;

        float r = ((pkt.colorInt >> 16) & 0xFF) / 255f;
        float g = ((pkt.colorInt >> 8)  & 0xFF) / 255f;
        float b = (pkt.colorInt         & 0xFF) / 255f;

        Color coreColor = new Color(Math.min(r + 0.5f, 1f), Math.min(g + 0.5f, 1f), Math.min(b + 0.5f, 1f));
        Color edgeColor = new Color(r, g, b);
        Color fadeColor = new Color(r * 0.15f, g * 0.15f, b * 0.15f);

        float s = pkt.size;

        switch (pkt.impactType) {
            case BLAST  -> spawnBlastImpact(level, pkt, s, coreColor, edgeColor, fadeColor);
            case WAVE   -> spawnWaveImpact(level, pkt, s, coreColor, edgeColor, fadeColor);
            case LASER  -> spawnLaserImpact(level, pkt, s, coreColor, edgeColor);
        }
    }

    private static void spawnBlastImpact(Level level, KiImpactS2C p, float s,
                                          Color core, Color edge, Color fade) {
        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, core).setCoefficient(2.0f).build())
                .setScaleData(GenericParticleData.create(s * 0.8f, 0f).build())
                .setTransparencyData(GenericParticleData.create(1.0f, 0f).build())
                .setLifetime(12)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, p.x, p.y, p.z);

        int sparkCount = Math.max(8, (int)(s * 3f));
        for (int i = 0; i < sparkCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double elev  = (Math.random() - 0.5) * Math.PI;
            double speed = 0.1 + Math.random() * s * 0.08;
            WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                    .setColorData(ColorParticleData.create(core, edge).setCoefficient(1.3f).build())
                    .setScaleData(GenericParticleData.create(s * 0.06f + 0.05f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.9f, 0f).build())
                    .setLifetime(16 + (int)(Math.random() * 10))
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .addMotion(
                            Math.cos(angle) * Math.cos(elev) * speed,
                            Math.sin(elev) * speed,
                            Math.sin(angle) * Math.cos(elev) * speed)
                    .spawn(level, p.x, p.y, p.z);
        }

        int ringCount = Math.max(12, (int)(s * 4f));
        for (int i = 0; i < ringCount; i++) {
            double angle = (Math.PI * 2.0 / ringCount) * i;
            double rad   = s * 0.3;
            WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                    .setColorData(ColorParticleData.create(edge, fade).setCoefficient(0.9f).build())
                    .setScaleData(GenericParticleData.create(s * 0.09f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.8f, 0f).build())
                    .setLifetime(20)
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .addMotion(Math.cos(angle) * s * 0.06, 0.03, Math.sin(angle) * s * 0.06)
                    .spawn(level, p.x + Math.cos(angle) * rad, p.y, p.z + Math.sin(angle) * rad);
        }
    }

    private static void spawnWaveImpact(Level level, KiImpactS2C p, float s,
                                         Color core, Color edge, Color fade) {
        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, core).setCoefficient(2.5f).build())
                .setScaleData(GenericParticleData.create(s * 1.4f, 0f).build())
                .setTransparencyData(GenericParticleData.create(1.0f, 0f).build())
                .setLifetime(14)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, p.x, p.y, p.z);

        int sparkCount = Math.max(16, (int)(s * 5f));
        for (int i = 0; i < sparkCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double elev  = (Math.random() - 0.5) * Math.PI * 0.6;
            double speed = 0.15 + Math.random() * s * 0.12;
            WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                    .setColorData(ColorParticleData.create(core, edge).setCoefficient(1.5f).build())
                    .setScaleData(GenericParticleData.create(s * 0.09f + 0.06f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.95f, 0f).build())
                    .setLifetime(20 + (int)(Math.random() * 12))
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .addMotion(
                            Math.cos(angle) * Math.cos(elev) * speed,
                            Math.sin(elev) * speed + 0.05,
                            Math.sin(angle) * Math.cos(elev) * speed)
                    .spawn(level, p.x, p.y, p.z);
        }

        for (int ring = 0; ring < 2; ring++) {
            float ringRad = s * (0.4f + ring * 0.3f);
            int ringCount = 16 + ring * 8;
            for (int i = 0; i < ringCount; i++) {
                double angle = (Math.PI * 2.0 / ringCount) * i;
                WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                        .setColorData(ColorParticleData.create(edge, fade).setCoefficient(0.8f).build())
                        .setScaleData(GenericParticleData.create(s * 0.08f, 0f).build())
                        .setTransparencyData(GenericParticleData.create(0.75f, 0f).build())
                        .setLifetime(22 + ring * 6)
                        .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                        .addMotion(
                                Math.cos(angle) * s * (0.07f + ring * 0.02f),
                                0.02,
                                Math.sin(angle) * s * (0.07f + ring * 0.02f))
                        .spawn(level,
                                p.x + Math.cos(angle) * ringRad,
                                p.y,
                                p.z + Math.sin(angle) * ringRad);
            }
        }
    }

    private static void spawnLaserImpact(Level level, KiImpactS2C p, float s,
                                          Color core, Color edge) {
        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, core).setCoefficient(2.2f).build())
                .setScaleData(GenericParticleData.create(s * 0.6f, 0f).build())
                .setTransparencyData(GenericParticleData.create(1.0f, 0f).build())
                .setLifetime(8)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, p.x, p.y, p.z);

        for (int i = 0; i < 10; i++) {
            double angle = Math.random() * Math.PI * 2;
            double elev  = (Math.random() - 0.5) * Math.PI;
            double speed = 0.08 + Math.random() * 0.1;
            WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                    .setColorData(ColorParticleData.create(core, edge).setCoefficient(1.2f).build())
                    .setScaleData(GenericParticleData.create(0.05f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.85f, 0f).build())
                    .setLifetime(10)
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .addMotion(
                            Math.cos(angle) * Math.cos(elev) * speed,
                            Math.sin(elev) * speed,
                            Math.sin(angle) * Math.cos(elev) * speed)
                    .spawn(level, p.x, p.y, p.z);
        }
    }
}