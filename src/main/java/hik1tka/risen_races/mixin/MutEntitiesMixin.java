package hik1tka.risen_races.mixin;

import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import hik1tka.risen_races.entity.humanoid.human.util.NPCConversionHandler;
import hik1tka.risen_races.register.ModEffect;
import hik1tka.risen_races.util.EntityTransmutation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MutEntitiesMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkHoglinMutation(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient() || !(entity instanceof HoglinEntity hoglin)) return;

        StatusEffectInstance effect = hoglin.getStatusEffect(ModEffect.ZOMBIFICATION);
        if (effect != null && effect.getDuration() <= 1) {
            EntityTransmutation.swap(hoglin, EntityType.ZOGLIN, (ServerWorld) hoglin.getWorld(),
                    SoundEvents.ENTITY_HOGLIN_CONVERTED_TO_ZOMBIFIED, 1.0f, 1.0f, false, true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkHorseMutation(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof HorseEntity horse) || horse.getWorld().isClient() || horse.age % 20 != 0) return;

        StatusEffectInstance effect = horse.getStatusEffect(ModEffect.ZOMBIFICATION);
        if (effect != null && effect.getDuration() <= 1) {
            EntityTransmutation.swap(horse, EntityType.ZOMBIE_HORSE, (ServerWorld) horse.getWorld(),
                    SoundEvents.ENTITY_ZOMBIE_HORSE_DEATH, 1.0f, 0.6f, false, false);
        }
    }

    @Inject(method = "onKilledBy", at = @At("HEAD"), cancellable = true)
    private void onHumanKilledByZombie(LivingEntity attacker, CallbackInfo ci) {
        if (!((Object) this instanceof HumanEntity human)) return;
        if (human.getWorld().isClient || !(attacker instanceof ZombieEntity)) return;

        // Замість стандартної смерті - конвертація в зомбі (з пам'яттю для лікування)
        NPCConversionHandler.humanToZombie(human);
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkPiglinMutation(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient() || !(entity instanceof PiglinEntity piglin)) return;

        StatusEffectInstance effect = piglin.getStatusEffect(ModEffect.ZOMBIFICATION);
        if (effect != null && effect.getDuration() <= 1) {
            EntityTransmutation.swap(piglin, EntityType.ZOMBIFIED_PIGLIN, (ServerWorld) piglin.getWorld(),
                    SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_ANGRY, 1.0f, 1.0f, false, true);
        }
    }
}
