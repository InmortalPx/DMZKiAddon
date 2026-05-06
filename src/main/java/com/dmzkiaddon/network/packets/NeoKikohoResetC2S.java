package com.dmzkiaddon.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class NeoKikohoResetC2S {

    private static final String COMBO_KEY = "NeoKikohoCombo";
    private static final String LAST_COMBO_TIME_KEY = "NeoKikohoLastComboTime";
    private static final long MIN_COMBO_TIME_MS = 3000L;

    public NeoKikohoResetC2S() {}
    public NeoKikohoResetC2S(FriendlyByteBuf buf) {}
    public void encode(FriendlyByteBuf buf) {}

    public static void handle(NeoKikohoResetC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            long now = System.currentTimeMillis();
            long lastComboTime = player.getPersistentData().getLong(LAST_COMBO_TIME_KEY);

            if (now - lastComboTime < MIN_COMBO_TIME_MS) {
                return;
            }

            player.getPersistentData().putInt(COMBO_KEY, 0);
            player.getPersistentData().putLong(LAST_COMBO_TIME_KEY, 0);
        });
        ctx.setPacketHandled(true);
    }
}