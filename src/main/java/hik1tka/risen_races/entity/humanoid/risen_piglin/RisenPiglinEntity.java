package hik1tka.risen_races.entity.humanoid.risen_piglin;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import hik1tka.risen_races.entity.humanoid.HumanoidRace;
import hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition;
import hik1tka.risen_races.entity.humanoid.goal.LowHealthFleeGoal;
import hik1tka.risen_races.util.IGenderedEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
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

    private static final float LOW_HEALTH_THRESHOLD = 5.0f;

    // TODO: тимчасовий булевий прапорець замість трекінгу конкретного гравця.
    // Коли з'явиться ефект зняття прокляття (кидаєш зілля у дикого ванільного
    // пігліна -> конвертується в RisenPiglinEntity, слідує до порталу) - саме
    // той мixin/ефект і виставлятиме hasRescuer = true в момент конвертації.
    // Поведінку "слідує за гравцем до порталу" і "тримається подалі від
    // порталу в нашому світі" підключимо окремими гоулами тоді ж - вони не
    // частина цієї змінної.
    private boolean hasRescuer = false;

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
    protected void initGoals() {
        super.initGoals();
        // Пріоритет 1 - вищий за MeleeAttackGoal (пріоритет 2 в базовому
        // HumanoidEntity), тому поки хп критично мале, бій фізично не може
        // тривати: LowHealthFleeGoal забирає Control.MOVE собі.
        this.goalSelector.add(1, new LowHealthFleeGoal(this));
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
    public float getHatYOffset() {
        // Голова пігліна геометрично вища за людську - без цього спільна
        // шапка-модель сидить трохи низько. 2px = 2/16. Підбери 2-3px
        // (0.125F-0.1875F) на око в грі.
        return -0.125F;
    }

    public void setHasRescuer(boolean value) {
        this.hasRescuer = value;
    }

    public boolean hasRescuer() {
        return this.hasRescuer;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean hurt = super.damage(source, amount);
        if (hurt && !this.getWorld().isClient && source.getAttacker() instanceof LivingEntity attacker) {
            reactToDamage(attacker);
        }
        return hurt;
    }

    private void reactToDamage(LivingEntity attacker) {
        if (this.getHealth() < LOW_HEALTH_THRESHOLD) {
            // критично мало хп - тікаємо від будь-кого, навіть якщо є рятівник
            this.setTarget(null);
            return;
        }
        if (this.hasRescuer) {
            // TODO: зараз "довіряє взагалі всім" замість "довіряє конкретно
            // тому, хто зняв прокляття" - нормально для заглушки, поки нема
            // самого ефекту й способу відрізнити свого гравця від чужого.
            return;
        }
        this.setTarget(attacker);
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
        nbt.putBoolean("HasRescuer", this.hasRescuer);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.hasRescuer = nbt.getBoolean("HasRescuer");
    }
}