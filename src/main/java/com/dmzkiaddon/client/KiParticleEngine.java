package com.dmzkiaddon.client;

import com.dmzkiaddon.entity.HellzoneGrenadeEntity;
import com.dmzkiaddon.entity.KiBlastAddon;
import com.dmzkiaddon.entity.KiLaserAddon;
import com.dmzkiaddon.entity.KiWaveAddon;
import com.dmzkiaddon.entity.MakankosappoEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder;
import team.lodestar.lodestone.systems.particle.data.GenericParticleData;
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData;
import team.lodestar.lodestone.systems.particle.render_types.LodestoneWorldParticleRenderType;
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry;

import java.awt.Color;
import java.util.List;

import static com.dmzkiaddon.DMZKiAddon.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class KiParticleEngine {

    private static final double PROCESS_DIST_SQ = 64.0 * 64.0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.isPaused()) return;

        Level level = mc.level;
        AABB range  = mc.player.getBoundingBox().inflate(64);

        level.getEntitiesOfClass(KiBlastAddon.class, range, Entity::isAlive)
                .forEach(e -> spawnBlastParticles(level, e));

        level.getEntitiesOfClass(KiWaveAddon.class, range, Entity::isAlive)
                .forEach(e -> spawnWaveParticles(level, e));

        level.getEntitiesOfClass(KiLaserAddon.class, range, Entity::isAlive)
                .forEach(e -> spawnLaserParticles(level, e));

        level.getEntitiesOfClass(MakankosappoEntity.class, range, Entity::isAlive)
                .forEach(e -> spawnMakankosappoParticles(level, e));

        level.getEntitiesOfClass(HellzoneGrenadeEntity.class, range, Entity::isAlive)
                .forEach(e -> spawnHellzoneParticles(level, e));
    }

    private static void spawnBlastParticles(Level level, KiBlastAddon blast) {
        float size = blast.getSize();
        Color[] colors = extractColors(blast.getColor());
        Color core = colors[0];
        Color edge = colors[1];

        Vec3 pos = blast.position().add(0, blast.getBbHeight() * 0.5, 0);

        if (size >= 0.5f) {
            WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                    .setColorData(ColorParticleData.create(Color.WHITE, core).setCoefficient(1.4f).build())
                    .setScaleData(GenericParticleData.create(size * 0.18f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.85f, 0f).build())
                    .setLifetime(8)
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .spawn(level, pos.x, pos.y, pos.z);
        }

        if (size >= 1.0f) {
            int haloCount = Math.max(1, (int)(size * 0.6f));
            for (int i = 0; i < haloCount; i++) {
                double angle = (blast.tickCount * 0.3 + i * (Math.PI * 2.0 / haloCount));
                double rad   = size * 0.55;
                WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                        .setColorData(ColorParticleData.create(core, edge).setCoefficient(1.2f).build())
                        .setScaleData(GenericParticleData.create(size * 0.12f, 0f).build())
                        .setTransparencyData(GenericParticleData.create(0.7f, 0f).build())
                        .setLifetime(12)
                        .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                        .addMotion(0, size * 0.008f, 0)
                        .spawn(level,
                                pos.x + Math.cos(angle) * rad,
                                pos.y + (Math.random() - 0.5) * size * 0.4,
                                pos.z + Math.sin(angle) * rad);
            }
        }

        if (size >= 2.5f) {
            Vec3 trail = blast.getDeltaMovement().normalize().scale(-1.0);
            for (int t = 1; t <= 3; t++) {
                Vec3 tp = pos.add(trail.scale(t * size * 0.4));
                float fs = size * 0.08f * (1f - t * 0.25f);
                if (fs <= 0) break;
                WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                        .setColorData(ColorParticleData.create(edge, dimColor(edge)).setCoefficient(0.8f).build())
                        .setScaleData(GenericParticleData.create(fs, 0f).build())
                        .setTransparencyData(GenericParticleData.create(0.6f, 0f).build())
                        .setLifetime(10)
                        .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                        .addMotion((Math.random() - 0.5) * 0.05, 0.02, (Math.random() - 0.5) * 0.05)
                        .spawn(level, tp.x, tp.y, tp.z);
            }
        }
    }

    private static void spawnWaveParticles(Level level, KiWaveAddon wave) {
        float size = wave.getSize();
        Color[] colors = extractColors(wave.getColor());
        Color core = colors[0];
        Color edge = colors[1];

        Vec3 pos = wave.position().add(0, wave.getBbHeight() * 0.5, 0);

        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, core).setCoefficient(1.8f).build())
                .setScaleData(GenericParticleData.create(size * 0.35f, 0f).build())
                .setTransparencyData(GenericParticleData.create(0.9f, 0f).build())
                .setLifetime(6)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, pos.x, pos.y, pos.z);

        int sparkCount = Math.max(2, (int)(size * 1.5f));
        for (int i = 0; i < sparkCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double rad   = size * (0.3 + Math.random() * 0.4);
            WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                    .setColorData(ColorParticleData.create(core, edge).setCoefficient(1.1f).build())
                    .setScaleData(GenericParticleData.create(size * 0.07f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.75f, 0f).build())
                    .setLifetime(8)
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .addMotion(
                            Math.cos(angle) * 0.04,
                            (Math.random() - 0.5) * 0.03,
                            Math.sin(angle) * 0.04)
                    .spawn(level,
                            pos.x + Math.cos(angle) * rad,
                            pos.y + (Math.random() - 0.5) * size * 0.3,
                            pos.z + Math.sin(angle) * rad);
        }
    }

    private static void spawnLaserParticles(Level level, KiLaserAddon laser) {
        float size = laser.getSize();
        Color[] colors = extractColors(laser.getColor());
        Color core = colors[0];
        Color edge = colors[1];

        Vec3 pos = laser.position().add(0, laser.getBbHeight() * 0.5, 0);

        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, core).setCoefficient(2.0f).build())
                .setScaleData(GenericParticleData.create(size * 0.25f, 0f).build())
                .setTransparencyData(GenericParticleData.create(0.95f, 0f).build())
                .setLifetime(5)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, pos.x, pos.y, pos.z);

        for (int i = 0; i < 2; i++) {
            double angle = Math.random() * Math.PI * 2;
            double rad   = size * 0.2;
            WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                    .setColorData(ColorParticleData.create(core, edge).setCoefficient(1.0f).build())
                    .setScaleData(GenericParticleData.create(0.04f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.8f, 0f).build())
                    .setLifetime(6)
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .addMotion(
                            Math.cos(angle) * 0.06,
                            (Math.random() - 0.5) * 0.04,
                            Math.sin(angle) * 0.06)
                    .spawn(level,
                            pos.x + Math.cos(angle) * rad,
                            pos.y,
                            pos.z + Math.sin(angle) * rad);
        }
    }

    private static void spawnMakankosappoParticles(Level level, MakankosappoEntity beam) {
        Vec3 pos = beam.position().add(0, beam.getBbHeight() * 0.5, 0);

        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, new Color(0.4f, 0.0f, 0.9f, 1f))
                        .setCoefficient(1.6f).build())
                .setScaleData(GenericParticleData.create(0.22f, 0f).build())
                .setTransparencyData(GenericParticleData.create(0.9f, 0f).build())
                .setLifetime(7)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, pos.x, pos.y, pos.z);

        double angle1 = Math.toRadians(beam.tickCount * 25.0);
        double angle2 = angle1 + Math.PI;
        double radius = 0.28;

        Vec3 vel = beam.getDeltaMovement();
        double spd = vel.length();
        if (spd > 1e-6) {
            Vec3 dir  = vel.scale(1.0 / spd);
            Vec3 up   = Math.abs(dir.y) < 0.99 ? new Vec3(0,1,0) : new Vec3(1,0,0);
            Vec3 p1   = cross(dir, up).normalize();
            Vec3 p2   = cross(dir, p1).normalize();

            for (double angle : new double[]{angle1, angle2}) {
                double ox = p1.x * Math.cos(angle) * radius + p2.x * Math.sin(angle) * radius;
                double oy = p1.y * Math.cos(angle) * radius + p2.y * Math.sin(angle) * radius;
                double oz = p1.z * Math.cos(angle) * radius + p2.z * Math.sin(angle) * radius;

                boolean isWhite = angle == angle1;
                Color c = isWhite ? Color.WHITE : new Color(0.55f, 0.0f, 1.0f, 1f);

                WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                        .setColorData(ColorParticleData.create(c, dimColor(c)).setCoefficient(1.0f).build())
                        .setScaleData(GenericParticleData.create(0.07f, 0f).build())
                        .setTransparencyData(GenericParticleData.create(0.85f, 0f).build())
                        .setLifetime(8)
                        .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                        .spawn(level, pos.x + ox, pos.y + oy, pos.z + oz);
            }
        }
    }

    private static void spawnHellzoneParticles(Level level, HellzoneGrenadeEntity grenade) {
        Vec3 pos = grenade.position().add(0, grenade.getBbHeight() * 0.5, 0);

        Color greenCore = new Color(0.3f, 1.0f, 0.3f, 1f);
        Color greenEdge = new Color(0.1f, 0.5f, 0.1f, 1f);

        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, greenCore).setCoefficient(1.5f).build())
                .setScaleData(GenericParticleData.create(0.15f, 0f).build())
                .setTransparencyData(GenericParticleData.create(0.9f, 0f).build())
                .setLifetime(6)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, pos.x, pos.y, pos.z);

        double angle = Math.random() * Math.PI * 2;
        WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                .setColorData(ColorParticleData.create(greenCore, greenEdge).setCoefficient(0.9f).build())
                .setScaleData(GenericParticleData.create(0.06f, 0f).build())
                .setTransparencyData(GenericParticleData.create(0.7f, 0f).build())
                .setLifetime(8)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .addMotion(Math.cos(angle) * 0.05, 0.03, Math.sin(angle) * 0.05)
                .spawn(level, pos.x, pos.y, pos.z);
    }

    public static void spawnHellzoneOrbit(Level level, Vec3 pos) {
        if (!level.isClientSide()) return;
        
        Color greenCore = new Color(0.3f, 1.0f, 0.3f, 1f);
        Color greenEdge = new Color(0.1f, 0.5f, 0.1f, 1f);
        
        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, greenCore).setCoefficient(1.5f).build())
                .setScaleData(GenericParticleData.create(0.2f, 0f).build())
                .setTransparencyData(GenericParticleData.create(0.9f, 0f).build())
                .setLifetime(10)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, pos.x, pos.y, pos.z);
    }

    public static void spawnKiTrail(Level level, Vec3 pos, Color color, float size) {
        if (!level.isClientSide()) return;
        
        Color core = new Color(
            Math.min(color.getRed() / 255f + 0.4f, 1f),
            Math.min(color.getGreen() / 255f + 0.4f, 1f),
            Math.min(color.getBlue() / 255f + 0.4f, 1f)
        );
        
        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(core, color).setCoefficient(1.2f).build())
                .setScaleData(GenericParticleData.create(size * 0.1f, 0f).build())
                .setTransparencyData(GenericParticleData.create(0.8f, 0f).build())
                .setLifetime(8)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .addMotion((Math.random() - 0.5) * 0.02, 0, (Math.random() - 0.5) * 0.02)
                .spawn(level, pos.x, pos.y, pos.z);
    }

    public static void spawnKiExplosion(Level level, Vec3 pos, Color color, float size) {
        if (!level.isClientSide()) return;
        
        Color core = new Color(
            Math.min(color.getRed() / 255f + 0.5f, 1f),
            Math.min(color.getGreen() / 255f + 0.5f, 1f),
            Math.min(color.getBlue() / 255f + 0.5f, 1f)
        );
        Color fade = new Color(
            color.getRed() / 255f * 0.15f,
            color.getGreen() / 255f * 0.15f,
            color.getBlue() / 255f * 0.15f
        );
        
        WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE.get())
                .setColorData(ColorParticleData.create(Color.WHITE, core).setCoefficient(2.0f).build())
                .setScaleData(GenericParticleData.create(size * 0.8f, 0f).build())
                .setTransparencyData(GenericParticleData.create(1.0f, 0f).build())
                .setLifetime(12)
                .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                .spawn(level, pos.x, pos.y, pos.z);

        int sparkCount = Math.max(8, (int)(size * 3f));
        for (int i = 0; i < sparkCount; i++) {
            double angle = Math.random() * Math.PI * 2;
            double elev  = (Math.random() - 0.5) * Math.PI;
            double speed = 0.1 + Math.random() * size * 0.08;
            WorldParticleBuilder.create(LodestoneParticleRegistry.SPARKLE_PARTICLE.get())
                    .setColorData(ColorParticleData.create(core, color).setCoefficient(1.3f).build())
                    .setScaleData(GenericParticleData.create(size * 0.06f + 0.05f, 0f).build())
                    .setTransparencyData(GenericParticleData.create(0.9f, 0f).build())
                    .setLifetime(16 + (int)(Math.random() * 10))
                    .setRenderType(LodestoneWorldParticleRenderType.ADDITIVE)
                    .addMotion(
                            Math.cos(angle) * Math.cos(elev) * speed,
                            Math.sin(elev) * speed,
                            Math.sin(angle) * Math.cos(elev) * speed)
                    .spawn(level, pos.x, pos.y, pos.z);
        }
    }

    private static Color[] extractColors(int colorInt) {
        float r = ((colorInt >> 16) & 0xFF) / 255f;
        float g = ((colorInt >> 8)  & 0xFF) / 255f;
        float b = (colorInt         & 0xFF) / 255f;
        Color core = new Color(Math.min(r + 0.4f, 1f), Math.min(g + 0.4f, 1f), Math.min(b + 0.4f, 1f));
        Color edge = new Color(r, g, b);
        return new Color[]{core, edge};
    }

    private static Color dimColor(Color c) {
        return new Color(c.getRed() / 255f * 0.2f, c.getGreen() / 255f * 0.2f, c.getBlue() / 255f * 0.2f, 1f);
    }

    private static Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x);
    }
}