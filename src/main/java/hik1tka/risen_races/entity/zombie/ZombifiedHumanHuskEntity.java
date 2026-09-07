package hik1tka.risen_races.entity.zombie;

import hik1tka.risen_races.RisenRaces;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

/**
 * Кадавр (Husk) - варіант ZombifiedHumanEntity для пустель/саван/джунглів
 * (див. ZombieVariantHelper.resolveVariant()). Успадковує всю логіку
 * памʼяті/лікування/статі/професії від ZombifiedHumanEntity - тут лише
 * два поведінкові штрихи, що відрізняють ванільного Husk від Zombie.
 */
public class ZombifiedHumanHuskEntity extends ZombifiedHumanEntity {

    public static final EntityType<ZombifiedHumanHuskEntity> ZOMBIFIED_HUMAN_HUSK = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(RisenRaces.MOD_ID, "zombified_human_husk"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ZombifiedHumanHuskEntity::new)
                    .dimensions(EntityDimensions.changing(0.6f, 1.95f))
                    .build()
    );

    public static DefaultAttributeContainer.Builder createZombifiedHumanHuskAttributes() {
        // Ті самі атрибути, що й у звичайного зомбі-людини - Husk у ваніллі
        // теж не відрізняється базовими статами від Zombie.
        return ZombifiedHumanEntity.createZombifiedHumanAttributes();
    }

    public ZombifiedHumanHuskEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    /**
     * Кадавр не горить на сонці - ключова відмінність від звичайного зомбі.
     * TODO: якщо isAffectedByDaylight() відсутній/перейменований у твоєму
     * Yarn мапінгу - звір з декомпільованим HuskEntity в IDE.
     */
    @Override
    protected boolean isAffectedByDaylight() {
        return false;
    }

    /**
     * Той самий ефект голоду при ударі, що у ванільного Husk (спрощено -
     * фіксована тривалість, без розбивки по силі удару/зброї).
     * TODO: якщо сигнатура tryAttack(Entity) відрізняється у твоєму мапінгу -
     * звір з декомпільованим MobEntity/ZombieEntity в IDE.
     */
    @Override
    public boolean tryAttack(Entity target) {
        boolean success = super.tryAttack(target);
        if (success && target instanceof LivingEntity livingTarget) {
            int durationTicks = this.getWorld().getDifficulty() == Difficulty.HARD ? 15 * 20 : 7 * 20;
            livingTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, durationTicks, 0));
        }
        return success;
    }
}
