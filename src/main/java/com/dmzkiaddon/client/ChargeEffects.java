package com.dmzkiaddon.client;

import com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType;
import com.dmzkiaddon.registry.ModSounds;
import com.dragonminez.common.init.MainParticles;
import com.dragonminez.common.init.particles.AuraParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

/**
 * Efectos client-side de partículas y sonido durante la carga y disparo de Ki.
 *
 * v1.3 — Aura volumétrica con MainParticles.AURA (columna SSJ real),
 *         sonidos de carga diferenciados por tipo de ataque,
 *         burst de disparo mejorado.
 */
@OnlyIn(Dist.CLIENT)
public class ChargeEffects {

    // Intervalos de sonido de carga
    private static final int CBEAM_INTERVAL   = 14;  // beams (kiai secuencial)
    private static final int GENERIC_INTERVAL = 20;

    // ─────────────────────────────────────────────────────────────────────────
    //  API pública
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Llamado cada tick mientras el jugador carga un ataque.
     *
     * @param chargeTicks ticks actuales de carga
     * @param maxCharge   ticks máximos
     * @param colorR/G/B  color del ataque (0–1)
     * @param type        tipo de ataque (para sonidos de carga correctos)
     */
    public static void onChargeTick(int chargeTicks, int maxCharge,
                                    float colorR, float colorG, float colorB,
                                    AttackType type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player  = mc.player;
        float progress = maxCharge > 0 ? (float) chargeTicks / maxCharge : 0f;

        spawnAuraColumn(player, colorR, colorG, colorB, progress);
        playChargeSound(player, progress, chargeTicks, type);
    }

    /** Burst de partículas al soltar el ataque. */
    public static void onFireAttack(float r, float g, float b, float chargeLevel) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player  = mc.player;
        Level  level   = player.level();
        int burstCount = 12 + (int)(chargeLevel * 20);

        for (int i = 0; i < burstCount; i++) {
            double speed = 0.1 + Math.random() * 0.35;
            double angle = Math.random() * Math.PI * 2;
            double velX  = Math.cos(angle) * speed;
            double velZ  = Math.sin(angle) * speed;
            double velY  = 0.05 + Math.random() * 0.45;

            level.addParticle(
                    new DustParticleOptions(new Vector3f(r, g, b), 1.4f),
                    player.getX(), player.getY() + player.getEyeHeight(), player.getZ(),
                    velX, velY, velZ);
        }

        // Flash final del AURA al disparar
        spawnAuraColumn(player, r, g, b, 1.0f);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Partículas
    // ─────────────────────────────────────────────────────────────────────────

    private static void spawnHandParticles(Player player, float progress,
                                           float r, float g, float b) {
        Level level    = player.level();
        DustParticleOptions dust  = new DustParticleOptions(new Vector3f(r, g, b), 0.8f + progress);
        DustParticleOptions white = new DustParticleOptions(new Vector3f(1f, 1f, 1f), 0.5f);

        Vec3 rHand = getHandPosition(player, true);
        Vec3 lHand = getHandPosition(player, false);

        int count = 2 + (int)(progress * 5);
        for (int i = 0; i < count; i++) {
            double ox = (Math.random() - 0.5) * 0.35;
            double oy = (Math.random() - 0.5) * 0.35;
            double oz = (Math.random() - 0.5) * 0.35;

            DustParticleOptions chosen = Math.random() < 0.65 ? dust : white;
            level.addParticle(chosen, rHand.x + ox, rHand.y + oy, rHand.z + oz, 0, 0.02, 0);
            level.addParticle(chosen, lHand.x + ox, lHand.y + oy, lHand.z + oz, 0, 0.02, 0);
        }
    }

    /**
     * Columna de aura volumétrica usando MainParticles.AURA (el mismo del SSJ).
     *
     * Composición visual:
     *  1. Instancias de AuraParticle distribuidas verticalmente con
     *     velocidad ascendente → efecto de energía que sube.
     *  2. DustParticle del color del ataque sobre el AURA → tinte cromático.
     *  3. Anillo de base en el suelo cuando la carga supera 30 %.
     */
    private static void spawnAuraColumn(Player player,
                                        float r, float g, float b, float progress) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // 1 ── AURA partículas volumétricas — máximo 3 por tick
        int auraCount = 1 + (int)(progress * 2); // 1-3
        for (int i = 0; i < auraCount; i++) {
            double yOff = -0.1 + Math.random() * 2.5;
            double xOff = (Math.random() - 0.5) * 0.4 * progress;
            double zOff = (Math.random() - 0.5) * 0.4 * progress;

            Particle auraP = mc.particleEngine.createParticle(
                    (net.minecraft.core.particles.ParticleOptions) MainParticles.AURA.get(),
                    player.getX() + xOff, player.getY() + yOff, player.getZ() + zOff,
                    (Math.random() - 0.5) * 0.01, 0.04 + Math.random() * 0.05, (Math.random() - 0.5) * 0.01);

            if (auraP instanceof AuraParticle ap) {
                ap.resize(0.3f + progress * 1.4f);
            }
        }

