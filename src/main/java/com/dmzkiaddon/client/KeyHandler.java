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

import java.util.EnumMap;
import java.util.Map;
import com.dmzkiaddon.network.packets.FinalExplosionC2S;
import com.dmzkiaddon.network.packets.FinalExplosionCancelC2S;
import com.dmzkiaddon.network.packets.FinalExplosionChargeC2S;
import com.dmzkiaddon.network.packets.KikohoC2S;
import com.dmzkiaddon.network.packets.NeoKikohoC2S;
import com.dmzkiaddon.network.packets.NeoKikohoResetC2S;

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
    private static int neoKikohoIdleTicks = 0;
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

            if (cd == 0 && selected.isSpecial() && curFire && !prevFire) {
                activateSpecialAttack(selected.type());
            }

            if (cd == 0 && !selected.isSpecial()) {
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
                                player.displayClientMessage(Component.literal("§cNo tienes suficiente Ki"), true);
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
                            player.displayClientMessage(Component.literal("§cNo tienes suficiente Ki"), true);
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
                    showCooldownMessage(player, Component.translatable("attack.dmzkiaddon.taiyoken").getString(), cooldownTaiyoken);
                } else {
                    AddonNetworkHandler.sendToServer(new FireKiAttackC2S(AttackType.TAIYOKEN, 1.0f));
                    cooldownTaiyoken = AddonConfig.getCooldown(AttackType.TAIYOKEN);
                    ScreenEffects.triggerFlash(1.0f, 20);
                }
            }
        }

        if (hasSkill(player, "addon_hellzone")) {
            if (curHellzone) {
                if (cooldownHellzone > 0) {
                    if (!prevHellzone) showCooldownMessage(player, Component.translatable("attack.dmzkiaddon.hellzone").getString(), cooldownHellzone);
                } else {
                    if (!prevHellzone) {
                        AddonNetworkHandler.sendToServer(new SpawnHellzoneC2S(getLockOnTargetId()));
                        hellzoneHoldTick = 0;
                        hellzoneActive = true;
                    }
                    if (hellzoneActive) {
                        hellzoneHoldTick++;
                        if (hellzoneHoldTick % HELLZONE_SPAWN_INTERVAL == 0) AddonNetworkHandler.sendToServer(new SpawnHellzoneC2S(getLockOnTargetId()));
                        float progress = Math.min(1.0f, (float) hellzoneHoldTick / HELLZONE_MAX_HOLD);
                        ScreenEffects.setCharging(true, progress, 0.3f, 0.9f, 0.3f, Component.translatable("attack.dmzkiaddon.hellzone").getString(), 0);
                    }
                }
            }
            if (!curHellzone && prevHellzone && hellzoneActive) {
                AddonNetworkHandler.sendToServer(new LaunchHellzoneC2S(getLockOnTargetId()));
                cooldownHellzone = AddonConfig.getCooldown(AttackType.HELLZONE);
                hellzoneHoldTick = 0;
                hellzoneActive = false;
                ScreenEffects.stopCharging();
            }
        }

        if (curHakaiSpam && !prevHakaiSpam && ScreenEffects.isHakaiActive()) AddonNetworkHandler.sendToServer(new HakaiKeyPressC2S());
        if (curHakai && !prevHakai && !ScreenEffects.isHakaiActive()) {
            if (hasSkill(player, "addon_hakai") && cooldownHakai == 0) {
                int targetId = getLockOnTargetId();
                if (targetId != -1) {
                    AddonNetworkHandler.sendToServer(new InitiateHakaiC2S(targetId));
                    cooldownHakai = AddonConfig.getCooldown(AttackType.HAKAI);
                }
            }
        }

        if (curTimeSkip && !prevTimeSkip) {
            if (hasSkill(player, "addon_time_skip") && cooldownTimeSkip == 0) {
                AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.TimeSkipC2S());
                cooldownTimeSkip = AddonConfig.getCooldown(AttackType.TIME_SKIP);
                ScreenEffects.triggerTimeSkip();
            }
        }

        if (curPointPressure && !prevPointPressure) {
            if (hasSkill(player, "addon_point_pressure") && cooldownPointPressure == 0) {
                AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.PointPressureC2S());
                cooldownPointPressure = AddonConfig.getCooldown(AttackType.POINT_PRESSURE);
                ScreenEffects.triggerPointPressureAura();
            }
        }


        // ── Final Explosion (hold = carga, soltar = detonar) ─────────────────
        if (hasSkill(player, "addon_final_explosion") && cooldownFinalExplosion == 0) {
            if (curFinalExplosion) {
                finalExplosionHoldTick++;
                finalExplosionActive = true;

                // Calcular VIT y daño estimado en cliente para mostrar en HUD
                float[] vitArr = {0f}, dmgArr = {0f};
                StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {
                    vitArr[0] = stats.getStats().getVitality();
                    float hp  = player.getHealth();
                    dmgArr[0] = (hp * 2.0f) + (vitArr[0] * 5.0f);
                });

                float progress = Math.min(1.0f, finalExplosionHoldTick / 80.0f);
                ScreenEffects.setFinalExplosionStats((int) vitArr[0], dmgArr[0]);
                ScreenEffects.setCharging(true, progress, 1.0f, 0.8f, 0.2f, "Final Explosion", 0);

                // Primer tick: avisar al servidor para el Slowness
                if (!prevFinalExplosion) {
                    AddonNetworkHandler.sendToServer(new FinalExplosionChargeC2S());
                }
            }
            if (!curFinalExplosion && prevFinalExplosion && finalExplosionActive) {
                if (finalExplosionHoldTick >= 20) { // mínimo 1 segundo
                    AddonNetworkHandler.sendToServer(new FinalExplosionC2S());
                    cooldownFinalExplosion = AddonConfig.getCooldown(AttackType.FINAL_EXPLOSION);
                } else {
                    AddonNetworkHandler.sendToServer(new FinalExplosionCancelC2S());
                }
                finalExplosionHoldTick = 0;
                finalExplosionActive   = false;
                ScreenEffects.stopCharging();
            }
        }

        // ── Kikoho ────────────────────────────────────────────────────────────
        if (curKikoho && !prevKikoho) {
            if (hasSkill(player, "addon_kikoho")) {
                if (cooldownKikoho > 0) {
                    showCooldownMessage(player, "Kikoho", cooldownKikoho);
                } else {
                    AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.KikohoC2S());
                    cooldownKikoho = AddonConfig.getCooldown(AttackType.KIKOHO);
                }
            }
        }

        // ── Neo Kikoho — dispara en cada press (sistema de combo) ────────────
        if (curNeoKikoho && !prevNeoKikoho) {
            if (hasSkill(player, "addon_neo_kikoho")) {
                if (cooldownNeoKikoho > 0) {
                    showCooldownMessage(player, "Neo Kikoho", cooldownNeoKikoho);
                } else {
                    AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.NeoKikohoC2S());
                    // Cooldown corto entre disparos para que el combo fluya
                    cooldownNeoKikoho = 10; // 0.5 segundos entre cada ráfaga
                }
            }
        }
        // Resetear combo si pasa mucho tiempo sin disparar
        if (!curNeoKikoho && !prevNeoKikoho) {
            neoKikohoIdleTicks++;
            if (neoKikohoIdleTicks > 60) { // 3 segundos sin usar = reset combo
                AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.NeoKikohoResetC2S());
                neoKikohoIdleTicks = 0;
            }
        } else {
            neoKikohoIdleTicks = 0;
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

    private static void activateSpecialAttack(AttackType type) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Player player = mc.player;
        switch (type) {
            case TAIYOKEN -> {
                if (hasSkill(player, "addon_taiyoken") && cooldownTaiyoken == 0) {
                    AddonNetworkHandler.sendToServer(new FireKiAttackC2S(AttackType.TAIYOKEN, 1.0f));
                    cooldownTaiyoken = AddonConfig.getCooldown(AttackType.TAIYOKEN);
                    ScreenEffects.triggerFlash(1.0f, 20);
                }
            }
            case HAKAI -> {
                if (hasSkill(player, "addon_hakai") && cooldownHakai == 0) {
                    int tid = getLockOnTargetId();
                    if (tid != -1) {
                        AddonNetworkHandler.sendToServer(new InitiateHakaiC2S(tid));
                        cooldownHakai = AddonConfig.getCooldown(AttackType.HAKAI);
                    }
                }
            }
            case TIME_SKIP -> {
                if (hasSkill(player, "addon_time_skip") && cooldownTimeSkip == 0) {
                    AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.TimeSkipC2S());
                    cooldownTimeSkip = AddonConfig.getCooldown(AttackType.TIME_SKIP);
                    ScreenEffects.triggerTimeSkip();
                }
            }
            case POINT_PRESSURE -> {
                if (hasSkill(player, "addon_point_pressure") && cooldownPointPressure == 0) {
                    AddonNetworkHandler.sendToServer(new com.dmzkiaddon.network.packets.PointPressureC2S());
                    cooldownPointPressure = AddonConfig.getCooldown(AttackType.POINT_PRESSURE);
                    ScreenEffects.triggerPointPressureAura();
                }
            }
            default -> player.displayClientMessage(Component.literal("§eUsa la tecla dedicada para este ataque."), true);
        }
    }

    private static void showCooldownMessage(Player player, String attackName, int cooldownTicks) {
        float secs = cooldownTicks / 20.0f;
        player.displayClientMessage(Component.literal("§b" + attackName + " §7en cooldown: §e" + String.format("%.1fs", secs)), true);
    }

    private static void fireAttack(KiAttackEntry entry, float chargeLevel) {
        AddonNetworkHandler.sendToServer(new FireKiAttackC2S(entry.type(), chargeLevel));
        ScreenEffects.stopCharging();
    }

    private static void showSelectedAttack(Player player) {
        KiAttackEntry selected = AttackSelector.getSelected();
        if (selected == null) return;
        player.displayClientMessage(Component.literal("§7Ataque: §b" + selected.displayName()), true);
    }

    private static boolean hasSkill(Player player, String skillId) {
        boolean[] res = {false};
        StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(s -> {
            var sk = s.getSkills().getSkill(skillId);
            res[0] = sk != null && sk.getLevel() > 0;
        });
        return res[0];
    }

    private static int getLockOnTargetId() {
        return LockOnBridge.getTargetId();
    }
}