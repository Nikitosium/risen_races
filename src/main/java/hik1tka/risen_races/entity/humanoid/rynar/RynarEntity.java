package hik1tka.risen_races.entity.humanoid.rynar;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import hik1tka.risen_races.entity.humanoid.HumanoidRace;
import hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition;
import hik1tka.risen_races.util.IGenderedEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Раса Ринар - заміна ванільного жителя, той самий HumanoidEntity-каркас,
 * що й у HumanEntity, але без множини скінів: у Ринара всього одна текстура,
 * стать змінює лише геометрію моделі (wide/slim), не текстуру.
 */
public class RynarEntity extends HumanoidEntity implements IGenderedEntity {

    public static final EntityType<RynarEntity> RYNAR = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "rynar"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RynarEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.8f))
                    .build()
    );

    public static net.minecraft.entity.attribute.DefaultAttributeContainer.Builder createRynarAttributes() {
        return HumanoidEntity.createHumanoidAttributes();
    }

    public RynarEntity(EntityType<? extends HumanoidEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
        // TODO: підключити торгівлю, коли буде готовий TradeOfferRegistry для раси RYNAR
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setRace(HumanoidRace.RYNAR);
        // Стать вирішується тут же, а не в initDataTracker() - той метод
        // виконується і на клієнті, де рандом ще не синхронізований з сервером
        // (той самий підхід, що й у HumanEntity.initialize()).
        this.setFemale(this.random.nextBoolean());
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void onBabyCreated(HumanoidEntity baby) {
        // Текстура одна на всіх - тут довизначати нічого, на відміну від
        // HumanEntity, де для дитини ще й скін по статі рандомізується.
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null; // розмноження повністю через HumanoidEntity#breedWith, не ванільне
    }

    public float getScaleFactor() {
        return this.isBaby() ? 0.7f : 1.0f;
    }

    @Override
    public List<ProfessionDefinition> getAvailableProfessions() {
        return List.of(
                new ProfessionDefinition("farmer", net.minecraft.world.poi.PointOfInterestTypes.FARMER, 48),
                new ProfessionDefinition("butcher", net.minecraft.world.poi.PointOfInterestTypes.BUTCHER, 48),
                new ProfessionDefinition("shepherd", net.minecraft.world.poi.PointOfInterestTypes.SHEPHERD, 48),
                new ProfessionDefinition("fisherman", net.minecraft.world.poi.PointOfInterestTypes.FISHERMAN, 48),
                new ProfessionDefinition("leatherworker", net.minecraft.world.poi.PointOfInterestTypes.LEATHERWORKER, 48),
                new ProfessionDefinition("cleric", net.minecraft.world.poi.PointOfInterestTypes.CLERIC, 48),
                new ProfessionDefinition("cartographer", net.minecraft.world.poi.PointOfInterestTypes.CARTOGRAPHER, 48)
        );
    }

    @Override
    public String getRaceId() {
        return this.getRace().name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public boolean isInLove() {
        return false;
    }

    @Override
    public void setLoveTicks(int ticks) {
    }

    @Override
    public boolean canBreedWith(PassiveEntity other) {
        return this.canBreedWithGendered(other);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        // Додаткових полів нема - усе (раса/стать/проф/jobSite) вже пише HumanoidEntity.
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
    }
}