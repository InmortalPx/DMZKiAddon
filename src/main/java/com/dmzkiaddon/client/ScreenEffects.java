package com.dmzkiaddon.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class ScreenEffects {

    private static int shakeTimer = 0;
    private static int shakeDuration = 0;
    private static float shakeIntensity = 0f;
    private static boolean isCharging = false;
    private static float chargeProgress = 0f;
    private static float chargeR = 1f, chargeG = 1f, chargeB = 1f;
    private static String attackName = "";
    private static int baseCost = 0;
    private static int maxKi = 0;
    private static int currentKi = 0;
    private static int flashTimer = 0;
    private static float flashAlpha = 0f;
    private static boolean hakaiActive = false;
    private static float hakaiMyProgress = 0f;
    private static float hakaiEnemyProgress = 0f;
    private static int hakaiFlashTimer = 0;
    private static boolean hakaiWon = false;
    private static int timeSkipTimer = 0;
    private static final int TIME_SKIP_DURATION = 100;
    private static int pointPressureAuraTimer = 0;
    private static final int POINT_PRESSURE_AURA_DURATION = 18;
    private static int finalExplosionVit = 0;
    private static float finalExplosionDmg = 0f;
    private static boolean isFinalExplosion = false;
    private static int kikohoFlashTimer = 0;
    private static int neoKikohoCombo = 0;

    public static void resetAllEffects() {
        shakeTimer = 0;
        shakeDuration = 0;
        shakeIntensity = 0f;
        flashTimer = 0;
        flashAlpha = 0f;
        timeSkipTimer = 0;
        pointPressureAuraTimer = 0;
        isCharging = false;
        isFinalExplosion = false;
        attackName = "";
        chargeProgress = 0f;
    }

    public static void setFinalExplosionStats(int vit, float estimatedDamage) {
        finalExplosionVit = vit;
        finalExplosionDmg = estimatedDamage;
    }

    public static boolean isFinalExplosionCharging() {
        return isCharging && isFinalExplosion && attackName.equals("Final Explosion");
    }

    public static int getFinalExplosionChargeTick() {
        if (!isFinalExplosionCharging()) return 0;
        return (int)(chargeProgress * 80);
    }

    public static void triggerKikohoFlash() {
        kikohoFlashTimer = 12;
    }

    public static void setNeoKikohoCombo(int combo) {
        neoKikohoCombo = combo;
    }

    public static void resetNeoKikohoCombo() {
        neoKikohoCombo = 0;
    }

    public static void triggerFlash(float alpha, int duration) {
        flashAlpha = Math.max(flashAlpha, alpha);
        flashTimer = duration;
    }

    public static void triggerShake(int intensity, int duration) {
        shakeIntensity = Math.max(shakeIntensity, intensity);
        shakeDuration = duration;
        shakeTimer = duration;
    }

    public static void triggerTimeSkip() {
        timeSkipTimer = TIME_SKIP_DURATION;
    }

    public static void triggerPointPressureAura() {
        pointPressureAuraTimer = POINT_PRESSURE_AURA_DURATION;
    }

    public static boolean isPointPressureAuraActive() {
        return pointPressureAuraTimer > 0;
    }

    public static int getPointPressureAuraTicks() {
        return pointPressureAuraTimer;
    }

    public static void setCharging(boolean charging, float progress, float r, float g, float b, String name, int kiBaseCost) {
        isCharging = charging;
        chargeProgress = progress;
        chargeR = r;
        chargeG = g;
        chargeB = b;
        attackName = name;
        baseCost = kiBaseCost;

        if (name.equals("Final Explosion")) {
            isFinalExplosion = true;
        } else {
            isFinalExplosion = false;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            com.dragonminez.common.stats.StatsProvider.get(
                    com.dragonminez.common.stats.StatsCapability.INSTANCE, mc.player)
                    .ifPresent(stats -> {
                        maxKi = stats.getMaxEnergy();
                        currentKi = stats.getResources().getCurrentEnergy();
                    });
        }
    }

    public static void stopCharging() {
        isCharging = false;
        isFinalExplosion = false;
        attackName = "";
        chargeProgress = 0f;
    }

    public static void updateHakaiBar(float myProgress, float enemyProgress) {
        hakaiActive = true;
        hakaiMyProgress = myProgress;
        hakaiEnemyProgress = enemyProgress;
    }

    public static void stopHakaiMinigame(boolean won) {
        hakaiActive = false;
        hakaiWon = won;
        hakaiFlashTimer = 40;
    }

    public static boolean isHakaiActive() {
        return hakaiActive;
    }

    public static void tick() {
        if (shakeTimer > 0) shakeTimer--;
        if (flashTimer > 0) flashTimer--;
        if (hakaiFlashTimer > 0) hakaiFlashTimer--;
        if (timeSkipTimer > 0) timeSkipTimer--;
        if (pointPressureAuraTimer > 0) pointPressureAuraTimer--;
        if (kikohoFlashTimer > 0) kikohoFlashTimer--;
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.level().isClientSide()) return;

        if (shakeTimer > 0) {
            double time = mc.player.level().getGameTime() + event.getPartialTick();
            float progress = (float) shakeTimer / shakeDuration;
            float amp = shakeIntensity * progress;
            event.setYaw((float) (event.getYaw() + Math.sin(time * 1.8) * amp));
            event.setPitch((float) (event.getPitch() + Math.cos(time * 2.2) * amp * 0.5));
            event.setRoll((float) (event.getRoll() + Math.sin(time * 3.0) * amp * 0.3));
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Window window = mc.getWindow();
        int screenW = window.getGuiScaledWidth();
        int screenH = window.getGuiScaledHeight();
        GuiGraphics gfx = event.getGuiGraphics();

        if (flashTimer > 0) {
            float fadeProgress = (float) flashTimer / 20f;
            float fade = Math.min(1f, fadeProgress);
            int color = net.minecraft.util.FastColor.ARGB32.color((int)(flashAlpha * fade * 255), 255, 255, 255);
            gfx.fill(0, 0, screenW, screenH, color);
        }

        if (timeSkipTimer > 0) {
            int elapsed = TIME_SKIP_DURATION - timeSkipTimer;
            float fade = timeSkipTimer > 15 ? 1.0f : timeSkipTimer / 15.0f;
            int borderAlpha = (int)(fade * 120);
            int borderColor = net.minecraft.util.FastColor.ARGB32.color(
                    borderAlpha, 160, 60, 255);
            int thickness = 3;
            gfx.fill(0, 0, thickness, screenH, borderColor);
            gfx.fill(screenW - thickness, 0, screenW, screenH, borderColor);
            gfx.fill(0, 0, screenW, thickness, borderColor);
            gfx.fill(0, screenH - thickness, screenW, screenH, borderColor);

            if (elapsed < 20) {
                String label = "TIME SKIP";
                int labelW = mc.font.width(label);
                int textAlpha = (int)(Math.min(1f, elapsed / 6.0f) * 200);
                gfx.drawString(mc.font, Component.literal(label),
                        screenW / 2 - labelW / 2, screenH / 2 - 10,
                        net.minecraft.util.FastColor.ARGB32.color(textAlpha, 210, 120, 255), true);
            }

            float secsLeft = timeSkipTimer / 20.0f;
            String timer = String.format("%.1fs", secsLeft);
            gfx.drawString(mc.font, Component.literal(timer),
                    screenW - mc.font.width(timer) - 8, 8,
                    net.minecraft.util.FastColor.ARGB32.color((int)(fade * 200), 200, 150, 255), true);
        }

        if (kikohoFlashTimer > 0) {
            float fade = (float) kikohoFlashTimer / 12.0f;
            int borderAlpha = (int)(fade * 180);
            int borderColor = net.minecraft.util.FastColor.ARGB32.color(borderAlpha, 255, 140, 0);
            int thickness = 4;
            gfx.fill(0, 0, thickness, screenH, borderColor);
            gfx.fill(screenW - thickness, 0, screenW, screenH, borderColor);
            gfx.fill(0, 0, screenW, thickness, borderColor);
            gfx.fill(0, screenH - thickness, screenW, screenH, borderColor);
        }

        if (neoKikohoCombo > 0) {
            int vigAlpha = 140;
            int vigColor = net.minecraft.util.FastColor.ARGB32.color(vigAlpha, 200, 0, 0);
            int vt = 12;
            gfx.fill(0, 0, vt, screenH, vigColor);
            gfx.fill(screenW - vt, 0, screenW, screenH, vigColor);
            gfx.fill(0, 0, screenW, vt, vigColor);
            gfx.fill(0, screenH - vt, screenW, screenH, vigColor);
            
            String comboText = "COMBO x" + neoKikohoCombo;
            int textW = mc.font.width(comboText);
            gfx.drawString(mc.font, Component.literal(comboText),
                    screenW / 2 - textW / 2, screenH / 2 + 20,
                    net.minecraft.util.FastColor.ARGB32.color(255, 255, 100, 0), true);
        }

        if (isCharging && chargeProgress > 0.15f && !isFinalExplosion) {
            int r = (int)(chargeR * 255);
            int g = (int)(chargeG * 255);
            int b = (int)(chargeB * 255);
            int vigAlpha = (int)(Math.min(chargeProgress, 0.85f) * 120);
            int vigColor = net.minecraft.util.FastColor.ARGB32.color(vigAlpha, r, g, b);
            int vt = 4 + (int)(chargeProgress * 14);
            gfx.fill(0, 0, vt, screenH, vigColor);
            gfx.fill(screenW - vt, 0, screenW, screenH, vigColor);
            gfx.fill(0, 0, screenW, vt, vigColor);
            gfx.fill(0, screenH - vt, screenW, screenH, vigColor);
        }

        if (hakaiActive) {
            renderHakaiBar(gfx, mc, screenW, screenH);
        }

        if (isCharging && !attackName.isEmpty()) {
            com.dragonminez.common.stats.StatsProvider.get(
                    com.dragonminez.common.stats.StatsCapability.INSTANCE, mc.player)
                    .ifPresent(stats -> {
                        maxKi = stats.getMaxEnergy();
                        currentKi = stats.getResources().getCurrentEnergy();
                    });

            int hudX = screenW / 2 - 60;
            int barY = screenH - 40;
            int barW = 120;
            int barH = 8;
            int fillW = (int)(barW * chargeProgress);

            int colorAtaque = net.minecraft.util.FastColor.ARGB32.color(200,
                    (int)(chargeR * 255), (int)(chargeG * 255), (int)(chargeB * 255));

            gfx.fill(hudX, barY, hudX + barW, barY + barH, 0xAA000000);
            gfx.fill(hudX, barY, hudX + fillW, barY + barH, colorAtaque);
            gfx.drawString(mc.font, Component.literal(attackName), hudX, barY - 12, colorAtaque, true);

            if (isFinalExplosion) {
                gfx.drawString(mc.font,
                        Component.literal("VIT: " + finalExplosionVit),
                        hudX + barW + 4, barY - 10, 0xFF88FF44, true);
                int estimatedDmg = (int)(finalExplosionDmg);
                gfx.drawString(mc.font,
                        Component.literal("DMG: ~" + estimatedDmg),
                        hudX + barW + 4, barY + 2, 0xFFFF4444, true);
            } else {
                int realCost = (int)(maxKi * (baseCost / 100f) * (1f + chargeProgress));
                int costColor = (realCost > currentKi && currentKi > 0) ? 0xFFFF4444 : 0xFFFFFFFF;
                gfx.drawString(mc.font, Component.literal("Ki: " + realCost),
                        hudX + barW + 4, barY, costColor, true);
            }
        }
    }

    private static void renderHakaiBar(GuiGraphics gfx, Minecraft mc, int screenW, int screenH) {
        int barTotalW = 200;
        int barX = screenW / 2 - barTotalW / 2;
        int barY = screenH / 2 - 30;
        int barH = 10;
        int halfW = barTotalW / 2;

        gfx.fill(barX, barY, barX + barTotalW, barY + barH, 0xAA000000);

        int myFill = (int)(halfW * hakaiMyProgress);
        gfx.fill(barX, barY, barX + myFill, barY + barH, 0xFF3399FF);

        int enemyBarX = barX + halfW;
        int enemyFill = (int)(halfW * hakaiEnemyProgress);
        gfx.fill(enemyBarX + halfW - enemyFill, barY, enemyBarX + halfW, barY + barH, 0xFFFF3333);

        String label = Component.translatable("msg.dmzkiaddon.hakai_label").getString();
        int labelW = mc.font.width(label);
        gfx.drawString(mc.font, Component.literal(label), screenW / 2 - labelW / 2, barY - 14, 0xFFFFD700, true);

        String hint = Component.translatable("msg.dmzkiaddon.hakai_hint").getString();
        int hintW = mc.font.width(hint);
        gfx.drawString(mc.font, Component.literal(hint), screenW / 2 - hintW / 2, barY + barH + 4, 0xFFFFFFFF, true);
    }

    public static void updateHUD(int cooldown, float r, float g, float b, String name, int cost) {
        chargeR = r;
        chargeG = g;
        chargeB = b;
        attackName = name;
        baseCost = cost;
    }
}