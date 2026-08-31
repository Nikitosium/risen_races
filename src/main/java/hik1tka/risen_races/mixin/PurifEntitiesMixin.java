package hik1tka.risen_races.mixin;

import hik1tka.risen_races.register.ModEffect;
import hik1tka.risen_races.util.EntityTransmutation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * PURIFICATION на ворожих/мутованих істотах. Зоглін і зомбіфікований піглін
 * не мають "чистого" стану, куди повертатись - вони просто гинуть у момент
 * закінчення ефекту. Зомбі-кінь має - повертається у звичайного коня через
 * EntityTransmutation.swap(...).
 */
@Mixin(LivingEntity.class)
public abstract class PurifEntitiesMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkZoglinPurification(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient() || !(entity instanceof ZoglinEntity zoglin)) return;

        StatusEffectInstance effect = zoglin.getStatusEffect(ModEffect.PURIFICATION);
        if (effect != null && effect.getDuration() <= 1) {
            zoglin.getWorld().playSound(null, zoglin.getX(), zoglin.getY(), zoglin.getZ(),
                    SoundEvents.ENTITY_ZOGLIN_HURT, SoundCategory.HOSTILE, 1.0f, 0.5f);
            zoglin.kill();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkHorsePurification(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient() || !(entity instanceof ZombieHorseEntity zombieHorse)) return;
        if (zombieHorse.age % 20 != 0) return;

        StatusEffectInstance effect = zombieHorse.getStatusEffect(ModEffect.PURIFICATION);
        if (effect != null && effect.getDuration() <= 1) {
            EntityTransmutation.swap(zombieHorse, EntityType.HORSE, (ServerWorld) zombieHorse.getWorld(),
                    SoundEvents.ENTITY_HORSE_AMBIENT, 1.0f, 1.2f, false, false);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void checkPiglinPurification(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient() || !(entity instanceof ZombifiedPiglinEntity zPiglin)) return;

        StatusEffectInstance effect = zPiglin.getStatusEffect(ModEffect.PURIFICATION);
        if (effect != null && effect.getDuration() <= 1) {
            zPiglin.getWorld().playSound(null, zPiglin.getX(), zPiglin.getY(), zPiglin.getZ(),
                    SoundEvents.ENTITY_PIGLIN_ANGRY, SoundCategory.HOSTILE, 1.0f, 0.6f);
            zPiglin.kill();
        }
    }
}
