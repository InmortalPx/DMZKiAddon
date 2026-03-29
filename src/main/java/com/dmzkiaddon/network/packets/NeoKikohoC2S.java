package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.config.AddonConfig;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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

public class NeoKikohoC2S {

    public NeoKikohoC2S() {}
    public NeoKikohoC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(NeoKikohoC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {

                CompoundTag nbt      = player.getPersistentData();
                int combo            = nbt.getInt("NeoKikohoCombo");

                int maxEnergy        = stats.getMaxEnergy();
                int currentEnergy    = stats.getResources().getCurrentEnergy();
                int kiCost           = (int)(maxEnergy * (AddonConfig.NEO_KIKOHO_KI_COST_PCT.get() / 100.0));

                float baseHpCost     = player.getMaxHealth() * 0.05f;
                float actualHpCost   = baseHpCost * (1.0f + (combo * 0.5f));

                if (currentEnergy < kiCost) return;

                if (player.getHealth() <= actualHpCost + 1.0f) {
                    player.setHealth(1.0f);
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,        200, 4, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER,           100, 1, false, true));
                    nbt.putInt("NeoKikohoCombo", 0);
                    return;
                }

                stats.getResources().setCurrentEnergy(currentEnergy - kiCost);
                player.hurt(player.damageSources().magic(), actualHpCost);

                float str    = stats.getStats().getStrength();
                float pwr    = (float) stats.getKiDamage();
                float scale  = AddonConfig.NEO_KIKOHO_DAMAGE_SCALE.get().floatValue();
                float damage = (str * 0.6f + pwr * 0.4f) * scale * 0.7f;

                Vec3 look  = player.getLookAngle();
                Vec3 start = player.getEyePosition();
                float reach = 25.0f;
                float half  = 1.8f;

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
                    Vec3 push = look.scale(2.0).add(0, -1.0, 0);
                    victim.setDeltaMovement(push);
                    victim.hurtMarked = true;
                }

                for (int i = 0; i < 50; i++) {
                    double t   = i / 50.0;
                    Vec3 pos   = start.add(look.scale(t * reach));
                    double spr = 0.4 + combo * 0.1;
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x, pos.y, pos.z, 1, spr, spr, spr, 0.3);
                }

                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        end.x, end.y, end.z, 3, 1.0, 1.0, 1.0, 0.1);

                float pitch = Math.min(2.0f, 1.2f + combo * 0.1f);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.5f, pitch);

                nbt.putInt("NeoKikohoCombo", combo + 1);
            });
        });
        ctx.setPacketHandled(true);
    }
}
