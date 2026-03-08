package com.dmzkiaddon.entity;

import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.MastersEntity;
import com.dragonminez.common.init.entities.ki.AbstractKiProjectile;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class HellzoneGrenadeEntity extends AbstractKiProjectile {

    public enum Phase { ORBIT, CONVERGE }

    private static final Vector3f COLOR_MAIN   = new Vector3f(0.3f, 0.8f, 0.3f);
    private static final Vector3f COLOR_BRIGHT = new Vector3f(0.8f, 1.0f, 0.8f);

    private Phase phase = Phase.ORBIT;
    private float orbitAngle;
    private float orbitHeight;
    private float orbitRadius;
    private int   orbitIndex;
    private int   orbitTick = 0;
    private int   totalOrbiting;
    private LivingEntity target;
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
        this.setColors(0x44FF44, 0xAAFFAA);
        this.setKiDamage(kiDamage);
        this.setSize(0.4f);
        this.phase = Phase.ORBIT;
    }

    public void setOrbitParams(int index, int total, float radius, LivingEntity center) {
        this.orbitIndex    = index;
        this.totalOrbiting = total;
        this.orbitRadius   = radius;
        this.orbitAngle    = (float)(2 * Math.PI * index / total);
        this.orbitHeight   = 0.8f + (float) Math.sin(index * 1.3f) * 0.5f;
    }

    public void converge(LivingEntity target, float damage) {
        this.phase    = Phase.CONVERGE;
        this.target   = target;
        this.kiDamage = damage;
    }

    public Phase getPhase() { return phase; }

    @Override
    public void tick() {
        super.tick();
        orbitTick++;

        if (!this.level().isClientSide() && !this.isAlive()) return;

        if (phase == Phase.ORBIT) {
            tickOrbit();
        } else {
            tickConverge();
        }
    }

    private void tickOrbit() {
        LivingEntity owner = (LivingEntity) this.getOwner();
        if (owner == null || !owner.isAlive()) { this.discard(); return; }

        double angle = orbitAngle + orbitTick * 0.08;
        double x = owner.getX() + Math.cos(angle) * orbitRadius;
        double z = owner.getZ() + Math.sin(angle) * orbitRadius;
        double y = owner.getY() + orbitHeight + Math.sin(orbitTick * 0.1 + orbitIndex) * 0.15;

        this.setDeltaMovement(x - this.getX(), y - this.getY(), z - this.getZ());
        this.setPos(x, y, z);

        spawnOrbitParticles();
    }

    private void tickConverge() {
        if (target == null || !target.isAlive()) { this.discard(); return; }

        double targetX = target.getX();
        double targetY = target.getY() + target.getBbHeight() * 0.5;
        double targetZ = target.getZ();

        Vec3 toTarget = new Vec3(targetX - this.getX(), targetY - this.getY(), targetZ - this.getZ());
        double dist = toTarget.length();

        if (dist < 0.8) {
            if (!this.level().isClientSide()) {
                // No dañar maestros
                if (!(target instanceof MastersEntity)) {
                    target.hurt(this.level().damageSources().fellOutOfWorld(), kiDamage);
                }

                this.level().explode(this, this.getX(), this.getY(), this.getZ(),
                        1.5f, false, explosionInteraction);

                spawnImpactParticles();
            }
            this.discard();
            return;
        }

        double speed = Math.min(0.8, dist * 0.15);
        Vec3 velocity = toTarget.normalize().scale(speed);
        this.setDeltaMovement(velocity);
        this.setPos(this.getX() + velocity.x, this.getY() + velocity.y, this.getZ() + velocity.z);
    }

    private void spawnOrbitParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(
                new DustParticleOptions(COLOR_MAIN, 0.5f),
                this.getX(), this.getY(), this.getZ(),
                2, 0.05, 0.05, 0.05, 0);
    }

    private void spawnImpactParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        for (int i = 0; i < 12; i++) {
            serverLevel.sendParticles(
                    new DustParticleOptions(COLOR_BRIGHT, 0.8f),
                    this.getX(), this.getY(), this.getZ(),
                    1, 0.3, 0.3, 0.3, 0.1);
        }
    }
}