package com.dmzkiaddon.entity;

import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.ki.AbstractKiProjectile;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

@SuppressWarnings("unchecked")
public class HakaiOrbEntity extends AbstractKiProjectile {

    private static final Vector3f COLOR_PURPLE = new Vector3f(0.55f, 0.0f, 1.0f);
    private static final Vector3f COLOR_WHITE  = new Vector3f(0.85f, 0.8f, 1.0f);
    private static final double   CONVERGE_SPEED = 0.6;

    public enum Phase { ORBIT, CONVERGE }

    private Phase        phase       = Phase.ORBIT;
    private LivingEntity orbitTarget = null;
    private float        orbitAngle  = 0f;
    private float        orbitHeight = 1.0f;
    private int          orbitIndex  = 0;
    private int          orbitTick   = 0;
    private float        damage      = 20f;

    public HakaiOrbEntity(Level level, LivingEntity owner) {
        super((EntityType) MainEntities.KI_BLAST.get(), level);
        this.setOwner(owner);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setColors(0x9900FF, 0xCC88FF);
        this.setSize(0.5f);
    }

    public void setOrbitParams(int index, int total, LivingEntity target) {
        this.orbitTarget = target;
        this.orbitIndex  = index;
        this.orbitAngle  = (float)(Math.PI * 2.0 * index / Math.max(total, 1));
        this.orbitHeight = 0.3f + (index % 3) * 0.6f;
    }

    public void converge(float damage) {
        this.phase  = Phase.CONVERGE;
        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();
        orbitTick++;

        switch (phase) {
            case ORBIT   -> tickOrbit();
            case CONVERGE -> tickConverge();
        }
    }

    private void tickOrbit() {
        if (orbitTarget == null || !orbitTarget.isAlive()) { this.discard(); return; }

        orbitAngle += 0.06f;
        if (orbitAngle > Math.PI * 2) orbitAngle -= (float)(Math.PI * 2);

        double radius = 2.0;
        double x = orbitTarget.getX() + Math.cos(orbitAngle) * radius;
        double z = orbitTarget.getZ() + Math.sin(orbitAngle) * radius;
        double y = orbitTarget.getY() + orbitHeight + Math.sin(orbitTick * 0.08 + orbitIndex) * 0.15;

        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);

        spawnParticles();
    }

    private void tickConverge() {
        if (orbitTarget == null || !orbitTarget.isAlive()) { this.discard(); return; }

        Vec3 to   = new Vec3(
                orbitTarget.getX() - this.getX(),
                orbitTarget.getY() + orbitTarget.getBbHeight() * 0.5 - this.getY(),
                orbitTarget.getZ() - this.getZ());
        double dist = to.length();

        boolean hitByAABB = orbitTarget.getBoundingBox().inflate(0.6).contains(this.position());
        if (dist < 1.0 || hitByAABB) {
            if (!this.level().isClientSide) {
                applyDirectDamage(orbitTarget, damage);
                spawnImpactParticles();
            }
            this.discard();
            return;
        }

        double speed = Math.min(CONVERGE_SPEED, dist * 0.18);
        Vec3 vel = to.normalize().scale(speed);
        this.setDeltaMovement(vel);
        this.setPos(this.getX() + vel.x, this.getY() + vel.y, this.getZ() + vel.z);
        spawnParticles();
    }

    private void applyDirectDamage(LivingEntity target, float damage) {
        float absorption = target.getAbsorptionAmount();
        if (absorption > 0) {
            float absReduction = Math.min(absorption, damage);
            target.setAbsorptionAmount(absorption - absReduction);
            damage -= absReduction;
        }
        target.setHealth(target.getHealth() - damage);
    }

    private void spawnParticles() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(new DustParticleOptions(COLOR_PURPLE, 0.9f),
                this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0);
        if (orbitTick % 3 == 0) {
            sl.sendParticles(new DustParticleOptions(COLOR_WHITE, 0.6f),
                    this.getX(), this.getY(), this.getZ(), 1, 0.08, 0.08, 0.08, 0);
        }
    }

    private void spawnImpactParticles() {
        if (!(this.level() instanceof ServerLevel sl)) return;
        for (int i = 0; i < 16; i++) {
            double angle = Math.random() * Math.PI * 2;
            sl.sendParticles(new DustParticleOptions(COLOR_PURPLE, 1.2f),
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    1, Math.cos(angle) * 0.4, 0.2, Math.sin(angle) * 0.4, 0.05);
        }
    }
}