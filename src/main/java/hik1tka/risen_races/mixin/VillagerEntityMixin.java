package hik1tka.risen_races.mixin;

import hik1tka.risen_races.util.IGenderedEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity implements IGenderedEntity {

    @Unique
    private static final TrackedData<Boolean> IS_FEMALE = DataTracker.registerData(VillagerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    // Задаємо випадкове значення при спавні (Шанс 51% що буде жінка)
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void onInitDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(IS_FEMALE, this.random.nextFloat() < 0.51f);
    }

    // Зберігаємо та читаємо стать зі світу (NBT), щоб після перезаходу вона не змінювалась
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("IsFemale", this.isFemale());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("IsFemale")) {
            this.setFemale(nbt.getBoolean("IsFemale"));
        }
    }

    @Unique
    @Override
    public boolean canBreedWith(PassiveEntity other) {
        return this.canBreedWithGendered(other);
    }

    @Unique
    @Override
    public boolean isFemale() {
        return this.dataTracker.get(IS_FEMALE);
    }

    @Unique
    @Override
    public void setFemale(boolean female) {
        this.dataTracker.set(IS_FEMALE, female);
    }

    @Unique
    @Override
    public String getRaceId() {
        return "rynar";
    }

    @Unique
    @Override
    public boolean isInLove() { return false; }

    @Unique
    @Override
    public void setLoveTicks(int ticks) {}
}