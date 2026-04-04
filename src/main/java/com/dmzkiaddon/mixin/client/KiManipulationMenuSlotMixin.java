package com.dmzkiaddon.mixin.client;

import com.dmzkiaddon.registry.AttackRegistry;
import com.dragonminez.client.gui.utilitymenu.menuslots.KiManipulationMenuSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(KiManipulationMenuSlot.class)
public class KiManipulationMenuSlotMixin {

    @Redirect(
        method = "render(Lcom/dragonminez/common/stats/StatsData;)Lcom/dragonminez/client/gui/utilitymenu/ButtonInfo;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
    )
    private MutableComponent redirectKiSelectionTranslation(String key) {
        if (key.contains(".custom_")) {
            if (key.endsWith(".desc")) {
                return Component.literal("");
            }
            String id = key.substring(key.lastIndexOf(".custom_") + 1);
            return Component.literal(AttackRegistry.getName(id));
        }
        return Component.translatable(key);
    }
}
