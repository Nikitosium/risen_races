package hik1tka.risen_races.client.humanoid.piglin;

import hik1tka.risen_races.client.humanoid.piglin.model.RisenFemalePiglinModel;
import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import hik1tka.risen_races.register.ModModelLayers;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * На відміну від RynarEntityRender (там одна геометрія, різниця лише в
 * scale) - тут чоловіки й жінки мають СПРАВДІ різну геометрію (жіночі руки
 * вужчі не через скейл, а вже так намальовані в моделі; жіноча голова без
 * "морди"/ікол узагалі). Тому тут два окремих екземпляри моделі, а не
 * скейл-перемикач на спільній.
 *
 * Чоловік - ванільна геометрія (PiglinEntityModel + EntityModelLayers.PIGLIN),
 * як і раніше. Жінка - RisenFemalePiglinModel, зібрана вручну з
 * female_piglin.json.
 */
public class RisenPiglinEntityRender extends MobEntityRenderer<RisenPiglinEntity, EntityModel<RisenPiglinEntity>> {

    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/entity/piglin/piglin.png");

    private final PiglinEntityModel<RisenPiglinEntity> maleModel;
    private final RisenFemalePiglinModel<RisenPiglinEntity> femaleModel;

    public RisenPiglinEntityRender(EntityRendererFactory.Context context) {
        super(context, new PiglinEntityModel<>(context.getPart(EntityModelLayers.PIGLIN)), 0.5F);
        this.maleModel = new PiglinEntityModel<>(context.getPart(EntityModelLayers.PIGLIN));
        this.femaleModel = new RisenFemalePiglinModel<>(context.getPart(ModModelLayers.RISEN_PIGLIN_FEMALE));
    }

    @Override
    public void render(RisenPiglinEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        this.model = entity.isFemale() ? this.femaleModel : this.maleModel;
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(RisenPiglinEntity entity) {
        // Та сама ванільна текстура для обох - UV-координати в
        // RisenFemalePiglinModel скопійовані з того самого файлу, що й
        // ванільна модель, тому текстура одна й та сама пасує обом.
        return TEXTURE;
    }

    @Override
    protected void scale(RisenPiglinEntity entity, MatrixStack matrices, float amount) {
        float babyScale = entity.getScaleFactor();
        matrices.scale(babyScale, babyScale, babyScale);
    }
}