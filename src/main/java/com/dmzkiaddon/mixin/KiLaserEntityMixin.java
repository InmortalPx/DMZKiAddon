package com.dmzkiaddon.mixin;

import com.dmzkiaddon.compat.KiGriefingHelper;
import com.dmzkiaddon.entity.KiLaserAddon;
import com.dragonminez.common.init.MainDamageTypes;
import com.dragonminez.common.init.MainParticles;
import com.dragonminez.common.init.entities.ki.KiLaserEntity;
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
import com.dmzkiaddon.network.AddonNetworkHandler;
import com.dmzkiaddon.network.packets.KiImpactS2C;
import net.minecraftforge.network.PacketDistributor;

@Mixin(value = KiLaserEntity.class, remap = false)
public abstract class KiLaserEntityMixin {

    @Inject(method = "explodeAndDie", at = @At("HEAD"), cancellable = true)
    private void onExplodeAndDie(Vec3 pos, CallbackInfo ci) {
        KiLaserEntity self = (KiLaserEntity)(Object) this;
        Level level = self.level();
        if (level.isClientSide) return;
        ci.cancel();

        float radius = self.getSize();

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
                    pos.x, pos.y, pos.z, 2, 0.2, 0.2, 0.2, 0.08);
            serverLevel.sendParticles(MainParticles.KI_SPLASH_WAVE.get(),
                    pos.x, pos.y, pos.z, 0,
                    (double) self.getColorBorde(), (double)(radius * 0.6f), 0.0D, 1.0D);
            serverLevel.sendParticles(MainParticles.KI_EXPLOSION_SPLASH.get(),
                    pos.x, pos.y, pos.z, 4, radius * 0.2, radius * 0.2, radius * 0.2, 0.12);

            // Burst Lodestone en el cliente
            KiImpactS2C pkt = new KiImpactS2C(pos.x, pos.y, pos.z,
                    self.getColor(), self.getSize(), KiImpactS2C.ImpactType.LASER);
            serverLevel.players().forEach(p -> AddonNetworkHandler.sendToPlayer(pkt, p));
        }

        // 3. Sonido
        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE,
                3.5F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.75F);

        // 4. Explosión
        Level.ExplosionInteraction mode = (self instanceof KiLaserAddon addon)
                ? addon.getExplosionInteraction()
                : KiGriefingHelper.getExplosionMode(level, pos.x, pos.y, pos.z, self.getOwner());

        level.explode(self, self.damageSources().explosion(self, self.getOwner()),
                null, pos.x, pos.y, pos.z, radius, false, mode, false);

        self.discard();
    }
}