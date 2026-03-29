package com.dmzkiaddon.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NeoKikohoResetC2S {

    public NeoKikohoResetC2S() {}
    public NeoKikohoResetC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(NeoKikohoResetC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            player.getPersistentData().putInt("NeoKikohoCombo", 0);
        });
        ctx.setPacketHandled(true);
    }
}
