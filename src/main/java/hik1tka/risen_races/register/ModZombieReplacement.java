package hik1tka.risen_races.register;

import hik1tka.risen_races.entity.zombie.IZombifiedHuman;
import hik1tka.risen_races.util.ZombieVariant;
import hik1tka.risen_races.util.ZombieVariantHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

/**
 * Той самий принцип, що ModVillagerReplacement, тільки для зомбі:
 *  - ховає ZOMBIE_SPAWN_EGG з креативної вкладки
 *  - будь-який ТОЧНО ZombieEntity (не Husk/Drowned/наш власний зомбі -
 *    getClass() != ZombieEntity.class відсікає підкласи), що з'являється у
 *    світі - видаляється і замінюється відповідним підвидом
 *    ZombifiedHumanEntity (звичайний/кадавр/утопець - за біомом спавну,
 *    див. ZombieVariantHelper.resolveVariant()).
 *
 * rollRandomSpawnData() викликається тут ЯВНО - ENTITY_LOAD не проходить
 * через initialize() (той шлях спрацьовує лише для мобспавнера/природного
 * спавну напряму), тому без явного виклику заміщені зомбі лишались би з
 * дефолтними значеннями.
 *
 * Дикого ванільного Husk/Drowned це не чіпає навмисно - getClass() !=
 * ZombieEntity.class відсікає їх так само, як і наш власний зомбі: якщо
 * колись знадобиться заміняти й дикого Husk/Drowned з ваніли - це окремий
 * крок (наразі за задумом всі варіанти нашого зомбі народжуються ЛИШЕ
 * через конвертацію звичайного ZombieEntity, за біомом).
 */
public class ModZombieReplacement {

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.getDisplayStacks().removeIf(stack -> stack.getItem() == Items.ZOMBIE_SPAWN_EGG);
            entries.getSearchTabStacks().removeIf(stack -> stack.getItem() == Items.ZOMBIE_SPAWN_EGG);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.getClass() != ZombieEntity.class) return;

            ZombieVariant variant = ZombieVariantHelper.resolveVariant(world, entity);
            // Тип - ZombieEntity (утопець більше не наслідує ZombifiedHumanEntity),
            // рандомні дані для "дикого" спавну ставить через інтерфейс.
            ZombieEntity zombie = ZombieVariantHelper.create(world, variant);
            if (zombie == null) return;

            zombie.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(),
                    entity.getYaw(), entity.getPitch());

            if (zombie instanceof IZombifiedHuman zombified) {
                zombified.rollRandomSpawnData();
            }

            entity.discard();
            world.spawnEntity(zombie);
        });
    }
}
