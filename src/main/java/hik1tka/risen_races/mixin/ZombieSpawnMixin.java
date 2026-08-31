package hik1tka.risen_races.mixin;

import hik1tka.risen_races.entity.zombie.FemaleZombieEntity;
import hik1tka.risen_races.entity.zombie.MaleZombieEntity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieEntity.class)
public abstract class ZombieSpawnMixin {

    @Inject(method = "initialize", at = @At("TAIL"))
    private void replaceZombieOnSpawn(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                       @Nullable EntityData entityData, @Nullable NbtCompound entityNbt,
                                       CallbackInfoReturnable<EntityData> cir) {
        ZombieEntity zombie = (ZombieEntity) (Object) this;

        // Заміняємо лише ВАНІЛЬНИХ зомбі - наші гендерні типи й так вже такі,
        // якими мають бути (уникаємо нескінченної підміни самих себе)
        if (zombie.getType() != EntityType.ZOMBIE || world.isClient()) return;

        boolean isFemale = world.getRandom().nextFloat() < 0.51f;
        EntityType<? extends ZombieEntity> nextType = isFemale
                ? FemaleZombieEntity.FEMALE_ZOMBIE
                : MaleZombieEntity.MALE_ZOMBIE;

        ZombieEntity newZombie = zombie.convertTo(nextType, true);
        if (newZombie == null) return;

        int randomSkin = isFemale ? world.getRandom().nextInt(12) : world.getRandom().nextInt(10);
        if (newZombie instanceof FemaleZombieEntity female) {
            female.setSkinId(randomSkin);
        } else if (newZombie instanceof MaleZombieEntity male) {
            male.setSkinId(randomSkin);
        }
    }
}
