package hik1tka.risen_races.register;

import hik1tka.risen_races.entity.zombie.ZombifiedHumanHuskEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class ModHuskReplacement {

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.getDisplayStacks().removeIf(stack -> stack.getItem() == Items.HUSK_SPAWN_EGG);
            entries.getSearchTabStacks().removeIf(stack -> stack.getItem() == Items.HUSK_SPAWN_EGG);
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity.getClass() != HuskEntity.class) return;

            ZombifiedHumanHuskEntity husk = ZombifiedHumanHuskEntity.ZOMBIFIED_HUMAN_HUSK.create(world);
            if (husk == null) return;

            husk.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(),
                    entity.getYaw(), entity.getPitch());

            husk.rollRandomSpawnData();

            entity.discard();
            world.spawnEntity(husk);
        });
    }
}