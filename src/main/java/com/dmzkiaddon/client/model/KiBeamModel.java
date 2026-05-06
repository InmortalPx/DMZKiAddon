package com.dmzkiaddon.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Modelo para beams de ki (Kamehameha, Galick Gun, etc.).
 * Dos planos cruzados orientados a lo largo del eje Z.
 */
public class KiBeamModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation("dmzkiaddon", "ki_beam"), "main");

    private final ModelPart planeH;
    private final ModelPart planeV;

    public KiBeamModel(ModelPart root) {
        this.planeH = root.getChild("plane_h");
        this.planeV = root.getChild("plane_v");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Plano horizontal — a lo largo del beam
        root.addOrReplaceChild("plane_h",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -0.5F, -8.0F, 16.0F, 1.0F, 16.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Plano vertical — cruzado 90°
        root.addOrReplaceChild("plane_v",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.5F, -8.0F, -8.0F, 1.0F, 16.0F, 16.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float rot = ageInTicks * 0.6F;
        planeH.zRot = rot;
        planeV.zRot = rot + (float) Math.PI / 2.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        planeH.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        planeV.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
