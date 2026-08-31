package hik1tka.risen_races.mixin;

import hik1tka.risen_races.register.ModEffect;
import hik1tka.risen_races.util.EntityTransmutation;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class MutVilMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkMutation(CallbackInfo ci) {
        VillagerEntity villager = (VillagerEntity) (Object) this;
        if (villager.getWorld().isClient()) return;

        StatusEffectInstance effect = villager.getStatusEffect(ModEffect.ZOMBIFICATION);
        if (effect != null && effect.getDuration() <= 10) {
            // fullNbt=true - зберігаємо VillagerData (професію/рівень) і торги,
            // вони записані всередині повного NBT, окремий setVillagerData() не потрібен
            EntityTransmutation.swap(villager, EntityType.ZOMBIE_VILLAGER, (ServerWorld) villager.getWorld(),
                    SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.8f, true, false);
        }
    }
}
