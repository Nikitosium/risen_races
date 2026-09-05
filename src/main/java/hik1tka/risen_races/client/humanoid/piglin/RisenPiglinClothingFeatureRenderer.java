package hik1tka.risen_races.client.humanoid.piglin;

import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Той самий принцип, що NPCClothingFeatureRenderer у людини - малює текстуру
 * "фартуха" професії поверх базової моделі. EntityModel<RisenPiglinEntity>
 * (не конкретний PiglinEntityModel) - бо RisenPiglinEntityRender перемикає
 * this.model між чоловічою (PiglinEntityModel) і жіночою (RisenFemalePiglinModel)
 * моделями, тому спільний тип фіча-рендера має бути найзагальніший.
 */
public class RisenPiglinClothingFeatureRenderer extends FeatureRenderer<RisenPiglinEntity, EntityModel<RisenPiglinEntity>> {

    public RisenPiglinClothingFeatureRenderer(FeatureRendererContext<RisenPiglinEntity, EntityModel<RisenPiglinEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, RisenPiglinEntity entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        String prof = entity.getProfession();
        if (entity.isInvisible() || "none".equals(prof)) return;

        // TODO: якщо для якоїсь професії текстури фартуха ще нема - як у
        // Human з "farmer", додай сюди такий самий early return.
        Identifier professionId = new Identifier("risen_races",
                "textures/entity/human/profession/" + prof + ".png");

        renderModel(this.getContextModel(), professionId, matrices, vertexConsumers, light, entity, 1.0F, 1.0F, 1.0F);
    }
}