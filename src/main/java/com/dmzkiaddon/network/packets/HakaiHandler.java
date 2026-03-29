package com.dmzkiaddon.network.packets;

import com.dragonminez.common.init.MainParticles;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = "dmzkiaddon")
public class HakaiHandler {

    // ===== HAKAI vs NPC =====
    // UUID del jugador → entidad que está siendo "hakaiada" + tick contador
    private static final Map<UUID, NpcHakaiData> NPC_HAKAI = new HashMap<>();

    // ===== HAKAI vs JUGADOR (minijuego) =====
    // Ambos jugadores deben estar en el mapa para que el minijuego esté activo
    private static final Map<UUID, PlayerHakaiData> PLAYER_HAKAI = new HashMap<>();

    // Duracion del hakai en NPC (ticks)
    private static final int NPC_HAKAI_DURATION = 100; // 5 segundos

    // Longitud de la barra (0.0 a 1.0 cada lado)
    public static final float BAR_MAX = 1.0f;

    // ── NPC DATA ─────────────────────────────────────────────────────────────
    private static class NpcHakaiData {
        LivingEntity target;
        int tick = 0;
        NpcHakaiData(LivingEntity target) { this.target = target; }
    }

    // ── PLAYER DATA ──────────────────────────────────────────────────────────
    public static class PlayerHakaiData {
        public UUID attackerId;
        public UUID defenderId;
        public float attackerProgress = 0f;
        public float defenderProgress = 0f;
        public float attackerSpeedPerPress; // cuanto avanza por tecla
        public float defenderSpeedPerPress;
        public boolean finished = false;

        PlayerHakaiData(UUID attackerId, UUID defenderId,
                        float attackerBP, float defenderBP) {
            this.attackerId = attackerId;
            this.defenderId = defenderId;
            // BP mas alto = mas avance por press. Minimo garantizado 0.04
            float totalBP = attackerBP + defenderBP;
            if (totalBP <= 0) totalBP = 1;
            this.attackerSpeedPerPress = Math.max(0.04f, (attackerBP / totalBP) * 0.12f);
            this.defenderSpeedPerPress = Math.max(0.04f, (defenderBP / totalBP) * 0.12f);
        }
    }

    // ===== API PUBLICA =======================================================

