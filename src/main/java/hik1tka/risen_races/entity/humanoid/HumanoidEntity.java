package hik1tka.risen_races.entity.humanoid;

import hik1tka.risen_races.entity.humanoid.data.HumanoidData;
import hik1tka.risen_races.entity.humanoid.data.ProfessionDefinition;
import hik1tka.risen_races.entity.humanoid.goal.AcquireProfessionGoal;
import hik1tka.risen_races.entity.humanoid.goal.FindMateGoal;
import hik1tka.risen_races.entity.humanoid.goal.PanicUntilSafeGoal;
import hik1tka.risen_races.entity.humanoid.goal.RizenPiglinDefenseGoal;
import net.minecraft.entity.EntityType;
//import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.MerchantEntity;
//import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.poi.PointOfInterestStorage;
//import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;
import java.util.List;

//import java.util.EnumSet;

/**
 * Гуманоїдний ентіті, що заміняє ванільного жителя.
 * Спільний "мозок" (торгівля, розмноження, базова небезпека) тут,
 * а все, що відрізняється по расі, береться з HumanoidRace.
 */
public abstract class HumanoidEntity extends MerchantEntity {

    // --- DataTracker: раса та стать, синхронізуються клієнт/сервер і зберігаються в NBT вручну ---
    private static final TrackedData<Integer> RACE =
            DataTracker.registerData(HumanoidEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> IS_FEMALE =
            DataTracker.registerData(HumanoidEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> PROFESSION =
            DataTracker.registerData(HumanoidEntity.class, TrackedDataHandlerRegistry.STRING);

    // Позиція робочого місця (POI) - лише на сервері, клієнту не потрібна.
    @Nullable
    private BlockPos jobSite;

    // кулдаун розмноження, у тіках (щоб не плодились щосекунди)
    private int breedingCooldown = 0;

    public HumanoidEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    // ---------- Реєстрація атрибутів (як у жителя) ----------
    public static DefaultAttributeContainer.Builder createHumanoidAttributes() {
        return MerchantEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.27D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(RACE, HumanoidRace.HUMAN.ordinal());
        // Дефолт false: конкретна раса сама вирішує, як рандомізувати стать
        // (див., напр., HumanEntity.initDataTracker()).
        this.dataTracker.startTracking(IS_FEMALE, false);
        this.dataTracker.startTracking(PROFESSION, "none");
    }
    // ---------- HumanoidData (Паспорт для Рендеру) ----------

    /**
     * Формує об'єкт даних для рендерерів одягу, текстур та шапок.
     */
    public HumanoidData getHumanoidData() {
        return new HumanoidData(
                this.getRace().name().toLowerCase(java.util.Locale.ROOT),
                this.isFemale(),
                this.getProfession(),
                1
        );
    }

    /**
     * Повертає id поточної професії, "none" якщо безробітний.
     */
    public String getProfession() {
        return this.dataTracker.get(PROFESSION);
    }

    public void setProfession(String professionId) {
        this.dataTracker.set(PROFESSION, professionId);
    }

    @Nullable
    public BlockPos getJobSite() {
        return jobSite;
    }

    public void setJobSite(BlockPos pos) {
        this.jobSite = pos;
    }

    public void clearJobSite() {
        this.jobSite = null;
        setProfession("none");
    }

    /**
     * Список професій, доступних цій расі, і POI-типів, які їх дають.
     * За замовчуванням пусто - раса, яка хоче професії (напр. HumanEntity),
     * перевизначає це.
     */
    public List<ProfessionDefinition> getAvailableProfessions() {
        return List.of();
    }

    // ---------- Race / isFemale гетери-сетери ----------

    public HumanoidRace getRace() {
        return HumanoidRace.values()[this.dataTracker.get(RACE)];
    }

    public void setRace(HumanoidRace race) {
        this.dataTracker.set(RACE, race.ordinal());
    }

    public boolean isFemale() {
        return this.dataTracker.get(IS_FEMALE);
    }

    public void setFemale(boolean female) {
        this.dataTracker.set(IS_FEMALE, female);
    }

    public boolean isBreedingReady() {
        return breedingCooldown <= 0;
    }

    @Override
    public float getSoundPitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.5F;
        }
        return super.getSoundPitch();
    }

    public void resetBreedingCooldown() {
        // приблизно 5 хв (20 тіків/сек) - підбери під свій баланс
        this.breedingCooldown = 6000;
    }

    /**
     * Основна перевірка сумісності для розмноження:
     * - однакова раса (RaceId збігається)
     * - протилежна стать (isFemale відрізняється)
     * - обидва вже можуть розмножуватись (кулдаун пройшов)
     */
    public boolean canBreedWith(HumanoidEntity other) {
        if (other == this) return false;
        if (this.getRace() != other.getRace()) return false;
        if (this.isFemale() == other.isFemale()) return false;
        return this.isBreedingReady() && other.isBreedingReady();
    }

