package hik1tka.risen_races.mixin;

import hik1tka.risen_races.util.IGenderedEntity;
import net.minecraft.entity.ai.brain.task.VillagerBreedTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(VillagerBreedTask.class)
public class VillagerBreedTaskMixin {

    // Перехоплюємо метод створення дитини у жителів.
    @Inject(method = "createChild", at = @At("HEAD"), cancellable = true)
    private void checkGenderBeforeBreeding(ServerWorld world, VillagerEntity parent, VillagerEntity partner, CallbackInfoReturnable<Optional<?>> cir) {
        if (parent instanceof IGenderedEntity parentGendered && partner instanceof IGenderedEntity partnerGendered) {
            // Перевіряємо за допомогою нашого методу (який перевіряє расу і стать)
            if (!parentGendered.canBreedWithGendered(partner)) {
                // Якщо статі однакові або раса різна - повертаємо пустий Optional (дитина не створюється)
                cir.setReturnValue(Optional.empty());
            }
        }
    }
}