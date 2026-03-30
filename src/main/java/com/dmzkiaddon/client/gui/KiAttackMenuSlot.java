package com.dmzkiaddon.client.gui;

import com.dmzkiaddon.config.AddonConfig;
import com.dmzkiaddon.registry.AttackRegistry.KiAttackEntry;
import com.dmzkiaddon.client.AttackSelector;
import com.dragonminez.client.gui.utilitymenu.AbstractMenuSlot;
import com.dragonminez.client.gui.utilitymenu.ButtonInfo;
import com.dragonminez.client.gui.utilitymenu.IUtilityMenuSlot;
import com.dragonminez.common.stats.StatsData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class KiAttackMenuSlot extends AbstractMenuSlot implements IUtilityMenuSlot {

    @Override
    public ButtonInfo render(StatsData statsData) {
        List<KiAttackEntry> allLearned = AttackSelector.getAllLearnedAttacks();
        Player player = Minecraft.getInstance().player;

        if (allLearned.isEmpty()) {
            ButtonInfo empty = new ButtonInfo(
                    Component.translatable("gui.dmzkiaddon.no_attacks").withStyle(ChatFormatting.BOLD),
                    Component.translatable("gui.dmzkiaddon.learn_from_master").withStyle(ChatFormatting.GRAY)
            );
            empty.setColor(0x888888);
            return empty;
        }

        KiAttackEntry selected = AttackSelector.getSelected();
        
        MutableComponent line1;
        if (selected != null) {
            int r = (int)(selected.colorR() * 200);
            int g = (int)(selected.colorG() * 200);
            int b = (int)(selected.colorB() * 200);
            int color = (r << 16) | (g << 8) | b;
            line1 = Component.literal("► " + selected.displayName())
                    .withStyle(style -> style.withColor(color).withBold(true));
        } else {
            line1 = Component.literal("Ataques Ki").withStyle(ChatFormatting.BOLD);
        }

        MutableComponent line2 = Component.empty();
        
        if (selected != null && player != null) {
            int index = AttackSelector.getSelectedIndex() + 1;
            final int[] colorHolder = {0x556666};
            final String[] textHolder = {""};
            
            switch (selected.type()) {
                case KIKOHO -> {
                    float hpPct = AddonConfig.KIKOHO_HP_COST_PCT.get().floatValue();
                    int hpCost = (int)(player.getMaxHealth() * (hpPct / 100.0f));
                    float kiPct = AddonConfig.KIKOHO_KI_COST_PCT.get().floatValue();
                    int kiCost = statsData != null ? (int)(statsData.getMaxEnergy() * (kiPct / 100.0f)) : 0;
                    textHolder[0] = "HP:" + hpCost + " Ki:" + kiCost;
                    colorHolder[0] = 0x884444;
                }
                case NEO_KIKOHO -> {
                    float kiPct = AddonConfig.NEO_KIKOHO_KI_COST_PCT.get().floatValue();
                    int kiCost = statsData != null ? (int)(statsData.getMaxEnergy() * (kiPct / 100.0f)) : 0;
                    textHolder[0] = "Ki:" + kiCost + " + HP↑";
                    colorHolder[0] = 0x886644;
                }
                case FINAL_EXPLOSION -> {
                    textHolder[0] = "HP:100%";
                    colorHolder[0] = 0x662222;
                }
                case TIME_SKIP -> {
                    int skpCost = AddonConfig.TIME_SKIP_SKP_COST.get();
                    textHolder[0] = "SKP:" + skpCost;
                    colorHolder[0] = 0x554477;
                }
                case POINT_PRESSURE -> {
                    float pct = AddonConfig.POINT_PRESSURE_KI_COST_PCT.get().floatValue();
                    int cost = statsData != null ? (int)(statsData.getMaxEnergy() * (pct / 100.0f)) : 0;
                    textHolder[0] = "Ki:" + cost;
                    colorHolder[0] = 0x446666;
                }
                case HAKAI -> {
                    float pct = AddonConfig.HAKAI_KI_COST_PCT.get().floatValue();
                    int cost = statsData != null ? (int)(statsData.getMaxEnergy() * (pct / 100.0f)) : 0;
                    textHolder[0] = "Ki:" + cost;
                    colorHolder[0] = 0x442244;
                }
                case HELLZONE -> {
                    float pct = AddonConfig.HELLZONE_KI_COST_PCT.get().floatValue();
                    int cost = statsData != null ? (int)(statsData.getMaxEnergy() * (pct / 100.0f)) : 0;
                    textHolder[0] = "Ki:" + cost + "/gd";
                    colorHolder[0] = 0x224422;
                }
                case TAIYOKEN -> {
                    float pct = AddonConfig.TAIYOKEN_KI_COST_PCT.get().floatValue();
                    int cost = statsData != null ? (int)(statsData.getMaxEnergy() * (pct / 100.0f)) : 0;
                    textHolder[0] = "Ki:" + cost;
                    colorHolder[0] = 0x888888;
                }
                default -> {
                    if (statsData != null) {
                        float pct = AddonConfig.getCostPercentage(selected.type());
                        int baseCost = (int)(statsData.getMaxEnergy() * (pct / 100.0f));
                        if (selected.isCharged()) {
                            textHolder[0] = "Ki:" + baseCost + "→" + (baseCost * 2);
                        } else {
                            textHolder[0] = "Ki:" + baseCost;
                        }
                        colorHolder[0] = 0x556666;
                    } else {
                        textHolder[0] = "Ki:???";
                        colorHolder[0] = 0x666666;
                    }
                }
            }
            
            final int finalColor = colorHolder[0];
            final String finalText = textHolder[0];
            
            line2.append(Component.literal(finalText).withStyle(style -> style.withColor(finalColor)));
            line2.append(Component.literal(" [" + index + "/" + allLearned.size() + "]")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        ButtonInfo info = new ButtonInfo(line1, line2, true);

        if (selected != null) {
            int r = (int)(selected.colorR() * 180);
            int g = (int)(selected.colorG() * 180);
            int b = (int)(selected.colorB() * 180);
            info.setColor((r << 16) | (g << 8) | b);
        } else {
            info.setColor(0x664466);
        }

        return info;
    }

    @Override
    public void handle(StatsData statsData, boolean rightClick) {
        List<KiAttackEntry> allLearned = AttackSelector.getAllLearnedAttacks();
        if (allLearned.isEmpty()) return;

        if (!rightClick) {
            AttackSelector.selectNext();
            KiAttackEntry selected = AttackSelector.getSelected();
            if (selected != null) {
                var player = Minecraft.getInstance().player;
                if (player != null) {
                    int index = AttackSelector.getSelectedIndex() + 1;
                    
                    String costType = switch (selected.type()) {
                        case KIKOHO, FINAL_EXPLOSION -> "HP";
                        case TIME_SKIP -> "SKP";
                        default -> "Ki";
                    };
                    
                    MutableComponent msg = Component.empty()
                            .append(Component.literal("Ataque: ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(selected.displayName())
                                    .withStyle(style -> {
                                        int r = (int)(selected.colorR() * 200);
                                        int g = (int)(selected.colorG() * 200);
                                        int b = (int)(selected.colorB() * 200);
                                        return style.withColor((r << 16) | (g << 8) | b);
                                    }))
                            .append(Component.literal(" [" + index + "/" + allLearned.size() + "]")
                                    .withStyle(ChatFormatting.DARK_GRAY))
                            .append(Component.literal(" (" + costType + ")").withStyle(ChatFormatting.GRAY));
                    
                    player.displayClientMessage(msg, true);
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