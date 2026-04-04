package com.dmzkiaddon.network.packets;

import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSkillLevelS2C {

    private final String skillId;
    private final int level;

    public SyncSkillLevelS2C(String skillId, int level) {
        this.skillId = skillId;
        this.level = level;
    }

    public SyncSkillLevelS2C(FriendlyByteBuf buffer) {
        this.skillId = buffer.readUtf();
        this.level = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(skillId);
        buffer.writeInt(level);
    }

    public static void handle(SyncSkillLevelS2C packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            distHandle(packet.skillId, packet.level);
        });
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void distHandle(String skillId, int level) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            StatsProvider.get(StatsCapability.INSTANCE, mc.player).ifPresent(stats -> {
                stats.getSkills().setSkillLevel(skillId, level);
            });
        }
    }
}
