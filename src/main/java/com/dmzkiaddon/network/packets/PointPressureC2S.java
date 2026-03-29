package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.config.AddonConfig;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * C2S — Vital Point Strike de Hit.
 *
 * El jugador ejecuta un dash rápido de 3–4 bloques hacia adelante.
 * Si hay una entidad viva en esa línea, recibe daño híbrido de punto vital:
 *   - 30% daño físico (playerAttack) — afectado por armadura
 *   - 70% daño mágico (magic)       — ignora armadura/RES completamente
 *
 * Daño base = SKP * 0.8 (scaled por damageScale en config)
 *
 * Debuffs de parálisis de sistema nervioso:
 *   - Slowness IV por 3 segundos (casi inmovilizado)
 *   - Weakness II por 3 segundos (ataca más débil)
 *
 * Partículas ENCHANTED_HIT en el pecho del objetivo.
 * Sonido PLAYER_ATTACK_CRIT.
 *
 * No requiere lock-on — alcanza cualquier entidad en la línea del dash.
 */
public class PointPressureC2S {

    /** Distancia del dash en bloques. */
    private static final double DASH_DISTANCE = 4.0;

    /** Radio lateral de detección de hit (hitbox del dash). */
    private static final double DASH_WIDTH = 1.2;

    public PointPressureC2S() {}

    public PointPressureC2S(FriendlyByteBuf buf) {}

    public void encode(FriendlyByteBuf buf) {}

    public static void handle(PointPressureC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {

                // ── 1. Verificar y consumir ENE ───────────────────────────────
                int maxEnergy     = stats.getMaxEnergy();
                int currentEnergy = stats.getResources().getCurrentEnergy();
                int cost          = (int)(maxEnergy * (AddonConfig.POINT_PRESSURE_KI_COST_PCT.get() / 100.0));
                if (currentEnergy < cost) return;
                stats.getResources().setCurrentEnergy(currentEnergy - cost);

                // ── 2. Calcular trayectoria del dash ──────────────────────────
                Vec3 look      = player.getLookAngle();
                Vec3 dashEnd   = player.position().add(look.scale(DASH_DISTANCE));

                // AABB que cubre la línea del dash
                AABB dashBox = new AABB(
                        Math.min(player.getX(), dashEnd.x) - DASH_WIDTH,
                        player.getY() - 0.5,
                        Math.min(player.getZ(), dashEnd.z) - DASH_WIDTH,
                        Math.max(player.getX(), dashEnd.x) + DASH_WIDTH,
                        player.getY() + 2.5,
                        Math.max(player.getZ(), dashEnd.z) + DASH_WIDTH
                );

                List<LivingEntity> targets = player.level()
                        .getEntitiesOfClass(LivingEntity.class, dashBox,
                                e -> !e.equals(player) && e.isAlive());

                // Ordenar por distancia al jugador — golpear al más cercano
                targets.sort(Comparator.comparingDouble(e -> e.distanceTo(player)));

                // ── 3. Dash del jugador ───────────────────────────────────────
                player.teleportTo(dashEnd.x, dashEnd.y, dashEnd.z);

                if (targets.isEmpty()) return;

                LivingEntity target = targets.get(0);

                // ── 4. Daño híbrido de punto vital ────────────────────────────
                float baseDamage = stats.getStats().getStrikePower() * 0.8f
                        * AddonConfig.POINT_PRESSURE_DAMAGE_SCALE.get().floatValue();

                // 30% físico — la armadura del enemigo lo mitiga parcialmente
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().playerAttack(player), baseDamage * 0.3f);

                // 70% mágico — ignora armadura y resistencia completamente
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().magic(), baseDamage * 0.7f);

                // ── 5. Parálisis de sistema nervioso ──────────────────────────
                // Slowness IV: casi inmovilizado durante 3 segundos
                target.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 60, 3, false, true));
                // Weakness II: ataca más débil durante 3 segundos
                target.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 60, 1, false, true));

                // ── 6. Partículas de impacto crítico en el pecho ──────────────
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                            target.getX(), target.getY() + 1.0, target.getZ(),
                            20, 0.25, 0.25, 0.25, 0.4);
                    // Segunda ráfaga en la cabeza — efecto de múltiples puntos vitales
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            target.getX(), target.getY() + 1.6, target.getZ(),
                            10, 0.15, 0.15, 0.15, 0.3);
                }

                // ── 7. Sonido ─────────────────────────────────────────────────
                player.level().playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT,
                        SoundSource.PLAYERS, 1.2f, 1.5f);
            });
        });
        ctx.setPacketHandled(true);
    }
}