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
 * Modelo billboard para bolas de ki.
 * Usa dos quads cruzados (X) para dar volumen sin depender de la cámara.
 */
public class KiBallModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation("dmzkiaddon", "ki_ball"), "main");

    private final ModelPart planeA;
    private final ModelPart planeB;

    public KiBallModel(ModelPart root) {
        this.planeA = root.getChild("plane_a");
        this.planeB = root.getChild("plane_b");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Plano A — vertical frontal
        root.addOrReplaceChild("plane_a",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -0.5F, 16.0F, 16.0F, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Plano B — cruzado 90 grados
        root.addOrReplaceChild("plane_b",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -0.5F, 16.0F, 16.0F, 1.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F,
                        0.0F, (float) Math.PI / 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float rot = ageInTicks * 0.4F;
        planeA.yRot = rot;
        planeB.yRot = rot + (float) Math.PI / 2.0F;
        // Pulso suave
        float pulse = 1.0F + (float) Math.sin(ageInTicks * 0.25F) * 0.08F;
        planeA.xScale = planeA.zScale = planeB.xScale = planeB.zScale = pulse;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        planeA.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        planeB.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