    /**
     * Iniciar Hakai vs NPC (requiere lock-on).
     */
    public static void startNpcHakai(ServerPlayer attacker, LivingEntity target) {
        if (target == null || !target.isAlive()) return;

        // Congelar el NPC: Slowness 255 + NoAI via tag
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
            NPC_HAKAI_DURATION + 20, 255, false, false));

        UUID id = attacker.getUUID();
        NPC_HAKAI.put(id, new NpcHakaiData(target));
    }

    /**
     * Iniciar Hakai vs jugador (minijuego).
     * Retorna false si el defender no existe o ya está en un minijuego.
     */
    public static boolean startPlayerHakai(ServerPlayer attacker, ServerPlayer defender) {
        if (defender == null || !defender.isAlive()) return false;
        if (PLAYER_HAKAI.containsKey(attacker.getUUID())) return false;
        if (PLAYER_HAKAI.containsKey(defender.getUUID())) return false;

        float[] attackerBP = {1000f};
        float[] defenderBP = {1000f};

        StatsProvider.get(StatsCapability.INSTANCE, (Entity) attacker).ifPresent(s ->
            attackerBP[0] = (float) s.getBattlePower());
        StatsProvider.get(StatsCapability.INSTANCE, (Entity) defender).ifPresent(s ->
            defenderBP[0] = (float) s.getBattlePower());

        PlayerHakaiData data = new PlayerHakaiData(
            attacker.getUUID(), defender.getUUID(),
            attackerBP[0], defenderBP[0]);

        PLAYER_HAKAI.put(attacker.getUUID(), data);
        PLAYER_HAKAI.put(defender.getUUID(), data);

        // Notificar a ambos clientes que empiece el minijuego
        HakaiUpdateS2C.send(attacker, data);
        HakaiUpdateS2C.send(defender, data);

        return true;
    }

    /**
     * Registrar press de tecla durante minijuego.
     */
    public static void registerKeyPress(ServerPlayer player) {
        PlayerHakaiData data = PLAYER_HAKAI.get(player.getUUID());
        if (data == null || data.finished) return;

        boolean isAttacker = player.getUUID().equals(data.attackerId);
        if (isAttacker) {
            data.attackerProgress = Math.min(BAR_MAX, data.attackerProgress + data.attackerSpeedPerPress);
        } else {
            data.defenderProgress = Math.min(BAR_MAX, data.defenderProgress + data.defenderSpeedPerPress);
        }

        // Enviar actualizacion a ambos
        ServerPlayer attacker = player.getServer().getPlayerList().getPlayer(data.attackerId);
        ServerPlayer defender = player.getServer().getPlayerList().getPlayer(data.defenderId);
        if (attacker != null) HakaiUpdateS2C.send(attacker, data);
        if (defender != null) HakaiUpdateS2C.send(defender, data);

        // Verificar si alguien ganó
        if (data.attackerProgress >= BAR_MAX) {
            finishMinigame(data, true, player.getServer());
        } else if (data.defenderProgress >= BAR_MAX) {
            finishMinigame(data, false, player.getServer());
        }
    }

    public static boolean isInMinigame(UUID playerId) {
        return PLAYER_HAKAI.containsKey(playerId);
    }

    // ===== TICK SERVIDOR =====================================================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // ── Hakai vs NPC ──────────────────────────────────────────────────────
        Set<UUID> toRemoveNpc = new HashSet<>();
        for (Map.Entry<UUID, NpcHakaiData> entry : NPC_HAKAI.entrySet()) {
            NpcHakaiData data = entry.getValue();
            data.tick++;

            if (data.target == null || !data.target.isAlive()) {
                toRemoveNpc.add(entry.getKey());
                continue;
            }

            if (!(data.target.level() instanceof ServerLevel sl)) continue;

            // Particulas divinas moradas alrededor del NPC
            if (data.tick % 2 == 0) {
                spawnDivineParticles(sl, data.target);
            }

            // Instakill al llegar al tick final
            if (data.tick >= NPC_HAKAI_DURATION) {
                data.target.hurt(data.target.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                toRemoveNpc.add(entry.getKey());
            }
        }
        NPC_HAKAI.keySet().removeAll(toRemoveNpc);

        // ── Decaimiento pasivo de la barra en minijuego ───────────────────────
        // La barra baja muy ligeramente si no presionas (presion para ambos lados)
        Set<PlayerHakaiData> processed = new HashSet<>();
        for (PlayerHakaiData data : PLAYER_HAKAI.values()) {
            if (processed.contains(data) || data.finished) continue;
            processed.add(data);
            // Decaer levemente hacia el centro cada 5 ticks
            if (event.getServer().getTickCount() % 5 == 0) {
                data.attackerProgress = Math.max(0, data.attackerProgress - 0.005f);
                data.defenderProgress = Math.max(0, data.defenderProgress - 0.005f);
            }
        }
    }

    // ===== INTERNOS ==========================================================

    private static void finishMinigame(PlayerHakaiData data, boolean attackerWon,
                                        net.minecraft.server.MinecraftServer server) {
        data.finished = true;

        ServerPlayer attacker = server.getPlayerList().getPlayer(data.attackerId);
        ServerPlayer defender = server.getPlayerList().getPlayer(data.defenderId);
        ServerPlayer loser    = attackerWon ? defender : attacker;

        if (loser != null && loser.isAlive()) {
            // Particulas divinas sobre el perdedor antes del instakill
            if (loser.level() instanceof ServerLevel sl) {
                spawnDivineParticles(sl, loser);
            }
            // Instakill
            loser.hurt(loser.level().damageSources().fellOutOfWorld(), Float.MAX_VALUE);
        }

        // Limpiar datos de ambos
        if (attacker != null) {
            PLAYER_HAKAI.remove(attacker.getUUID());
            HakaiUpdateS2C.sendFinished(attacker, attackerWon);
        }
        if (defender != null) {
            PLAYER_HAKAI.remove(defender.getUUID());
            HakaiUpdateS2C.sendFinished(defender, !attackerWon);
        }
    }

    private static void spawnDivineParticles(ServerLevel level, LivingEntity entity) {
        double cx = entity.getX();
        double cy = entity.getY() + entity.getBbHeight() * 0.5;
        double cz = entity.getZ();
        double r  = entity.getBbWidth() * 0.8;

        // DustParticleOptions acepta RGB — morado divino
        net.minecraft.core.particles.DustParticleOptions divineColor =
            new net.minecraft.core.particles.DustParticleOptions(
                new org.joml.Vector3f(0.55f, 0.0f, 1.0f), 1.2f);
        net.minecraft.core.particles.DustParticleOptions divineBright =
            new net.minecraft.core.particles.DustParticleOptions(
                new org.joml.Vector3f(0.7f, 0.2f, 1.0f), 1.0f);

        for (int i = 0; i < 6; i++) {
            double angle = Math.random() * Math.PI * 2;
            double px = cx + Math.cos(angle) * r;
            double py = cy + (Math.random() - 0.5) * entity.getBbHeight();
            double pz = cz + Math.sin(angle) * r;
            level.sendParticles(divineColor, px, py, pz, 1, 0, 0.05, 0, 0.01);
        }

        for (int i = 0; i < 3; i++) {
            level.sendParticles(divineBright,
                cx + (Math.random() - 0.5) * r,
                entity.getY() + Math.random() * entity.getBbHeight(),
                cz + (Math.random() - 0.5) * r,
                1, 0, 0.08, 0, 0.01);
        }
    }
}