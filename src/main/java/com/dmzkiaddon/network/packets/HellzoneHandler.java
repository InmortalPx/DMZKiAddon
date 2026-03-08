package com.dmzkiaddon.network.packets;

import com.dmzkiaddon.compat.KiGriefingHelper;
import com.dmzkiaddon.entity.HellzoneGrenadeEntity;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class HellzoneHandler {

    private static final int MAX_GRENADES = 8;

    private static final Map<UUID, List<HellzoneGrenadeEntity>> ORBITING     = new HashMap<>();
    private static final Map<UUID, LivingEntity>                LOCK_TARGETS = new HashMap<>();

    public static void spawnGrenade(ServerPlayer player, float damage, LivingEntity lockTarget) {
        UUID id = player.getUUID();
        ORBITING.putIfAbsent(id, new ArrayList<>());
        List<HellzoneGrenadeEntity> list = ORBITING.get(id);

        // Remove dead grenades
        list.removeIf(g -> !g.isAlive());

        if (list.size() >= MAX_GRENADES) return;

        // Deduct Ki cost (5 por granada)
        boolean[] hasKi = {false};
        StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {
            int energy = stats.getResources().getCurrentEnergy();
            int cost = 5;
            if (energy < cost) return;
            stats.getResources().setCurrentEnergy(energy - cost);
            hasKi[0] = true;
        });
        if (!hasKi[0]) return;

        if (lockTarget != null) LOCK_TARGETS.put(id, lockTarget);
        LivingEntity orbitCenter = LOCK_TARGETS.getOrDefault(id, player);

        HellzoneGrenadeEntity grenade = new HellzoneGrenadeEntity(player.level(), player);
        grenade.setKiDamage(damage);
        grenade.setPos(player.getX(), player.getY() + 1.0, player.getZ());
        grenade.setOrbitParams(list.size(), list.size() + 1, 2.5f, orbitCenter);
        player.level().addFreshEntity(grenade);
        list.add(grenade);

        // Re-distribuir los ángulos de órbita
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setOrbitParams(i, list.size(), 2.5f, orbitCenter);
        }
    }

    public static void launch(ServerPlayer player, float damage, LivingEntity lockTarget) {
        UUID id = player.getUUID();
        List<HellzoneGrenadeEntity> list = ORBITING.getOrDefault(id, Collections.emptyList());
        list.removeIf(g -> !g.isAlive());

        LivingEntity target = lockTarget != null ? lockTarget : findNearestTarget(player);
        if (target == null) {
            cancel(player);
            return;
        }

        Level level = player.level();

        for (HellzoneGrenadeEntity grenade : list) {
            // Pasar el modo de explosión según el gamerule del DMZ
            Level.ExplosionInteraction explosionMode = KiGriefingHelper.getExplosionMode(
                    level, grenade.getX(), grenade.getY(), grenade.getZ(), player);
            grenade.setExplosionInteraction(explosionMode);
            grenade.converge(target, damage);
        }

        list.clear();
        ORBITING.remove(id);
        LOCK_TARGETS.remove(id);
    }

    public static void cancel(ServerPlayer player) {
        UUID id = player.getUUID();
        List<HellzoneGrenadeEntity> list = ORBITING.getOrDefault(id, Collections.emptyList());
        for (HellzoneGrenadeEntity g : list) g.discard();
        list.clear();
        ORBITING.remove(id);
        LOCK_TARGETS.remove(id);
    }

    public static boolean hasGrenades(ServerPlayer player) {
        List<HellzoneGrenadeEntity> list = ORBITING.get(player.getUUID());
        return list != null && !list.isEmpty() && list.stream().anyMatch(HellzoneGrenadeEntity::isAlive);
    }

    public static int getCount(ServerPlayer player) {
        List<HellzoneGrenadeEntity> list = ORBITING.get(player.getUUID());
        if (list == null) return 0;
        list.removeIf(g -> !g.isAlive());
        return list.size();
    }

    private static LivingEntity findNearestTarget(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(20.0);
        List<LivingEntity> candidates = player.level().getEntitiesOfClass(
                LivingEntity.class, area,
                e -> !e.equals(player) && e.isAlive());

        LivingEntity nearest = null;
        double minDist = Double.MAX_VALUE;
        for (LivingEntity e : candidates) {
            double dist = player.distanceTo(e);
            if (dist < minDist) {
                minDist = dist;
                nearest = e;
            }
        }
        return nearest;
    }
}