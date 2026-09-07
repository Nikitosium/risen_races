package hik1tka.risen_races.register;

import hik1tka.risen_races.entity.zombie.ZombifiedHumanEntity;
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
 *    світі - видаляється і замінюється ZombifiedHumanEntity
 *
 * rollRandomSpawnData() викликається тут ЯВНО - ENTITY_LOAD не проходить
 * через initialize() (той шлях спрацьовує лише для мобспавнера/природного
 * спавну напряму), тому без явного виклику заміщені зомбі лишались би з
 * дефолтними значеннями.
 *
 * Кадаврів (Husk) і утопців (Drowned) це поки НЕ чіпає - вони наступний крок
 * (обговорювали окремо).
 */
public class ModZombieReplacement {

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.getDisplayStacks().removeIf(stack -> stack.getItem() == Items.ZOMBIE_SPAWN_EGG);
            entries.getSearchTabStacks().removeIf(stack -> stack.getItem() == Items.ZOMBIE_SPAWN_EGG);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.getClass() != ZombieEntity.class) return;

            ZombifiedHumanEntity zombie = ZombifiedHumanEntity.ZOMBIFIED_HUMAN.create(world);
            if (zombie == null) return;

            zombie.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(),
                    entity.getYaw(), entity.getPitch());
            zombie.rollRandomSpawnData();

            entity.discard();
            world.spawnEntity(zombie);
        });
    }
}
