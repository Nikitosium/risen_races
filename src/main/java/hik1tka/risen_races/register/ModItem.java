package hik1tka.risen_races.register;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItem {

    public static void registerModItem(){
        RisenRaces.LOGGER.info("Register Mod Items for "+ RisenRaces.MOD_ID);
    }

    public static final Item HUMAN_SPAWN_EGG = registerItem("human_spawn_egg",
            new SpawnEggItem(HumanEntity.HUMAN, 0xffcc99, 0x664422, new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RisenRaces.MOD_ID, name), item);
    }
}
