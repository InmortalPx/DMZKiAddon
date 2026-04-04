package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType;
import com.dmzkiaddon.registry.AttackRegistry;
import com.dmzkiaddon.registry.AttackRegistry.KiAttackEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncAttackRegistryS2C {

    private final KiAttackEntry entry;

    public SyncAttackRegistryS2C(KiAttackEntry entry) {
        this.entry = entry;
    }

    public SyncAttackRegistryS2C(FriendlyByteBuf buffer) {
        this.entry = new KiAttackEntry(
                buffer.readEnum(AttackType.class),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readFloat()
        );
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(entry.type());
        buffer.writeUtf(entry.id());
        buffer.writeUtf(entry.displayName());
        buffer.writeFloat(entry.colorR());
        buffer.writeFloat(entry.colorG());
        buffer.writeFloat(entry.colorB());
        buffer.writeInt(entry.baseCost());
        buffer.writeInt(entry.cooldownTicks());
        buffer.writeBoolean(entry.isCharged());
        buffer.writeBoolean(entry.requiresSkill());
        buffer.writeBoolean(entry.isSpecial());
        buffer.writeFloat(entry.powerMultiplier());
    }

    public static void handle(SyncAttackRegistryS2C packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            AttackRegistry.register(packet.entry);
        });
        ctx.setPacketHandled(true);
    }
}
