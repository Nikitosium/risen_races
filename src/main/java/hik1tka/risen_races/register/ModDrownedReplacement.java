package hik1tka.risen_races.register;

import hik1tka.risen_races.entity.zombie.ZombifiedHumanDrownedEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class ModDrownedReplacement {

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.getDisplayStacks().removeIf(stack -> stack.getItem() == Items.DROWNED_SPAWN_EGG);
            entries.getSearchTabStacks().removeIf(stack -> stack.getItem() == Items.DROWNED_SPAWN_EGG);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.getClass() != DrownedEntity.class) return;

            ZombifiedHumanDrownedEntity drowned = ZombifiedHumanDrownedEntity.ZOMBIFIED_HUMAN_DROWNED.create(world);
            if (drowned == null) return;

            drowned.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(),
                    entity.getYaw(), entity.getPitch());

            drowned.rollRandomSpawnData();

            entity.discard();
            world.spawnEntity(drowned);
        });
    }
}