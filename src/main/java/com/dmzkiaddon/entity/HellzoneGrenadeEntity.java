package com.dmzkiaddon.entity;

import com.dmzkiaddon.client.KiParticleEngine;
import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.MastersEntity;
import com.dragonminez.common.init.entities.ki.AbstractKiProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.awt.Color;

public class HellzoneGrenadeEntity extends AbstractKiProjectile {

    public enum Phase { ORBIT, CONVERGE, CONVERGE_POINT }

    private static final double CONVERGE_SPEED_MAX = 0.776;
    private static final float  ORBIT_SPEED        = 0.05f;

    private Phase phase = Phase.ORBIT;
    private float orbitAngle;
    private float orbitHeight;
    private float orbitRadius;
    private int   orbitIndex;
    private int   orbitTick = 0;
    private int   totalOrbiting;
    private LivingEntity orbitTarget;
    private LivingEntity target;
    private Vec3  targetPoint;
    private float kiDamage = 6f;

    private Level.ExplosionInteraction explosionInteraction = Level.ExplosionInteraction.MOB;

    public void setExplosionInteraction(Level.ExplosionInteraction mode) {
        this.explosionInteraction = mode;
    }

    @SuppressWarnings("unchecked")
    public HellzoneGrenadeEntity(Level level, LivingEntity owner) {
        super((EntityType) MainEntities.KI_BLAST.get(), level);
        this.setOwner(owner);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setColors(0x44FF44, 0xAAFFAA);
        this.setKiDamage(kiDamage);
        this.setSize(0.4f);
        this.phase = Phase.ORBIT;
    }

    public void setOrbitParams(int index, int total, float radius, LivingEntity center) {
        this.orbitIndex    = index;
        this.totalOrbiting = total;
        this.orbitRadius   = radius;
        this.orbitAngle    = (float)(2 * Math.PI * index / Math.max(total, 1));
        this.orbitHeight   = 0.3f + (index % 3) * 0.6f;
    }

    public void setOrbitTarget(LivingEntity target) {
        this.orbitTarget = target;
    }

    public void converge(LivingEntity target, float damage) {
        this.phase    = Phase.CONVERGE;
        this.target   = target;
        this.kiDamage = damage;
    }

    public void convergeToPoint(Vec3 point, float damage) {
        this.phase       = Phase.CONVERGE_POINT;
        this.targetPoint = point;
        this.kiDamage    = damage;
    }

    public Phase getPhase() { return phase; }

    @Override
    public void tick() {
        super.tick();
        orbitTick++;

        if (!this.level().isClientSide() && !this.isAlive()) return;

        switch (phase) {
            case ORBIT          -> tickOrbit();
            case CONVERGE       -> tickConverge();
            case CONVERGE_POINT -> tickConvergePoint();
        }
    }

    private void tickOrbit() {
        LivingEntity owner = (LivingEntity) this.getOwner();
        if (owner == null || !owner.isAlive()) { this.discard(); return; }

        LivingEntity center = (orbitTarget != null && orbitTarget.isAlive()) ? orbitTarget : owner;

        orbitAngle += ORBIT_SPEED;
        if (orbitAngle > Math.PI * 2) orbitAngle -= (float)(Math.PI * 2);

        double x = center.getX() + Math.cos(orbitAngle) * orbitRadius;
        double z = center.getZ() + Math.sin(orbitAngle) * orbitRadius;
        double y = center.getY() + orbitHeight;

        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(x, y, z);

        if (this.level().isClientSide()) {
            KiParticleEngine.spawnHellzoneOrbit(this.level(), this.position());
        }
    }

    private void tickConverge() {
        if (target == null || !target.isAlive()) { this.discard(); return; }

        double targetX = target.getX();
        double targetY = target.getY() + target.getBbHeight() * 0.5;
        double targetZ = target.getZ();

        Vec3 toTarget = new Vec3(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ());
        double dist = toTarget.length();

        boolean hitByAABB = target.getBoundingBox().inflate(0.5).contains(this.position());
        boolean hitByDist = dist < 1.2;

        if (this.level().isClientSide()) {
            KiParticleEngine.spawnKiTrail(this.level(), this.position(), new Color(76, 204, 76), 0.6f);
        }

        if (hitByAABB || hitByDist) {
            if (!this.level().isClientSide()) {
                if (!(target instanceof MastersEntity)) {
                    target.hurt(this.level().damageSources().fellOutOfWorld(), kiDamage);
                }
                this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                        1.5f, false, explosionInteraction);
            }
            this.discard();
            return;
        }

        double speed = Math.min(CONVERGE_SPEED_MAX, dist * 0.15);
        Vec3 velocity = toTarget.normalize().scale(speed);
        this.setDeltaMovement(velocity);
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
    }

    private void tickConvergePoint() {
        if (targetPoint == null) { this.discard(); return; }

        Vec3 toPoint = new Vec3(
                targetPoint.x - this.getX(),
                targetPoint.y - this.getY(),
                targetPoint.z - this.getZ());
        double dist = toPoint.length();

        if (this.level().isClientSide()) {
            KiParticleEngine.spawnKiTrail(this.level(), this.position(), new Color(76, 204, 76), 0.6f);
        }

        if (dist < 0.8) {
            if (!this.level().isClientSide()) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                        1.5f, false, explosionInteraction);
            }
            this.discard();
            return;
        }

        double speed = Math.min(CONVERGE_SPEED_MAX, dist * 0.15);
        Vec3 velocity = toPoint.normalize().scale(speed);
        this.setDeltaMovement(velocity);
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level().isClientSide() && reason == RemovalReason.DISCARDED) {
            KiParticleEngine.spawnKiExplosion(this.level(), this.position(), new Color(76, 204, 76), 0.8f);
        }
        super.remove(reason);
    }
}