package hik1tka.risen_races.util;

import hik1tka.risen_races.register.ModEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class EffectAnnihilator {

    public static void checkAndAnnihilate(LivingEntity entity) {
        if (entity.getWorld().isClient) return;

        // Перевіряємо наявність обох ефектів
        if (entity.hasStatusEffect(ModEffect.PURIFICATION) && entity.hasStatusEffect(ModEffect.ZOMBIFICATION)) {

            // Видаляємо ПОВНІСТЮ (використовуємо метод, що гарантує видалення)
            entity.removeStatusEffect(ModEffect.PURIFICATION);
            entity.removeStatusEffect(ModEffect.ZOMBIFICATION);

            // Додаткова перевірка: якщо ефект все ще висить (баг Fabric/Minecraft), видаляємо його через ітератор
            if (entity.hasStatusEffect(ModEffect.PURIFICATION)) {
                entity.removeStatusEffect(ModEffect.PURIFICATION);
            }

            // Побічні ефекти
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 1));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));

            // Ефект "схлопування" енергії
            entity.getWorld().playSound(null, entity.getBlockPos(),
                    SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.NEUTRAL, 0.8f, 1.5f);
        }
    }
}