package hik1tka.risen_races.mixin;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.util.IGenderedEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.VillagerBreedTask;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Ми НЕ переписуємо VillagerBreedTask з нуля через @Overwrite — так довелось би
 * вручну відтворювати приватну ванільну логіку (їжа, кулдауни, анімації), а це і
 * ризиковано (легко зламати щось непомітне), і зайве. Замість цього ми "вбудовуємо"
 * нашу гендерно-расову перевірку у ВСІ три ключові точки життєвого циклу задачі:
 *
 *   1) shouldRun          — чи можна ВЗАГАЛІ почати розмноження з цим партнером
 *   2) shouldKeepRunning  — чи можна ПРОДОВЖУВАТИ (партнер міг змінитись/віддалитись)
 *   3) createChild        — фінальний запобіжник перед появою дитини
 *
 * Це дає той самий ефект, що й повний "переписаний" метод, що працює "так само як
 * і звичайний" — вся ванільна поведінка (кулдауни, рух, анімації, їжа) лишається
 * без змін, просто несумісні пари більше НЕ обираються одне одним як ціль для
 * розмноження і не починають/не продовжують "залицяння".
 */
@Mixin(VillagerBreedTask.class)
public class VillagerBreedTaskMixin {

    @Inject(method = "shouldRun", at = @At("RETURN"), cancellable = true)
    private void risen_races$gateShouldRun(ServerWorld world, VillagerEntity entity, CallbackInfoReturnable<Boolean> cir) {
        // Якщо ваніль і так каже "ні" (немає цілі, немає їжі і т.д.) — нічого не чіпаємо
        if (!cir.getReturnValueZ()) {
            return;
        }
        if (!risen_races$isCompatibleWithBreedTarget(entity)) {
            RisenRaces.LOGGER.info("[Breeding Gate] shouldRun заблоковано: несумісна пара для " + entity.getUuidAsString());
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldKeepRunning", at = @At("RETURN"), cancellable = true)
    private void risen_races$gateShouldKeepRunning(ServerWorld world, VillagerEntity entity, long time, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        if (!risen_races$isCompatibleWithBreedTarget(entity)) {
            RisenRaces.LOGGER.info("[Breeding Gate] shouldKeepRunning заблоковано: несумісна пара для " + entity.getUuidAsString());
            cir.setReturnValue(false);
        }
    }

    // Останній запобіжник — навіть якщо десь пропустили перевірку вище, дитина все одно не з'явиться.
    @Inject(method = "createChild", at = @At("HEAD"), cancellable = true)
    private void risen_races$gateCreateChild(ServerWorld world, VillagerEntity parent, VillagerEntity partner, CallbackInfoReturnable<Optional<?>> cir) {
        if (!(parent instanceof IGenderedEntity parentGendered)) {
            return;
        }
        if (!parentGendered.canBreedWithGendered(partner)) {
            RisenRaces.LOGGER.info("[Breeding Gate] createChild заблоковано: несумісна пара parent=" + parent.getUuidAsString()
                    + " partner=" + partner.getUuidAsString());
            cir.setReturnValue(Optional.empty());
        }
    }

    /**
     * Дістає поточну ціль розмноження з пам'яті мозку жителя (BREED_TARGET) і перевіряє
     * стать/расу через вашу існуючу IGenderedEntity#canBreedWithGendered.
     * Якщо житель чи ціль не підтримують нашу гендерну систему — не блокуємо (щоб не
     * зламати сумісність з іншими модами/расами, які її не використовують).
     */
    private static boolean risen_races$isCompatibleWithBreedTarget(VillagerEntity entity) {
        if (!(entity instanceof IGenderedEntity entityGendered)) {
            return true;
        }

        // BREED_TARGET типізований як MemoryModuleType<PassiveEntity>
        Optional<PassiveEntity> target = entity.getBrain().getOptionalMemory(MemoryModuleType.BREED_TARGET);
        if (target.isEmpty()) {
            return true;
        }

        return entityGendered.canBreedWithGendered(target.get());
    }
}
