package hik1tka.risen_races.entity.humanoid.human;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.util.IGenderedEntity;
import hik1tka.risen_races.register.ModSounds;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class HumanEntity extends VillagerEntity implements IGenderedEntity {
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

    private static final TrackedData<Boolean> FEMALE = DataTracker.registerData(HumanEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> SKIN_ID = DataTracker.registerData(HumanEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public HumanEntity(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(FEMALE, false);
        this.dataTracker.startTracking(SKIN_ID, 0);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        boolean isFemale = this.random.nextBoolean();
        this.setFemale(isFemale);

        int maxSkins = isFemale ? 12 : 10;
        this.setSkinId(this.random.nextInt(maxSkins));

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
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

    public void setSkinId(int id) {
        this.dataTracker.set(SKIN_ID, id);
    }

    public String getProfession() {
        return Registries.VILLAGER_PROFESSION.getId(this.getVillagerData().getProfession()).getPath();
    }

    public float getScaleModifier() {
        return this.isFemale() ? 0.95f : 1.0f;
    }

    @Override
    public String getRaceId() {
        return "human";
    }

    @Override
    public boolean isFemale() {
        return this.dataTracker.get(FEMALE);
    }

    @Override
    public void setFemale(boolean female) {
        this.dataTracker.set(FEMALE, female);
    }

    // Люди (як і звичайні rynar-жителі) розмножуються повністю ВАНІЛЬНИМ шляхом:
    // ліжка + їжа в інвентарі + брейн-задача VillagerBreedTask. Ніякого годування
    // хлібом та ручного пошуку пари тут більше немає — canBreed()/wantsToStartBreeding()
    // навмисно НЕ перевизначені, тож працює стандартна логіка VillagerEntity.
    // Стать/раса при цьому враховуються автоматично через VillagerBreedTaskMixin.
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
        nbt.putBoolean("IsFemale", this.isFemale());
        nbt.putInt("SkinId", this.getSkinId());
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
    }
}
