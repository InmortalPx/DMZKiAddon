package com.dmzkiaddon.client.gui;

import com.dmzkiaddon.client.ClientSetup;
import com.dmzkiaddon.config.AddonConfig;
import com.dmzkiaddon.registry.AttackRegistry.KiAttackEntry;
import com.dmzkiaddon.client.AttackSelector;
import com.dragonminez.client.gui.utilitymenu.AbstractMenuSlot;
import com.dragonminez.client.gui.utilitymenu.ButtonInfo;
import com.dragonminez.client.gui.utilitymenu.IUtilityMenuSlot;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KiAttackMenuSlot extends AbstractMenuSlot implements IUtilityMenuSlot {

    private static Map<String, KeyMapping> buildSpecialKeyMap() {
        Map<String, KeyMapping> map = new LinkedHashMap<>();
        map.put("addon_hellzone",        ClientSetup.KEY_HELLZONE);
        map.put("addon_hakai",           ClientSetup.KEY_HAKAI);
        map.put("addon_time_skip",       ClientSetup.KEY_TIME_SKIP);
        map.put("addon_point_pressure",  ClientSetup.KEY_POINT_PRESSURE);
        map.put("addon_final_explosion", ClientSetup.KEY_FINAL_EXPLOSION);
        map.put("addon_kikoho",          ClientSetup.KEY_KIKOHO);
        map.put("addon_neo_kikoho",      ClientSetup.KEY_NEO_KIKOHO);
        return map;
    }

    private static String getKeyName(KeyMapping key) {
        String name = key.getTranslatedKeyMessage().getString();
        if (name == null || name.isBlank() || name.equals("key.keyboard.unknown")) return "-";
        if (name.length() > 4) name = name.substring(0, 4);
        return name;
    }

    @Override
    public ButtonInfo render(StatsData statsData) {
        List<KiAttackEntry> allLearned    = AttackSelector.getAllLearnedAttacks();
        List<KiAttackEntry> normalLearned = AttackSelector.getLearnedAttacks();

        if (allLearned.isEmpty()) {
            ButtonInfo empty = new ButtonInfo(
                    Component.translatable("gui.dmzkiaddon.no_attacks").withStyle(ChatFormatting.BOLD),
                    Component.translatable("gui.dmzkiaddon.learn_from_master")
            );
            empty.setColor(0xAAAAAA);
            return empty;
        }

        KiAttackEntry selected = AttackSelector.getSelected();

        Component line1;
        if (selected != null) {
            line1 = Component.literal(selected.displayName()).withStyle(ChatFormatting.BOLD);
        } else {
            line1 = Component.literal("Ataques Ki").withStyle(ChatFormatting.BOLD);
        }

        String line2Text;
        if (selected != null) {
            int index = AttackSelector.getSelectedIndex() + 1;
            int realKiCost = 0;
            if (statsData != null) {
                int maxEnergy = statsData.getMaxEnergy();
                double costPct = AddonConfig.getCostPercentage(selected.type());
                realKiCost = (int)(maxEnergy * (costPct / 100.0));
            }
            line2Text = "Ki:" + realKiCost + " [" + index + "/" + normalLearned.size() + "]";
        } else {
            line2Text = "Ataques Ki";
        }

        List<KiAttackEntry> specialLearned = allLearned.stream()
                .filter(KiAttackEntry::isSpecial)
                .toList();

        if (!specialLearned.isEmpty()) {
            Map<String, KeyMapping> keyMap = buildSpecialKeyMap();
            StringBuilder sb = new StringBuilder();
            for (KiAttackEntry sp : specialLearned) {
                KeyMapping km = keyMap.get(sp.id());
                String keyName = km != null ? getKeyName(km) : "-";
                String shortName = sp.displayName().length() > 6
                        ? sp.displayName().substring(0, 6)
                        : sp.displayName();
                if (sb.length() > 0) sb.append(" ");
                sb.append(shortName).append("(").append(keyName).append(")");
            }
            line2Text = line2Text + " | " + sb;
        }

        ButtonInfo info = new ButtonInfo(line1, Component.literal(line2Text), true);

        if (selected != null) {
            int r = (int)(selected.colorR() * 255);
            int g = (int)(selected.colorG() * 255);
            int b = (int)(selected.colorB() * 255);
            info.setColor((r << 16) | (g << 8) | b);
        } else {
            info.setColor(0xAA00AA);
        }

        return info;
    }

    @Override
    public void handle(StatsData statsData, boolean rightClick) {
        List<KiAttackEntry> normalLearned = AttackSelector.getLearnedAttacks();
        List<KiAttackEntry> allLearned    = AttackSelector.getAllLearnedAttacks();
        if (allLearned.isEmpty()) return;

        if (!rightClick && !normalLearned.isEmpty()) {
            AttackSelector.selectNext();
            KiAttackEntry selected = AttackSelector.getSelected();
            if (selected != null) {
                net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    int index = AttackSelector.getSelectedIndex() + 1;
                    player.displayClientMessage(
                            Component.empty()
                                    .append(Component.literal("Ataque seleccionado: ").withStyle(ChatFormatting.GRAY))
                                    .append(Component.literal(selected.displayName()).withStyle(ChatFormatting.AQUA))
                                    .append(Component.literal(" [" + index + "/" + normalLearned.size() + "]")
                                            .withStyle(ChatFormatting.DARK_GRAY)),
                            true
                    );
                }
            }
        }

        playUiSound();
    }

    private void playUiSound() {
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.get(), 1.0F));
    }
}