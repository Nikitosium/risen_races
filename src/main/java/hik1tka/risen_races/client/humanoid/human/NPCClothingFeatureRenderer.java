package hik1tka.risen_races.client.humanoid.human;

import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NPCClothingFeatureRenderer extends FeatureRenderer<HumanEntity, PlayerEntityModel<HumanEntity>> {
    public NPCClothingFeatureRenderer(FeatureRendererContext<HumanEntity, PlayerEntityModel<HumanEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, HumanEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        // Перевіряємо професію безпосередньо з нашої сутності
        if (entity.isInvisible() || "none".equals(entity.getProfession())) return;

        String prof = entity.getProfession();

        // Перевірка: якщо це фермер, ми припиняємо рендер тут, і фартух не малюється
        if (prof.equals("farmer")) {
            return;
        }

        Identifier professionId = new Identifier("risen_races", "textures/entity/human/profession/" + prof + ".png");
        renderModel(this.getContextModel(), professionId, matrices, vertexConsumers, light, entity, 1.0F, 1.0F, 1.0F);
    }
}