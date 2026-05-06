package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.config.AddonConfig;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class NeoKikohoC2S {

    private static final String COMBO_KEY = "NeoKikohoCombo";
    private static final String LAST_USE_KEY = "NeoKikohoLastUse";
    private static final String LAST_COMBO_TIME_KEY = "NeoKikohoLastComboTime";
    private static final long COOLDOWN_MS = 500L;
    private static final long COMBO_WINDOW_MS = 8000L;
    private static final int MAX_COMBO = 10;
    private static final float COLLAPSE_THRESHOLD = 2.0f;

    public NeoKikohoC2S() {}
    public NeoKikohoC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(NeoKikohoC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (player.isDeadOrDying()) return;
            if (player.isSpectator()) return;
            if (!(player.level() instanceof ServerLevel serverLevel)) return;

            CompoundTag nbt = player.getPersistentData();
            long now = System.currentTimeMillis();
            long lastUse = nbt.getLong(LAST_USE_KEY);

            if (now - lastUse < COOLDOWN_MS) {
                return;
            }

            nbt.putLong(LAST_USE_KEY, now);

            long lastComboTime = nbt.getLong(LAST_COMBO_TIME_KEY);
            int combo = nbt.getInt(COMBO_KEY);

            if (now - lastComboTime > COMBO_WINDOW_MS) {
                combo = 0;
            }

            final int finalCombo = combo;

            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {
                int maxEnergy = stats.getMaxEnergy();
                int currentEnergy = stats.getResources().getCurrentEnergy();
                int kiCost = (int)(maxEnergy * (AddonConfig.NEO_KIKOHO_KI_COST_PCT.get() / 100.0));

                if (currentEnergy < kiCost) {
                    player.displayClientMessage(Component.translatable("message.dmzkiaddon.neo_kikoho.no_energy"), true);
                    return;
                }

                float baseHpCost = player.getMaxHealth() * 0.05f;
                float actualHpCost = baseHpCost * (1.0f + (finalCombo * 0.5f));

                if (player.getHealth() <= actualHpCost + COLLAPSE_THRESHOLD) {
                    player.setHealth(1.0f);
                    player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4, false, true));
                    player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1, false, true));
                    nbt.putInt(COMBO_KEY, 0);
                    nbt.putLong(LAST_COMBO_TIME_KEY, 0);
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.8f);
                    player.displayClientMessage(Component.translatable("message.dmzkiaddon.neo_kikoho.collapse"), true);
                    return;
                }

                stats.getResources().setCurrentEnergy(currentEnergy - kiCost);
                KikohoC2S.applyDirectHp(player, actualHpCost);

                float str = stats.getStats().getStrength();
                float pwr = (float) stats.getKiDamage();
                float scale = AddonConfig.NEO_KIKOHO_DAMAGE_SCALE.get().floatValue();
                float damage = (str * 0.6f + pwr * 0.4f) * scale * 0.7f * (1.0f + (finalCombo * 0.3f));

                Vec3 look = player.getLookAngle();
                Vec3 start = player.getEyePosition();
                float reach = 25.0f;
                float half = 1.8f;

                Set<LivingEntity> hitSet = new HashSet<>();
                for (int step = 0; step < (int) reach; step++) {
                    Vec3 center = start.add(look.scale(step + 0.5));
                    AABB slice = new AABB(
                            center.x - half, center.y - half, center.z - half,
                            center.x + half, center.y + half, center.z + half);
                    serverLevel.getEntitiesOfClass(LivingEntity.class, slice,
                            e -> !e.equals(player) && e.isAlive()).forEach(hitSet::add);
                }

                Vec3 end = start.add(look.scale(reach));

                for (LivingEntity victim : hitSet) {
                    KikohoC2S.applyDirectDamage(victim, damage);
                    Vec3 push = look.scale(2.0).add(0, -1.0, 0);
                    victim.setDeltaMovement(push);
                    victim.hurtMarked = true;
                }

                int particleCount = 50 + finalCombo * 15;
                for (int i = 0; i < particleCount; i++) {
                    double t = i / (double) particleCount;
                    Vec3 pos = start.add(look.scale(t * reach));
                    double spr = 0.4 + finalCombo * 0.15;
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            pos.x, pos.y, pos.z, 1, spr, spr, spr, 0.3);
                }
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        end.x, end.y, end.z, 3 + finalCombo, 1.0, 1.0, 1.0, 0.1);

                if (finalCombo >= 5) {
                    for (int i = 0; i < 20 + finalCombo * 5; i++) {
                        double angle = Math.random() * Math.PI * 2;
                        double radius = Math.random() * 2.0;
                        double px = player.getX() + Math.cos(angle) * radius;
                        double py = player.getY() + Math.random() * 2.0;
                        double pz = player.getZ() + Math.sin(angle) * radius;
                        serverLevel.sendParticles(ParticleTypes.LAVA, px, py, pz, 1, 0.1, 0.1, 0.1, 0.05);
                    }
                }

                float pitch = Math.min(2.0f, 1.2f + finalCombo * 0.1f);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.5f, pitch);

                int newCombo = Math.min(finalCombo + 1, MAX_COMBO);
                nbt.putInt(COMBO_KEY, newCombo);
                nbt.putLong(LAST_COMBO_TIME_KEY, now);

                if (newCombo > 1) {
                    player.displayClientMessage(Component.translatable("message.dmzkiaddon.neo_kikoho.combo", newCombo), true);
                }

                if (newCombo >= MAX_COMBO) {
                    player.displayClientMessage(Component.translatable("message.dmzkiaddon.neo_kikoho.max_combo"), true);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}