package hik1tka.risen_races.mixin;

import hik1tka.risen_races.register.ModEffect;
import hik1tka.risen_races.util.EntityTransmutation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombieVillagerEntity.class)
public abstract class PurifVilMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkCuring(CallbackInfo ci) {
        ZombieVillagerEntity zombie = (ZombieVillagerEntity) (Object) this;
        if (zombie.getWorld().isClient()) return;

        StatusEffectInstance effect = zombie.getStatusEffect(ModEffect.PURIFICATION);
        if (effect != null && effect.getDuration() <= 10) {
            EntityTransmutation.swap(zombie, EntityType.VILLAGER, (ServerWorld) zombie.getWorld(),
                    SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f, true, false);
        }
    }
}
