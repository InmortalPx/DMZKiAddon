package com.dmzkiaddon.config;

import com.dmzkiaddon.network.packets.FireKiAttackC2S;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Config file: config/DMZKiAddon/config.toml
 *
 * Each attack has its own [attacks.<name>] section with all settings together.
 */
public class AddonConfig {

    public static final ForgeConfigSpec SPEC;

    // ── Per-attack config entries ──────────────────────────────────────────

    // KI_LASER
    public static final ForgeConfigSpec.IntValue   KI_LASER_KI_COST;
    public static final ForgeConfigSpec.DoubleValue KI_LASER_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue KI_LASER_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   KI_LASER_CHARGE_KI_PER_SEC;

    // DODOMPA
    public static final ForgeConfigSpec.IntValue   DODOMPA_KI_COST;
    public static final ForgeConfigSpec.DoubleValue DODOMPA_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue DODOMPA_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   DODOMPA_CHARGE_KI_PER_SEC;

    // KI_VOLLEY
    public static final ForgeConfigSpec.IntValue   KI_VOLLEY_KI_COST;
    public static final ForgeConfigSpec.DoubleValue KI_VOLLEY_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue KI_VOLLEY_DAMAGE_SCALE;

    // MASENKO
    public static final ForgeConfigSpec.IntValue   MASENKO_KI_COST;
    public static final ForgeConfigSpec.DoubleValue MASENKO_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue MASENKO_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   MASENKO_CHARGE_KI_PER_SEC;

    // KI_DISC
    public static final ForgeConfigSpec.IntValue   KI_DISC_KI_COST;
    public static final ForgeConfigSpec.DoubleValue KI_DISC_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue KI_DISC_DAMAGE_SCALE;

    // GALICK_GUN
    public static final ForgeConfigSpec.IntValue   GALICK_GUN_KI_COST;
    public static final ForgeConfigSpec.DoubleValue GALICK_GUN_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue GALICK_GUN_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   GALICK_GUN_CHARGE_KI_PER_SEC;

    // KAMEHAMEHA
    public static final ForgeConfigSpec.IntValue   KAMEHAMEHA_KI_COST;
    public static final ForgeConfigSpec.DoubleValue KAMEHAMEHA_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue KAMEHAMEHA_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   KAMEHAMEHA_CHARGE_KI_PER_SEC;

    // MAKANKOSAPPO
    public static final ForgeConfigSpec.IntValue   MAKANKOSAPPO_KI_COST;
    public static final ForgeConfigSpec.DoubleValue MAKANKOSAPPO_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue MAKANKOSAPPO_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   MAKANKOSAPPO_CHARGE_KI_PER_SEC;

    // TAIYOKEN
    public static final ForgeConfigSpec.IntValue   TAIYOKEN_KI_COST;
    public static final ForgeConfigSpec.DoubleValue TAIYOKEN_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue TAIYOKEN_DAMAGE_SCALE;

    // BIG_BANG
    public static final ForgeConfigSpec.IntValue   BIG_BANG_KI_COST;
    public static final ForgeConfigSpec.DoubleValue BIG_BANG_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue BIG_BANG_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   BIG_BANG_CHARGE_KI_PER_SEC;

    // SPIRIT_BOMB
    public static final ForgeConfigSpec.IntValue   SPIRIT_BOMB_KI_COST;
    public static final ForgeConfigSpec.DoubleValue SPIRIT_BOMB_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue SPIRIT_BOMB_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   SPIRIT_BOMB_CHARGE_KI_PER_SEC;

    // DEATH_BALL
    public static final ForgeConfigSpec.IntValue   DEATH_BALL_KI_COST;
    public static final ForgeConfigSpec.DoubleValue DEATH_BALL_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue DEATH_BALL_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   DEATH_BALL_CHARGE_KI_PER_SEC;

    // HELLZONE
    public static final ForgeConfigSpec.IntValue   HELLZONE_KI_COST;
    public static final ForgeConfigSpec.DoubleValue HELLZONE_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue HELLZONE_DAMAGE_SCALE;

    // FINAL_FLASH
    public static final ForgeConfigSpec.IntValue   FINAL_FLASH_KI_COST;
    public static final ForgeConfigSpec.DoubleValue FINAL_FLASH_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue FINAL_FLASH_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   FINAL_FLASH_CHARGE_KI_PER_SEC;

