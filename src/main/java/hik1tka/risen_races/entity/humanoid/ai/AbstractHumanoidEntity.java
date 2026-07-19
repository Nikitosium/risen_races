package hik1tka.risen_races.entity.humanoid.ai;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import hik1tka.risen_races.util.IGenderedEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
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
    private static final TrackedData<Boolean> FEMALE = DataTracker.registerData(AbstractHumanoidEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int loveTicks;
    private final SimpleInventory inventory = new SimpleInventory(8);
    private final String raceId;

    // 1. СЕНСОРИ (що моб бачить)
    protected static final ImmutableList<SensorType<? extends Sensor<? super AbstractHumanoidEntity>>> SENSORS = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            SensorType.NEAREST_BED,
            SensorType.HURT_BY
    );

    // 2. ПАМ'ЯТЬ (що моб запам'ятовує)
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
            MemoryModuleType.HOME,               // Ліжко
            MemoryModuleType.JOB_SITE,           // Робочий блок
            MemoryModuleType.MEETING_POINT,      // Колокол
            MemoryModuleType.WALK_TARGET,        // Куди йти
            MemoryModuleType.LOOK_TARGET,        // На що дивитись
            MemoryModuleType.PATH,               // Шлях
            MemoryModuleType.NEAREST_BED,        // Найближче ліжко
            MemoryModuleType.HURT_BY,            // Хто вдарив
            MemoryModuleType.NEAREST_HOSTILE,    // Найближчий ворог
            MemoryModuleType.BREED_TARGET        // Партнер для розмноження
    );

    public enum Race {
        HUMAN, RYNAR, PIGLIN
    }

    protected AbstractHumanoidEntity(EntityType<? extends PassiveEntity> entityType, World world, String raceId) {
        super(entityType, world);
        this.raceId = raceId;
    }

    public abstract Race getRace();

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FEMALE, false);
    }

    // ==========================================
    // БРЕЙН СИСТЕМА ЗАМІСТЬ GOALS
    // ==========================================

    @Override
    protected Brain.Profile<AbstractHumanoidEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        Brain<AbstractHumanoidEntity> brain = this.createBrainProfile().deserialize(dynamic);
        HumanoidBrain.init(brain, this); // Передаємо мозок в наш утилітний клас для налаштування
        return brain;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<AbstractHumanoidEntity> getBrain() {
        return (Brain<AbstractHumanoidEntity>) super.getBrain();
    }

    @Override
    protected void mobTick() {
        this.getWorld().getProfiler().push("humanoidBrain");
        // Основний тік мозку (запускає всі таски)
        this.getBrain().tick((ServerWorld) this.getWorld(), this);
        this.getWorld().getProfiler().pop();

        // Оновлюємо розклад (чи треба зараз спати, чи працювати, чи йти до колокола)
        HumanoidBrain.updateActivities(this);
        super.mobTick();
    }

    // Залишаємо пустим, бо Goal-система нам більше не потрібна!
    @Override
    protected void initGoals() {
    }

    // ==========================================

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

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getWorld().isClient() && this.loveTicks > 0) {
            this.loveTicks--;
        }
    }

    @Override
    public @Nullable net.minecraft.entity.EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable net.minecraft.entity.EntityData entityData, @Nullable NbtCompound entityNbt) {
        IGenderedEntity.generateRandomGender(this, this.random);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity mate) {
        EntityType<? extends PassiveEntity> childType = (EntityType<? extends PassiveEntity>) this.getType();
        AbstractHumanoidEntity child = (AbstractHumanoidEntity) childType.create(world);
        if (child != null) {
            child.setBreedingAge(-24000);
            IGenderedEntity.generateRandomGender(child, this.random);
        }
        return child;
    }

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