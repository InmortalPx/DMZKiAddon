package com.dmzkiaddon.client.renderer;

import com.dmzkiaddon.DMZKiAddon;
import com.dmzkiaddon.client.model.MasterHitModel;
import com.dmzkiaddon.entity.masters.MasterHitEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MasterHitRenderer extends GeoEntityRenderer<MasterHitEntity> {

    public MasterHitRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MasterHitModel());
    }

    @Override
    public ResourceLocation getTextureLocation(MasterHitEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(DMZKiAddon.MOD_ID,
                "textures/entity/master/hit.png");
    }
}
