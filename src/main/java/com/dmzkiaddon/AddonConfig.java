package com.dmzkiaddon;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Config file: config/DMZKiAddon/config.toml
 * Permite configurar el costo de Ki y cooldown de cada ataque individualmente.
 */
public class AddonConfig {

    public static final ForgeConfigSpec SPEC;

    // ── Costos de Ki por ataque ────────────────────────────────────────────
    public static final ForgeConfigSpec.IntValue COST_KI_LASER;
    public static final ForgeConfigSpec.IntValue COST_DODOMPA;
    public static final ForgeConfigSpec.IntValue COST_KI_VOLLEY;
    public static final ForgeConfigSpec.IntValue COST_MASENKO;
    public static final ForgeConfigSpec.IntValue COST_KI_DISC;
    public static final ForgeConfigSpec.IntValue COST_GALICK_GUN;
    public static final ForgeConfigSpec.IntValue COST_KAMEHAMEHA;
    public static final ForgeConfigSpec.IntValue COST_MAKANKOSAPPO;
    public static final ForgeConfigSpec.IntValue COST_TAIYOKEN;
    public static final ForgeConfigSpec.IntValue COST_BIG_BANG;
    public static final ForgeConfigSpec.IntValue COST_SPIRIT_BOMB;
    public static final ForgeConfigSpec.IntValue COST_DEATH_BALL;
    public static final ForgeConfigSpec.IntValue COST_HELLZONE;
    public static final ForgeConfigSpec.IntValue COST_FINAL_FLASH;
    public static final ForgeConfigSpec.IntValue COST_FINAL_KAMEHAMEHA;
    public static final ForgeConfigSpec.IntValue COST_HAKAI;

    // ── Cooldowns por ataque (en ticks, 20 ticks = 1 segundo) ─────────────
    public static final ForgeConfigSpec.IntValue CD_KI_LASER;
    public static final ForgeConfigSpec.IntValue CD_DODOMPA;
    public static final ForgeConfigSpec.IntValue CD_KI_VOLLEY;
    public static final ForgeConfigSpec.IntValue CD_MASENKO;
    public static final ForgeConfigSpec.IntValue CD_KI_DISC;
    public static final ForgeConfigSpec.IntValue CD_GALICK_GUN;
    public static final ForgeConfigSpec.IntValue CD_KAMEHAMEHA;
    public static final ForgeConfigSpec.IntValue CD_MAKANKOSAPPO;
    public static final ForgeConfigSpec.IntValue CD_TAIYOKEN;
    public static final ForgeConfigSpec.IntValue CD_BIG_BANG;
    public static final ForgeConfigSpec.IntValue CD_SPIRIT_BOMB;
    public static final ForgeConfigSpec.IntValue CD_DEATH_BALL;
    public static final ForgeConfigSpec.IntValue CD_HELLZONE;
    public static final ForgeConfigSpec.IntValue CD_FINAL_FLASH;
    public static final ForgeConfigSpec.IntValue CD_FINAL_KAMEHAMEHA;
    public static final ForgeConfigSpec.IntValue CD_HAKAI;

    // ── Colores del mensaje de cooldown (RGB 0-255) ────────────────────────
    public static final ForgeConfigSpec.IntValue MSG_COLOR_ATTACK_R;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_ATTACK_G;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_ATTACK_B;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_LABEL_R;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_LABEL_G;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_LABEL_B;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_TIME_R;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_TIME_G;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_TIME_B;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // ── Costos de Ki ──────────────────────────────────────────────────
        builder.comment("Costo de Ki de cada ataque (se multiplica por el nivel de carga)").push("kiCosts");

