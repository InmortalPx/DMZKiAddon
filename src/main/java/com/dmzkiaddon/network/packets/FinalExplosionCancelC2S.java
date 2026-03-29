package com.dmzkiaddon.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S — Cancelación de la carga del Final Explosion.
 * Descarta el KiOrbEntity si existe, quita el Slowness de carga.
 */
public class FinalExplosionCancelC2S {

    public FinalExplosionCancelC2S() {}
    public FinalExplosionCancelC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(FinalExplosionCancelC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            int orbId = player.getPersistentData().getInt("ActiveFinalExplosionOrb");
            if (orbId != 0) {
                net.minecraft.world.entity.Entity orb = player.level().getEntity(orbId);
                if (orb != null) orb.discard();
                player.getPersistentData().remove("ActiveFinalExplosionOrb");
            }
            player.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
        });
        ctx.setPacketHandled(true);
    }
}
