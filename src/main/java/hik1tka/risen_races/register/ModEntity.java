package hik1tka.risen_races.register;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import hik1tka.risen_races.entity.humanoid.rynar.RynarEntity;
import hik1tka.risen_races.entity.zombie.ZombifiedHumanDrownedEntity;
import hik1tka.risen_races.entity.zombie.ZombifiedHumanEntity;
import hik1tka.risen_races.entity.zombie.ZombifiedHumanHuskEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModEntity {
    public static void registerModEntity(){
        RisenRaces.LOGGER.info("Register Mod Entities for " + RisenRaces.MOD_ID);

        //Entities:
        FabricDefaultAttributeRegistry.register(HumanEntity.HUMAN, HumanEntity.createHumanAttributes());
        FabricDefaultAttributeRegistry.register(RisenPiglinEntity.RISEN_PIGLIN, RisenPiglinEntity.createRisenPiglinAttributes());
        FabricDefaultAttributeRegistry.register(RynarEntity.RYNAR, RynarEntity.createRynarAttributes());
        FabricDefaultAttributeRegistry.register(ZombifiedHumanEntity.ZOMBIFIED_HUMAN, ZombifiedHumanEntity.createZombifiedHumanAttributes());
        FabricDefaultAttributeRegistry.register(ZombifiedHumanHuskEntity.ZOMBIFIED_HUMAN_HUSK, ZombifiedHumanHuskEntity.createZombifiedHumanHuskAttributes());
        FabricDefaultAttributeRegistry.register(ZombifiedHumanDrownedEntity.ZOMBIFIED_HUMAN_DROWNED, ZombifiedHumanDrownedEntity.createZombifiedHumanDrownedAttributes());
    }
}