        // 2 ── Dust de color — solo 2 por tick máximo
        if (Math.random() < 0.5f) {
            double spread = 0.3 + progress * 0.4;
            mc.level.addParticle(
                    new DustParticleOptions(new Vector3f(r, g, b), 0.6f + progress * 0.8f),
                    player.getX() + (Math.random() - 0.5) * spread,
                    player.getY() + Math.random() * 2.2,
                    player.getZ() + (Math.random() - 0.5) * spread,
                    0, 0.04 + Math.random() * 0.06, 0);
        }

        // 3 ── Anillo de base solo si progress > 50%, cada 2 ticks
        if (progress > 0.5f && player.level().getGameTime() % 2 == 0) {
            int ringCount = 4;
            long time = player.level().getGameTime();
            for (int i = 0; i < ringCount; i++) {
                double angle  = (Math.PI * 2.0 / ringCount) * i + time * 0.09;
                double radius = 0.6 + progress * 0.7;
                mc.level.addParticle(
                        new DustParticleOptions(new Vector3f(r, g, b), 0.5f),
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + 0.05,
                        player.getZ() + Math.sin(angle) * radius,
                        -Math.sin(angle) * 0.04, 0.01, Math.cos(angle) * 0.04);
            }
        }
    }

    /** Orbes orbitales alrededor del jugador — máximo 2 por tick. */
    private static void spawnEnergyOrbs(Player player, float r, float g, float b, float progress) {
        if (progress < 0.4f) return; // solo aparecen cuando ya hay buena carga
        Level level = player.level();
        int orbCount = 2;
        long time    = level.getGameTime();

        for (int i = 0; i < orbCount; i++) {
            float angle  = (float) Math.toRadians((time * (8 + progress * 6) + i * 180.0) % 360);
            float radius = 0.7f + progress * 0.4f;
            double ox    = Math.cos(angle) * radius;
            double oz    = Math.sin(angle) * radius;
            double oy    = Math.sin(time * 0.07 + i) * 0.4 + 1.0;

            level.addParticle(
                    new DustParticleOptions(new Vector3f(r, g, b), 0.6f + progress * 0.5f),
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    -Math.sin(angle) * 0.06, 0, Math.cos(angle) * 0.06);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Sonidos de carga por tipo de ataque
    // ─────────────────────────────────────────────────────────────────────────

    private static void playChargeSound(Player player, float progress,
                                        int chargeTicks, AttackType type) {
        int interval = switch (type) {
            case KAMEHAMEHA, MASENKO, FINAL_KAMEHAMEHA,
                 KI_LASER, DODOMPA, MAKANKOSAPPO,
                 BIG_BANG, SPIRIT_BOMB               -> CBEAM_INTERVAL;
            case GALICK_GUN, FINAL_FLASH             -> CBEAM_INTERVAL;
            case KI_DISC                             -> 18;
            case DEATH_BALL                          -> 22;
            default                                  -> GENERIC_INTERVAL;
        };

        if (chargeTicks % interval != 0) return;

        float volume = 0.35f + progress * 0.55f;
        float pitch  = 0.85f + progress * 0.35f;

        SoundEvent sound = switch (type) {
            // Kamehameha-style: CBEAM kiai secuencial
            case KAMEHAMEHA, MASENKO, MAKANKOSAPPO,
                 KI_LASER, DODOMPA, FINAL_KAMEHAMEHA,
                 BIG_BANG, SPIRIT_BOMB                ->
                    getCbeam((chargeTicks / interval) % 7);

            // Galick Gun: FBEAM (Vegeta kiai)
            case GALICK_GUN ->
                    getFbeam((chargeTicks / interval) % 5);

            // Final Flash: primer tick -> charge, luego FBEAM
            case FINAL_FLASH -> {
                if (chargeTicks < interval) yield ModSounds.FINALFLASH_CHARGE.get();
                yield getFbeam((chargeTicks / interval) % 5);
            }

            // Death Ball: sonido de carga rugiente
            case DEATH_BALL -> ModSounds.DEATHBALL_CHARGE.get();

            // Kienzan: voces de disco alternadas
            case KI_DISC -> ((chargeTicks / interval) % 2 == 0)
                    ? ModSounds.CDISK_1.get()
                    : ModSounds.CDISK_2.get();

            // Ataques no cargables / especiales: sin sonido de carga
            default -> null;
        };

        if (sound != null) {
            player.level().playSound(
                    null,   // null = incluye al propio jugador
                    player.getX(), player.getY(), player.getZ(),
                    sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Aura de Vital Point Strike (Point Pressure)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Llama cada tick mientras {@link ScreenEffects#isPointPressureAuraActive()} sea true.
     * Genera:
     *  - Columna de aura morada intensa alrededor del jugador (MainParticles.AURA)
     *  - DustParticles morados en un anillo apretado (el aura visible)
     *  - Estela de DustParticles que se quedan atrás en la posición anterior del jugador
     *
     * @param player     el jugador local
     * @param auraProgress  0.0 al principio (ticks altos) → 1.0 al final (ticks bajos)
     */
    public static void spawnPointPressureAura(Player player, float auraProgress) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Color morado del aura de Hit
        final float r = 0.55f, g = 0.0f, b = 1.0f;
        final float rCore = 0.75f, gCore = 0.2f, bCore = 1.0f; // morado más brillante para el núcleo

        // ── 1. Columna de AuraParticle (hit-style, concentrada) ───────────
        int auraCount = 4 + (int)(auraProgress * 4);
        for (int i = 0; i < auraCount; i++) {
            double yOff  = Math.random() * 2.2;
            double xOff  = (Math.random() - 0.5) * 0.35;
            double zOff  = (Math.random() - 0.5) * 0.35;
            double velY  = 0.06 + Math.random() * 0.09;

            Particle auraP = mc.particleEngine.createParticle(
                    (net.minecraft.core.particles.ParticleOptions) MainParticles.AURA.get(),
                    player.getX() + xOff,
                    player.getY() + yOff,
                    player.getZ() + zOff,
                    (Math.random() - 0.5) * 0.01, velY, (Math.random() - 0.5) * 0.01);

            if (auraP instanceof AuraParticle ap) {
                ap.resize(0.25f + auraProgress * 0.9f); // más pequeño que el SSJ — Hit es preciso
            }
        }

        // ── 2. Anillo morado apretado (el aura de Ki visible) ────────────
        int ringCount = 8;
        long time = player.level().getGameTime();
        for (int i = 0; i < ringCount; i++) {
            double angle  = (Math.PI * 2.0 / ringCount) * i + time * 0.18; // gira rápido
            double radius = 0.4 + auraProgress * 0.25;
            double px     = player.getX() + Math.cos(angle) * radius;
            double pz     = player.getZ() + Math.sin(angle) * radius;
            double velX   = -Math.sin(angle) * 0.06;
            double velZ   =  Math.cos(angle) * 0.06;

            mc.level.addParticle(
                    new DustParticleOptions(new Vector3f(rCore, gCore, bCore), 0.6f + auraProgress * 0.5f),
                    px, player.getY() + 0.1 + Math.random() * 1.8, pz,
                    velX, 0.02, velZ);
        }

        // ── 3. Estela — partículas en la posición actual que se quedan atrás ─
        int trailCount = 5 + (int)(auraProgress * 6);
        for (int i = 0; i < trailCount; i++) {
            double ox = (Math.random() - 0.5) * 0.5;
            double oy = Math.random() * 2.0;
            double oz = (Math.random() - 0.5) * 0.5;

            // Velocidad opuesta a la dirección de vista → la estela queda atrás
            Vec3 back = player.getLookAngle().scale(-0.08 - Math.random() * 0.06);

            mc.level.addParticle(
                    new DustParticleOptions(new Vector3f(r, g, b), 0.9f + auraProgress * 0.6f),
                    player.getX() + ox,
                    player.getY() + oy,
                    player.getZ() + oz,
                    back.x + (Math.random() - 0.5) * 0.02,
                    -0.01,
                    back.z + (Math.random() - 0.5) * 0.02);
        }
    }

    private static SoundEvent getCbeam(int idx) {
        return switch (idx) {
            case 0 -> ModSounds.CBEAM_1.get();
            case 1 -> ModSounds.CBEAM_2.get();
            case 2 -> ModSounds.CBEAM_3.get();
            case 3 -> ModSounds.CBEAM_4.get();
            case 4 -> ModSounds.CBEAM_5.get();
            case 5 -> ModSounds.CBEAM_6.get();
            default -> ModSounds.CBEAM_7.get();
        };
    }

    private static SoundEvent getFbeam(int idx) {
        return switch (idx) {
            case 0 -> ModSounds.FBEAM_1.get();
            case 1 -> ModSounds.FBEAM_2.get();
            case 2 -> ModSounds.FBEAM_3.get();
            case 3 -> ModSounds.FBEAM_4.get();
            default -> ModSounds.FBEAM_5.get();
        };
    }

    private static Vec3 getHandPosition(Player player, boolean rightHand) {
        Vec3 look  = player.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        double side = rightHand ? 0.4 : -0.4;
        return player.getEyePosition().add(
                look.x * 0.5 + right.x * side,
                -0.2,
                look.z * 0.5 + right.z * side);
    }
}