package com.dmzkiaddon.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FinalExplosionChargeC2S {

    private static final int SLOWNESS_DURATION = 90;

    public FinalExplosionChargeC2S() {}
    public FinalExplosionChargeC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(FinalExplosionChargeC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, 10, false, false));
        });
        ctx.setPacketHandled(true);
    }
}