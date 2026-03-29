package com.dmzkiaddon.client;

import com.dmzkiaddon.config.AddonConfig;
import com.dmzkiaddon.registry.AttackRegistry.KiAttackEntry;
import com.dmzkiaddon.network.AddonNetworkHandler;
import com.dmzkiaddon.network.packets.FireKiAttackC2S;
import com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType;
import com.dmzkiaddon.network.packets.HakaiKeyPressC2S;
import com.dmzkiaddon.network.packets.InitiateHakaiC2S;
import com.dmzkiaddon.network.packets.LaunchHellzoneC2S;
import com.dmzkiaddon.network.packets.SpawnHellzoneC2S;
import com.dmzkiaddon.network.packets.ToggleKiShieldC2S;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.dmzkiaddon.client.ChargeEffects;
import com.dmzkiaddon.client.ScreenEffects;

import java.util.EnumMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class KeyHandler {

    private static final int MAX_CHARGE              = 100;
    private static final int MIN_CHARGE              = 10;
    private static final int MAX_GRENADES            = 8;
    private static final int HELLZONE_SPAWN_INTERVAL = 8;
    private static final int HELLZONE_MAX_HOLD       = MAX_GRENADES * HELLZONE_SPAWN_INTERVAL;

    private static final Map<AttackType, Integer> cooldownMap = new EnumMap<>(AttackType.class);

    public static int getCooldownFor(AttackType type) {
        return cooldownMap.getOrDefault(type, 0);
    }

    private static int chargeTick = 0;
    private static boolean prevFire     = false;
    private static boolean prevNext     = false;
    private static boolean prevPrev     = false;
    private static boolean prevKiShield = false;

    private static boolean prevHellzone   = false;
    private static int hellzoneHoldTick   = 0;
    private static int cooldownHellzone   = 0;
    private static boolean hellzoneActive = false;

    private static boolean prevHakaiSpam = false;
    private static boolean prevHakai     = false;
    private static int cooldownHakai     = 0;

    private static boolean prevTaiyoken = false;
    private static int cooldownTaiyoken = 0;

    private static boolean prevTimeSkip = false;
    private static int cooldownTimeSkip = 0;

    private static boolean prevPointPressure = false;
    private static int cooldownPointPressure = 0;

    private static boolean prevFinalExplosion   = false;
    private static int cooldownFinalExplosion   = 0;
    private static int finalExplosionHoldTick   = 0;
    private static boolean finalExplosionActive = false;
    private static final int FINAL_EXPLOSION_MAX_CHARGE = 80;

    private static boolean prevKikoho     = false;
    private static int cooldownKikoho     = 0;

    private static boolean prevNeoKikoho  = false;
    private static int cooldownNeoKikoho  = 0;
    private static int neoKikohoBurstTick = 0;
    private static int neoKikohoComboLocal = 0;
    private static final int NEO_KIKOHO_BURST_INTERVAL = 12;

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide()) return;
        ScreenEffects.resetAllEffects();
        chargeTick             = 0;
        hellzoneHoldTick       = 0;
        hellzoneActive         = false;
        finalExplosionHoldTick = 0;
        finalExplosionActive   = false;
        neoKikohoBurstTick     = 0;
        neoKikohoComboLocal    = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        Player player = mc.player;
        ScreenEffects.tick();

        for (AttackType type : AttackType.values()) {
            int cd = cooldownMap.getOrDefault(type, 0);
            if (cd > 0) cooldownMap.put(type, cd - 1);
        }
        if (cooldownHellzone > 0) cooldownHellzone--;
        if (cooldownHakai    > 0) cooldownHakai--;
        if (cooldownTaiyoken > 0) cooldownTaiyoken--;
        if (cooldownTimeSkip > 0) cooldownTimeSkip--;
        if (cooldownPointPressure > 0) cooldownPointPressure--;
        if (cooldownFinalExplosion > 0) cooldownFinalExplosion--;
        if (cooldownKikoho > 0) cooldownKikoho--;
        if (cooldownNeoKikoho > 0) cooldownNeoKikoho--;

        boolean curFire      = ClientSetup.KEY_FIRE.isDown();
        boolean curNext      = ClientSetup.KEY_ATTACK_NEXT.isDown();
        boolean curPrev      = ClientSetup.KEY_ATTACK_PREV.isDown();
        boolean curKiShield  = ClientSetup.KEY_KI_SHIELD.isDown();
        boolean curHellzone  = ClientSetup.KEY_HELLZONE.isDown();
        boolean curHakaiSpam = ClientSetup.KEY_HAKAI_SPAM.isDown();
        boolean curHakai     = ClientSetup.KEY_HAKAI.isDown();
        boolean curTaiyoken  = ClientSetup.KEY_TAIYOKEN.isDown();
        boolean curTimeSkip  = ClientSetup.KEY_TIME_SKIP.isDown();
        boolean curPointPressure   = ClientSetup.KEY_POINT_PRESSURE.isDown();
        boolean curFinalExplosion  = ClientSetup.KEY_FINAL_EXPLOSION.isDown();
        boolean curKikoho          = ClientSetup.KEY_KIKOHO.isDown();
        boolean curNeoKikoho       = ClientSetup.KEY_NEO_KIKOHO.isDown();

        if (curNext && !prevNext) {
            AttackSelector.selectNext();
            ScreenEffects.stopCharging();
            chargeTick = 0;
            showSelectedAttack(player);
        }
        if (curPrev && !prevPrev) {
            AttackSelector.selectPrev();
            ScreenEffects.stopCharging();
            chargeTick = 0;
            showSelectedAttack(player);
        }

        KiAttackEntry selected = AttackSelector.getSelected();
        if (selected != null) {
            int cd = getCooldownFor(selected.type());

            if (cd > 0 && curFire && !prevFire) {
                showCooldownMessage(player, selected.displayName(), cd);
            }

            if (cd == 0) {
                if (selected.isCharged()) {
                    if (curFire) {
                        chargeTick = Math.min(chargeTick + 1, MAX_CHARGE);
                        ScreenEffects.setCharging(true, (float) chargeTick / MAX_CHARGE,
                                selected.colorR(), selected.colorG(), selected.colorB(),
                                selected.displayName(),
                                (int) AddonConfig.getCostPercentage(selected.type()));
                        ChargeEffects.onChargeTick(chargeTick, MAX_CHARGE,
                                selected.colorR(), selected.colorG(), selected.colorB(),
                                selected.type());
                    }
                    if (!curFire && prevFire) {
                        if (chargeTick >= MIN_CHARGE) {
                            float chargeLevel = (float) chargeTick / MAX_CHARGE;
                            boolean hasKi = StatsProvider.get(StatsCapability.INSTANCE, player)
                                    .map(stats -> {
                                        int cost = (int)(stats.getMaxEnergy()
                                                * (AddonConfig.getCostPercentage(selected.type()) / 100f)
                                                * (1f + chargeLevel));
                                        return stats.getResources().getCurrentEnergy() >= cost;
                                    }).orElse(false);
                            if (hasKi) {
                                fireAttack(selected, chargeLevel);
                                cooldownMap.put(selected.type(), AddonConfig.getCooldown(selected.type()));
                            } else {
                                player.displayClientMessage(
                                        net.minecraft.network.chat.Component.literal("§cNo tienes suficiente Ki"),
                                        true);
                                ScreenEffects.stopCharging();
                            }
                        } else {
                            ScreenEffects.stopCharging();
                        }
                        chargeTick = 0;
                    }
                } else {
                    if (curFire && !prevFire) {
                        boolean hasKi = StatsProvider.get(StatsCapability.INSTANCE, player)
                                .map(stats -> {
                                    int cost = (int)(stats.getMaxEnergy()
                                            * (AddonConfig.getCostPercentage(selected.type()) / 100f)
                                            * 2f);
                                    return stats.getResources().getCurrentEnergy() >= cost;
                                }).orElse(false);
                        if (hasKi) {
                            fireAttack(selected, 1.0f);
                            cooldownMap.put(selected.type(), AddonConfig.getCooldown(selected.type()));
                        } else {
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal("§cNo tienes suficiente Ki"),
                                    true);
                        }
                    }
                }
            }
        }

        if (!curFire) chargeTick = 0;

        if (curKiShield && !prevKiShield) {
            AddonNetworkHandler.sendToServer(new ToggleKiShieldC2S());
        }

        if (curTaiyoken && !prevTaiyoken) {
            if (hasSkill(player, "addon_taiyoken")) {
                if (cooldownTaiyoken > 0) {
                    showCooldownMessage(player,
                            Component.translatable("attack.dmzkiaddon.taiyoken").getString(),
                            cooldownTaiyoken);
                } else {
                    AddonNetworkHandler.sendToServer(new FireKiAttackC2S(AttackType.TAIYOKEN, 1.0f));
                    cooldownTaiyoken = AddonConfig.getCooldown(AttackType.TAIYOKEN);
                    ScreenEffects.triggerFlash(1.0f, 20);
                    ScreenEffects.triggerShake(3, 6);
                }
            } else {
                player.displayClientMessage(
                        Component.translatable("msg.dmzkiaddon.taiyoken_no_skill")
                                .withStyle(s -> s.withColor(0xFFFF55)),
                        true
                );
            }
        }

        if (hasSkill(player, "addon_hellzone")) {
            if (curHellzone) {
                if (cooldownHellzone > 0) {
                    if (!prevHellzone) {
                        showCooldownMessage(player,
                                Component.translatable("attack.dmzkiaddon.hellzone").getString(),
                                cooldownHellzone);
                    }
                } else {
                    if (!prevHellzone) {
                        AddonNetworkHandler.sendToServer(new SpawnHellzoneC2S(getLockOnTargetId()));
                        hellzoneHoldTick = 0;
                        hellzoneActive = true;
                    }
                    if (hellzoneActive) {
                        hellzoneHoldTick++;
                        if (hellzoneHoldTick % HELLZONE_SPAWN_INTERVAL == 0) {
                            AddonNetworkHandler.sendToServer(new SpawnHellzoneC2S(getLockOnTargetId()));
                        }
                        float progress = Math.min(1.0f, (float) hellzoneHoldTick / HELLZONE_MAX_HOLD);
                        int hellzoneCostDisplay = StatsProvider.get(StatsCapability.INSTANCE, player)
                                .map(s -> (int)(s.getMaxEnergy() * (AddonConfig.HELLZONE_KI_COST_PCT.get() / 100f)))
                                .orElse(0);
                        ScreenEffects.setCharging(true, progress,
                                0.3f, 0.9f, 0.3f,
                                Component.translatable("attack.dmzkiaddon.hellzone").getString(),
                                hellzoneCostDisplay);
                    }
                }
            }

            if (!curHellzone && prevHellzone && hellzoneActive && cooldownHellzone == 0) {
                AddonNetworkHandler.sendToServer(new LaunchHellzoneC2S(getLockOnTargetId()));
                cooldownHellzone = AddonConfig.getCooldown(AttackType.HELLZONE);
                hellzoneHoldTick = 0;
                hellzoneActive = false;
                ScreenEffects.stopCharging();
                ScreenEffects.triggerShake(5, 10);
            }

            if (!curHellzone && prevHellzone && cooldownHellzone > 0) {
                hellzoneHoldTick = 0;
                hellzoneActive = false;
                ScreenEffects.stopCharging();
            }
        }

        if (curHakaiSpam && !prevHakaiSpam && ScreenEffects.isHakaiActive()) {
            AddonNetworkHandler.sendToServer(new HakaiKeyPressC2S());
        }

        if (curHakai && !prevHakai && !ScreenEffects.isHakaiActive()) {
            if (hasSkill(player, "addon_hakai")) {
                if (cooldownHakai > 0) {
                    showCooldownMessage(player,
                            Component.translatable("attack.dmzkiaddon.hakai").getString(),
                            cooldownHakai);
                } else {
                    int targetId = getLockOnTargetId();
                    if (targetId != -1) {
                        AddonNetworkHandler.sendToServer(new InitiateHakaiC2S(targetId));
                        cooldownHakai = AddonConfig.getCooldown(AttackType.HAKAI);
                    } else {
                        player.displayClientMessage(
                                Component.translatable("msg.dmzkiaddon.hakai_no_target")
                                        .withStyle(s -> s.withColor(0xAA00AA)),
                                true
                        );
                    }
                }
            }
        }

        if (curTimeSkip && !prevTimeSkip) {
            if (hasSkill(player, "addon_time_skip")) {
                if (cooldownTimeSkip > 0) {
                    showCooldownMessage(player,
                            Component.translatable("attack.dmzkiaddon.time_skip").getString(),
                            cooldownTimeSkip);
                } else {
                    AddonNetworkHandler.sendToServer(
                            new com.dmzkiaddon.network.packets.TimeSkipC2S());
                    cooldownTimeSkip = AddonConfig.getCooldown(AttackType.TIME_SKIP);
                    ScreenEffects.triggerTimeSkip();
                    ScreenEffects.triggerShake(3, 6);
                }
            } else {
                player.displayClientMessage(
                        Component.translatable("msg.dmzkiaddon.time_skip_no_skill")
                                .withStyle(s -> s.withColor(0xAA55FF)),
                        true);
            }
        }

        if (ScreenEffects.isPointPressureAuraActive()) {
            int ticks = ScreenEffects.getPointPressureAuraTicks();
            float auraProgress = (float) ticks / 18f;
            ChargeEffects.spawnPointPressureAura(player, auraProgress);
        }

        if (curPointPressure && !prevPointPressure) {
            if (hasSkill(player, "addon_point_pressure")) {
                if (cooldownPointPressure > 0) {
                    showCooldownMessage(player,
                            Component.translatable("attack.dmzkiaddon.point_pressure").getString(),
                            cooldownPointPressure);
                } else {
                    AddonNetworkHandler.sendToServer(
                            new com.dmzkiaddon.network.packets.PointPressureC2S());
                    cooldownPointPressure = AddonConfig.getCooldown(AttackType.POINT_PRESSURE);
                    ScreenEffects.triggerPointPressureAura();
                    ScreenEffects.triggerShake(5, 8);
                }
            } else {
                player.displayClientMessage(
                        Component.translatable("msg.dmzkiaddon.point_pressure_no_skill")
                                .withStyle(s -> s.withColor(0x44DDDD)),
                        true);
            }
        }

        if (hasSkill(player, "addon_final_explosion")) {
            if (curFinalExplosion) {
                if (cooldownFinalExplosion > 0) {
                    if (!prevFinalExplosion) {
                        showCooldownMessage(player,
                                Component.translatable("attack.dmzkiaddon.final_explosion").getString(),
                                cooldownFinalExplosion);
                    }
                } else {
                    if (!prevFinalExplosion) {
                        AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.FinalExplosionChargeC2S());
                        finalExplosionHoldTick = 0;
                        finalExplosionActive = true;
                        ScreenEffects.setFinalExplosionStats(0, 0);

                        com.dragonminez.common.stats.StatsProvider.get(
                                com.dragonminez.common.stats.StatsCapability.INSTANCE, player)
                                .ifPresent(stats -> {
                                    int vit = stats.getStats().getVitality();
                                    float currentHp = player.getHealth();
                                    float estimatedDmg = (currentHp * 2.0f) + (vit * 5.0f);
                                    ScreenEffects.setFinalExplosionStats(vit, estimatedDmg);
                                });
                    }

                    if (finalExplosionActive) {
                        finalExplosionHoldTick++;
                        float progress = (float) finalExplosionHoldTick / FINAL_EXPLOSION_MAX_CHARGE;

                        float shakeProgress = progress * progress * progress;
                        int shakeIntensity = 1 + (int)(shakeProgress * 7);
                        ScreenEffects.triggerShake(shakeIntensity, 3);

                        spawnFinalExplosionChargeParticles(player, progress);

                        ScreenEffects.setCharging(true, progress,
                                1.0f, 0.8f, 0.2f,
                                Component.translatable("attack.dmzkiaddon.final_explosion").getString(),
                                0);

                        if (finalExplosionHoldTick >= FINAL_EXPLOSION_MAX_CHARGE) {
                            AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.FinalExplosionC2S());
                            cooldownFinalExplosion = AddonConfig.getCooldown(AttackType.FINAL_EXPLOSION);
                            finalExplosionHoldTick = 0;
                            finalExplosionActive = false;
                            ScreenEffects.stopCharging();
                            ScreenEffects.triggerShake(10, 15);
                        }
                    }
                }
            }

            if (!curFinalExplosion && prevFinalExplosion && finalExplosionActive) {
                finalExplosionHoldTick = 0;
                finalExplosionActive = false;
                ScreenEffects.stopCharging();
                AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.FinalExplosionCancelC2S());
                player.displayClientMessage(
                        Component.literal("§6Final Explosion cancelado"),
                        true);
            }
        } else if (curFinalExplosion && !prevFinalExplosion) {
            player.displayClientMessage(
                    Component.translatable("msg.dmzkiaddon.final_explosion_no_skill")
                            .withStyle(s -> s.withColor(0xFF8800)),
                    true);
        }

        if (hasSkill(player, "addon_kikoho")) {
            if (curKikoho && !prevKikoho) {
                if (cooldownKikoho > 0) {
                    showCooldownMessage(player,
                            Component.translatable("attack.dmzkiaddon.kikoho").getString(),
                            cooldownKikoho);
                } else {
                    AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.KikohoC2S());
                    cooldownKikoho = AddonConfig.getCooldown(AttackType.KIKOHO);
                    ScreenEffects.triggerKikohoFlash();
                    ScreenEffects.triggerShake(8, 12);
                }
            }
        } else if (curKikoho && !prevKikoho) {
            player.displayClientMessage(
                    Component.translatable("msg.dmzkiaddon.kikoho_no_skill")
                            .withStyle(s -> s.withColor(0xFF8800)), true);
        }

        if (hasSkill(player, "addon_neo_kikoho")) {
            if (curNeoKikoho) {
                if (cooldownNeoKikoho > 0) {
                    if (!prevNeoKikoho) {
                        showCooldownMessage(player,
                                Component.translatable("attack.dmzkiaddon.neo_kikoho").getString(),
                                cooldownNeoKikoho);
                    }
                } else {
                    neoKikohoBurstTick++;
                    if (neoKikohoBurstTick >= NEO_KIKOHO_BURST_INTERVAL) {
                        neoKikohoBurstTick = 0;
                        AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.NeoKikohoC2S());
                        neoKikohoComboLocal++;
                        ScreenEffects.setNeoKikohoCombo(neoKikohoComboLocal);
                        ScreenEffects.triggerKikohoFlash();
                        ScreenEffects.triggerShake(3 + Math.min(neoKikohoComboLocal, 6), 6);
                    }
                }
            }

            if (!curNeoKikoho && prevNeoKikoho) {
                if (neoKikohoComboLocal > 0) {
                    cooldownNeoKikoho = AddonConfig.getCooldown(AttackType.NEO_KIKOHO);
                    AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.NeoKikohoResetC2S());
                }
                neoKikohoBurstTick  = 0;
                neoKikohoComboLocal = 0;
                ScreenEffects.resetNeoKikohoCombo();
            }
        } else if (curNeoKikoho && !prevNeoKikoho) {
            player.displayClientMessage(
                    Component.translatable("msg.dmzkiaddon.neo_kikoho_no_skill")
                            .withStyle(s -> s.withColor(0xFF8800)), true);
        }

        prevFire            = curFire;
        prevNext            = curNext;
        prevPrev            = curPrev;
        prevKiShield        = curKiShield;
        prevHellzone        = curHellzone;
        prevHakaiSpam       = curHakaiSpam;
        prevHakai           = curHakai;
        prevTaiyoken        = curTaiyoken;
        prevTimeSkip        = curTimeSkip;
        prevPointPressure   = curPointPressure;
        prevFinalExplosion  = curFinalExplosion;
        prevKikoho          = curKikoho;
        prevNeoKikoho       = curNeoKikoho;
    }

    private static void showCooldownMessage(Player player, String attackName, int cooldownTicks) {
        float secs = cooldownTicks / 20.0f;
        int cAttack = AddonConfig.getAttackNameColor();
        int cLabel  = AddonConfig.getLabelColor();
        int cTime   = AddonConfig.getTimeColor();
        player.displayClientMessage(
                Component.empty()
                        .append(Component.literal(attackName)
                                .withStyle(s -> s.withColor(cAttack)))
                        .append(Component.translatable("msg.dmzkiaddon.cooldown_label")
                                .withStyle(s -> s.withColor(cLabel)))
                        .append(Component.literal(String.format("%.1fs", secs))
                                .withStyle(s -> s.withColor(cTime))),
                true
        );
    }

    private static void fireAttack(KiAttackEntry entry, float chargeLevel) {
        AddonNetworkHandler.sendToServer(new FireKiAttackC2S(entry.type(), chargeLevel));
        switch (entry.type()) {
            case FINAL_KAMEHAMEHA               -> ScreenEffects.triggerShake(8, 12);
            case SPIRIT_BOMB, DEATH_BALL        -> ScreenEffects.triggerShake(7, 10);
            case KAMEHAMEHA, GALICK_GUN,
                 FINAL_FLASH, BIG_BANG          -> ScreenEffects.triggerShake(5, 8);
            case MAKANKOSAPPO, MASENKO          -> ScreenEffects.triggerShake(4, 6);
            case KI_DISC, DODOMPA, KI_LASER     -> ScreenEffects.triggerShake(2, 4);
            default -> {}
        }
        ScreenEffects.stopCharging();
    }

    private static void showSelectedAttack(Player player) {
        KiAttackEntry selected = AttackSelector.getSelected();
        if (selected == null) return;
        java.util.List<KiAttackEntry> learned = AttackSelector.getLearnedAttacks();
        int index = AttackSelector.getSelectedIndex() + 1;
        player.displayClientMessage(
                Component.empty()
                        .append(Component.translatable("msg.dmzkiaddon.attack_selected")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(selected.displayName())
                                .withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" [" + index + "/" + learned.size() + "]")
                                .withStyle(ChatFormatting.DARK_GRAY)),
                true
        );
    }

    private static boolean hasSkill(Player player, String skillId) {
        boolean[] result = {false};
        StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {
            var skill = stats.getSkills().getSkill(skillId);
            result[0] = skill != null && skill.getLevel() > 0;
        });
        return result[0];
    }

    private static int getLockOnTargetId() {
        return LockOnBridge.getTargetId();
    }

    private static void spawnFinalExplosionChargeParticles(Player player, float progress) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) return;

        int sparkCount = 2 + (int)(progress * progress * 18);
        for (int i = 0; i < sparkCount; i++) {
            double ox = (Math.random() - 0.5) * 1.5 * progress;
            double oy = Math.random() * 2.5;
            double oz = (Math.random() - 0.5) * 1.5 * progress;
            double vx = (Math.random() - 0.5) * 0.15;
            double vy = 0.05 + Math.random() * 0.2;
            double vz = (Math.random() - 0.5) * 0.15;
            mc.level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    vx, vy, vz);
        }

        int smokeCount = 1 + (int)(progress * 8);
        for (int i = 0; i < smokeCount; i++) {
            double ox = (Math.random() - 0.5) * 0.6;
            double oy = 0.5 + Math.random() * 2.0;
            double oz = (Math.random() - 0.5) * 0.6;
            mc.level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    0, 0.04, 0);
        }

        if (progress > 0.4f) {
            int fireCount = (int)(progress * 10);
            long time = player.level().getGameTime();
            for (int i = 0; i < fireCount; i++) {
                double angle  = (Math.PI * 2.0 / Math.max(fireCount, 1)) * i + time * 0.12;
                double radius = 0.6 + progress * 1.8;
                mc.level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + 0.1 + Math.random() * 0.4,
                        player.getZ() + Math.sin(angle) * radius,
                        -Math.sin(angle) * 0.04, 0.03, Math.cos(angle) * 0.04);
            }
        }
    }
}