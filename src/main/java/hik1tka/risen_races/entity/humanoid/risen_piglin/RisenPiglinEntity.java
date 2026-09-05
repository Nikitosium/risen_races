package hik1tka.risen_races.entity.humanoid.risen_piglin;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import hik1tka.risen_races.entity.humanoid.HumanoidRace;
import hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition;
import hik1tka.risen_races.entity.humanoid.goal.FollowRescuerGoal;
import hik1tka.risen_races.entity.humanoid.goal.LowHealthFleeGoal;
import hik1tka.risen_races.entity.humanoid.goal.SeekNetherPortalGoal;
import hik1tka.risen_races.util.IGenderedEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    // Радіус пошуку "сусідів по каравану" при формуванні паровозика.
    private static final double TRAIN_SEARCH_RADIUS = 16.0D;

    // UUID гравця, який зняв прокляття з цього пігліна. null - ще не врятований.
    // TODO: виставляється через setRescuer(...), який має викликати майбутній
    // ефект/mixin конверсії дикого ванільного пігліна в RisenPiglinEntity.
    @Nullable
    private UUID rescuerUuid;

    // UUID іншого RisenPiglin, який іде ПОПЕРЕДУ цього в ланцюжку каравану
    // ("паровозиком", як лами). null означає "я головний у каравані - слідую
    // напряму за гравцем" (rescuerUuid), а не за іншим пігліном.
    @Nullable
    private UUID trainLeaderUuid;

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
        // Пріоритет 0 - вищий за все (навіть за SeekNetherPortalGoal): поки хп
        // критично мале, бій і біг до порталу фізично не можуть тривати -
        // LowHealthFleeGoal забирає Control.MOVE собі.
        this.goalSelector.add(0, new LowHealthFleeGoal(this));
        // Пріоритет 1: "тупо біжить" до порталу, щойно бачить - вищий за бій
        // (2) і слідування (4), нижчий лише за критичну втечу (0).
        this.goalSelector.add(1, new SeekNetherPortalGoal(this));
        // Пріоритет 4: фонове слідування за рятівником/лідером каравану -
        // нижчий і за бій (2), і за "економіку" раси (3), щоб піглін не
        // ігнорував напад чи професію заради простого прямування за кимось.
        this.goalSelector.add(4, new FollowRescuerGoal(this));
    }

    /**
     * Викликається (поки що вручну/ззовні - в майбутньому ефектом зняття
     * прокляття) в момент, коли гравець "рятує" цього пігліна. Прив'язує
     * рятівника й одразу намагається вписати пігліна в існуючий караван
     * (паровозик) інших уже врятованих піглінів того самого гравця -
     * див. assignTrainPosition().
     */
    public void setRescuer(PlayerEntity player) {
        this.rescuerUuid = player.getUuid();
        assignTrainPosition();
    }

    public boolean hasRescuer() {
        return this.rescuerUuid != null;
    }

    @Nullable
    public UUID getRescuerUuid() {
        return this.rescuerUuid;
    }

    /**
     * Хто йде "попереду" в караванному ланцюжку (паровозик, як у лам):
     * якщо серед уже врятованих піглінів того самого гравця поблизу є хтось,
     * за ким ще ніхто не йде ("хвіст" каравану) - чіпляємось за нього.
     * Якщо поруч нікого (перший врятований, чи всі вже комусь лідери) -
     * trainLeaderUuid лишається null, і піглін іде напряму за гравцем.
     */
    private void assignTrainPosition() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld) || rescuerUuid == null) {
            return;
        }

        Box box = this.getBoundingBox().expand(TRAIN_SEARCH_RADIUS);
        List<RisenPiglinEntity> siblings = serverWorld.getEntitiesByClass(
                RisenPiglinEntity.class, box,
                e -> e != this && rescuerUuid.equals(e.rescuerUuid));

        if (siblings.isEmpty()) {
            this.trainLeaderUuid = null;
            return;
        }

        // Хто вже комусь "лідер" (є хтось, хто вже за ним іде) - той не тягне
        // за собою ще одного, ланцюжок росте лише в "хвіст".
        Set<UUID> alreadyFollowed = new HashSet<>();
        for (RisenPiglinEntity sibling : siblings) {
            if (sibling.trainLeaderUuid != null) {
                alreadyFollowed.add(sibling.trainLeaderUuid);
            }
        }

        RisenPiglinEntity tail = null;
        double closestDistSq = Double.MAX_VALUE;
        for (RisenPiglinEntity sibling : siblings) {
            if (alreadyFollowed.contains(sibling.getUuid())) {
                continue;
            }
            double d = this.squaredDistanceTo(sibling);
            if (d < closestDistSq) {
                closestDistSq = d;
                tail = sibling;
            }
        }

        this.trainLeaderUuid = (tail != null) ? tail.getUuid() : null;
    }

    /**
     * Ціль, за якою зараз реально треба йти (FollowRescuerGoal). Якщо лідер
     * попереду в ланцюжку помер/зник - "просуваємось" і тимчасово йдемо
     * напряму за гравцем (спрощення - в ідеалі варто перечіплятись за
     * НАСТУПНОГО в ланцюжку, а не одразу за гравця, але для цього потрібен
     * зворотній зв'язок "хто йшов за зниклим", якого зараз немає).
     */
    @Nullable
    public LivingEntity resolveFollowTarget() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return null;
        }

        if (trainLeaderUuid != null) {
            Entity leader = serverWorld.getEntity(trainLeaderUuid);
            if (leader instanceof RisenPiglinEntity piglinLeader && piglinLeader.isAlive()) {
                return piglinLeader;
            }
            trainLeaderUuid = null;
        }

        if (rescuerUuid != null) {
            PlayerEntity player = serverWorld.getPlayerByUuid(rescuerUuid);
            if (player != null && player.isAlive()) {
                return player;
            }
        }
        return null;
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
            // критично мало хп - тікаємо від будь-кого, навіть якщо це рятівник
            this.setTarget(null);
            return;
        }
        // Тепер довіряємо КОНКРЕТНО тому гравцю, що зняв прокляття (rescuerUuid),
        // а не всім підряд - на відміну від старої заглушки-прапорця.
        if (this.hasRescuer() && attacker instanceof PlayerEntity player
                && player.getUuid().equals(this.rescuerUuid)) {
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
        if (this.rescuerUuid != null) {
            nbt.putUuid("RescuerUuid", this.rescuerUuid);
        }
        if (this.trainLeaderUuid != null) {
            nbt.putUuid("TrainLeaderUuid", this.trainLeaderUuid);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.rescuerUuid = nbt.containsUuid("RescuerUuid") ? nbt.getUuid("RescuerUuid") : null;
        this.trainLeaderUuid = nbt.containsUuid("TrainLeaderUuid") ? nbt.getUuid("TrainLeaderUuid") : null;
    }
}