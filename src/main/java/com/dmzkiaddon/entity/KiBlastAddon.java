package com.dmzkiaddon.entity;

import com.dmzkiaddon.network.AddonNetworkHandler;
import com.dmzkiaddon.network.packets.ImpactShakeS2C;
import com.dragonminez.common.init.MainEntities;
import com.dragonminez.common.init.entities.ki.KiBlastEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

/**
 * KiBlastAddon — proyectil ki con game feel mejorado:
 * 1. Crecimiento dinámico  — crece en los primeros 30 ticks
 * 2. Rastro de energía     — partículas END_ROD (solo bolas grandes)
 * 3. Presión espacial      — empuja entidades cercanas
 * 4. Impacto cinematográfico — shake + sonido en capas
 */
public class KiBlastAddon extends KiBlastEntity {

    private boolean growthEnabled  = false;
    private boolean windEnabled    = false;
    private float   targetSize     = 1.0f;
    private int     shakeIntensity = 5;
    private int     shakeDuration  = 10;

    @SuppressWarnings("unchecked")
    public KiBlastAddon(Level level, LivingEntity owner) {
        super(MainEntities.KI_BLAST.get(), level);
        this.setOwner(owner);
        this.setNoGravity(true);
    }

    public void enableGrowth(float finalSize) {
        this.growthEnabled = true;
        this.targetSize    = finalSize;
    }

    public void enableWind() {
        this.windEnabled = true;
    }

    public void setImpactShake(int intensity, int duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration  = duration;
    }

    @Override
    protected void onKiTick() {
        super.onKiTick();

        // ── 1. Crecimiento dinámico ───────────────────────────────────────────
        if (growthEnabled && this.tickCount < 30) {
            float step = (targetSize - this.getSize()) / Math.max(1, 30 - this.tickCount);
            this.setSize(this.getSize() + step * 1.2f);
        }

        // ── 2. Rastro de energía — solo bolas grandes, cada 3 ticks ──────────
        if (this.tickCount % 3 == 0
                && this.getSize() >= 2.0f
                && this.level() instanceof ServerLevel serverLevel) {
            float s = this.getSize();
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + this.getBbHeight() / 2.0, this.getZ(),
                    1, s * 0.1, s * 0.1, s * 0.1, 0.01);
        }

        // ── 3. Presión espacial ───────────────────────────────────────────────
        if (windEnabled && this.tickCount % 5 == 0 && this.getSize() > 2.5f) {
            AABB windBox = this.getBoundingBox().inflate(this.getSize() * 2.5);
            List<LivingEntity> nearby = this.level().getEntitiesOfClass(
                    LivingEntity.class, windBox,
                    e -> !e.equals(this.getOwner()) && e.isAlive());
            for (LivingEntity e : nearby) {
                Vec3 push = e.position().subtract(this.position()).normalize().scale(0.35);
                e.push(push.x, 0.25, push.z);
                e.hurtMarked = true;
            }
        }
    }

    // ── 4. Impacto cinematográfico — override de onHit via tick ──────────────
    // En 2.0.3 no hay onSuccessfulHit, usamos tick para detectar impacto
    @Override
    public void tick() {
        boolean wasAlive = this.isAlive();
        super.tick();
        // Si la entidad fue removida en este tick, es que impactó
        if (wasAlive && !this.isAlive()) {
            broadcastImpact();
        }
    }

    private void broadcastImpact() {
        if (this.level().isClientSide) return;
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 5.0f, 0.5f);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 2.0f, 1.2f);

        ImpactShakeS2C shakePacket = new ImpactShakeS2C(shakeIntensity, shakeDuration);
        serverLevel.players().stream()
                .filter(p -> p instanceof ServerPlayer)
                .filter(p -> p.distanceToSqr(this.getX(), this.getY(), this.getZ()) < 40 * 40)
                .forEach(p -> AddonNetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) p), shakePacket));
    }
}