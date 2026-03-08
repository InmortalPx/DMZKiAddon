package com.dmzkiaddon.entity;

import com.dragonminez.common.init.entities.ki.KiLaserEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Piccolo's Special Beam Cannon — a spiraling double helix laser.
 * Extends the base KiLaserEntity and adds a purple/white spiral particle effect.
 */
public class MakankosappoEntity extends KiLaserEntity {

    private static final Vector3f COLOR_PURPLE = new Vector3f(0.3f, 0.1f, 0.9f);
    private static final Vector3f COLOR_WHITE  = new Vector3f(0.9f, 0.9f, 1.0f);

    private int spiralTick = 0;

    @SuppressWarnings("unchecked")
    public MakankosappoEntity(Level level, LivingEntity owner) {
        super((EntityType) com.dragonminez.common.init.MainEntities.KI_WAVE.get(), level);
        this.setOwner(owner);
        this.setNoGravity(true);
        Vec3 look = owner.getLookAngle();
        Vec3 start = owner.getEyePosition().add(look.scale(0.5));
        this.setPos(start.x, start.y, start.z);
        this.setDeltaMovement(look.scale(2.5));
        this.setYRot(owner.getYRot());
        this.setXRot(owner.getXRot());
    }

    @Override
    public void tick() {
        super.tick();
        spiralTick++;

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 pos = this.position();
        Vec3 velocity = this.getDeltaMovement().normalize();

        // Gram-Schmidt: find a vector perpendicular to velocity
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 perp1 = velocity.cross(worldUp).normalize();
        if (perp1.lengthSqr() < 1e-6) perp1 = new Vec3(1, 0, 0);
        Vec3 perp2 = velocity.cross(perp1).normalize();

        double rotSpeed = 0.5;
        double radius   = 0.25;

        for (int strand = 0; strand < 2; strand++) {
            double angle = Math.toRadians(spiralTick * 20.0 + strand * 180.0);
            Vec3 offset = perp1.scale(Math.cos(angle) * radius)
                    .add(perp2.scale(Math.sin(angle) * radius));

            serverLevel.sendParticles(
                    new DustParticleOptions(strand == 0 ? COLOR_PURPLE : COLOR_WHITE, 0.6f),
                    pos.x + offset.x, pos.y + offset.y, pos.z + offset.z,
                    1, 0, 0, 0, 0);
        }
    }
}
