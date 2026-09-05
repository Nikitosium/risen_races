package hik1tka.risen_races.client.humanoid.human;

import hik1tka.risen_races.client.humanoid.human.model.profession.hat.FarmerHatModel;
import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

// M послаблено до просто EntityModel<T> (без & ModelWithHead) - для
// RisenPiglinEntityRender, що перемикає this.model між двома різними
// класами моделі, немає способу статично довести дженериком, що ОБИДВІ
// завжди мають ModelWithHead. Замість цього - runtime-перевірка нижче;
// обидві наші моделі (PiglinEntityModel, RisenFemalePiglinModel) насправді
// його реалізують, тому це безпечно.
public class FarmerHatFeatureRenderer<T extends HumanoidEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private final FarmerHatModel<T> hatModel;
    private static final Identifier TEXTURE = new Identifier("risen_races", "textures/entity/human/profession/farmer.png");

    public FarmerHatFeatureRenderer(FeatureRendererContext<T, M> context, FarmerHatModel<T> hatModel) {
        super(context);
        this.hatModel = hatModel;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        // Малюємо ТІЛЬКИ якщо професія "farmer"
        if (!"farmer".equals(entity.getHumanoidData().getProfession()) || entity.isInvisible()) return;

        matrices.push();

        // Прив'язка до голови NPC - runtime-перевірка (див. коментар на класі).
        if (this.getContextModel() instanceof ModelWithHead modelWithHead) {
            modelWithHead.getHead().rotate(matrices);
        }
        
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        this.hatModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
        
        matrices.pop();
    }
}