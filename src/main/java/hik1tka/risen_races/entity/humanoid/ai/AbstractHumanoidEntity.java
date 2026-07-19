package hik1tka.risen_races.entity.humanoid.ai;

import hik1tka.risen_races.util.IGenderedEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public abstract class AbstractHumanoidEntity extends PassiveEntity implements IGenderedEntity {

    // Синхронізуємо стать між сервером і клієнтом (щоб рендер моделі не багався)
    private static final TrackedData<Boolean> FEMALE = DataTracker.registerData(AbstractHumanoidEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private int loveTicks;
    private final SimpleInventory inventory = new SimpleInventory(8); // Інвентар на 8 слотів
    private final String raceId;

    public enum Race {
        HUMAN, RYNAR, PIGLIN
    }

    protected AbstractHumanoidEntity(EntityType<? extends PassiveEntity> entityType, World world, String raceId) {
        super(entityType, world);
        this.raceId = raceId;
    }

    // Абстрактний метод, який реалізує кожна раса (Human, Rynar тощо)
    public abstract Race getRace();

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FEMALE, false); // За замовчуванням — чоловік
    }

    // Реалізація твого інтерфейсу IGenderedEntity
    @Override
    public boolean isFemale() { return this.dataTracker.get(FEMALE); }

    @Override
    public void setFemale(boolean female) { this.dataTracker.set(FEMALE, female); }

    @Override
    public boolean isInLove() { return this.loveTicks > 0; }

    @Override
    public void setLoveTicks(int ticks) { this.loveTicks = ticks; }

    @Override
    public String getRaceId() { return this.raceId; }

    @Override
    public boolean canBreedWith(PassiveEntity other) {
        if (other instanceof AbstractHumanoidEntity otherHumanoid) {
            return this.canBreedWithGendered(otherHumanoid) && this.isInLove() && otherHumanoid.isInLove();
        }
        return false;
    }

    public SimpleInventory getInventory() { return this.inventory; }

    // Ванільна логіка: оновлення таймерів закоханості
    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient() && this.loveTicks > 0) {
            this.loveTicks--;
        }
    }

    // Рандом статі при першому спавні в світі
    @Override
    public @Nullable net.minecraft.entity.EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable net.minecraft.entity.EntityData entityData, @Nullable NbtCompound entityNbt) {
        IGenderedEntity.generateRandomGender(this, this.random);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    // Універсальний спавн дитини для всіх рас
    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity mate) {
        EntityType<? extends PassiveEntity> childType = (EntityType<? extends PassiveEntity>) this.getType();
        AbstractHumanoidEntity child = (AbstractHumanoidEntity) childType.create(world);
        if (child != null) {
            child.setBreedingAge(-24000); // Робимо дитиною
            IGenderedEntity.generateRandomGender(child, this.random); // Кидаємо стать малюку
        }
        return child;
    }

    // Збереження даних в NBT
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsFemale", this.isFemale());
        nbt.putInt("LoveTicks", this.loveTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setFemale(nbt.getBoolean("IsFemale"));
        this.loveTicks = nbt.getInt("LoveTicks");
    }
}