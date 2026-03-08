package com.dmzkiaddon.mixin;

import com.dmzkiaddon.KiGriefingHelper;
import com.dmzkiaddon.entity.KiLaserAddon;
import com.dragonminez.common.init.MainDamageTypes;
import com.dragonminez.common.init.entities.ki.KiLaserEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = KiLaserEntity.class, remap = false)
public abstract class KiLaserEntityMixin {

    @Inject(
            method = "explodeAndDie",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onExplodeAndDie(Vec3 pos, CallbackInfo ci) {
        KiLaserEntity self = (KiLaserEntity)(Object) this;
        Level level = self.level();

        if (level.isClientSide) return;

        ci.cancel();

        float radius = self.getSize();

        // Daño a entidades
        AABB area = new AABB(pos, pos).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : entities) {
            if (!self.shouldDamage(target)) continue;
            double dist = target.distanceToSqr(pos);
            if (dist <= radius * radius) {
                target.hurt(
                        MainDamageTypes.kiblast(level, self, self.getOwner()),
                        self.getKiDamage()
                );
            }
        }

        // Partícula y sonido
        level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0, 0.0, 0.0);
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                4.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        // Modo explosión según gamerule
        Level.ExplosionInteraction mode;
        if (self instanceof KiLaserAddon addon) {
            mode = addon.getExplosionInteraction();
        } else {
            mode = KiGriefingHelper.getExplosionMode(level, pos.x, pos.y, pos.z, self.getOwner());
        }

        level.explode(
                self,
                self.damageSources().explosion(self, self.getOwner()),
                null,
                pos.x, pos.y, pos.z,
                radius, false, mode, false
        );

        self.discard();
    }
}