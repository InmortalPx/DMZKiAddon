package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.client.ScreenEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ImpactShakeS2C {

    private final int intensity;
    private final int duration;

    public ImpactShakeS2C(int intensity, int duration) {
        this.intensity = intensity;
        this.duration  = duration;
    }

    public ImpactShakeS2C(FriendlyByteBuf buf) {
        this.intensity = buf.readInt();
        this.duration  = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(intensity);
        buf.writeInt(duration);
    }

    public static void handle(ImpactShakeS2C pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                ScreenEffects.triggerShake(pkt.intensity, pkt.duration)
            )
        );
        ctx.get().setPacketHandled(true);
    }
}