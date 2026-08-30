package hik1tka.risen_races.register;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import hik1tka.risen_races.entity.humanoid.rynar.RynarEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItem {

    public static final Item HUMAN_SPAWN_EGG = registerItem("human_spawn_egg",
            new SpawnEggItem(HumanEntity.HUMAN, 0xffcc99, 0x664422, new Item.Settings()));

    public static final Item RISEN_PIGLIN_SPAWN_EGG = registerItem("risen_piglin_spawn_egg",
            new SpawnEggItem(RisenPiglinEntity.RISEN_PIGLIN, 0xd8b09c, 0x6b2020, new Item.Settings()));

    public static final Item RYNAR_SPAWN_EGG = registerItem("rynar_spawn_egg",
            new SpawnEggItem(RynarEntity.RYNAR, 0x8b7355, 0x4a3728, new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RisenRaces.MOD_ID, name), item);
    }

    public static void registerModItem(){
        RisenRaces.LOGGER.info("Register Mod Items for "+ RisenRaces.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(HUMAN_SPAWN_EGG);
            entries.add(RISEN_PIGLIN_SPAWN_EGG);
            entries.add(RYNAR_SPAWN_EGG);
        });
    }
}