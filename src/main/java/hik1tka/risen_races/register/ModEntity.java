package hik1tka.risen_races.register;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import hik1tka.risen_races.entity.humanoid.rynar.RynarEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModEntity {
    public static void registerModEntity(){
        RisenRaces.LOGGER.info("Register Mod Entities for " + RisenRaces.MOD_ID);

        //Entities:
        FabricDefaultAttributeRegistry.register(HumanEntity.HUMAN, HumanEntity.createHumanAttributes());
        FabricDefaultAttributeRegistry.register(RisenPiglinEntity.RISEN_PIGLIN, RisenPiglinEntity.createRisenPiglinAttributes());
        FabricDefaultAttributeRegistry.register(RynarEntity.RYNAR, RynarEntity.createRynarAttributes());
    }
}
