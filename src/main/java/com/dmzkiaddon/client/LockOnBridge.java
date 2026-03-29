package com.dmzkiaddon.client;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LockOnBridge {

    private static LivingEntity cachedTarget = null;

    public static void setTarget(LivingEntity target) {
        cachedTarget = target;
    }

    public static void clearTarget() {
        cachedTarget = null;
    }

    public static LivingEntity getTarget() {
        if (cachedTarget != null && !cachedTarget.isAlive()) {
            cachedTarget = null;
        }
        return cachedTarget;
    }

    public static int getTargetId() {
        LivingEntity t = getTarget();
        return t != null ? t.getId() : -1;
    }
}
