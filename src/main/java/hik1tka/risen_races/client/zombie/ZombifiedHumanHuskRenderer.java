package hik1tka.risen_races.client.zombie;

import hik1tka.risen_races.entity.zombie.ZombifiedHumanHuskEntity;
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
 * Той самий прийом гендерного скейлу рук, що ZombifiedHumanEntityRenderer -
 * лише інша текстура/шар моделі (ванільний Husk).
 * TODO: якщо EntityModelLayers.HUSK відсутній у твоєму мапінгу - звір
 * точну назву константи в декомпільованому EntityModelLayers.
 */
public class ZombifiedHumanHuskRenderer extends MobEntityRenderer<ZombifiedHumanHuskEntity, ZombieEntityModel<ZombifiedHumanHuskEntity>> {

    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/entity/zombie/husk.png");

    private static final float FEMALE_ARM_SCALE = 0.82f;
    private static final float MALE_ARM_SCALE = 1.0f;

    public ZombifiedHumanHuskRenderer(EntityRendererFactory.Context context) {
        super(context, new ZombieEntityModel<>(context.getPart(EntityModelLayers.HUSK)), 0.5F);
    }

    @Override
    public void render(ZombifiedHumanHuskEntity entity, float yaw, float tickDelta, MatrixStack matrices,
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
    public Identifier getTexture(ZombifiedHumanHuskEntity entity) {
        return TEXTURE;
    }
}
