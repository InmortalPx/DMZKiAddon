package com.dmzkiaddon.client.model;

import com.dmzkiaddon.DMZKiAddon;
import com.dmzkiaddon.entity.masters.MasterTenshinhanEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MasterTenshinhanModel extends GeoModel<MasterTenshinhanEntity> {

    @Override
    public ResourceLocation getModelResource(MasterTenshinhanEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(DMZKiAddon.MOD_ID,
                "geo/master_tenshinhan.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MasterTenshinhanEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(DMZKiAddon.MOD_ID,
                "textures/entity/master/tien.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MasterTenshinhanEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("dragonminez",
                "animations/entity/master/master_goku.animation.json");
    }
}