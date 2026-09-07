package hik1tka.risen_races.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Той самий прийом, що VillagerModelAccessor для носа Ринара - лише дає
 * доступ до leftArm/rightArm, щоб зробити гендерну різницю (слім/вайд)
 * через скейл, а не через дві окремі геометрії. ZombieEntityModel успадковує
 * BipedEntityModel, тому цей акцесор працює й на зомбі, і на кадаврів/
 * утопців (вони теж - ZombieEntityModel-похідні).
 */
@Mixin(BipedEntityModel.class)
public interface BipedModelAccessor {
    @Accessor("leftArm")
    ModelPart getLeftArm();

    @Accessor("rightArm")
    ModelPart getRightArm();
}
