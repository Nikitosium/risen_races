package hik1tka.risen_races.register;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItem {

    // Створюємо яйце призову (кольори: шкіряний та коричневий)
    public static final Item HUMAN_SPAWN_EGG = registerItem("human_spawn_egg",
            new SpawnEggItem(HumanEntity.HUMAN, 0xffcc99, 0x664422, new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RisenRaces.MOD_ID, name), item);
    }

    public static void registerModItem(){
        RisenRaces.LOGGER.info("Register Mod Items for "+ RisenRaces.MOD_ID);

        // Додаємо яйце у креативну вкладку "Яйця призову"
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
            entries.add(HUMAN_SPAWN_EGG);
        });
    }
}