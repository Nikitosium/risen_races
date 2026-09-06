package hik1tka.risen_races.register;

import hik1tka.risen_races.entity.humanoid.rynar.RynarEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemGroups;

/**
 * Прибирає ванільного жителя з гри й заміняє на Rynar:
 *  - ховає VILLAGER_SPAWN_EGG з креативної вкладки (яйце фізично лишається
 *    зареєстрованим предметом гри - Minecraft/датапаки цього вимагають -
 *    просто гравець його більше ніде не бачить і не може дістати звичним шляхом)
 *  - будь-який VillagerEntity, що зʼявляється у світі (природна генерація
 *    села, завантаження чанка зі старим сейвом і т.п.) - одразу видаляється
 *    і на його місці зʼявляється RynarEntity
 *
 * НЕ блокує /summon minecraft:villager (команда - пряма дія гравця/адміна,
 * не "генерація") і не займається біомними варіаціями Rynar - за словами
 * автора, це буде підключено окремо пізніше.
 */
public class ModVillagerReplacement {

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.getDisplayStacks().removeIf(stack -> stack.getItem() == Items.VILLAGER_SPAWN_EGG);
            entries.getSearchTabStacks().removeIf(stack -> stack.getItem() == Items.VILLAGER_SPAWN_EGG);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.getClass() != VillagerEntity.class) return;

            RynarEntity rynar = RynarEntity.RYNAR.create(world);
            if (rynar == null) return;

            rynar.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(),
                    entity.getYaw(), entity.getPitch());

            entity.discard();
            world.spawnEntity(rynar);
        });
    }
}