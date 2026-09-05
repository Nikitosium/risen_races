package hik1tka.risen_races.client.humanoid.piglin.model;

import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Java-модель, вручну перенесена з female_piglin.json (Bedrock-формат,
 * format_version 1.12.0). Bedrock і Java використовують РІЗНІ системи
 * координат (Bedrock: Y вгору, пивоти абсолютні; Java: Y вниз, пивоти
 * відносні до батька) - тому це не механічне копіювання чисел, а
 * перерахований еквівалент. Якщо десь виглядатиме "трохи не так" -
 * найімовірніше, помилка саме в конвертації одного вузла, кажи, поправлю.
 *
 * Свідомі відхилення від файлу:
 *  - "snout" (морда/плита рота) з голови ВЗАГАЛІ не намальована - ти назвав
 *    це "іклами" і хотів прибрати; оскільки ця модель призначена ТІЛЬКИ для
 *    жінок (не перемикається рантайм-скейлом, як у Ринара), простіше не
 *    будувати цей куб узагалі, ніж будувати і ховати.
 *  - "hat", "rightItem", "leftItem" - порожні кістки-кріплення в JSON без
 *    кубів, для базового рендеру не потрібні, пропущено.
 */
public class RisenFemalePiglinModel<T extends RisenPiglinEntity> extends SinglePartEntityModel<T> implements net.minecraft.client.render.entity.model.ModelWithHead {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public RisenFemalePiglinModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.leftEar = head.getChild("leftear");
        this.rightEar = head.getChild("rightear");
        this.rightArm = body.getChild("rightarm");
        this.leftArm = body.getChild("leftarm");
        this.rightLeg = body.getChild("rightleg");
        this.leftLeg = body.getChild("leftleg");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        ModelPartData body = root.addChild("body", ModelPartBuilder.create()
                        .uv(16, 16).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
                        .uv(16, 32).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // Основа голови + п'ятачок (носик) - той самий плаский куб, що й у
        // ванільного пігліна. Раніше я його помилково прибрав, сплутавши з
        // "іклами" - це різні частини, п'ятачок лишається.
        ModelPartData head = body.addChild("head", ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new Dilation(-0.02F))
                        .uv(31, 1).cuboid(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, new Dilation(-0.02F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        head.addChild("leftear", ModelPartBuilder.create()
                        .uv(51, 6).cuboid(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F),
                ModelTransform.of(5.0F, -6.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        head.addChild("rightear", ModelPartBuilder.create()
                        .uv(39, 6).cuboid(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F),
                ModelTransform.of(-5.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

        // Руки - вже 3px (не 4px, як у ванілі) прямо в геометрії, згідно файлу.
        body.addChild("rightarm", ModelPartBuilder.create()
                        .uv(41, 16).cuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
                        .uv(40, 32).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F));

        body.addChild("leftarm", ModelPartBuilder.create()
                        .uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
                        .uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F));

        body.addChild("rightleg", ModelPartBuilder.create()
                        .uv(0, 16).cuboid(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .uv(0, 32).cuboid(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(-1.9F, 12.0F, 0.0F));

        body.addChild("leftleg", ModelPartBuilder.create()
                        .uv(16, 48).cuboid(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .uv(0, 48).cuboid(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.25F)),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        this.head.yaw = headYaw * ((float) Math.PI / 180F);
        this.head.pitch = headPitch * ((float) Math.PI / 180F);

        this.rightArm.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 2.0F * limbDistance * 0.5F;
        this.leftArm.pitch = MathHelper.cos(limbAngle * 0.6662F) * 2.0F * limbDistance * 0.5F;
        this.rightLeg.pitch = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
        this.leftLeg.pitch = MathHelper.cos(limbAngle * 0.6662F + (float) Math.PI) * 1.4F * limbDistance;
    }

    @Override
    public ModelPart getHead() {
        return this.head;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
                       float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}