package hik1tka.risen_races.entity.humanoid.human;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import hik1tka.risen_races.util.IGenderedEntity;
import hik1tka.risen_races.register.ModSounds;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HumanEntity extends HumanoidEntity implements IGenderedEntity {
    public static final EntityType<HumanEntity> HUMAN = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "human"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, HumanEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.8f))
                    .build()
    );

    public static net.minecraft.entity.attribute.DefaultAttributeContainer.Builder createHumanAttributes() {
        // Береш з HumanoidEntity, а не з VillagerEntity - HumanEntity з ним не споріднений.
        return HumanoidEntity.createHumanoidAttributes();
    }

    private static final TrackedData<Integer> SKIN_ID = DataTracker.registerData(HumanEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public HumanEntity(EntityType<? extends HumanoidEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SKIN_ID, 0);
        // Стать НЕ рандомізуємо тут - initDataTracker() виконується і на
        // клієнті (для кожної локальної "примарної" копії ентіті), а не
        // тільки на сервері. Рандомний setFemale() тут створював стан, коли
        // клієнтська копія на мить мала СВОЄ власне випадкове isFemale,
        // не синхронізоване з сервером ще - і якщо звук встигав програтись
        // саме в цю мить, лунала не та стать. Реальна стать вирішується
        // один раз, тільки на сервері, у initialize() нижче.
    }

    @Override
    protected void afterUsing(TradeOffer offer) {

    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setRace(hik1tka.risen_races.entity.humanoid.HumanoidRace.HUMAN);

        // Стать вирішується тут, а не в initDataTracker() - цей метод
        // гарантовано виконується лише на сервері й лише один раз за спавн.
        this.setFemale(this.random.nextBoolean());
        this.rollSkinForGender();

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    /**
     * Рандомізує SkinId відповідно до поточної статі (isFemale()).
     * Викликається і при природному спавні (initialize), і для дітей,
     * народжених через HumanoidEntity#breedWith (onBabyCreated).
     */
    private void rollSkinForGender() {
        if (this.isFemale()) {
            // Жіночі скіни: 0..11 (всього 12 скінів)
            this.setSkinId(this.random.nextInt(12));
        } else {
            // Чоловічі скіни: 0..9 (всього 10 скінів)
            this.setSkinId(this.random.nextInt(10));
        }
    }

    @Override
    protected void onBabyCreated(hik1tka.risen_races.entity.humanoid.HumanoidEntity baby) {
        // На момент виклику цього хука стать дитини (setFemale) вже виставлена
        // в breedWith(), тому тут просто підбираємо скін під неї.
        if (baby instanceof HumanEntity human) {
            human.rollSkinForGender();
        }
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isFemale() ? ModSounds.FEMALE_AMBIENT : ModSounds.MALE_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (!this.isFemale() && this.random.nextInt(1000) == 0) {
            return ModSounds.MALE_NO_GOD;
        }
        return this.isFemale() ? ModSounds.FEMALE_HURT : ModSounds.MALE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ENTITY_DEATH;
    }

    @Override
    protected SoundEvent getTradingSound(boolean sold) {
        if (!sold) {
            return this.isFemale() ? super.getTradingSound(false) : ModSounds.MALE_NO;
            // Коли буде готовий звук для жінки, розкоментуй:
            // return this.isFemale() ? ModSounds.FEMALE_NO : ModSounds.MALE_NO;
        }
        return super.getTradingSound(sold);
    }

    public int getSkinId() {
        return this.dataTracker.get(SKIN_ID);
    }

    @Override
    public float getScaleFactor() {
        return this.isBaby() ? 0.7f : 1.0f;
    }

    public void setSkinId(int id) {
        this.dataTracker.set(SKIN_ID, id);
    }

    @Override
    public java.util.List<hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition> getAvailableProfessions() {
        return java.util.List.of(
                new hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition(
                        "farmer", net.minecraft.world.poi.PointOfInterestTypes.FARMER, 48),
                new hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition(
                        "fisherman", net.minecraft.world.poi.PointOfInterestTypes.FISHERMAN, 48),
                // "builder" - навмисно наш власний id на ванільному POI каменеріза
                // (стільниці не було своєї - берем готовий MASON POI, прив'язаний
                // до Blocks.STONECUTTER).
                new hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition(
                        "builder", net.minecraft.world.poi.PointOfInterestTypes.MASON, 48)
        );
    }

    // TODO: getProfession() приберено — getVillagerData() існує лише у VillagerEntity,
    // якого HumanoidEntity не наслідує. Коли зробиш власну систему професій,
    // додай сюди свій метод (наприклад через ще один TrackedData<String>).

    public float getScaleModifier() {
        return this.isFemale() ? 0.95f : 1.0f;
    }

    @Override
    public String getRaceId() {
        // Похідне від getRace() з HumanoidEntity, а не окремий хардкод -
        // одне джерело правди для раси.
        return this.getRace().name().toLowerCase(java.util.Locale.ROOT);
    }

    // isFemale()/setFemale() тепер повністю успадковані від HumanoidEntity -
    // окремо не перевизначаємо, щоб не було двох джерел правди.

    @Override
    public boolean isInLove() {
        return false;
    }

    @Override
    public void setLoveTicks(int ticks) {
        // Не використовується: люди не мають "закоханості" від їжі гравцем,
        // як і звичайні жителі — розмноження повністю автоматичне (ваніль).
    }

    @Override
    public boolean canBreedWith(PassiveEntity other) {
        return this.canBreedWithGendered(other);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SkinId", this.getSkinId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("SkinId")) {
            this.setSkinId(nbt.getInt("SkinId"));
        }
    }
}