package hik1tka.risen_races.entity.zombie;

import hik1tka.risen_races.entity.zombie.util.ZombieHelper;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

/**
 * Спільна база FemaleZombieEntity/MaleZombieEntity. У попередній версії ці
 * два класи були майже дослівною копією одне одного (SkinID/Profession/Scale
 * трекінг, NBT читання/запис, hurt/death/ambient звуки, loot table) - тепер
 * різниця між статями зведена рівно до того, що дійсно відрізняється:
 * isFemale(), питч звуку і межі скіна (0-9 чоловік / 0-11 жінка).
 */
public abstract class GenderedZombieEntity extends ZombieEntity {

    private static final float MIN_SCALE = 0.85f;
    private static final float MAX_SCALE = 1.10f;

    private final TrackedData<Integer> skinId;
    private final TrackedData<String> profession;
    private final TrackedData<Float> scale;

    protected GenderedZombieEntity(EntityType<? extends ZombieEntity> entityType, World world,
                                    TrackedData<Integer> skinId, TrackedData<String> profession, TrackedData<Float> scale) {
        super(entityType, world);
        this.skinId = skinId;
        this.profession = profession;
        this.scale = scale;
    }

    /** Верхня межа для випадкового SkinID цієї статі (10 у чоловіків, 12 у жінок) - за зразком HumanEntity. */
    public abstract int getSkinCount();

    public static DefaultAttributeContainer.Builder createGenderedZombieAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23);
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(baby);
        ZombieHelper.updateBabySpeed(this, baby);
        this.calculateDimensions();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        float s = this.getScaleModifier();
        if (this.isBaby()) s *= 0.7f;
        return super.getDimensions(pose).scaled(s);
    }

    @Override
    public void onDataTrackerUpdate(List<DataTracker.SerializedEntry<?>> entries) {
        super.onDataTrackerUpdate(entries);
        this.calculateDimensions();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(skinId, 0);
        this.dataTracker.startTracking(profession, "none");
        this.dataTracker.startTracking(scale, 1.0f);
    }

    public int getSkinId() {
        return this.dataTracker.get(skinId);
    }

    public void setSkinId(int id) {
        this.dataTracker.set(skinId, id);
    }

    public String getProfession() {
        return this.dataTracker.get(profession);
    }

    public void setProfession(String value) {
        this.dataTracker.set(profession, value);
    }

    public float getScaleModifier() {
        return this.dataTracker.get(scale);
    }

    public void setScaleModifier(float value) {
        this.dataTracker.set(scale, Math.max(MIN_SCALE, Math.min(MAX_SCALE, value)));
        this.calculateDimensions();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("SkinId", this.getSkinId());
        nbt.putString("Profession", this.getProfession());
        nbt.putFloat("Scale", this.getScaleModifier());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setSkinId(nbt.getInt("SkinId"));
        this.setProfession(nbt.contains("Profession") ? nbt.getString("Profession") : "none");
        this.setScaleModifier(nbt.contains("Scale") ? nbt.getFloat("Scale") : 1.0f);
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected Identifier getLootTableId() {
        return new Identifier("minecraft", "entities/zombie");
    }
}
