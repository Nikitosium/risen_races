package hik1tka.risen_races.entity.zombie;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import hik1tka.risen_races.util.NPCConstants;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Утопець (Drowned) - варіант зомбіфікованої людини для океанів/річок/боліт/
 * пляжів та будь-якої іншої води (див. ZombieVariantHelper.resolveVariant()).
 *
 * Успадковує ВАНІЛЬНОГО DrownedEntity, а не нашого ZombifiedHumanEntity:
 * вся "утопцева" поведінка (плавання SwimNavigation, водяний MoveControl,
 * кидок тризуба по дистанції, спливання/занурення, вихід на берег) живе у
 * ПРИВАТНИХ внутрішніх класах DrownedEntity - їх не можна ні викликати, ні
 * перевикористати з іншого дерева класів, тому єдиний спосіб справді
 * "наслідувати утопця" - це успадкування самого класу. Раніше, через
 * extends ZombifiedHumanEntity, утопець умів лише "не задихатись під водою",
 * але топтався по дну як звичайний зомбі.
 *
 * Стать/професію/пам'ять/лікування дублюємо з ZombifiedHumanEntity за
 * контрактом IZombifiedHuman (див. коментар там) - NBT-ключі лишаються ті самі,
 * тож старі сейви з утопленими читаються без змін.
 */
public class ZombifiedHumanDrownedEntity extends DrownedEntity implements IZombifiedHuman {