    // FINAL_KAMEHAMEHA
    public static final ForgeConfigSpec.IntValue   FINAL_KAMEHAMEHA_KI_COST;
    public static final ForgeConfigSpec.DoubleValue FINAL_KAMEHAMEHA_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue FINAL_KAMEHAMEHA_DAMAGE_SCALE;
    public static final ForgeConfigSpec.IntValue   FINAL_KAMEHAMEHA_CHARGE_KI_PER_SEC;

    // HAKAI
    public static final ForgeConfigSpec.IntValue   HAKAI_KI_COST;
    public static final ForgeConfigSpec.DoubleValue HAKAI_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue HAKAI_DAMAGE_SCALE;

    // ── Cooldown message colors (RGB 0-255) ───────────────────────────────
    public static final ForgeConfigSpec.IntValue MSG_COLOR_ATTACK_R;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_ATTACK_G;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_ATTACK_B;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_LABEL_R;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_LABEL_G;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_LABEL_B;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_TIME_R;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_TIME_G;
    public static final ForgeConfigSpec.IntValue MSG_COLOR_TIME_B;


    // ── TP cost to purchase each skill from a master ───────────────────────
    // These are written to skills_config.json on server start.
    public static final ForgeConfigSpec.IntValue TP_KI_LASER;
    public static final ForgeConfigSpec.IntValue TP_DODOMPA;
    public static final ForgeConfigSpec.IntValue TP_KI_VOLLEY;
    public static final ForgeConfigSpec.IntValue TP_MASENKO;
    public static final ForgeConfigSpec.IntValue TP_KI_DISC;
    public static final ForgeConfigSpec.IntValue TP_GALICK_GUN;
    public static final ForgeConfigSpec.IntValue TP_KAMEHAMEHA;
    public static final ForgeConfigSpec.IntValue TP_MAKANKOSAPPO;
    public static final ForgeConfigSpec.IntValue TP_TAIYOKEN;
    public static final ForgeConfigSpec.IntValue TP_BIG_BANG;
    public static final ForgeConfigSpec.IntValue TP_SPIRIT_BOMB;
    public static final ForgeConfigSpec.IntValue TP_DEATH_BALL;
    public static final ForgeConfigSpec.IntValue TP_HELLZONE;
    public static final ForgeConfigSpec.IntValue TP_FINAL_FLASH;
    public static final ForgeConfigSpec.IntValue TP_FINAL_KAMEHAMEHA;
    public static final ForgeConfigSpec.IntValue TP_HAKAI;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment(
                "DMZKiAddon configuration",
                "Each attack has its own section with all settings grouped together.",
                "",
                "  ki_cost            - Ki consumed on fire",
                "  cooldown_seconds   - Cooldown after firing (in seconds)",
                "  damage_scale       - Damage multiplier: 1.0 = normal, 2.0 = double, 0.5 = half",
                "  charge_ki_per_sec  - Ki drained per second while holding charge (0 = free to hold)",
                "                       Only present on chargeable attacks."
        ).push("attacks");

