package hik1tka.risen_races.entity.humanoid.goal;

import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Box;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Активно веде HumanoidEntity до найближчого предмета їжі (їстівне з
 * HumanoidEntity.BREEDING_FOOD_VALUES) в радіусі DETECTION_RADIUS.
 *
 * Сам підбір ("взяти в руки" + звук) робить вбудований цикл
 * MobEntity.tick() через HumanoidEntity.loot(...), коли предмет
 * опиняється в межах ~1 блоку - цей Goal лише забезпечує підхід.
 */
public class PickUpFoodGoal extends Goal {

    private static final double DETECTION_RADIUS = 8.0D;
    private static final double MOVE_SPEED = 0.6D;

    private final HumanoidEntity humanoid;
    private ItemEntity targetItem;

    public PickUpFoodGoal(HumanoidEntity humanoid) {
        this.humanoid = humanoid;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Вже досить їжі назбирано - не відволікаємось на нову, звільняємо
        // пріоритет для FindMateGoal.
        if (humanoid.hasEnoughFoodToBreed()) {
            return false;
        }
        Box box = humanoid.getBoundingBox().expand(DETECTION_RADIUS);
        List<ItemEntity> items = humanoid.getWorld().getEntitiesByClass(
                ItemEntity.class, box,
                e -> e.isAlive()
                        && !e.getStack().isEmpty()
                        && HumanoidEntity.isBreedingFood(e.getStack().getItem())
                        && humanoid.canSee(e));
        targetItem = items.stream()
                .min(Comparator.comparingDouble(humanoid::squaredDistanceTo))
                .orElse(null);
        return targetItem != null;
    }

    @Override
    public boolean shouldContinue() {
        return targetItem != null
                && targetItem.isAlive()
                && !targetItem.getStack().isEmpty()
                && !humanoid.hasEnoughFoodToBreed();
    }

    @Override
    public void tick() {
        if (targetItem == null) {
            return;
        }
        humanoid.getNavigation().startMovingTo(targetItem, MOVE_SPEED);
    }

    @Override
    public void stop() {
        targetItem = null;
        humanoid.getNavigation().stop();
    }
}