    public static final EntityType<ZombifiedHumanDrownedEntity> ZOMBIFIED_HUMAN_DROWNED = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "zombified_human_drowned"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ZombifiedHumanDrownedEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.95f))
                    .build()
    );

    // --- Стать/професія: TrackedData реєструється ПРИВ'ЯЗАНО до конкретного
    // класу ентіті, тому спільні з ZombifiedHumanEntity об'єкти тут не підійдуть
    // (той зареєстрований проти свого класу, а ми не його нащадок). Ключі NBT
    // лишаємо ті самі - сумісність сейвів. ---
    private static final TrackedData<Boolean> IS_FEMALE =
            DataTracker.registerData(ZombifiedHumanDrownedEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<String> PROFESSION =
            DataTracker.registerData(ZombifiedHumanDrownedEntity.class, TrackedDataHandlerRegistry.STRING);

    // --- Дубльовано з ZombifiedHumanEntity навмисно: спільного предка нема
    // (див. клас-коментар), тягнути сталі через статику чужого класу гірше,
    // ніж кілька рядків дубля біля логіки, яка ними користується. ---
    private static final String[] PROFESSION_POOL = {
            "farmer", "butcher", "shepherd", "fisherman", "leatherworker", "cleric", "cartographer"
    };
    private static final float UNEMPLOYED_CHANCE = 0.4f;

    /** Орієнтовний максимум getClampedLocalDifficulty() у ваніллі - формула шансу лікування нормалізує відносно нього. */
    private static final float ASSUMED_MAX_DIFFICULTY = 6.75f;
    private static final float CURE_CHANCE_AT_MIN_DIFFICULTY = 0.35f;
    private static final float CURE_CHANCE_AT_MAX_DIFFICULTY = 0.10f;

    @Nullable
    private NbtCompound npcMemory;

    public static DefaultAttributeContainer.Builder createZombifiedHumanDrownedAttributes() {
        // Ванільний утопець не додає нічого поверх зомбачих атрибутів -
        // той самий набір, що і раніше, щоб не міняти баланс існуючих утопців.
        return ZombieEntity.createZombieAttributes();
    }

    public ZombifiedHumanDrownedEntity(EntityType<? extends DrownedEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Природний спавн (мобспавнер тощо) проходить через initialize() - тут
     * генеруємо випадкові стать/професію. ВАЖЛИВО: super.initialize() - це
     * саме DrownedEntity.initialize(), який роздає ванільне спорядження
     * утопця (іноді тризуб/зілля в руку через initEquipment) - частина
     * поведінки, яку наслідуємо, тому НЕ заміщуємо її повністю.
     */
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.rollRandomSpawnData();
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(IS_FEMALE, false);
        this.dataTracker.startTracking(PROFESSION, "none");
    }

    // ---------- IZombifiedHuman: стать / професія / пам'ять ----------

    @Override
    public boolean isFemale() {
        return this.dataTracker.get(IS_FEMALE);
    }

    @Override
    public void setFemale(boolean female) {
        this.dataTracker.set(IS_FEMALE, female);
    }

    @Override
    public String getProfession() {
        return this.dataTracker.get(PROFESSION);
    }

    @Override
    public void setProfession(String profession) {
        this.dataTracker.set(PROFESSION, profession);
    }

    @Nullable
    @Override
    public NbtCompound getNpcMemory() {
        return this.npcMemory;
    }

    @Override
    public void setNpcMemory(@Nullable NbtCompound memory) {
        this.npcMemory = memory;
    }

    @Override
    public void rollRandomSpawnData() {
        setFemale(this.random.nextBoolean());
        setProfession(this.random.nextFloat() < UNEMPLOYED_CHANCE
                ? "none"
                : PROFESSION_POOL[this.random.nextInt(PROFESSION_POOL.length)]);
    }

    // ---------- NBT (ті самі ключі, що в ZombifiedHumanEntity - сейви сумісні) ----------

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("RisenIsFemale", isFemale());
        nbt.putString("RisenProfession", getProfession());
        if (npcMemory != null) {
            nbt.put("MD_NPC_Memory", npcMemory);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("RisenIsFemale")) setFemale(nbt.getBoolean("RisenIsFemale"));
        if (nbt.contains("RisenProfession")) setProfession(nbt.getString("RisenProfession"));
        if (nbt.contains("MD_NPC_Memory")) npcMemory = nbt.getCompound("MD_NPC_Memory");
    }

    // ---------- Звук / пітч ----------

    @Override
    public float getSoundPitch() {
        // Базові звуки тут - утопцеві (успадковані від DrownedEntity разом з
        // "водяними"/"сухопутними" варіантами ambient/hurt/death), гендер
        // лише крутить пітч - той самий прийом, що в ZombifiedHumanEntity.
        float base = super.getSoundPitch();
        return isFemale() ? base * 1.15f : base * 0.9f;
    }

    // ---------- Лікування (той самий механізм, що в ZombifiedHumanEntity) ----------

    public float getCureChance(ServerWorld world) {
        float localDifficulty = world.getLocalDifficulty(this.getBlockPos()).getClampedLocalDifficulty();
        float t = MathHelper.clamp(localDifficulty / ASSUMED_MAX_DIFFICULTY, 0.0f, 1.0f);
        return MathHelper.lerp(t, CURE_CHANCE_AT_MIN_DIFFICULTY, CURE_CHANCE_AT_MAX_DIFFICULTY);
    }

    /**
     * Викликати, коли PURIFICATION-ефект добігає кінця (той самий контракт,
     * що в ZombifiedHumanEntity - підключення окремим гоулом/тиком ще попереду).
     */
    public boolean tryCure(ServerWorld world) {
        if (world.getRandom().nextFloat() >= getCureChance(world)) {
            return false;
        }

        HumanoidEntity restored = createRestoredHuman(world);
        if (restored == null) return false;

        restored.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        world.spawnEntity(restored);
        this.discard();
        return true;
    }

    /**
     * Кого відновлюємо при лікуванні - тут звичайну HumanEntity (з пам'яттю,
     * якщо утопець народився з людини; інакше випадкова нова людина).
     */
    @Nullable
    protected HumanoidEntity createRestoredHuman(ServerWorld world) {
        HumanEntity human = HumanEntity.HUMAN.create(world);
        if (human == null) return null;

        Random random = world.getRandom();
        if (npcMemory != null) {
            human.setFemale(npcMemory.getBoolean("WasFemale"));
            human.setSkinId(npcMemory.getInt("SkinID"));
            human.setProfession(npcMemory.contains("Profession") ? npcMemory.getString("Profession") : "none");
            human.setBreedingAge(npcMemory.getBoolean("IsBaby") ? -24000 : 0);
            String storedName = npcMemory.getString("StoredName");
            if (!storedName.isEmpty()) {
                human.setCustomName(Text.literal(storedName));
                human.setCustomNameVisible(true);
            }
        } else {
            boolean female = isFemale();
            human.setFemale(female);
            human.setSkinId(female ? random.nextInt(12) : random.nextInt(10));
            human.setProfession("none");
            String[] pool = female ? NPCConstants.FEMALE_NAMES : NPCConstants.MALE_NAMES;
            human.setCustomName(Text.literal(pool[random.nextInt(pool.length)]));
            human.setCustomNameVisible(true);
        }
        return human;
    }
}
