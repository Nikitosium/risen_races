package hik1tka.risen_races.entity.humanoid.risen_piglin;

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

public class RisenPiglinEntity extends HumanoidEntity implements IGenderedEntity {

    public static final EntityType<RisenPiglinEntity> RISEN_PIGLIN = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "risen_piglin"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, RisenPiglinEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.95f))
                    .build()
    );

    public static net.minecraft.entity.attribute.DefaultAttributeContainer.Builder createRisenPiglinAttributes() {
        return HumanoidEntity.createHumanoidAttributes();
    }

    public RisenPiglinEntity(EntityType<? extends HumanoidEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.setRace(HumanoidRace.RIZEN_PIGLIN);
        this.setFemale(this.random.nextBoolean());
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void onBabyCreated(HumanoidEntity baby) {
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public float getScaleFactor() {
        return this.isBaby() ? 0.7f : 1.0f;
    }

    @Override
    public List<ProfessionDefinition> getAvailableProfessions() {
        return List.of(
                new ProfessionDefinition("mason", net.minecraft.world.poi.PointOfInterestTypes.MASON, 48)
                // TODO: кастомний коваль на TOOLSMITH+WEAPONSMITH+ARMORER одразу
                //   (один ProfessionDefinition з predicate на всі три POI) -
                //   потребує своєї trade/замовлення-логіки, ще не готова.
                // TODO: важка піхота (ANVIL_POI, ліміт 3) - чекає на
                //   узагальнений capped-механізм у profession_plus/.
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
}