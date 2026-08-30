package hik1tka.risen_races.client.humanoid.rynar;

import hik1tka.risen_races.entity.humanoid.rynar.RynarEntity;
import hik1tka.risen_races.mixin.VillagerModelAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RynarEntityRender extends MobEntityRenderer<RynarEntity, VillagerResemblingModel<RynarEntity>> {

    private static final Identifier TEXTURE =
            new Identifier("minecraft", "textures/entity/villager/villager.png");

    public RynarEntityRender(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public void render(RynarEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        VillagerModelAccessor accessor = (VillagerModelAccessor) this.getModel();

        ModelPart root = accessor.getRoot();
        ModelPart arms = root.getChild("arms");
        ModelPart nose = accessor.getNose();

        if (entity.isFemale()) {
            nose.xScale = 0.8f;
            nose.yScale = 0.4f;
            nose.zScale = 0.8f;
            nose.pivotZ = -0.8f;
            nose.pivotY = -3.0f;

            arms.xScale = 0.9f;
            arms.zScale = 0.9f;
        } else {
            nose.xScale = 1.0f;
            nose.yScale = 1.0f;
            nose.zScale = 1.0f;
            nose.pivotZ = 0.0f;
            nose.pivotY = -2.0f;

            arms.xScale = 1.0f;
            arms.zScale = 1.0f;
        }

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(RynarEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(RynarEntity entity, MatrixStack matrices, float amount) {
        float f = entity.getScaleFactor();
        matrices.scale(f, f, f);
    }
}