        COST_KI_LASER         = builder.comment("Ki Laser")          .defineInRange("ki_laser",          10,  1, 9999);
        COST_DODOMPA          = builder.comment("Dodompa")            .defineInRange("dodompa",           10,  1, 9999);
        COST_KI_VOLLEY        = builder.comment("Ki Volley")          .defineInRange("ki_volley",         20,  1, 9999);
        COST_MASENKO          = builder.comment("Masenko")            .defineInRange("masenko",           30,  1, 9999);
        COST_KI_DISC          = builder.comment("Kienzan")            .defineInRange("ki_disc",           30,  1, 9999);
        COST_GALICK_GUN       = builder.comment("Galick Gun")         .defineInRange("galick_gun",        40,  1, 9999);
        COST_KAMEHAMEHA       = builder.comment("Kamehameha")         .defineInRange("kamehameha",        40,  1, 9999);
        COST_MAKANKOSAPPO     = builder.comment("Makankosappo")       .defineInRange("makankosappo",      50,  1, 9999);
        COST_TAIYOKEN         = builder.comment("Taiyoken")           .defineInRange("taiyoken",          20,  1, 9999);
        COST_BIG_BANG         = builder.comment("Big Bang Attack")    .defineInRange("big_bang",          60,  1, 9999);
        COST_SPIRIT_BOMB      = builder.comment("Spirit Bomb")        .defineInRange("spirit_bomb",       80,  1, 9999);
        COST_DEATH_BALL       = builder.comment("Death Ball")         .defineInRange("death_ball",        70,  1, 9999);
        COST_HELLZONE         = builder.comment("Hellzone Grenade (por granada)").defineInRange("hellzone", 5,  1, 9999);
        COST_FINAL_FLASH      = builder.comment("Final Flash")        .defineInRange("final_flash",       70,  1, 9999);
        COST_FINAL_KAMEHAMEHA = builder.comment("Final Kamehameha")   .defineInRange("final_kamehameha", 100,  1, 9999);
        COST_HAKAI            = builder.comment("Hakai")              .defineInRange("hakai",            100,  1, 9999);

        builder.pop();

        // ── Cooldowns ─────────────────────────────────────────────────────
        builder.comment("Cooldown de cada ataque en ticks (20 ticks = 1 segundo)").push("cooldowns");

        CD_KI_LASER         = builder.comment("Ki Laser (default: 10t = 0.5s)")         .defineInRange("ki_laser",           10,  1, 72000);
        CD_DODOMPA          = builder.comment("Dodompa (default: 60t = 3s)")             .defineInRange("dodompa",            60,  1, 72000);
        CD_KI_VOLLEY        = builder.comment("Ki Volley (default: 20t = 1s)")           .defineInRange("ki_volley",          20,  1, 72000);
        CD_MASENKO          = builder.comment("Masenko (default: 160t = 8s)")            .defineInRange("masenko",           160,  1, 72000);
        CD_KI_DISC          = builder.comment("Kienzan (default: 30t = 1.5s)")           .defineInRange("ki_disc",            30,  1, 72000);
        CD_GALICK_GUN       = builder.comment("Galick Gun (default: 220t = 11s)")        .defineInRange("galick_gun",        220,  1, 72000);
        CD_KAMEHAMEHA       = builder.comment("Kamehameha (default: 200t = 10s)")        .defineInRange("kamehameha",        200,  1, 72000);
        CD_MAKANKOSAPPO     = builder.comment("Makankosappo (default: 240t = 12s)")      .defineInRange("makankosappo",      240,  1, 72000);
        CD_TAIYOKEN         = builder.comment("Taiyoken (default: 120t = 6s)")           .defineInRange("taiyoken",          120,  1, 72000);
        CD_BIG_BANG         = builder.comment("Big Bang Attack (default: 260t = 13s)")   .defineInRange("big_bang",          260,  1, 72000);
        CD_SPIRIT_BOMB      = builder.comment("Spirit Bomb (default: 400t = 20s)")       .defineInRange("spirit_bomb",       400,  1, 72000);
        CD_DEATH_BALL       = builder.comment("Death Ball (default: 280t = 14s)")        .defineInRange("death_ball",        280,  1, 72000);
        CD_HELLZONE         = builder.comment("Hellzone Grenade (default: 100t = 5s)")   .defineInRange("hellzone",          100,  1, 72000);
        CD_FINAL_FLASH      = builder.comment("Final Flash (default: 300t = 15s)")       .defineInRange("final_flash",       300,  1, 72000);
        CD_FINAL_KAMEHAMEHA = builder.comment("Final Kamehameha (default: 400t = 20s)")  .defineInRange("final_kamehameha",  400,  1, 72000);
        CD_HAKAI            = builder.comment("Hakai (default: 200t = 10s)")             .defineInRange("hakai",             200,  1, 72000);

        builder.pop();

        // ── Colores del mensaje de cooldown ───────────────────────────────
        builder.comment("Colores RGB (0-255) del mensaje de cooldown en el actionbar").push("cooldownMessage");

