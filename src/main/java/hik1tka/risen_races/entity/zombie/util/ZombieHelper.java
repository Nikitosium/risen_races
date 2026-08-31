package hik1tka.risen_races.entity.zombie.util;

import hik1tka.risen_races.entity.humanoid.human.util.NPCConstants;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;

public class ZombieHelper {
    public static void updateBabySpeed(ZombieEntity zombie, boolean isBaby) {
        if (zombie.getWorld().isClient) return;

        EntityAttributeInstance instance = zombie.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(NPCConstants.BABY_SPEED_MODIFIER_ID);
            if (isBaby) {
                instance.addTemporaryModifier(new EntityAttributeModifier(
                        NPCConstants.BABY_SPEED_MODIFIER_ID,
                        "Baby speed boost",
                        NPCConstants.BABY_SPEED_BOOST,
                        EntityAttributeModifier.Operation.MULTIPLY_BASE
                ));
            }
        }
    }
}
