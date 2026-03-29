package com.dmzkiaddon.entity;

import com.dmzkiaddon.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

public class MakankosappoEntity extends Entity {

    private static final Vector3f COLOR_PURPLE = new Vector3f(0.4f, 0.0f, 0.9f);
    private static final Vector3f COLOR_WHITE  = new Vector3f(0.85f, 0.85f, 1.0f);

    private int    spiralTick  = 0;
    private float  kiDamage    = 10f;
    private UUID   ownerUUID   = null;

    private static final int MAX_LIFE_TICKS = 120;

    public MakankosappoEntity(EntityType<MakankosappoEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public MakankosappoEntity(Level level, LivingEntity owner) {
        this(ModEntities.MAKANKOSAPPO.get(), level);
        this.ownerUUID = owner.getUUID();

        float yaw   = owner.getYHeadRot();
        float pitch = owner.getXRot();
        this.setYRot(yaw);
        this.setXRot(pitch);

        Vec3 look  = Vec3.directionFromRotation(pitch, yaw);
        Vec3 start = owner.getEyePosition().add(look.scale(1.5));
        this.setPos(start.x, start.y, start.z);
        this.setDeltaMovement(look.scale(2.5));
    }

    public void setKiDamage(float damage) { this.kiDamage = damage; }
    public float getKiDamage()            { return kiDamage; }

    @Override
    public void tick() {
        super.tick();

        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);

        spiralTick++;

        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            double spd = movement.length();
            if (spd > 1e-6) {
                Vec3 dir     = movement.scale(1.0 / spd);
                Vec3 worldUp = Math.abs(dir.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
                Vec3 perp1   = cross(dir, worldUp).normalize();
                Vec3 perp2   = cross(dir, perp1).normalize();
                double radius = 0.35;
                for (int strand = 0; strand < 2; strand++) {
                    double angle = Math.toRadians(spiralTick * 25.0 + strand * 180.0);
                    Vec3 offset = new Vec3(
                            perp1.x * Math.cos(angle) * radius + perp2.x * Math.sin(angle) * radius,
                            perp1.y * Math.cos(angle) * radius + perp2.y * Math.sin(angle) * radius,
                            perp1.z * Math.cos(angle) * radius + perp2.z * Math.sin(angle) * radius
                    );
                    serverLevel.sendParticles(
                            new DustParticleOptions(strand == 0 ? COLOR_PURPLE : COLOR_WHITE, 0.8f),
                            pos.x + offset.x, pos.y + offset.y, pos.z + offset.z,
                            1, 0, 0, 0, 0);
                }
            }

            if (this.tickCount > MAX_LIFE_TICKS) {
                this.discard();
                return;
            }

            if (this.tickCount > 2) {
                net.minecraft.world.phys.AABB box = this.getBoundingBox().inflate(0.4);
                for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, box,
                        en -> !en.getUUID().equals(ownerUUID) && en.isAlive())) {
                    e.hurt(this.damageSources().magic(), kiDamage);
                    this.discard();
                    return;
                }

                net.minecraft.world.phys.BlockHitResult blockHit = this.level().clip(
                        new net.minecraft.world.level.ClipContext(
                                this.position(), this.position().add(movement),
                                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                                net.minecraft.world.level.ClipContext.Fluid.NONE, this));
                if (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                    this.discard();
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) this.ownerUUID = tag.getUUID("OwnerUUID");
        this.kiDamage = tag.getFloat("KiDamage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putFloat("KiDamage", kiDamage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }

    private static Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x);
    }
}