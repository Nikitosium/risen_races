package hik1tka.risen_races.mixin;

import hik1tka.risen_races.RisenRaces;
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
    // ЦЕ ЄДИНЕ реальне місце, де гра дозволяє/забороняє розмноження — canBreedWith(PassiveEntity)
    // ваша гра НІКОЛИ не викликає для жителів, він існує лише для AnimalEntity (коні/коти/вовки).
    @Inject(method = "createChild", at = @At("HEAD"), cancellable = true)
    private void checkGenderBeforeBreeding(ServerWorld world, VillagerEntity parent, VillagerEntity partner, CallbackInfoReturnable<Optional<?>> cir) {
        RisenRaces.LOGGER.info("[Breeding Gate] createChild called: parent=" + parent.getClass().getSimpleName()
                + " partner=" + partner.getClass().getSimpleName());

        if (!(parent instanceof IGenderedEntity parentGendered)) {
            RisenRaces.LOGGER.info("[Breeding Gate] parent is NOT IGenderedEntity — this should never happen, skipping gate");
            return;
        }
        if (!(partner instanceof IGenderedEntity partnerGendered)) {
            RisenRaces.LOGGER.info("[Breeding Gate] partner is NOT IGenderedEntity — this should never happen, skipping gate");
            return;
        }

        RisenRaces.LOGGER.info("[Breeding Gate] parent: race=" + parentGendered.getRaceId() + " female=" + parentGendered.isFemale()
                + " | partner: race=" + partnerGendered.getRaceId() + " female=" + partnerGendered.isFemale());

        boolean compatible = parentGendered.canBreedWithGendered(partner);
        RisenRaces.LOGGER.info("[Breeding Gate] compatible=" + compatible);

        if (!compatible) {
            // Якщо статі однакові або раса різна - повертаємо пустий Optional (дитина не створюється)
            cir.setReturnValue(Optional.empty());
        }
    }
}