package hik1tka.risen_races.entity.zombie;

import hik1tka.risen_races.RisenRaces;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MaleZombieEntity extends GenderedZombieEntity {

    public static final EntityType<MaleZombieEntity> MALE_ZOMBIE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "male_zombie"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, MaleZombieEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.95f))
                    .build()
    );

    private static final TrackedData<Integer> SKIN_ID =
            DataTracker.registerData(MaleZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PROFESSION =
            DataTracker.registerData(MaleZombieEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Float> SCALE =
            DataTracker.registerData(MaleZombieEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public MaleZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world, SKIN_ID, PROFESSION, SCALE);
    }

    @Override
    public float getSoundPitch() {
        return this.isBaby() ? 1.5f : 1.0f;
    }

    @Override
    public int getSkinCount() {
        return 10; // 0-9, як у чоловічих скінів HumanEntity
    }

    public boolean isFemale() {
        return false;
    }
}
