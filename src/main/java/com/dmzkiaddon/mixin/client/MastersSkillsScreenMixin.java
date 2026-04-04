package com.dmzkiaddon.mixin.client;

import com.dmzkiaddon.registry.AttackRegistry;
import com.dragonminez.client.gui.MastersSkillsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MastersSkillsScreen.class)
public class MastersSkillsScreenMixin {

    @Redirect(
        method = {
            "renderSkillsList(Lnet/minecraft/client/gui/GuiGraphics;IIII)V",
            "renderRightPanel(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            "renderSkillDetails(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            "initPurchaseButton()V"
        },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent redirectSkillNameTranslation(String key) {
        if (key.startsWith("skill.dragonminez.custom_")) {
            if (key.endsWith(".desc")) {
                return Component.literal("");
            }
            String id = key.substring("skill.dragonminez.".length());
            return Component.literal(AttackRegistry.getName(id));
        }
        return Component.translatable(key);
    }

    @Redirect(
        method = {
            "renderSkillsList(Lnet/minecraft/client/gui/GuiGraphics;IIII)V",
            "renderRightPanel(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            "renderSkillDetails(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            "initPurchaseButton()V"
        },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent redirectSkillNameTranslationWithArgs(String key, Object[] args) {
        if (key.startsWith("skill.dragonminez.custom_")) {
            if (key.endsWith(".desc")) {
                return Component.literal("");
            }
            String id = key.substring("skill.dragonminez.".length());
            return Component.literal(AttackRegistry.getName(id));
        }
        return Component.translatable(key, args);
    }
}