        builder.comment("Color del nombre del ataque").push("attackNameColor");
        MSG_COLOR_ATTACK_R = builder.defineInRange("r", 255, 0, 255);
        MSG_COLOR_ATTACK_G = builder.defineInRange("g", 85,  0, 255);
        MSG_COLOR_ATTACK_B = builder.defineInRange("b", 85,  0, 255);
        builder.pop();

        builder.comment("Color del texto 'en cooldown:'").push("labelColor");
        MSG_COLOR_LABEL_R = builder.defineInRange("r", 170, 0, 255);
        MSG_COLOR_LABEL_G = builder.defineInRange("g", 170, 0, 255);
        MSG_COLOR_LABEL_B = builder.defineInRange("b", 170, 0, 255);
        builder.pop();

        builder.comment("Color del tiempo restante (ej: 2.5s)").push("timeColor");
        MSG_COLOR_TIME_R = builder.defineInRange("r", 255, 0, 255);
        MSG_COLOR_TIME_G = builder.defineInRange("g", 255, 0, 255);
        MSG_COLOR_TIME_B = builder.defineInRange("b", 85,  0, 255);
        builder.pop();

        builder.pop(); // cooldownMessage

        SPEC = builder.build();
    }

    // ── Getters por tipo de ataque ─────────────────────────────────────────

    public static int getCost(com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType type) {
        return switch (type) {
            case KI_LASER         -> COST_KI_LASER.get();
            case DODOMPA          -> COST_DODOMPA.get();
            case KI_VOLLEY        -> COST_KI_VOLLEY.get();
            case MASENKO          -> COST_MASENKO.get();
            case KI_DISC          -> COST_KI_DISC.get();
            case GALICK_GUN       -> COST_GALICK_GUN.get();
            case KAMEHAMEHA       -> COST_KAMEHAMEHA.get();
            case MAKANKOSAPPO     -> COST_MAKANKOSAPPO.get();
            case TAIYOKEN         -> COST_TAIYOKEN.get();
            case BIG_BANG         -> COST_BIG_BANG.get();
            case SPIRIT_BOMB      -> COST_SPIRIT_BOMB.get();
            case DEATH_BALL       -> COST_DEATH_BALL.get();
            case HELLZONE         -> COST_HELLZONE.get();
            case FINAL_FLASH      -> COST_FINAL_FLASH.get();
            case FINAL_KAMEHAMEHA -> COST_FINAL_KAMEHAMEHA.get();
            case HAKAI            -> COST_HAKAI.get();
        };
    }

    public static int getCooldown(com.dmzkiaddon.network.packets.FireKiAttackC2S.AttackType type) {
        return switch (type) {
            case KI_LASER         -> CD_KI_LASER.get();
            case DODOMPA          -> CD_DODOMPA.get();
            case KI_VOLLEY        -> CD_KI_VOLLEY.get();
            case MASENKO          -> CD_MASENKO.get();
            case KI_DISC          -> CD_KI_DISC.get();
            case GALICK_GUN       -> CD_GALICK_GUN.get();
            case KAMEHAMEHA       -> CD_KAMEHAMEHA.get();
            case MAKANKOSAPPO     -> CD_MAKANKOSAPPO.get();
            case TAIYOKEN         -> CD_TAIYOKEN.get();
            case BIG_BANG         -> CD_BIG_BANG.get();
            case SPIRIT_BOMB      -> CD_SPIRIT_BOMB.get();
            case DEATH_BALL       -> CD_DEATH_BALL.get();
            case HELLZONE         -> CD_HELLZONE.get();
            case FINAL_FLASH      -> CD_FINAL_FLASH.get();
            case FINAL_KAMEHAMEHA -> CD_FINAL_KAMEHAMEHA.get();
            case HAKAI            -> CD_HAKAI.get();
        };
    }

    public static int getAttackNameColor() {
        return (MSG_COLOR_ATTACK_R.get() << 16) | (MSG_COLOR_ATTACK_G.get() << 8) | MSG_COLOR_ATTACK_B.get();
    }

    public static int getLabelColor() {
        return (MSG_COLOR_LABEL_R.get() << 16) | (MSG_COLOR_LABEL_G.get() << 8) | MSG_COLOR_LABEL_B.get();
    }

    public static int getTimeColor() {
        return (MSG_COLOR_TIME_R.get() << 16) | (MSG_COLOR_TIME_G.get() << 8) | MSG_COLOR_TIME_B.get();
    }
}