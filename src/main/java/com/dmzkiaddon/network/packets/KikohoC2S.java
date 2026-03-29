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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KikohoC2S {

    public KikohoC2S() {}
    public KikohoC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(KikohoC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {

                int maxEnergy    = stats.getMaxEnergy();
                int currentEnergy = stats.getResources().getCurrentEnergy();
                int kiCost        = (int)(maxEnergy * (AddonConfig.KIKOHO_KI_COST_PCT.get() / 100.0));
                float hpCost      = player.getMaxHealth() * AddonConfig.KIKOHO_HP_COST_PCT.get().floatValue() / 100.0f;

                if (currentEnergy < kiCost) return;
                if (player.getHealth() <= hpCost + 1.0f) return;

                stats.getResources().setCurrentEnergy(currentEnergy - kiCost);
                player.hurt(player.damageSources().magic(), hpCost);

                float str      = stats.getStats().getStrength();
                float pwr      = (float) stats.getKiDamage();
                float scale    = AddonConfig.KIKOHO_DAMAGE_SCALE.get().floatValue();
                float damage   = (str * 0.6f + pwr * 0.4f) * scale;

                Vec3 look  = player.getLookAngle();
                Vec3 start = player.getEyePosition();
                float reach = 30.0f;
                float half  = 2.5f;

                java.util.Set<LivingEntity> hitSet = new java.util.HashSet<>();
                for (int step = 0; step < (int) reach; step++) {
                    Vec3 center = start.add(look.scale(step + 0.5));
                    AABB slice  = new AABB(
                            center.x - half, center.y - half, center.z - half,
                            center.x + half, center.y + half, center.z + half);
                    serverLevel.getEntitiesOfClass(LivingEntity.class, slice,
                            e -> !e.equals(player) && e.isAlive()).forEach(hitSet::add);
                }

                DamageSource src = player.damageSources().magic();
                Vec3 end = start.add(look.scale(reach));

                for (LivingEntity victim : hitSet) {
                    victim.invulnerableTime = 0;
                    victim.hurt(src, damage);
                    Vec3 push = look.scale(2.8).add(0, -1.5, 0);
                    victim.setDeltaMovement(push);
                    victim.hurtMarked = true;
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, false));
                }

                for (int i = 0; i < 80; i++) {
                    double t  = i / 80.0;
                    Vec3 pos  = start.add(look.scale(t * reach));
                    double ox = (Math.random() - 0.5) * 5.0;
                    double oy = (Math.random() - 0.5) * 5.0;
                    serverLevel.sendParticles(ParticleTypes.FLASH,
                            pos.x, pos.y, pos.z, 1, ox * 0.1, oy * 0.1, ox * 0.1, 0.01);
                }

                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        end.x, end.y, end.z, 4, 1.5, 1.5, 1.5, 0.2);

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        start.x, start.y + 1, start.z, 60, 0.3, 0.3, 0.3, 1.2);

                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 4.0f, 0.6f);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0f, 1.4f);
            });
        });
        ctx.setPacketHandled(true);
    }
}
