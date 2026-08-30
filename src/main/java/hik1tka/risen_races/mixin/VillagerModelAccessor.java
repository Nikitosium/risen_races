package hik1tka.risen_races.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerResemblingModel.class)
public interface VillagerModelAccessor {
    @Accessor("nose")
    ModelPart getNose();

    @Accessor("root")
    ModelPart getRoot();
}