    /**
     * Викликається, коли пара знайдена і умови зустрілись (FindMateGoal).
     * Тут створюєш дитину, копіюєш расу, рандомізуєш стать і т.п.
     */
    public void breedWith(HumanoidEntity partner) {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        if (!canBreedWith(partner)) return;

        HumanoidEntity baby = (HumanoidEntity) getType().create(serverWorld);
        if (baby == null) return;

        baby.setRace(this.getRace());
        baby.setFemale(this.random.nextBoolean());
        // Робимо ентіті дитиною: без цього isBaby() == false і getScaleFactor()
        // (в HumanEntity) ніколи не застосовує зменшений масштаб.
        baby.setBreedingAge(-24000);
        baby.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
        // Даємо расі шанс довизначити щось специфічне для щойно народженої дитини
        // (наприклад, скін по статі - див. HumanEntity.onBabyCreated).
        onBabyCreated(baby);
        serverWorld.spawnEntityAndPassengers(baby);

        this.resetBreedingCooldown();
        partner.resetBreedingCooldown();
    }

    /**
     * Хук, що викликається одразу після створення й ініціалізації дитини
     * в breedWith(), до її заспавнення у світі. За замовчуванням нічого
     * не робить. Раси, яким треба щось довизначити для дитини (напр. скін,
     * що не входить у HumanoidData/RACE/IS_FEMALE), перевизначають цей метод.
     */
    protected void onBabyCreated(HumanoidEntity baby) {
    }

    // ---------- Goals ----------
    @Override
    protected void initGoals() {
        super.initGoals();
        GoalSelector goals = this.goalSelector;

        // Розмноження - спільне для всіх рас
        goals.add(1, new PanicUntilSafeGoal(this, 1.3D, 5));
        goals.add(2, new FindMateGoal(this));
        goals.add(3, new AcquireProfessionGoal(this));
        goals.add(6, new WanderAroundFarGoal(this, 0.6D)); // Блукання по світу
        goals.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F)); // Дивитися на гравця
        goals.add(8, new LookAroundGoal(this));

        switch (getRace().getDangerBehavior()) {
            case FLEE -> {
                // Ванільний FleeEntityGoal + список небезпечних мобів.
                // TODO: заміни HostileEntity.class на власний предикат/список,
                // якщо треба тікати не від усіх ворожих мобів, а від конкретного списку.
                goals.add(1, new FleeEntityGoal<>(this, HostileEntity.class, 8.0F, 1.0D, 1.2D));
            }
            case FIGHT -> {
                goals.add(1, new RizenPiglinDefenseGoal(this));
            }
        }
    }

    // ---------- Звуки по расі ----------
    @Override
    protected SoundEvent getAmbientSound() {
        return getRace().getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return getRace().getHurtSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return getRace().getDeathSound();
    }

    // ---------- NBT: race / isFemale ----------
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Race", getRace().ordinal());
        nbt.putBoolean("IsFemale", isFemale());
        nbt.putInt("BreedingCooldown", breedingCooldown);
        nbt.putString("Profession", getProfession());
        if (jobSite != null) {
            nbt.put("JobSite", net.minecraft.nbt.NbtHelper.fromBlockPos(jobSite));
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("Race")) {
            setRace(HumanoidRace.values()[nbt.getInt("Race")]);
        }
        if (nbt.contains("IsFemale")) {
            setFemale(nbt.getBoolean("IsFemale"));
        }
        if (nbt.contains("Profession")) {
            setProfession(nbt.getString("Profession"));
        }
        if (nbt.contains("JobSite")) {
            this.jobSite = net.minecraft.nbt.NbtHelper.toBlockPos(nbt.getCompound("JobSite"));
        }
        this.breedingCooldown = nbt.getInt("BreedingCooldown");
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient()) {
            if (breedingCooldown > 0) {
                breedingCooldown--;
            }
            // Раз на секунду перевіряємо, що робоче місце ще існує (не зламане/
            // не замінене іншим блоком) - інакше звільняємо професію.
            if (jobSite != null && this.age % 20 == 0
                    && getWorld() instanceof ServerWorld serverWorld) {
                boolean stillValid = getAvailableProfessions().stream()
                        .filter(p -> p.id().equals(getProfession()))
                        .findFirst()
                        .map(p -> serverWorld.getPointOfInterestStorage().test(jobSite, p.workstationPredicate()))
                        .orElse(false);
                if (!stillValid) {
                    clearJobSite();
                }
            }
        }
    }

    // ---------- Торгівля ----------
    // MerchantEntity вже дає тобі getOffers()/setOffers() і меню безкоштовно.
    // Тут просто підвантажуєш офери зі свого окремого дерева товарів по расі,
    // наприклад TradeOfferRegistry.getOffersFor(getRace()).
    /*@Override
    public void setOffersFromServerData(TradeOfferList offers) {
        super.setOffersFromServerData(offers);
    }*/

    @Override
    protected void fillRecipes() {
        // TODO: підʼєднай сюди свій TradeOfferRegistry, наприклад:
        // this.setOffers(TradeOfferRegistry.getOffersFor(getRace(), this.random));
        // Метод/точна назва (fillRecipes vs offers) залежить від мапінгів твоєї
        // версії Yarn - звір з декомпільованим MerchantEntity/VillagerEntity в IDE.
    }

    @Override
    public boolean isClient() {
        return this.getWorld().isClient();
    }
}