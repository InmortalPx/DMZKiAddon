package com.dmzkiaddon.mixin;

import com.dmzkiaddon.compat.KiGriefingHelper;
import com.dmzkiaddon.entity.KiWaveAddon;
import com.dragonminez.common.init.MainDamageTypes;
import com.dragonminez.common.init.entities.ki.KiWaveEntity;
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

@Mixin(value = KiWaveEntity.class, remap = false)
public abstract class KiWaveEntityMixin {

    @Inject(
            method = "explodeAndDie",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onExplodeAndDie(Vec3 pos, CallbackInfo ci) {
        KiWaveEntity self = (KiWaveEntity)(Object) this;
        Level level = self.level();

        // explodeAndDie solo corre en servidor, pero por seguridad:
        if (level.isClientSide) return;

        ci.cancel();

        float radius = self.getSize() * 1.5F;

        // 1. Daño a entidades en el radio (replica lógica original de DMZ)
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

        // 2. Partícula y sonido (igual que DMZ original)
        level.addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0, 0.0, 0.0);
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 1.0F);

        // 3. Resolver modo de explosión según gamerule
        Level.ExplosionInteraction mode;
        if (self instanceof KiWaveAddon addon) {
            // KiWaveAddon tiene el modo pre-calculado al momento de spawn
            mode = addon.getExplosionInteraction();
        } else {
            // KiWave nativo del DMZ → consultar gamerule en tiempo real
            mode = KiGriefingHelper.getExplosionMode(level, pos.x, pos.y, pos.z, self.getOwner());
        }

        // 4. Explosión con el modo correcto
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