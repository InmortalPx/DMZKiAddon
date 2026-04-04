package com.dmzkiaddon.api;

import com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType;
import com.dmzkiaddon.registry.AttackRegistry;
import com.dmzkiaddon.registry.AttackRegistry.KiAttackEntry;

public class KiAttackBuilder {

    public static void create(AttackType type, String id, String displayName, 
                              float r, float g, float b, 
                              int cost, int cooldown, 
                              boolean charged, boolean skill, float powerMultiplier) {
        AttackRegistry.register(new KiAttackEntry(type, id, displayName, r, g, b, cost, cooldown, charged, skill, false, powerMultiplier));
    }

    public static void createSpecial(AttackType type, String id, String displayName, 
                                     float r, float g, float b, 
                                     int cost, int cooldown, 
                                     boolean charged, boolean skill, float powerMultiplier) {
        AttackRegistry.register(new KiAttackEntry(type, id, displayName, r, g, b, cost, cooldown, charged, skill, true, powerMultiplier));
    }
}
