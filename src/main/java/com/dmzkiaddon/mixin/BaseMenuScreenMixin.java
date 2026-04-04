package com.dmzkiaddon.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.dragonminez.client.gui.ScaledScreen;
import com.dragonminez.client.gui.buttons.CustomTextureButton;
import com.dragonminez.client.gui.character.BaseMenuScreen;
import com.dragonminez.common.init.MainSounds;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Mixin(value = BaseMenuScreen.class, remap = false)
public class BaseMenuScreenMixin extends ScaledScreen {

    @Shadow
    protected static ResourceLocation SCREEN_BUTTONS;

    protected BaseMenuScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "initNavigationButtons", at = @At("RETURN"))
    void onInitNavigationButtons(CallbackInfo ci) {
        int centerX = this.getUiWidth() / 2;
        int bottomY = this.getUiHeight() - 30;

        this.addRenderableWidget(
                new CustomTextureButton.Builder()
                        .position(centerX + 80, bottomY)
                        .size(20, 20)
                        .texture(SCREEN_BUTTONS)
                        .textureSize(20, 20)
                        .textureCoords(80, 0, 80, 20)
                        .onPress(btn -> switchMenu(new com.dmzkiaddon.client.gui.KiAttackCreationMenuScreen(
                                Component.literal("Create Ki Attack"))))
                        .sound(MainSounds.UI_MENU_SWITCH.get())
                        .build());
    }

    @Shadow
    protected void switchMenu(net.minecraft.client.gui.screens.Screen screen) {
    }
}
