package hik1tka.risen_races.client.humanoid.human.model.profession.hat;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;

public class FishermanHatModel<T extends Entity> extends EntityModel<T> {
    private final ModelPart hat;

    public FishermanHatModel(ModelPart root) {
        this.hat = root.getChild("hat");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        // Додано Dilation(0.5F) для розширення моделі на 1 піксель сумарно
        modelPartData.addChild("hat", ModelPartBuilder.create()
                        .uv(26, 2).cuboid(-6.0F, -9.0F, -6.0F, 12.0F, 0.0F, 12.0F, new Dilation(0.6F)) // Поля шляпи
                        .uv(0, 0).cuboid(-4.0F, -13.2F, -4.0F, 8.0F, 4.0F, 8.0F, new Dilation(0.6F)), // Верх шляпи
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