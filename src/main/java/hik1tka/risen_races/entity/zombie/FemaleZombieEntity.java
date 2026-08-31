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

public class FemaleZombieEntity extends GenderedZombieEntity {

    // EntityType живе тут же, на класі сутності - та сама конвенція,
    // що вже використана для HumanEntity.HUMAN / RynarEntity.RYNAR.
    public static final EntityType<FemaleZombieEntity> FEMALE_ZOMBIE = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "female_zombie"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, FemaleZombieEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.95f))
                    .build()
    );

    private static final TrackedData<Integer> SKIN_ID =
            DataTracker.registerData(FemaleZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<String> PROFESSION =
            DataTracker.registerData(FemaleZombieEntity.class, TrackedDataHandlerRegistry.STRING);
    private static final TrackedData<Float> SCALE =
            DataTracker.registerData(FemaleZombieEntity.class, TrackedDataHandlerRegistry.FLOAT);

    public FemaleZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world, SKIN_ID, PROFESSION, SCALE);
    }

    @Override
    public float getSoundPitch() {
        return this.isBaby() ? 1.5f : 1.2f;
    }

    @Override
    public int getSkinCount() {
        return 12; // 0-11, як у жіночих скінів HumanEntity
    }

    public boolean isFemale() {
        return true;
    }
}