        // ── Ki Laser ──────────────────────────────────────────────────────
        b.push("ki_laser");
        KI_LASER_KI_COST          = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           10,   0, 9999);
        KI_LASER_COOLDOWN         = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",   0.5, 0.05, 3600.0);
        KI_LASER_DAMAGE_SCALE     = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        KI_LASER_CHARGE_KI_PER_SEC= b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",   0,   0, 9999);
        b.pop();

        // ── Dodompa ───────────────────────────────────────────────────────
        b.push("dodompa");
        DODOMPA_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           10,   0, 9999);
        DODOMPA_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",   3.0, 0.05, 3600.0);
        DODOMPA_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        DODOMPA_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",   0,   0, 9999);
        b.pop();

        // ── Ki Volley ─────────────────────────────────────────────────────
        b.push("ki_volley");
        KI_VOLLEY_KI_COST         = b.comment("Ki consumed per shot")                           .defineInRange("ki_cost",           20,   0, 9999);
        KI_VOLLEY_COOLDOWN        = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",   1.0, 0.05, 3600.0);
        KI_VOLLEY_DAMAGE_SCALE    = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        b.pop();

        // ── Masenko ───────────────────────────────────────────────────────
        b.push("masenko");
        MASENKO_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           30,   0, 9999);
        MASENKO_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",   8.0, 0.05, 3600.0);
        MASENKO_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        MASENKO_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  20,   0, 9999);
        b.pop();

        // ── Kienzan (Ki Disc) ─────────────────────────────────────────────
        b.push("ki_disc");
        KI_DISC_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           30,   0, 9999);
        KI_DISC_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",   1.5, 0.05, 3600.0);
        KI_DISC_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        b.pop();

        // ── Galick Gun ────────────────────────────────────────────────────
        b.push("galick_gun");
        GALICK_GUN_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           40,   0, 9999);
        GALICK_GUN_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  11.0, 0.05, 3600.0);
        GALICK_GUN_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        GALICK_GUN_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  40,   0, 9999);
        b.pop();

        // ── Kamehameha ────────────────────────────────────────────────────
        b.push("kamehameha");
        KAMEHAMEHA_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           40,   0, 9999);
        KAMEHAMEHA_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  10.0, 0.05, 3600.0);
        KAMEHAMEHA_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        KAMEHAMEHA_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  40,   0, 9999);
        b.pop();

        // ── Makankosappo ──────────────────────────────────────────────────
        b.push("makankosappo");
        MAKANKOSAPPO_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           50,   0, 9999);
        MAKANKOSAPPO_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  12.0, 0.05, 3600.0);
        MAKANKOSAPPO_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        MAKANKOSAPPO_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  40,   0, 9999);
        b.pop();

        // ── Taiyoken ──────────────────────────────────────────────────────
        b.push("taiyoken");
        TAIYOKEN_KI_COST          = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           20,   0, 9999);
        TAIYOKEN_COOLDOWN         = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",   6.0, 0.05, 3600.0);
        TAIYOKEN_DAMAGE_SCALE     = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        b.pop();

        // ── Big Bang Attack ───────────────────────────────────────────────
        b.push("big_bang");
        BIG_BANG_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           60,   0, 9999);
        BIG_BANG_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  13.0, 0.05, 3600.0);
        BIG_BANG_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        BIG_BANG_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  60,   0, 9999);
        b.pop();

        // ── Spirit Bomb ───────────────────────────────────────────────────
        b.push("spirit_bomb");
        SPIRIT_BOMB_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           80,   0, 9999);
        SPIRIT_BOMB_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  20.0, 0.05, 3600.0);
        SPIRIT_BOMB_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        SPIRIT_BOMB_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  80,   0, 9999);
        b.pop();

        // ── Death Ball ────────────────────────────────────────────────────
        b.push("death_ball");
        DEATH_BALL_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           70,   0, 9999);
        DEATH_BALL_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  14.0, 0.05, 3600.0);
        DEATH_BALL_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        DEATH_BALL_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  60,   0, 9999);
        b.pop();

        // ── Hellzone Grenade ──────────────────────────────────────────────
        b.push("hellzone");
        HELLZONE_KI_COST          = b.comment("Ki consumed per grenade spawned")                 .defineInRange("ki_cost",            5,   0, 9999);
        HELLZONE_COOLDOWN         = b.comment("Cooldown in seconds")                              .defineInRange("cooldown_seconds",   5.0, 0.05, 3600.0);
        HELLZONE_DAMAGE_SCALE     = b.comment("Damage multiplier (1.0 = normal)")                 .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        b.pop();

        // ── Final Flash ───────────────────────────────────────────────────
        b.push("final_flash");
        FINAL_FLASH_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",           70,   0, 9999);
        FINAL_FLASH_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  15.0, 0.05, 3600.0);
        FINAL_FLASH_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        FINAL_FLASH_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec",  60,   0, 9999);
        b.pop();

        // ── Final Kamehameha ──────────────────────────────────────────────
        b.push("final_kamehameha");
        FINAL_KAMEHAMEHA_KI_COST           = b.comment("Ki consumed when fired")                         .defineInRange("ki_cost",          100,   0, 9999);
        FINAL_KAMEHAMEHA_COOLDOWN          = b.comment("Cooldown in seconds")                             .defineInRange("cooldown_seconds",  20.0, 0.05, 3600.0);
        FINAL_KAMEHAMEHA_DAMAGE_SCALE      = b.comment("Damage multiplier (1.0 = normal)")                .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        FINAL_KAMEHAMEHA_CHARGE_KI_PER_SEC = b.comment("Ki drained per second while charging (0 = free)").defineInRange("charge_ki_per_sec", 100,   0, 9999);
        b.pop();

        // ── Hakai ─────────────────────────────────────────────────────────
        b.push("hakai");
        HAKAI_KI_COST         = b.comment("Ki consumed when activated")                          .defineInRange("ki_cost",          100,   0, 9999);
        HAKAI_COOLDOWN        = b.comment("Cooldown in seconds")                                  .defineInRange("cooldown_seconds",  10.0, 0.05, 3600.0);
        HAKAI_DAMAGE_SCALE    = b.comment("Damage multiplier (1.0 = normal)")                     .defineInRange("damage_scale",       1.0, 0.01, 100.0);
        b.pop();

        b.pop(); // attacks

        // ── Cooldown message colors ───────────────────────────────────────
        b.comment("RGB colors (0-255) for the cooldown actionbar message").push("cooldown_message");
        b.comment("Color of the attack name").push("attack_name_color");
        MSG_COLOR_ATTACK_R = b.defineInRange("r", 255, 0, 255);
        MSG_COLOR_ATTACK_G = b.defineInRange("g",  85, 0, 255);
        MSG_COLOR_ATTACK_B = b.defineInRange("b",  85, 0, 255);
        b.pop();
        b.comment("Color of the 'on cooldown:' label").push("label_color");
        MSG_COLOR_LABEL_R = b.defineInRange("r", 170, 0, 255);
        MSG_COLOR_LABEL_G = b.defineInRange("g", 170, 0, 255);
        MSG_COLOR_LABEL_B = b.defineInRange("b", 170, 0, 255);
        b.pop();
        b.comment("Color of the remaining time (e.g. 2.5s)").push("time_color");
        MSG_COLOR_TIME_R = b.defineInRange("r", 255, 0, 255);
        MSG_COLOR_TIME_G = b.defineInRange("g", 255, 0, 255);
        MSG_COLOR_TIME_B = b.defineInRange("b",  85, 0, 255);
        b.pop();
        b.pop(); // cooldown_message


        // ── TP cost to purchase each skill from a master ──────────────────
        b.comment(
                "TP (Training Points) cost to purchase each Ki attack from a master.",
                "This is written to DragonMineZ skills_config.json on server start.",
                "",
                "  Basic tier     : ki_laser, ki_volley, taiyoken",
                "  Standard tier  : dodompa, masenko, ki_disc, galick_gun, kamehameha",
                "  Advanced tier  : makankosappo, big_bang, hellzone, death_ball, final_flash",
                "  Legendary tier : spirit_bomb, final_kamehameha, hakai"
        ).push("skill_tp_costs");
        TP_KI_LASER          = b.comment("Basic     — Ki Laser")         .defineInRange("ki_laser",              500, 0, 1_000_000);
        TP_DODOMPA           = b.comment("Standard  — Dodompa")           .defineInRange("dodompa",             3_000, 0, 1_000_000);
        TP_KI_VOLLEY         = b.comment("Basic     — Ki Volley")         .defineInRange("ki_volley",             800, 0, 1_000_000);
        TP_MASENKO           = b.comment("Standard  — Masenko")           .defineInRange("masenko",             4_000, 0, 1_000_000);
        TP_KI_DISC           = b.comment("Standard  — Kienzan")           .defineInRange("ki_disc",             4_500, 0, 1_000_000);
        TP_GALICK_GUN        = b.comment("Standard  — Galick Gun")        .defineInRange("galick_gun",          6_000, 0, 1_000_000);
        TP_KAMEHAMEHA        = b.comment("Standard  — Kamehameha")        .defineInRange("kamehameha",          7_000, 0, 1_000_000);
        TP_MAKANKOSAPPO      = b.comment("Advanced  — Makankosappo")      .defineInRange("makankosappo",       10_000, 0, 1_000_000);
        TP_TAIYOKEN          = b.comment("Basic     — Taiyoken")          .defineInRange("taiyoken",            1_500, 0, 1_000_000);
        TP_BIG_BANG          = b.comment("Advanced  — Big Bang Attack")   .defineInRange("big_bang",           15_000, 0, 1_000_000);
        TP_SPIRIT_BOMB       = b.comment("Legendary — Spirit Bomb")       .defineInRange("spirit_bomb",        35_000, 0, 1_000_000);
        TP_DEATH_BALL        = b.comment("Advanced  — Death Ball")        .defineInRange("death_ball",         20_000, 0, 1_000_000);
        TP_HELLZONE          = b.comment("Advanced  — Hellzone Grenade")  .defineInRange("hellzone",           18_000, 0, 1_000_000);
        TP_FINAL_FLASH       = b.comment("Advanced  — Final Flash")       .defineInRange("final_flash",        25_000, 0, 1_000_000);
        TP_FINAL_KAMEHAMEHA  = b.comment("Legendary — Final Kamehameha")  .defineInRange("final_kamehameha",   45_000, 0, 1_000_000);
        TP_HAKAI             = b.comment("Legendary — Hakai")             .defineInRange("hakai",              50_000, 0, 1_000_000);
        b.pop(); // skill_tp_costs

        SPEC = b.build();
    }

    // ── Getters ───────────────────────────────────────────────────────────

    public static int getCost(FireKiAttackC2S.AttackType type) {
        return switch (type) {
            case KI_LASER         -> KI_LASER_KI_COST.get();
            case DODOMPA          -> DODOMPA_KI_COST.get();
            case KI_VOLLEY        -> KI_VOLLEY_KI_COST.get();
            case MASENKO          -> MASENKO_KI_COST.get();
            case KI_DISC          -> KI_DISC_KI_COST.get();
            case GALICK_GUN       -> GALICK_GUN_KI_COST.get();
            case KAMEHAMEHA       -> KAMEHAMEHA_KI_COST.get();
            case MAKANKOSAPPO     -> MAKANKOSAPPO_KI_COST.get();
            case TAIYOKEN         -> TAIYOKEN_KI_COST.get();
            case BIG_BANG         -> BIG_BANG_KI_COST.get();
            case SPIRIT_BOMB      -> SPIRIT_BOMB_KI_COST.get();
            case DEATH_BALL       -> DEATH_BALL_KI_COST.get();
            case HELLZONE         -> HELLZONE_KI_COST.get();
            case FINAL_FLASH      -> FINAL_FLASH_KI_COST.get();
            case FINAL_KAMEHAMEHA -> FINAL_KAMEHAMEHA_KI_COST.get();
            case HAKAI            -> HAKAI_KI_COST.get();
        };
    }

    /**
     * Returns cooldown in ticks (converted from seconds in config).
     * Multiply seconds * 20 to get ticks.
     */
    public static int getCooldown(FireKiAttackC2S.AttackType type) {
        double seconds = switch (type) {
            case KI_LASER         -> KI_LASER_COOLDOWN.get();
            case DODOMPA          -> DODOMPA_COOLDOWN.get();
            case KI_VOLLEY        -> KI_VOLLEY_COOLDOWN.get();
            case MASENKO          -> MASENKO_COOLDOWN.get();
            case KI_DISC          -> KI_DISC_COOLDOWN.get();
            case GALICK_GUN       -> GALICK_GUN_COOLDOWN.get();
            case KAMEHAMEHA       -> KAMEHAMEHA_COOLDOWN.get();
            case MAKANKOSAPPO     -> MAKANKOSAPPO_COOLDOWN.get();
            case TAIYOKEN         -> TAIYOKEN_COOLDOWN.get();
            case BIG_BANG         -> BIG_BANG_COOLDOWN.get();
            case SPIRIT_BOMB      -> SPIRIT_BOMB_COOLDOWN.get();
            case DEATH_BALL       -> DEATH_BALL_COOLDOWN.get();
            case HELLZONE         -> HELLZONE_COOLDOWN.get();
            case FINAL_FLASH      -> FINAL_FLASH_COOLDOWN.get();
            case FINAL_KAMEHAMEHA -> FINAL_KAMEHAMEHA_COOLDOWN.get();
            case HAKAI            -> HAKAI_COOLDOWN.get();
        };
        return (int) Math.round(seconds * 20.0);
    }

    /**
     * Returns damage scale as float.
     * Use as: (float) data.getKiDamage() * AddonConfig.getDamageScale(type)
     */
    public static float getDamageScale(FireKiAttackC2S.AttackType type) {
        double scale = switch (type) {
            case KI_LASER         -> KI_LASER_DAMAGE_SCALE.get();
            case DODOMPA          -> DODOMPA_DAMAGE_SCALE.get();
            case KI_VOLLEY        -> KI_VOLLEY_DAMAGE_SCALE.get();
            case MASENKO          -> MASENKO_DAMAGE_SCALE.get();
            case KI_DISC          -> KI_DISC_DAMAGE_SCALE.get();
            case GALICK_GUN       -> GALICK_GUN_DAMAGE_SCALE.get();
            case KAMEHAMEHA       -> KAMEHAMEHA_DAMAGE_SCALE.get();
            case MAKANKOSAPPO     -> MAKANKOSAPPO_DAMAGE_SCALE.get();
            case TAIYOKEN         -> TAIYOKEN_DAMAGE_SCALE.get();
            case BIG_BANG         -> BIG_BANG_DAMAGE_SCALE.get();
            case SPIRIT_BOMB      -> SPIRIT_BOMB_DAMAGE_SCALE.get();
            case DEATH_BALL       -> DEATH_BALL_DAMAGE_SCALE.get();
            case HELLZONE         -> HELLZONE_DAMAGE_SCALE.get();
            case FINAL_FLASH      -> FINAL_FLASH_DAMAGE_SCALE.get();
            case FINAL_KAMEHAMEHA -> FINAL_KAMEHAMEHA_DAMAGE_SCALE.get();
            case HAKAI            -> HAKAI_DAMAGE_SCALE.get();
        };
        return (float) scale;
    }

    /**
     * Returns Ki drained per tick during charge.
     * Converts from Ki/second in config: value / 20.
     * Returns 0 for non-chargeable attacks.
     */
    public static float getChargeKiPerTick(FireKiAttackC2S.AttackType type) {
        int perSec = switch (type) {
            case KI_LASER         -> KI_LASER_CHARGE_KI_PER_SEC.get();
            case DODOMPA          -> DODOMPA_CHARGE_KI_PER_SEC.get();
            case MASENKO          -> MASENKO_CHARGE_KI_PER_SEC.get();
            case GALICK_GUN       -> GALICK_GUN_CHARGE_KI_PER_SEC.get();
            case KAMEHAMEHA       -> KAMEHAMEHA_CHARGE_KI_PER_SEC.get();
            case MAKANKOSAPPO     -> MAKANKOSAPPO_CHARGE_KI_PER_SEC.get();
            case BIG_BANG         -> BIG_BANG_CHARGE_KI_PER_SEC.get();
            case SPIRIT_BOMB      -> SPIRIT_BOMB_CHARGE_KI_PER_SEC.get();
            case DEATH_BALL       -> DEATH_BALL_CHARGE_KI_PER_SEC.get();
            case FINAL_FLASH      -> FINAL_FLASH_CHARGE_KI_PER_SEC.get();
            case FINAL_KAMEHAMEHA -> FINAL_KAMEHAMEHA_CHARGE_KI_PER_SEC.get();
            default               -> 0; // non-chargeable attacks
        };
        return perSec / 20.0f;
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

    /**
     * Returns a map of skill ID → TP cost for all addon Ki attacks.
     * Used by SkillsConfigPatcher to write costs into skills_config.json.
     */
    public static java.util.Map<String, Integer> getAllTpCosts() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        map.put("addon_ki_laser",         TP_KI_LASER.get());
        map.put("addon_dodompa",          TP_DODOMPA.get());
        map.put("addon_ki_volley",        TP_KI_VOLLEY.get());
        map.put("addon_masenko",          TP_MASENKO.get());
        map.put("addon_ki_disc",          TP_KI_DISC.get());
        map.put("addon_galick_gun",       TP_GALICK_GUN.get());
        map.put("addon_kamehameha",       TP_KAMEHAMEHA.get());
        map.put("addon_makankosappo",     TP_MAKANKOSAPPO.get());
        map.put("addon_taiyoken",         TP_TAIYOKEN.get());
        map.put("addon_big_bang",         TP_BIG_BANG.get());
        map.put("addon_spirit_bomb",      TP_SPIRIT_BOMB.get());
        map.put("addon_death_ball",       TP_DEATH_BALL.get());
        map.put("addon_hellzone",         TP_HELLZONE.get());
        map.put("addon_final_flash",      TP_FINAL_FLASH.get());
        map.put("addon_final_kamehameha", TP_FINAL_KAMEHAMEHA.get());
        map.put("addon_hakai",            TP_HAKAI.get());
        return map;
    }
}