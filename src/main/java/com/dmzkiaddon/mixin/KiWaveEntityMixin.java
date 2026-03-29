package com.dmzkiaddon.mixin;

import com.dmzkiaddon.compat.KiGriefingHelper;
import com.dmzkiaddon.entity.KiWaveAddon;
import com.dragonminez.common.init.MainDamageTypes;
import com.dragonminez.common.init.MainParticles;
import com.dragonminez.common.init.entities.ki.KiWaveEntity;
import net.minecraft.server.level.ServerLevel;
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

    @Inject(method = "explodeAndDie", at = @At("HEAD"), cancellable = true)
    private void onExplodeAndDie(Vec3 pos, CallbackInfo ci) {
        KiWaveEntity self = (KiWaveEntity)(Object) this;
        Level level = self.level();
        if (level.isClientSide) return;
        ci.cancel();

        float radius = self.getSize() * 1.5F;

        // 1. Daño a entidades
        AABB area = new AABB(pos, pos).inflate(radius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : entities) {
            if (!self.shouldDamage(target)) continue;
            if (target.distanceToSqr(pos) <= radius * radius)
                target.hurt(MainDamageTypes.kiblast(level, self, self.getOwner()), self.getKiDamage());
        }

        // 2. Partículas DMZ de impacto
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(MainParticles.KI_EXPLOSION_FLASH.get(),
                    pos.x, pos.y, pos.z, 3, 0.3, 0.3, 0.3, 0.1);
            serverLevel.sendParticles(MainParticles.KI_SPLASH_WAVE.get(),
                    pos.x, pos.y, pos.z, 0,
                    (double) self.getColorBorde(), (double)(radius * 0.8f), 0.0D, 1.0D);
            serverLevel.sendParticles(MainParticles.KI_EXPLOSION_SPLASH.get(),
                    pos.x, pos.y, pos.z, 6, radius * 0.4, radius * 0.4, radius * 0.4, 0.15);
        }

        // 3. Sonido de impacto potente
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 0.85F);

        // 4. Explosión
        Level.ExplosionInteraction mode = (self instanceof KiWaveAddon addon)
                ? addon.getExplosionInteraction()
                : KiGriefingHelper.getExplosionMode(level, pos.x, pos.y, pos.z, self.getOwner());

        level.explode(self, self.damageSources().explosion(self, self.getOwner()),
                null, pos.x, pos.y, pos.z, radius, false, mode, false);

        self.discard();
    }
}