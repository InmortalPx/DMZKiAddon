package com.dmzkiaddon.client.model;

import com.dmzkiaddon.DMZKiAddon;
import com.dmzkiaddon.entity.masters.MasterHitEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MasterHitModel extends GeoModel<MasterHitEntity> {

    @Override
    public ResourceLocation getModelResource(MasterHitEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(DMZKiAddon.MOD_ID,
                "geo/master_hit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MasterHitEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(DMZKiAddon.MOD_ID,
                "textures/entity/master/hit.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MasterHitEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonminez",
                "animations/entity/master/master_kaiosama.animation.json");
    }
}