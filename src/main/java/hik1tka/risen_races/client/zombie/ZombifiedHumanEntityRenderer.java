package hik1tka.risen_races.client.zombie;

import hik1tka.risen_races.entity.zombie.ZombifiedHumanEntity;
import hik1tka.risen_races.mixin.BipedModelAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Одна спільна ZombieEntityModel (не дві окремі геометрії, як у пігліна) -
 * гендер видно через скейл рук, той самий accessor-прийом, що з носом
 * Ринара. Текстура - твоя власна (не ванільний зомбі), і поки ОДНА на обидві
 * статі - руки в жіночого варіанту трохи тонші, але з тим самим малюнком
 * (без окремого slim-UV) - за твоїм рішенням у цій розмові.
 */
public class ZombifiedHumanEntityRenderer extends MobEntityRenderer<ZombifiedHumanEntity, ZombieEntityModel<ZombifiedHumanEntity>> {

    private static final Identifier TEXTURE =
            new Identifier("risen_races", "textures/entity/zombie/zombified_human.png");

    private static final float FEMALE_ARM_SCALE = 0.82f;
    private static final float MALE_ARM_SCALE = 1.0f;

    public ZombifiedHumanEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new ZombieEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public void render(ZombifiedHumanEntity entity, float yaw, float tickDelta, MatrixStack matrices,
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
    public Identifier getTexture(ZombifiedHumanEntity entity) {
        return TEXTURE;
    }
}
