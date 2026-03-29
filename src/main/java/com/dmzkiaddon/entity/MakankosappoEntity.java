package com.dmzkiaddon.entity;

import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.ki.KiLaserEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

public class MakankosappoEntity extends KiLaserEntity {

    private Level.ExplosionInteraction explosionInteraction = Level.ExplosionInteraction.MOB;

    public void setExplosionInteraction(Level.ExplosionInteraction mode) {
        this.explosionInteraction = mode;
    }

    public Level.ExplosionInteraction getExplosionInteraction() {
        return explosionInteraction;
    }

    @SuppressWarnings("unchecked")
    public MakankosappoEntity(net.minecraft.world.entity.EntityType<MakankosappoEntity> type, Level level) {
        super((EntityType) MainEntities.KI_LASER.get(), level);
    }

    @SuppressWarnings("unchecked")
    public MakankosappoEntity(Level level, LivingEntity owner) {
        super((EntityType) MainEntities.KI_LASER.get(), level);
        this.setOwner(owner);
        this.setNoGravity(true);
        this.noPhysics = true;

        float yaw   = owner.getYHeadRot();
        float pitch = owner.getXRot();

        this.setYRot(yaw);
        this.setXRot(pitch);

        setFixedRotation(yaw, pitch);

        Vec3 look     = Vec3.directionFromRotation(pitch, yaw);
        Vec3 startPos = owner.getEyePosition().add(look.scale(0.8));
        this.setPos(startPos.x, startPos.y, startPos.z);

        this.setColors(0x6600CC, 0xCC88FF);
        this.setSize(0.8f);
        this.setKiSpeed(2.5f);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setFixedRotation(float yaw, float pitch) {
        try {
            Class<?> clazz = KiLaserEntity.class;
            Field fixedYawField   = null;
            Field fixedPitchField = null;

            for (Field f : clazz.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == EntityDataAccessor.class) {
                    String name = f.getName();
                    if (name.equals("FIXED_YAW"))   fixedYawField   = f;
                    if (name.equals("FIXED_PITCH")) fixedPitchField = f;
                }
            }

            if (fixedYawField != null && fixedPitchField != null) {
                EntityDataAccessor<Float> yawAccessor   = (EntityDataAccessor<Float>) fixedYawField.get(null);
                EntityDataAccessor<Float> pitchAccessor = (EntityDataAccessor<Float>) fixedPitchField.get(null);
                this.entityData.set(yawAccessor, yaw);
                this.entityData.set(pitchAccessor, pitch);
            } else {
                int accessorCount = 0;
                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    if (f.getType() == EntityDataAccessor.class) {
                        accessorCount++;
                        if (accessorCount == 2) {
                            EntityDataAccessor<Float> acc = (EntityDataAccessor<Float>) f.get(null);
                            this.entityData.set(acc, yaw);
                        } else if (accessorCount == 3) {
                            EntityDataAccessor<Float> acc = (EntityDataAccessor<Float>) f.get(null);
                            this.entityData.set(acc, pitch);
                        }
                    }
                }
            }
        } catch (Exception e) {
            com.dmzkiaddon.DMZKiAddon.LOGGER.warn("MakankosappoEntity: No se pudo setear FIXED_YAW/PITCH: {}", e.getMessage());
        }
    }
}