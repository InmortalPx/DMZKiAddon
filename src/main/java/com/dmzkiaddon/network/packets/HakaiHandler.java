package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.DMZKiAddon;
import com.dmzkiaddon.entity.HakaiOrbEntity;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.Collections;

@Mod.EventBusSubscriber(modid = "dmzkiaddon", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HakaiHandler {

    private static final int HAKAI_ASCENDED_LEVEL = 1000;
    private static final int ORB_COUNT            = 6;
    private static final int NPC_HAKAI_DURATION   = 100;
    public static final float BAR_MAX = 1.0f;

    private static final Map<UUID, NpcHakaiData>      NPC_HAKAI    = new HashMap<>();
    private static final Map<UUID, PlayerHakaiData>    PLAYER_HAKAI = new HashMap<>();
    private static final Map<UUID, List<HakaiOrbEntity>> HAKAI_ORBS = new HashMap<>();

    private static class NpcHakaiData {
        LivingEntity target;
        int  tick         = 0;
        boolean lowLevel  = false;
        NpcHakaiData(LivingEntity t, boolean low) { target = t; lowLevel = low; }
    }

    public static class PlayerHakaiData {
        public UUID  attackerId;
        public UUID  defenderId;
        public float attackerProgress      = 0f;
        public float defenderProgress      = 0f;
        public float attackerSpeedPerPress;
        public float defenderSpeedPerPress;
        public boolean finished            = false;
        public boolean attackerLowLevel    = false;

        PlayerHakaiData(UUID att, UUID def, float aBP, float dBP, boolean lowLevel) {
            this.attackerId       = att;
            this.defenderId       = def;
            this.attackerLowLevel = lowLevel;
            float total = aBP + dBP;
            if (total <= 0) total = 1;
            this.attackerSpeedPerPress = Math.max(0.04f, (aBP / total) * 0.12f);
            this.defenderSpeedPerPress = Math.max(0.04f, (dBP / total) * 0.12f);
        }
    }

    public static void startNpcHakai(ServerPlayer attacker, LivingEntity target) {
        if (target == null || !target.isAlive()) return;

        boolean lowLevel = getLevel(attacker) < HAKAI_ASCENDED_LEVEL;

        if (lowLevel) {
            spawnHakaiOrbs(attacker, target);
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    NPC_HAKAI_DURATION + 20, 255, false, false));
        }

        NPC_HAKAI.put(attacker.getUUID(), new NpcHakaiData(target, lowLevel));
    }

    public static boolean startPlayerHakai(ServerPlayer attacker, ServerPlayer defender) {
        if (defender == null || !defender.isAlive()) return false;
        if (PLAYER_HAKAI.containsKey(attacker.getUUID())) return false;
        if (PLAYER_HAKAI.containsKey(defender.getUUID())) return false;

        float[] aBP = {1000f}, dBP = {1000f};
        StatsProvider.get(StatsCapability.INSTANCE, (Entity) attacker)
                .ifPresent(s -> aBP[0] = (float) s.getBattlePower());
        StatsProvider.get(StatsCapability.INSTANCE, (Entity) defender)
                .ifPresent(s -> dBP[0] = (float) s.getBattlePower());

        boolean lowLevel = getLevel(attacker) < HAKAI_ASCENDED_LEVEL;

        PlayerHakaiData data = new PlayerHakaiData(
                attacker.getUUID(), defender.getUUID(), aBP[0], dBP[0], lowLevel);

        PLAYER_HAKAI.put(attacker.getUUID(), data);
        PLAYER_HAKAI.put(defender.getUUID(), data);
        HakaiUpdateS2C.send(attacker, data);
        HakaiUpdateS2C.send(defender, data);
        return true;
    }

    public static void registerKeyPress(ServerPlayer player) {
        PlayerHakaiData data = PLAYER_HAKAI.get(player.getUUID());
        if (data == null || data.finished) return;

        boolean isAttacker = player.getUUID().equals(data.attackerId);
        if (isAttacker) data.attackerProgress = Math.min(BAR_MAX, data.attackerProgress + data.attackerSpeedPerPress);
        else            data.defenderProgress = Math.min(BAR_MAX, data.defenderProgress + data.defenderSpeedPerPress);

        ServerPlayer att = player.getServer().getPlayerList().getPlayer(data.attackerId);
        ServerPlayer def = player.getServer().getPlayerList().getPlayer(data.defenderId);
        if (att != null) HakaiUpdateS2C.send(att, data);
        if (def != null) HakaiUpdateS2C.send(def, data);

        if      (data.attackerProgress >= BAR_MAX) finishMinigame(data, true,  player.getServer());
        else if (data.defenderProgress >= BAR_MAX)  finishMinigame(data, false, player.getServer());
    }

    public static boolean isInMinigame(UUID playerId) {
        return PLAYER_HAKAI.containsKey(playerId);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Set<UUID> remove = new HashSet<>();
        for (Map.Entry<UUID, NpcHakaiData> e : NPC_HAKAI.entrySet()) {
            NpcHakaiData d = e.getValue();
            d.tick++;

            if (d.target == null || !d.target.isAlive()) { remove.add(e.getKey()); continue; }
            if (!(d.target.level() instanceof ServerLevel sl)) continue;

            if (d.tick % 2 == 0) spawnDivineParticles(sl, d.target);

            if (d.tick >= NPC_HAKAI_DURATION) {
                if (d.lowLevel) {
                    convergeOrbs(e.getKey(), d.target, 30f);
                } else {
                    executeHakai(d.target);
                }
                remove.add(e.getKey());
            }
        }
        NPC_HAKAI.keySet().removeAll(remove);

        Set<PlayerHakaiData> processed = new HashSet<>();
        for (PlayerHakaiData d : PLAYER_HAKAI.values()) {
            if (processed.contains(d) || d.finished) continue;
            processed.add(d);
            if (event.getServer().getTickCount() % 5 == 0) {
                d.attackerProgress = Math.max(0, d.attackerProgress - 0.005f);
                d.defenderProgress = Math.max(0, d.defenderProgress - 0.005f);
            }
        }
    }

    private static void spawnHakaiOrbs(ServerPlayer attacker, LivingEntity target) {
        if (!(attacker.level() instanceof ServerLevel sl)) return;

        List<HakaiOrbEntity> orbs = new ArrayList<>();
        for (int i = 0; i < ORB_COUNT; i++) {
            HakaiOrbEntity orb = new HakaiOrbEntity(sl, attacker);
            orb.setOrbitParams(i, ORB_COUNT, target);
            orb.setPos(attacker.getX(), attacker.getY() + 1.5, attacker.getZ());
            sl.addFreshEntity(orb);
            orbs.add(orb);
        }
        HAKAI_ORBS.put(attacker.getUUID(), orbs);
    }

    private static void convergeOrbs(UUID attackerId, LivingEntity target, float damage) {
        List<HakaiOrbEntity> orbs = HAKAI_ORBS.remove(attackerId);
        if (orbs == null) return;
        float dmgPerOrb = damage / orbs.size();
        for (HakaiOrbEntity orb : orbs) {
            if (orb.isAlive()) orb.converge(dmgPerOrb);
        }
    }

    private static final Set<UUID> HAKAI_IN_PROGRESS = Collections.synchronizedSet(new HashSet<>());

    static void executeHakai(LivingEntity target) {
        if (target == null) return;
        if (HAKAI_IN_PROGRESS.contains(target.getUUID())) return;
        HAKAI_IN_PROGRESS.add(target.getUUID());

        try {
            target.setAbsorptionAmount(0f);
            target.invulnerableTime = 0;
            target.removeAllEffects();

            try {
                var maxHealthAttr = target.getAttribute(
                        net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                if (maxHealthAttr != null) {
                    maxHealthAttr.removeModifiers();
                    maxHealthAttr.setBaseValue(1.0);
                }
            } catch (Exception ignored) {}

            try {
                net.minecraft.nbt.CompoundTag persistData = target.getPersistentData();
                for (String key : new String[]{
                        "IsInvulnerable", "GodMode", "TitanShield", "TitanArmor",
                        "TitanSoul", "SpiritMode", "GodslayerImmune", "NightmareMode",
                        "InvulnerabilityFlag", "BossShield", "UltimateDef"}) {
                    persistData.remove(key);
                }
                net.minecraft.nbt.CompoundTag entityNbt = new net.minecraft.nbt.CompoundTag();
                target.save(entityNbt);
                entityNbt.remove("Invulnerable");
                entityNbt.putBoolean("Invulnerable", false);
            } catch (Exception ignored) {}

            if (target instanceof Player player) {
                player.getAbilities().invulnerable = false;
                player.onUpdateAbilities();
                net.minecraft.nbt.CompoundTag data = player.getPersistentData();
                data.remove("TotemUsed");
                data.remove("IsInvulnerable");
                data.remove("GodMode");
                if (player.isSpectator() && player instanceof ServerPlayer sp) {
                    sp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                }
            }

            try {
                target.setNoGravity(true);
                target.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                target.hurtMarked = false;
            } catch (Exception ignored) {}

            forceSetHealthField(target, 0.0f);
            forceSetHealthReflectiveDeep(target, 0.0f);

            if (target.isAlive()) {
                target.hurt(target.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
            }
            if (target.isAlive()) {
                target.kill();
            }
            if (target.isAlive() || !target.isRemoved()) {
                target.remove(Entity.RemovalReason.KILLED);
            }

            if (target.level() instanceof ServerLevel sl) {
                double range = Math.max(target.getBbWidth() * 4.0, 32.0);
                List<LivingEntity> linked = sl.getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(range),
                        e -> e != target
                          && !HAKAI_IN_PROGRESS.contains(e.getUUID())
                          && isTitanRelated(e, target));

                for (LivingEntity spirit : linked) {
                    HAKAI_IN_PROGRESS.add(spirit.getUUID());
                    try {
                        spirit.setAbsorptionAmount(0f);
                        spirit.invulnerableTime = 0;
                        spirit.removeAllEffects();
                        forceSetHealthField(spirit, 0.0f);
                        forceSetHealthReflectiveDeep(spirit, 0.0f);
                        if (spirit.isAlive()) spirit.hurt(sl.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                        if (spirit.isAlive()) spirit.kill();
                        spirit.remove(Entity.RemovalReason.KILLED);
                    } finally {
                        HAKAI_IN_PROGRESS.remove(spirit.getUUID());
                    }
                }
            }

        } finally {
            HAKAI_IN_PROGRESS.remove(target.getUUID());
        }
    }

    private static void forceSetHealthField(LivingEntity target, float value) {
        try {
            Class<?> clazz = LivingEntity.class;
            java.lang.reflect.Field healthField = null;

            for (String fname : new String[]{"f_20626_", "health", "entityHealth"}) {
                try { healthField = clazz.getDeclaredField(fname); break; }
                catch (NoSuchFieldException ignored) {}
            }

            if (healthField == null) {
                for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                    if (f.getType() == float.class) {
                        f.setAccessible(true);
                        float current = (float) f.get(target);
                        if (current > 0 && current <= target.getMaxHealth() + 1) {
                            healthField = f;
                            break;
                        }
                    }
                }
            }

            if (healthField != null) {
                healthField.setAccessible(true);
                healthField.set(target, value);
            }
        } catch (Exception e) {
            DMZKiAddon.LOGGER.warn("[Hakai] forceSetHealthField falló en {}: {}",
                    target.getClass().getSimpleName(), e.getMessage());
        }
    }

    private static void forceSetHealthReflectiveDeep(LivingEntity target, float value) {
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != LivingEntity.class && clazz != Object.class) {
            for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                String name = f.getName().toLowerCase();
                if (f.getType() == float.class &&
                    (name.contains("health") || name.contains("hp") || name.contains("life"))) {
                    try {
                        f.setAccessible(true);
                        float current = (float) f.get(target);
                        if (current > 0) {
                            f.set(target, value);
                        }
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    private static boolean isTitanRelated(LivingEntity entity, LivingEntity titan) {
        String entityPkg = entity.getClass().getPackageName();
        String titanPkg  = titan.getClass().getPackageName();

        if (!titanPkg.isEmpty() && !entityPkg.isEmpty() && entityPkg.equals(titanPkg)) return true;

        String entityClass = entity.getClass().getName().toLowerCase();
        if (entityClass.contains("spirit") || entityClass.contains("soul")
                || entityClass.contains("titan") || entityClass.contains("god")) {
            return titan.distanceTo(entity) < 32.0;
        }

        return false;
    }

    private static void finishMinigame(PlayerHakaiData data, boolean attackerWon,
                                        net.minecraft.server.MinecraftServer server) {
        data.finished = true;
        ServerPlayer att  = server.getPlayerList().getPlayer(data.attackerId);
        ServerPlayer def  = server.getPlayerList().getPlayer(data.defenderId);
        ServerPlayer loser = attackerWon ? def : att;

        if (loser != null && loser.isAlive()) {
            if (loser.level() instanceof ServerLevel sl) spawnDivineParticles(sl, loser);

            if (data.attackerLowLevel) {
                if (att != null && loser.level() instanceof ServerLevel sl) {
                    for (int i = 0; i < ORB_COUNT; i++) {
                        HakaiOrbEntity orb = new HakaiOrbEntity(sl, att);
                        orb.setOrbitParams(i, ORB_COUNT, loser);
                        orb.setPos(loser.getX() + Math.cos(i) * 2.5,
                                loser.getY() + 1.0,
                                loser.getZ() + Math.sin(i) * 2.5);
                        orb.converge(50f); 
                        sl.addFreshEntity(orb);
                    }
                }
            } else {
                executeHakai(loser);
            }
        }

        if (att != null) { PLAYER_HAKAI.remove(att.getUUID()); HakaiUpdateS2C.sendFinished(att, attackerWon); }
        if (def != null) { PLAYER_HAKAI.remove(def.getUUID()); HakaiUpdateS2C.sendFinished(def, !attackerWon); }
    }

    private static int getLevel(ServerPlayer player) {
        int[] level = {0};
        StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {
            level[0] = stats.getLevel();
        });
        return level[0];
    }

    private static void spawnDivineParticles(ServerLevel level, LivingEntity entity) {
        double cx = entity.getX(), cy = entity.getY() + entity.getBbHeight() * 0.5, cz = entity.getZ();
        double r  = entity.getBbWidth() * 0.8;

        net.minecraft.core.particles.DustParticleOptions purple =
                new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.55f, 0.0f, 1.0f), 1.2f);
        net.minecraft.core.particles.DustParticleOptions bright =
                new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.7f, 0.2f, 1.0f), 1.0f);

        for (int i = 0; i < 6; i++) {
            double angle = Math.random() * Math.PI * 2;
            level.sendParticles(purple,
                    cx + Math.cos(angle) * r, cy + (Math.random() - 0.5) * entity.getBbHeight(),
                    cz + Math.sin(angle) * r, 1, 0, 0.05, 0, 0.01);
        }
        for (int i = 0; i < 3; i++) {
            level.sendParticles(bright,
                    cx + (Math.random() - 0.5) * r,
                    entity.getY() + Math.random() * entity.getBbHeight(),
                    cz + (Math.random() - 0.5) * r, 1, 0, 0.08, 0, 0.01);
        }
    }
}