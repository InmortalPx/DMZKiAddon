package com.dmzkiaddon.mixin;

import com.dmzkiaddon.client.LockOnBridge;
import com.dragonminez.client.events.LockOnEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(value = LockOnEvent.class, remap = false)
public class LockOnEventMixin {

    @Shadow
    private static LivingEntity lockedTarget;

    @Inject(method = "toggleLock", at = @At("TAIL"))
    private static void onToggleLock(CallbackInfo ci) {
        LockOnBridge.setTarget(lockedTarget);
    }

    @Inject(method = "unlock", at = @At("HEAD"))
    private static void onUnlock(CallbackInfo ci) {
        LockOnBridge.clearTarget();
    }
}