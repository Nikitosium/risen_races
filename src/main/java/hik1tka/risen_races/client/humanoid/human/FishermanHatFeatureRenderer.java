package hik1tka.risen_races.client.humanoid.human;

import hik1tka.risen_races.client.humanoid.human.model.profession.hat.FishermanHatModel;
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

// M послаблено до просто EntityModel<T> (без & ModelWithHead) - див.
// коментар у FarmerHatFeatureRenderer, той самий випадок.
public class FishermanHatFeatureRenderer<T extends HumanoidEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private final FishermanHatModel<T> hatModel;

    // ПЕРЕВІР ЦЕЙ ШЛЯХ: файл має лежати в assets/m_and_d/textures/entity/human/profession/fisherman.png
    private static final Identifier TEXTURE = new Identifier("risen_races", "textures/entity/human/profession/fisherman_hat.png");

    public FishermanHatFeatureRenderer(FeatureRendererContext<T, M> context, FishermanHatModel<T> hatModel) {
        super(context);
        this.hatModel = hatModel;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        // Рендеримо ТІЛЬКИ якщо професія "fisherman"
        if (!"fisherman".equals(entity.getHumanoidData().getProfession()) || entity.isInvisible()) return;

        matrices.push();

        // Прив'язка до голови NPC - runtime-перевірка (див. коментар на класі).
        if (this.getContextModel() instanceof net.minecraft.client.render.entity.model.ModelWithHead modelWithHead) {
            modelWithHead.getHead().rotate(matrices);
        }
        matrices.translate(0.0, entity.getHatYOffset(), 0.0);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(TEXTURE));
        this.hatModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

        matrices.pop();
    }
}