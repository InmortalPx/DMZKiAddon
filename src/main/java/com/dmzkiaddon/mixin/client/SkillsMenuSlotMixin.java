package com.dmzkiaddon.mixin.client;

import com.dmzkiaddon.registry.AttackRegistry;
import com.dragonminez.client.gui.utilitymenu.menuslots.SkillsMenuSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SkillsMenuSlot.class)
public class SkillsMenuSlotMixin {

    @Redirect(
        method = "render(Lcom/dragonminez/common/stats/StatsData;)Lcom/dragonminez/client/gui/utilitymenu/ButtonInfo;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent redirectSkillTranslation(String key) {
        if (key.startsWith("skill.dragonminez.custom_")) {
            if (key.endsWith(".desc")) {
                return Component.literal("");
            }
            String id = key.substring("skill.dragonminez.".length());
            return Component.literal(AttackRegistry.getName(id));
        }
        return Component.translatable(key);
    }
}
