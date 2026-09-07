package hik1tka.risen_races.entity.zombie;

import hik1tka.risen_races.RisenRaces;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * Утопець (Drowned) - варіант ZombifiedHumanEntity для океанів/річок/боліт/
 * пляжів та будь-якої іншої води (див. ZombieVariantHelper.resolveVariant()).
 * Успадковує памʼять/лікування/стать/професію від ZombifiedHumanEntity.
 *
 * КАРКАС: наразі лише "не задихається під водою" (canBreatheInWater()).
 * Повноцінне плавання (SwimNavigation/AquaticMoveControl, як у ванільного
 * DrownedEntity) - свідомо не чіпаю в цьому кроці, бо там багато
 * версієзалежних деталей (createNavigation()/createMoveControl()/
 * updateSwimming()). Якщо на практиці утопець буде незграбно ходити по дну
 * замість плавання - наступний крок саме сюди.
 */
public class ZombifiedHumanDrownedEntity extends ZombifiedHumanEntity {

    public static final EntityType<ZombifiedHumanDrownedEntity> ZOMBIFIED_HUMAN_DROWNED = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "zombified_human_drowned"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ZombifiedHumanDrownedEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.95f))
                    .build()
    );

    public static DefaultAttributeContainer.Builder createZombifiedHumanDrownedAttributes() {
        return ZombifiedHumanEntity.createZombifiedHumanAttributes();
    }

    public ZombifiedHumanDrownedEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * TODO: перевір точну назву в декомпільованому LivingEntity під твій
     * Yarn мапінг, якщо не компілюється (можливо canBreatheInWater() або
     * подібне за змістом).
     */
    @Override
    public boolean canBreatheInWater() {
        return true;
    }
}
