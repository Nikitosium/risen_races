package hik1tka.risen_races.entity.humanoid.human;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.util.IGenderedEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HumanEntity extends VillagerEntity implements IGenderedEntity {

    // Реєструємо тип сутності
    public static final EntityType<HumanEntity> HUMAN = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "human"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, HumanEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.8f))
                    .build()
    );

    public static net.minecraft.entity.attribute.DefaultAttributeContainer.Builder createHumanAttributes() {
        return VillagerEntity.createMobAttributes()
                .add(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5);
    }

    // Синхронізуємо дані між сервером та клієнтом через DataTracker (щоб рендер бачив стать та скін)
    private static final TrackedData<Boolean> FEMALE = DataTracker.registerData(HumanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKIN_ID = DataTracker.registerData(HumanEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private int loveTicks;

    public HumanEntity(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
    }


    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        // Реєструємо наші змінні в трекері за замовчуванням
        this.dataTracker.startTracking(FEMALE, false);
        this.dataTracker.startTracking(SKIN_ID, 0);
    }

    // Метод викликається один раз при першому спавні моба у світі
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        // 1. Стать: 50% шанс бути жінкою
        boolean isFemale = this.random.nextBoolean();
        this.setFemale(isFemale);

        // 2. Скін: обираємо випадковий ID текстури (у тебе для жінок 0-11, для чоловіків 0-9)
        int maxSkins = isFemale ? 12 : 10;
        this.setSkinId(this.random.nextInt(maxSkins));

        // Тут згодом додамо твій масив імен (вибір імені залежно від статі):
        // String name = isFemale ? getRandomFemaleName() : getRandomMaleName();
        // this.setCustomName(Text.literal(name));
        // this.setCustomNameVisible(true);

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    // ==========================================
    // МЕТОДИ ДЛЯ РЕНДЕРЕРУ (HumanEntityRender)
    // ==========================================

    public int getSkinId() {
        return this.dataTracker.get(SKIN_ID); // Рендерер бере ID скіна звідси
    }

    public void setSkinId(int id) {
        this.dataTracker.set(SKIN_ID, id);
    }

    // Отримуємо професію (поки повертаємо "none", щоб носили звичайні скіни без робочого одягу)
    public String getProfession() {
        return "none"; // Рендерер використовує це для вибору папки текстур
    }

    // Модифікатор розміру (наприклад, жінки трохи менші, або просто рандомний зріст)
    public float getScaleModifier() {
        return this.isFemale() ? 0.95f : 1.0f; // Рендерер масштабує модель
    }

    // ==========================================
    // РЕАЛІЗАЦІЯ INTERFACE (IGenderedEntity)
    // ==========================================

    @Override
    public String getRaceId() {
        return "human";
    }

    @Override
    public boolean isFemale() {
        return this.dataTracker.get(FEMALE); // Тепер береться з трекера, щоб клієнт бачив стать[cite: 3]
    }

    @Override
    public void setFemale(boolean female) {
        this.dataTracker.set(FEMALE, female);
    }

    @Override
    public boolean isInLove() {
        return this.loveTicks > 0;
    }

    @Override
    public void setLoveTicks(int ticks) {
        this.loveTicks = ticks;
    }

    @Override
    public boolean canBreedWith(PassiveEntity other) {
        // Викликаємо нашу загальну гендерну перевірку сумісності
        return this.canBreedWithGendered(other);
    }

    // ==========================================
    // ЗБЕРЕЖЕННЯ В СВІТ (NBT)
    // ==========================================

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("IsFemale", this.isFemale());
        nbt.putInt("SkinId", this.getSkinId());
        nbt.putInt("LoveTicks", this.loveTicks);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("IsFemale")) {
            this.setFemale(nbt.getBoolean("IsFemale"));
        }
        if (nbt.contains("SkinId")) {
            this.setSkinId(nbt.getInt("SkinId"));
        }
        this.loveTicks = nbt.getInt("LoveTicks");
    }
}