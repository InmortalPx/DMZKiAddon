package com.dmzkiaddon.client;

import com.dmzkiaddon.registry.AttackRegistry;
import com.dmzkiaddon.registry.AttackRegistry.KiAttackEntry;
import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side singleton que gestiona el ataque de Ki seleccionado.
 * El ciclo incluye TODOS los ataques aprendidos (normales + especiales) en orden.
 */
@OnlyIn(Dist.CLIENT)
public class AttackSelector {

    private static int selectedIndex = 0;

    private AttackSelector() {}

    /**
     * Todos los ataques aprendidos (normales + especiales) en el orden de AttackRegistry.ALL.
     * Este es el ciclo principal — los 20 ataques.
     */
    public static List<KiAttackEntry> getAllLearnedAttacks() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return new ArrayList<>(AttackRegistry.ALL);

        List<KiAttackEntry> learned = new ArrayList<>();
        boolean[] capFound = {false};
        StatsProvider.get(StatsCapability.INSTANCE, player).ifPresent(stats -> {
            capFound[0] = true;
            for (KiAttackEntry entry : AttackRegistry.ALL) {
                var skill = stats.getSkills().getSkill(entry.id());
                if (skill != null && skill.getLevel() > 0) {
                    learned.add(entry);
                }
            }
        });
        if (!capFound[0]) learned.addAll(AttackRegistry.ALL);
        return learned;
    }

    /**
     * Solo ataques normales (no especiales) aprendidos.
     * Mantenido por compatibilidad con código existente.
     */
    public static List<KiAttackEntry> getLearnedAttacks() {
        List<KiAttackEntry> all = getAllLearnedAttacks();
        List<KiAttackEntry> normal = new ArrayList<>();
        for (KiAttackEntry e : all) {
            if (!e.isSpecial()) normal.add(e);
        }
        return normal;
    }

    /** Ataque actualmente seleccionado (puede ser normal o especial). */
    public static KiAttackEntry getSelected() {
        List<KiAttackEntry> learned = getAllLearnedAttacks();
        if (learned.isEmpty()) return null;
        selectedIndex = Math.min(selectedIndex, learned.size() - 1);
        return learned.get(selectedIndex);
    }

    public static void selectById(String id) {
        List<KiAttackEntry> learned = getAllLearnedAttacks();
        for (int i = 0; i < learned.size(); i++) {
            if (learned.get(i).id().equals(id)) {
                selectedIndex = i;
                return;
            }
        }
    }

    public static void selectNext() {
        List<KiAttackEntry> learned = getAllLearnedAttacks();
        if (learned.isEmpty()) return;
        selectedIndex = (selectedIndex + 1) % learned.size();
    }

    public static void selectPrev() {
        List<KiAttackEntry> learned = getAllLearnedAttacks();
        if (learned.isEmpty()) return;
        selectedIndex = ((selectedIndex - 1) + learned.size()) % learned.size();
    }

    public static int getSelectedIndex() {
        return selectedIndex;
    }

    public static int indexOfId(String id) {
        List<KiAttackEntry> learned = getAllLearnedAttacks();
        for (int i = 0; i < learned.size(); i++) {
            if (learned.get(i).id().equals(id)) return i;
        }
        return -1;
    }
}