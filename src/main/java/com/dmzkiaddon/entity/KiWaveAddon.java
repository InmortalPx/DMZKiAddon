package com.dmzkiaddon.entity;

import com.dmzkiaddon.registry.ModSounds;
import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.ki.KiWaveEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

public class KiWaveAddon extends KiWaveEntity {

    public enum SoundType { KAMEHAMEHA, GALICK_GUN, FINAL_FLASH }

    private static EntityDataAccessor<Float> CACHED_YAW_ACCESSOR  = null;
    private static EntityDataAccessor<Float> CACHED_PITCH_ACCESSOR = null;
    private static boolean reflectionResolved = false;

    private Level.ExplosionInteraction explosionInteraction = Level.ExplosionInteraction.MOB;

    public void setExplosionInteraction(Level.ExplosionInteraction mode) {
        this.explosionInteraction = mode;
    }

    public Level.ExplosionInteraction getExplosionInteraction() {
        return explosionInteraction;
    }

    @SuppressWarnings("unchecked")
    public KiWaveAddon(Level level, LivingEntity owner, SoundType soundType) {
        super((EntityType) MainEntities.KI_WAVE.get(), level);
        this.setOwner(owner);
        this.setNoGravity(true);
        this.noPhysics = true;

        float yaw   = owner.getYHeadRot();
        float pitch = owner.getXRot();

        this.setYRot(yaw);
        this.setXRot(pitch);
        setFixedRotation(yaw, pitch);

        Vec3 look     = Vec3.directionFromRotation(pitch, yaw);
        Vec3 startPos = owner.getEyePosition().add(look.scale(0.5));
        this.setPos(startPos.x, startPos.y, startPos.z);

        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            net.minecraft.sounds.SoundEvent sound = switch (soundType) {
                case KAMEHAMEHA  -> ModSounds.HAMEHA_FIRE.get();
                case GALICK_GUN  -> ModSounds.FBEAM_1.get();
                case FINAL_FLASH -> ModSounds.FINALFLASH_CHARGE.get();
            };
            level.playSound(player, owner.getX(), owner.getY(), owner.getZ(),
                    sound, SoundSource.PLAYERS, 1.0f,
                    0.9f + level.random.nextFloat() * 0.2f);
        }
    }

    /** Llamar durante el setup del mod para resolver la reflection antes del primer disparo. */
    public static void prewarmAccessors() {
        if (!reflectionResolved) resolveAccessors();
    }

    @SuppressWarnings({"unchecked"})
    private void setFixedRotation(float yaw, float pitch) {
        if (!reflectionResolved) resolveAccessors();
        if (CACHED_YAW_ACCESSOR != null && CACHED_PITCH_ACCESSOR != null) {
            this.entityData.set(CACHED_YAW_ACCESSOR, yaw);
            this.entityData.set(CACHED_PITCH_ACCESSOR, pitch);
        } else {
            com.dmzkiaddon.DMZKiAddon.LOGGER.warn("KiWaveAddon: FIXED_YAW/PITCH accessors not found");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static synchronized void resolveAccessors() {
        if (reflectionResolved) return;
        try {
            Class<?> clazz = KiWaveEntity.class;
            Field yawField = null, pitchField = null;
            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == EntityDataAccessor.class) {
                    String name = f.getName();
                    if (name.equals("FIXED_YAW"))   yawField   = f;
                    if (name.equals("FIXED_PITCH"))  pitchField = f;
                }
            }
            if (yawField != null && pitchField != null) {
                CACHED_YAW_ACCESSOR   = (EntityDataAccessor<Float>) yawField.get(null);
                CACHED_PITCH_ACCESSOR = (EntityDataAccessor<Float>) pitchField.get(null);
            } else {
                int count = 0;
                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.getType() == EntityDataAccessor.class) {
                        count++;
                        if (count == 2) CACHED_YAW_ACCESSOR   = (EntityDataAccessor<Float>) f.get(null);
                        if (count == 3) CACHED_PITCH_ACCESSOR  = (EntityDataAccessor<Float>) f.get(null);
                    }
                }
            }
        } catch (Exception e) {
            com.dmzkiaddon.DMZKiAddon.LOGGER.error("KiWaveAddon: reflection failed: {}", e.getMessage());
        } finally {
            reflectionResolved = true;
        }
    }
}