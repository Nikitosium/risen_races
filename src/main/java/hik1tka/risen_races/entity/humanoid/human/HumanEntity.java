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

    private int loveTicks;

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
        return net.minecraft.registry.Registries.VILLAGER_PROFESSION.getId(this.getVillagerData().getProfession()).getPath();
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

    @Override
    public boolean isInLove() {
        return this.loveTicks > 0;
    }

    @Override
    public void setLoveTicks(int ticks) {
        this.loveTicks = ticks;
    }

    private int diagnosticTickCounter = 0;

    @Override
    public boolean canBreed() {
        boolean baseCanBreed = super.canBreed();
        RisenRaces.LOGGER.info("[Human Breeding] canBreed() for Human (Female=" + this.isFemale() + ", Profession=" + this.getProfession() + "). Result: " + baseCanBreed);
        return baseCanBreed;
    }

    @Override
    public void tick() {
        super.tick();

        // Діагностика раз на ~5 секунд (100 тіків), тільки на сервері
        if (!this.getWorld().isClient) {
            this.diagnosticTickCounter++;
            if (this.diagnosticTickCounter >= 20) {
                this.diagnosticTickCounter = 0;
                logBreedingDiagnostics();
            }
        }
    }

    private void logBreedingDiagnostics() {
        int nearbyAdultVillagers = this.getWorld().getEntitiesByClass(
                VillagerEntity.class,
                this.getBoundingBox().expand(16.0),
                v -> !v.isBaby()
        ).size();

        int freeBeds = -1;
        try {
            net.minecraft.world.poi.PointOfInterestStorage poiStorage = ((net.minecraft.server.world.ServerWorld) this.getWorld()).getPointOfInterestStorage();
            freeBeds = (int) poiStorage.getInSquare(
                    poiType -> poiType.matchesKey(net.minecraft.world.poi.PointOfInterestTypes.HOME),
                    this.getBlockPos(),
                    16,
                    net.minecraft.world.poi.PointOfInterestStorage.OccupationStatus.HAS_SPACE
            ).count();
        } catch (Exception e) {
            RisenRaces.LOGGER.info("[Human Breeding] POI query failed: " + e);
        }

        StringBuilder inv = new StringBuilder();
        net.minecraft.inventory.SimpleInventory inventory = this.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            net.minecraft.item.ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty()) {
                inv.append(stack.getItem()).append("x").append(stack.getCount()).append(" ");
            }
        }

        RisenRaces.LOGGER.info("[Human Breeding][DIAG] pos=" + this.getBlockPos()
                + " isBaby=" + this.isBaby()
                + " age=" + this.getBreedingAge()
                + " canBreed=" + this.canBreed()
                + " nearbyAdultVillagers(16 blocks)=" + nearbyAdultVillagers
                + " freeBedsWithSpace(16 blocks)=" + freeBeds
                + " inventory=[" + inv.toString().trim() + "]");
    }

    @Override
    public boolean wantsToStartBreeding() {
        boolean result = super.wantsToStartBreeding();
        RisenRaces.LOGGER.info("[Human Breeding] wantsToStartBreeding called for Human (Female=" + this.isFemale() + "). Result: " + result);
        return result;
    }

    @Override
    public void eatForBreeding() {
        RisenRaces.LOGGER.info("[Human Breeding] eatForBreeding called for Human (Female=" + this.isFemale() + ").");
        super.eatForBreeding();
    }

    @Override
    public boolean canBreedWith(PassiveEntity other) {
        boolean canBreed = this.canBreedWithGendered(other);
        RisenRaces.LOGGER.info("[Human Breeding] canBreedWith called between Human (Female=" + this.isFemale() + ") and Other (Class=" + other.getClass().getSimpleName() + "). Result: " + canBreed);
        return canBreed;
    }

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