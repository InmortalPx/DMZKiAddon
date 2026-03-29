package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.config.AddonConfig;
import com.dmzkiaddon.compat.KiGriefingHelper;
import com.dmzkiaddon.entity.KiBlastAddon;
import com.dmzkiaddon.entity.KiLaserAddon;
import com.dmzkiaddon.entity.KiWaveAddon;
import com.dmzkiaddon.entity.MakankosappoEntity;
import com.dmzkiaddon.registry.ModSounds;
import com.dragonminez.common.init.entities.ki.KiBlastEntity;
import com.dragonminez.common.init.entities.ki.KiDiscEntity;
import com.dragonminez.common.stats.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class FireKiAttackC2S {

    public enum AttackType {
        KAMEHAMEHA, GALICK_GUN, FINAL_FLASH, KI_VOLLEY, KI_DISC, KI_LASER,
        SPIRIT_BOMB, BIG_BANG, DEATH_BALL, MASENKO, DODOMPA, MAKANKOSAPPO,
        TAIYOKEN, FINAL_KAMEHAMEHA, HELLZONE, HAKAI, TIME_SKIP, POINT_PRESSURE,
        FINAL_EXPLOSION, KIKOHO, NEO_KIKOHO
    }

    private final AttackType attackType;
    private final float chargeLevel;

    public FireKiAttackC2S(AttackType attackType, float chargeLevel) {
        this.attackType = attackType;
        this.chargeLevel = chargeLevel;
    }

    public FireKiAttackC2S(FriendlyByteBuf buffer) {
        this.attackType = buffer.readEnum(AttackType.class);
        this.chargeLevel = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(attackType);
        buffer.writeFloat(chargeLevel);
    }

    public static void handle(FireKiAttackC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats ->
                    fireAttack(packet.attackType, player, stats, packet.chargeLevel));
        });
        ctx.setPacketHandled(true);
    }

    private static void fireAttack(AttackType type, ServerPlayer player, StatsData stats, float charge) {
        // Validación básica — jugador debe estar vivo
        if (!player.isAlive()) return;

        // Los ataques especiales tienen su propio paquete C2S y ya cobran ki allí.
        // FireKiAttackC2S solo maneja los ataques del ciclo KEY_FIRE + TAIYOKEN.
        if (type == AttackType.HELLZONE   || type == AttackType.HAKAI          ||
            type == AttackType.TIME_SKIP  || type == AttackType.POINT_PRESSURE ||
            type == AttackType.FINAL_EXPLOSION || type == AttackType.KIKOHO    ||
            type == AttackType.NEO_KIKOHO) return;

        int maxEnergy     = stats.getMaxEnergy();
        int currentEnergy = stats.getResources().getCurrentEnergy();
        int attackCost    = (int)(maxEnergy * (AddonConfig.getCostPercentage(type) / 100f) * (1f + charge));
        if (currentEnergy < attackCost) return;

        Level level    = player.level();
        Vec3 lookAngle = player.getLookAngle();
        float sizeMult = 1f + charge * 0.5f;
        float kiDamage = (float) stats.getKiDamage() * (1f + charge) * AddonConfig.getDamageScale(type);

        // Cobrar energía SOLO para los ataques que realmente ejecutan algo
        stats.getResources().setCurrentEnergy(currentEnergy - attackCost);

        switch (type) {

            // ── KiWaveAddon: el constructor maneja su propio sonido via SoundType ──
            // NO agregar playSound extra aquí — duplicaría el sonido.

            case KAMEHAMEHA -> {
                KiWaveAddon wave = new KiWaveAddon(level, player, KiWaveAddon.SoundType.KAMEHAMEHA);
                wave.setColors(0x44AAFF, 0x88CCFF);   // Azul cielo brillante — Full Color manga
                wave.setKiDamage(kiDamage);
                wave.setSize(sizeMult);
                wave.setKiSpeed(1.5f);
                wave.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(wave);
            }
            case GALICK_GUN -> {
                KiWaveAddon wave = new KiWaveAddon(level, player, KiWaveAddon.SoundType.GALICK_GUN);
                wave.setColors(0xCC00FF, 0xFF44FF);    // Fucsia/violeta — Full Color manga
                wave.setKiDamage(kiDamage * 1.1f);
                wave.setSize(sizeMult);
                wave.setKiSpeed(1.4f);
                wave.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(wave);
            }
            case FINAL_FLASH -> {
                KiWaveAddon wave = new KiWaveAddon(level, player, KiWaveAddon.SoundType.FINAL_FLASH);
                wave.setColors(0xFFFF00, 0xFFFFAA);    // Amarillo puro brillante — Full Color manga
                wave.setKiDamage(kiDamage * 1.4f);
                wave.setSize(sizeMult * 1.3f);
                wave.setKiSpeed(1.3f);
                wave.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(wave);
            }
            case MASENKO -> {
                KiWaveAddon wave = new KiWaveAddon(level, player, KiWaveAddon.SoundType.KAMEHAMEHA);
                wave.setColors(0xFFDD00, 0xFFFF66);    // Amarillo dorado — Full Color manga
                wave.setKiDamage(kiDamage);
                wave.setSize(sizeMult * 0.9f);
                wave.setKiSpeed(1.6f);
                wave.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(wave);
            }
            case FINAL_KAMEHAMEHA -> {
                // Núcleo azul — más pequeño y más rápido (inner beam)
                KiWaveAddon waveBlue = new KiWaveAddon(level, player, KiWaveAddon.SoundType.KAMEHAMEHA);
                waveBlue.setColors(0x44AAFF, 0x88CCFF);
                waveBlue.setKiDamage(kiDamage * 1.5f);
                waveBlue.setSize(sizeMult * 1.2f);       // más estrecho
                waveBlue.setKiSpeed(1.5f);               // ligeramente más rápido
                waveBlue.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(waveBlue);

                // Halo amarillo — más grande y más lento (outer beam)
                KiWaveAddon waveYellow = new KiWaveAddon(level, player, KiWaveAddon.SoundType.FINAL_FLASH);
                waveYellow.setColors(0xFFFF00, 0xFFFFAA);
                waveYellow.setKiDamage(kiDamage * 0.8f); // menos daño — es el halo
                waveYellow.setSize(sizeMult * 1.7f);      // más ancho — envuelve al azul
                waveYellow.setKiSpeed(1.3f);              // ligeramente más lento
                waveYellow.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(waveYellow);
            }

            case KI_LASER -> {
                KiLaserAddon laser = new KiLaserAddon(level, player);
                laser.setColors(0x88DDFF, 0xCCEEFF);  // Azul claro — ki genérico
                laser.setKiDamage(kiDamage * 0.6f);
                laser.setKiSpeed(2.0f);
                laser.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(laser);
                playOne(level, player, ModSounds.BASICBEAM_FIRE.get(), 0.8f, 1.2f);
            }
            case DODOMPA -> {
                KiLaserAddon dodompa = new KiLaserAddon(level, player);
                dodompa.setColors(0xFF00AA, 0xFF66CC);  // Rosa/magenta — Red Ribbon, Tao Pai Pai
                dodompa.setKiDamage(kiDamage * 0.8f);
                dodompa.setKiSpeed(1.2f);
                dodompa.setExplosionInteraction(KiGriefingHelper.getExplosionMode(level, player.getX(), player.getY(), player.getZ(), player));
                level.addFreshEntity(dodompa);
                playOne(level, player, ModSounds.BASICBEAM_FIRE.get(), 0.8f, 0.85f);
            }
            case MAKANKOSAPPO -> {
                MakankosappoEntity beam = new MakankosappoEntity(level, player);
                beam.setKiDamage(kiDamage * 1.3f);
                level.addFreshEntity(beam);
                playOne(level, player, ModSounds.BASICBEAM_FIRE.get(), 0.6f, 0.8f);
            }
            case KI_VOLLEY -> {
                for (int i = 0; i < 5; i++) {
                    Vec3 dir = lookAngle.add(
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2,
                            (Math.random() - 0.5) * 0.2);
                    KiBlastAddon blast = new KiBlastAddon(level, player);
                    blast.setColors(0xFFEE00, 0xFFFFCC);
                    blast.setKiDamage(kiDamage * 0.3f);
                    blast.setSize(0.5f);
                    blast.setImpactShake(2, 3);
                    shootProjectile(blast, player, dir, 2.5f);
                    level.addFreshEntity(blast);
                }
                playOne(level, player, ModSounds.KIBALL_RELEASE.get(), 0.8f, 1.1f);
            }
            case SPIRIT_BOMB -> {
                KiBlastAddon spirit = new KiBlastAddon(level, player);
                spirit.setColors(0x44AAFF, 0xCCEEFF);
                spirit.setKiDamage(kiDamage * 2.0f * Math.max(charge, 0.1f));
                float spiritFinal = sizeMult * 2.5f;
                spirit.setSize(spiritFinal * 0.3f);
                spirit.enableGrowth(spiritFinal);
                spirit.enableWind();
                spirit.setImpactShake(9, 16);
                shootProjectile(spirit, player, lookAngle, 0.8f);
                level.addFreshEntity(spirit);
                playOne(level, player, ModSounds.BLAST.get(), 1.0f, 0.55f);
            }
            case BIG_BANG -> {
                KiBlastAddon bigBang = new KiBlastAddon(level, player);
                bigBang.setColors(0x88DDFF, 0xFFFFFF);
                bigBang.setKiDamage(kiDamage * 1.8f);
                float bigBangFinal = sizeMult * 4.5f;
                bigBang.setSize(bigBangFinal * 0.4f);
                bigBang.enableGrowth(bigBangFinal);
                bigBang.setImpactShake(7, 12);
                shootProjectile(bigBang, player, lookAngle, 1.1f);
                level.addFreshEntity(bigBang);
                playOne(level, player, ModSounds.BIGBANG_FIRE.get(), 1.0f, 0.9f);
            }
            case DEATH_BALL -> {
                KiBlastAddon dball = new KiBlastAddon(level, player);
                dball.setColors(0x660088, 0xFF00FF);
                dball.setKiDamage(kiDamage * 2.0f);
                float dballFinal = sizeMult * 6.0f;
                dball.setSize(dballFinal * 0.25f);
                dball.enableGrowth(dballFinal);
                dball.enableWind();
                dball.setImpactShake(10, 18);
                shootProjectile(dball, player, lookAngle, 0.9f);
                level.addFreshEntity(dball);
                playOne(level, player, ModSounds.DEATHBALL_FIRE.get(), 1.0f, 0.8f);
            }
            case KI_DISC -> {
                KiDiscEntity disc = new KiDiscEntity(level, player);
                disc.setColors(0xFFEE00, 0xFFFFAA);
                disc.setKiDamage(kiDamage);
                shootProjectile(disc, player, lookAngle, 2.2f);
                level.addFreshEntity(disc);
                playOne(level, player, ModSounds.DISC_FIRE.get(), 0.6f, 1.0f);
            }
            case TAIYOKEN -> {
                AABB area = player.getBoundingBox().inflate(10.0);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> !e.equals(player) && e.isAlive());
                for (LivingEntity target : targets) {
                    if (!player.hasLineOfSight(target)) continue; // no atraviesa paredes
                    target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,         (int)(60 * (1f + charge)), 0, false, false));
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, (int)(80 * (1f + charge)), 2, false, false));
                }
                playOne(level, player, ModSounds.BLAST.get(), 1.0f, 1.7f);
            }
        }
    }

    /**
     * Toca un sonido UNA sola vez para todos los jugadores cercanos incluyendo el caster.
     * Usar null como primer argumento en Level.playSound incluye al propio jugador.
     */
    private static void playOne(Level level, ServerPlayer player,
                                 net.minecraft.sounds.SoundEvent sound,
                                 float volume, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void shootProjectile(Projectile proj, ServerPlayer player, Vec3 direction, float speed) {
        Vec3 dir = direction.normalize();
        double chestY = player.getY() + player.getBbHeight() * 0.6;
        Vec3 spawnPos = new Vec3(player.getX(), chestY, player.getZ()).add(dir.scale(0.8));
        proj.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        proj.setOwner(player);
        proj.setDeltaMovement(dir.scale(speed));
        proj.hurtMarked = true;
    }
}