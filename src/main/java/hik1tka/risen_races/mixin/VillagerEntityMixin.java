package hik1tka.risen_races.mixin;

import hik1tka.risen_races.util.IGenderedEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin {

    // Впорскуємося в кінець (TAIL) ванільного методу перевірки розмноження
    @Inject(method = "canBreedWith", at = @At("TAIL"), cancellable = true)
    private void injectGenderBreedingCheck(PassiveEntity other, CallbackInfoReturnable<Boolean> cir) {
        // Якщо ванільні умови (ситість, ліжка) вже повернули false, то нам і перевіряти стать немає сенсу
        if (!cir.getReturnValue()) {
            return;
        }

        // Приводимо себе (this) до інтерфейсу IGenderedEntity
        if (!((Object) this instanceof IGenderedEntity thisEntity)) return;

        // Викликаємо нашу перевірку статі та раси, яку ми написали на Кроці 1
        boolean canBreed = thisEntity.canBreedWithGendered(other);

        // Перезаписуємо результат повернення методу (якщо статі однакові — змусимо повернути false)
        cir.setReturnValue(canBreed);
    }
}