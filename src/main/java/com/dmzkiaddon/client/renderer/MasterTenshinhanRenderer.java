package com.dmzkiaddon.client.renderer;

import com.dmzkiaddon.DMZKiAddon;
import com.dmzkiaddon.client.model.MasterTenshinhanModel;
import com.dmzkiaddon.entity.masters.MasterTenshinhanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MasterTenshinhanRenderer extends GeoEntityRenderer<MasterTenshinhanEntity> {

    public MasterTenshinhanRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MasterTenshinhanModel());
    }

    @Override
    public ResourceLocation getTextureLocation(MasterTenshinhanEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(DMZKiAddon.MOD_ID,
                "textures/entity/master/tien.png");
    }
}
