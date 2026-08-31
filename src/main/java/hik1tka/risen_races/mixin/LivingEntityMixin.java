package hik1tka.risen_races.mixin;

import hik1tka.risen_races.util.EffectAnnihilator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Якщо на сутність одночасно накладені PURIFICATION і ZOMBIFICATION -
 * EffectAnnihilator (вже є в util/) скасовує обидва й додає побічний ефект.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "onStatusEffectApplied", at = @At("TAIL"))
    private void onEffectApplied(StatusEffectInstance effect, Entity source, CallbackInfo ci) {
        EffectAnnihilator.checkAndAnnihilate((LivingEntity) (Object) this);
    }
}
