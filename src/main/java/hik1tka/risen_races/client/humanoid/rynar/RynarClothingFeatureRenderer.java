package hik1tka.risen_races.client.humanoid.rynar;

import hik1tka.risen_races.entity.humanoid.rynar.RynarEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RynarClothingFeatureRenderer extends FeatureRenderer<RynarEntity, VillagerResemblingModel<RynarEntity>> {

    public RynarClothingFeatureRenderer(FeatureRendererContext<RynarEntity, VillagerResemblingModel<RynarEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, RynarEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (entity.isInvisible() || "none".equals(entity.getHumanoidData().getProfession())) return;

        String prof = entity.getHumanoidData().getProfession();

        // Пропускаємо рендер для фермера
        if ("farmer".equals(prof)) {
            return;
        }

        // Беремо текстуру професії
        Identifier professionId = new Identifier("risen_races", "textures/entity/human/profession/" + prof + ".png");

        renderModel(this.getContextModel(), professionId, matrices, vertexConsumers, light, entity, 1.0F, 1.0F, 1.0F);
    }
}