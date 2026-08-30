package hik1tka.risen_races.client.humanoid.piglin;

import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PiglinEntityModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/**
 * Текстура - ванільна (та сама, що й у звичайного пігліна). Модель залежить
 * від статі: чоловіча - ванільна PiglinEntityModel (нема сенсу городити
 * власну, форма та сама), жіноча - твоя кастомна форма (FemaleRisenPiglinModel,
 * заглушка нижче - підключиться, щойно експортуєш модель з Blockbench).
 */
public class RisenPiglinEntityRender extends MobEntityRenderer<RisenPiglinEntity, PiglinEntityModel<RisenPiglinEntity>> {

    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/entity/piglin/piglin.png");

    private final PiglinEntityModel<RisenPiglinEntity> maleModel;
    private final PiglinEntityModel<RisenPiglinEntity> femaleModel;

    public RisenPiglinEntityRender(EntityRendererFactory.Context context) {
        // ВАЖЛИВО: точний layer ("main" чи інший) і точний generic-тип
        // PiglinEntityModel<T> звір з декомпільованим класом під свою
        // версію Yarn-мапінгів - тут дано найбільш імовірний варіант.
        super(context, new PiglinEntityModel<>(context.getPart(EntityModelLayers.PIGLIN)), 0.5f);
        this.maleModel = this.model;
        // TODO: замінити на власний клас моделі, коли буде готова жіноча
        // форма - FemaleRisenPiglinModel(context.getPart(ModModelLayers.FEMALE_RISEN_PIGLIN))
        this.femaleModel = this.maleModel;
    }

    @Override
    public void render(RisenPiglinEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        this.model = entity.isFemale() ? this.femaleModel : this.maleModel;
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(RisenPiglinEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(RisenPiglinEntity entity, MatrixStack matrices, float amount) {
        float f = entity.getScaleFactor();
        matrices.scale(f, f, f);
    }
}