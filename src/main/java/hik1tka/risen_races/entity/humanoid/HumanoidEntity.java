package hik1tka.risen_races.entity.humanoid;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public class HumanoidEntity extends MerchantEntity {
    private static final TrackedData<Boolean> IS_FEMALE =
            DataTracker.registerData(HumanoidEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> RACE_ID =
            DataTracker.registerData(HumanoidEntity.class, TrackedDataHandlerRegistry.STRING);

    public HumanoidEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_FEMALE, false);
        this.dataTracker.startTracking(RACE_ID, "");
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(IS_FEMALE, nbt.getBoolean("IsFemale"));
        this.dataTracker.set(RACE_ID, nbt.getString("RaceId"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsFemale", this.dataTracker.get(IS_FEMALE));
        nbt.putString("RaceId", this.dataTracker.get(RACE_ID));
    }

    @Override
    protected void fillRecipes() {
        // заглушка — трейдів поки немає
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        // заглушка — нічого не робимо після трейду
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null; // заглушка — розмноження поки не потрібне
    }
}