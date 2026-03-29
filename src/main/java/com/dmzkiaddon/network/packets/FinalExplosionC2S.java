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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * C2S — Final Explosion de Vegeta.
 *
 * Mecánica:
 *  - Daño = (HP actual × 2) + (VIT × 5), ignorando armadura y resistencias (daño mágico).
 *  - Radio de explosión configurable (default 15 bloques).
 *  - Destrucción de bloques si AddonConfig.FINAL_EXPLOSION_GRIEFING está activo.
 *  - El caster queda a 0.5 HP con debuffs extremos (Weakness V + Wither III por 10s).
 *    No se mata directamente para compatibilidad con tótems y habilidades de supervivencia DMZ.
 */
public class FinalExplosionC2S {

    public FinalExplosionC2S() {}
    public FinalExplosionC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(FinalExplosionC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {

                if (!(player.level() instanceof ServerLevel serverLevel)) return;

                // ── 0. Descartar la bola visual (ruptura visual) ──────────────
                int orbId = player.getPersistentData().getInt("ActiveFinalExplosionOrb");
                if (orbId != 0) {
                    net.minecraft.world.entity.Entity orbEntity = serverLevel.getEntity(orbId);
                    if (orbEntity != null) orbEntity.discard();
                    player.getPersistentData().remove("ActiveFinalExplosionOrb");
                }
                float currentHp = player.getHealth();
                int vit         = stats.getStats().getVitality();

                float nukeDamage = (currentHp * 2.0f) + (vit * 5.0f);
                float radius     = AddonConfig.FINAL_EXPLOSION_RADIUS.get().floatValue();

                // ── 2. Partículas masivas antes de que todo vuele ─────────────

                // Flash blanco cegador
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        60, radius * 0.4, radius * 0.4, radius * 0.4, 0.05);

                // Ráfaga de chispas eléctricas (Vegeta SSJ2 aura)
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        200, radius * 0.3, radius * 0.3, radius * 0.3, 0.8);

                // Nube de explosión
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        player.getX(), player.getY(), player.getZ(),
                        8, radius * 0.2, radius * 0.2, radius * 0.2, 0.3);

                // Fuego y humo en el epicentro
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        80, radius * 0.15, radius * 0.15, radius * 0.15, 0.2);

                // ── 3. Sonido — explosión ensordecedora ───────────────────────
                serverLevel.playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EXPLODE,
                        SoundSource.PLAYERS, 8.0f, 0.4f); // volumen máximo, pitch grave

                // Segundo sonido con delay visual (echo)
                serverLevel.playSound(null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.PLAYERS, 6.0f, 0.6f);

                // ── 4. Daño AoE — atraviesa todo ─────────────────────────────
                AABB blastZone = player.getBoundingBox().inflate(radius);
                List<LivingEntity> victims = serverLevel.getEntitiesOfClass(
                        LivingEntity.class, blastZone,
                        e -> !e.equals(player) && e.isAlive());

                for (LivingEntity victim : victims) {
                    // Daño mágico — ignora armadura, resistencia y Ki Shield
                    victim.invulnerableTime = 0;
                    victim.hurt(player.damageSources().magic(), nukeDamage);

                    // Knockback físico real: más fuerte en el centro, decae con la distancia
                    double dist     = victim.distanceTo(player);
                    double strength = (1.0 - (dist / radius)) * 3.5;
                    net.minecraft.world.phys.Vec3 push = victim.position()
                            .subtract(player.position()).normalize().scale(strength);
                    victim.setDeltaMovement(push.x, push.y + 0.5, push.z); // +0.5 → vuelan hacia arriba
                    victim.hurtMarked = true; // fuerza sync del movimiento al cliente
                }

                // ── 5. Destrucción de bloques (respeta el gamerule mobGriefing) ──
                if (com.dragonminez.common.init.MainGameRules.canKiGrief(
                        serverLevel, player.blockPosition(), player)) {
                    serverLevel.explode(player,
                            player.getX(), player.getY(), player.getZ(),
                            radius * 0.5f,
                            Level.ExplosionInteraction.TNT);
                }

                // ── 6. El sacrificio — Vegeta al borde de la muerte ──────────
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        6, 2.0, 2.0, 2.0, 0.2);

                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                player.setHealth(0.5f);

                serverLevel.getServer().tell(new net.minecraft.server.TickTask(
                        serverLevel.getServer().getTickCount() + 25, () -> {
                    if (player.isAlive()) {
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,          200, 4, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.WITHER,            200, 2, false, true));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3, false, true));
                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ASH,
                                player.getX(), player.getY() + 1.0, player.getZ(),
                                120, 0.4, 0.8, 0.4, 0.05);
                    }
                }));
            });
        });
        ctx.setPacketHandled(true);
    }
}