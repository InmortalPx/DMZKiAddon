package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.api.KiAttackBuilder;
import com.dmzkiaddon.network.AddonNetworkHandler;
import com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType;
import com.dmzkiaddon.registry.AttackRegistry;
import com.dragonminez.common.network.NetworkHandler;
import com.dragonminez.common.network.S2C.StatsSyncS2C;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

public class CreateCustomAttackC2S {

    private final AttackType type;
    private final String name;
    private final float r, g, b;
    private final float powerMultiplier;

    public CreateCustomAttackC2S(AttackType type, String name, float r, float g, float b, float powerMultiplier) {
        this.type = type;
        this.name = name;
        this.r = r;
        this.g = g;
        this.b = b;
        this.powerMultiplier = powerMultiplier;
    }

    public CreateCustomAttackC2S(FriendlyByteBuf buffer) {
        this.type = buffer.readEnum(AttackType.class);
        this.name = buffer.readUtf();
        this.r = buffer.readFloat();
        this.g = buffer.readFloat();
        this.b = buffer.readFloat();
        this.powerMultiplier = buffer.readFloat();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
        buffer.writeUtf(name);
        buffer.writeFloat(r);
        buffer.writeFloat(g);
        buffer.writeFloat(b);
        buffer.writeFloat(powerMultiplier);
    }

    public static void handle(CreateCustomAttackC2S packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            String uuidPart = player.getUUID().toString().substring(0, 8);
            long count = AttackRegistry.ALL.stream()
                    .filter(e -> e.id().startsWith("custom_" + uuidPart))
                    .count();
            String uniqueId = "custom_" + uuidPart + "_" + count;

            int baseCost = (int) (40 * packet.powerMultiplier);
            int cooldown = (int) (100 * packet.powerMultiplier);
            
            KiAttackBuilder.create(packet.type, uniqueId, packet.name, 
                                   packet.r, packet.g, packet.b, 
                                   baseCost, cooldown, 
                                   true, true, packet.powerMultiplier);

            AttackRegistry.byId(uniqueId).ifPresent(entry -> {
                AddonNetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                        new SyncAttackRegistryS2C(entry));
            });

            StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(data -> {
                setSkillLevelThoroughly(data.getSkills(), uniqueId, 1);
                
                AddonNetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncSkillLevelS2C(uniqueId, 1));
                
                NetworkHandler.sendToTrackingEntityAndSelf(new StatsSyncS2C(player), player);
            });

            player.sendSystemMessage(Component.literal("§aCustom Ki Attack created: " + packet.name));
        });
        ctx.setPacketHandled(true);
    }
    
    private static void setSkillLevelThoroughly(Object skillsObj, String skillId, int level) {
        try {
            Method setMethod = skillsObj.getClass().getMethod("setSkillLevel", String.class, int.class);
            setMethod.invoke(skillsObj, skillId, level);
            Method getMethod = skillsObj.getClass().getMethod("getSkillLevel", String.class);
            if ((int) getMethod.invoke(skillsObj, skillId) == level) return;
        } catch (Exception ignored) {}

        String[] candidateFields = { "skills", "skillLevels", "learnedSkills", "skillMap", "data" };
        for (String fieldName : candidateFields) {
            try {
                Field f = getFieldFromHierarchy(skillsObj.getClass(), fieldName);
                if (f == null) continue;
                f.setAccessible(true);
                Object val = f.get(skillsObj);
                if (val instanceof Map map) {
                    map.put(skillId, level);
                    return;
                }
            } catch (Exception ignored) {}
        }
    }
    
    private static Field getFieldFromHierarchy(Class<?> clazz, String name) {
        while (clazz != null && clazz != Object.class) {
            try { return clazz.getDeclaredField(name); } 
            catch (NoSuchFieldException ignored) {}
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
