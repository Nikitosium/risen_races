package hik1tka.risen_races.client.zombie;

import hik1tka.risen_races.entity.zombie.ZombifiedHumanDrownedEntity;
import hik1tka.risen_races.mixin.BipedModelAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.DrownedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Рендер утопця-людини. Модель - ВАНІЛЬНА DrownedEntityModel на шарі
 * EntityModelLayers.DROWNED, той самий клас, що використовує ванільний
 * DrownedEntityRenderer.
 *
 * Чому раніше "зовнішній шар" (нарости на тілі утопця) не відображався:
 * старий код мотав геометрію шару DROWNED у ZombieEntityModel, який знає
 * лише стандартний біпедний скелет - додаткові частини утопця (розширені
 * dilated копії кінцівок) ніхто ні рендерив, ні анімував. DrownedEntityModel
 * описує і анімує їх саме так, як у ванільного утопця, - тому беремо її,
 * а не ремонтуємо ZombieEntityModel вручну.
 *
 * Гендерний скейл рук лишається тим самим прийомом (BipedModelAccessor),
 * бо DrownedEntityModel - нащадок ZombieEntityModel/BipedEntityModel.
 */
public class ZombifiedHumanDrownedRenderer extends MobEntityRenderer<ZombifiedHumanDrownedEntity, DrownedEntityModel<ZombifiedHumanDrownedEntity>> {

    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/entity/zombie/drowned.png");

    private static final float FEMALE_ARM_SCALE = 0.82f;
    private static final float MALE_ARM_SCALE = 1.0f;

    public ZombifiedHumanDrownedRenderer(EntityRendererFactory.Context context) {
        super(context, new DrownedEntityModel<>(context.getPart(EntityModelLayers.DROWNED)), 0.5F);
    }

    @Override
    public void render(ZombifiedHumanDrownedEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        BipedModelAccessor accessor = (BipedModelAccessor) this.getModel();
        ModelPart leftArm = accessor.getLeftArm();
        ModelPart rightArm = accessor.getRightArm();

        float armScale = entity.isFemale() ? FEMALE_ARM_SCALE : MALE_ARM_SCALE;
        leftArm.xScale = armScale;
        leftArm.zScale = armScale;
        rightArm.xScale = armScale;
        rightArm.zScale = armScale;

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(ZombifiedHumanDrownedEntity entity) {
        return TEXTURE;
    }
}
