package hik1tka.risen_races.client.humanoid.human.model.profession.hat;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class FarmerHatModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart hat;

    public FarmerHatModel(ModelPart root) {
        this.hat = root.getChild("hat");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Додано Dilation(0.5F) для збільшення моделі та запобігання Z-fighting
        modelPartData.addChild("hat", ModelPartBuilder.create()
                        .uv(15, 48).cuboid(-8.0F, -8.0F, -8.0F, 16.0F, -0.6F, 16.0F, new Dilation(0.6F))
                        .uv(32, 0).cuboid(-4.0F, -13.2F, -4.0F, 8.0F, 4.0F, 8.0F, new Dilation(0.6F)), // Тулія
                ModelTransform.pivot(0.0F, 3.0F, 0.0F)); 

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        hat.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}