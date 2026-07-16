package hik1tka.risen_races.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class ZombificationEffect extends StatusEffect {

    public ZombificationEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2, false, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 100, 2, false, false, false));
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0, false, false, false));
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0; // Спрацьовує раз на секунду
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {

        super.onApplied(entity, attributes, amplifier);
    }
}