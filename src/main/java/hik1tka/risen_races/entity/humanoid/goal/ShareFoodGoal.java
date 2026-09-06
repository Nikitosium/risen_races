package hik1tka.risen_races.entity.humanoid.goal;

import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Box;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * Гуманоїд із "зайвою" їжею (>= 2 штуки одного їстівного предмета) підходить
 * до найбіднішого сусіда поруч (менше штук їжі, ніж у нього) і віддає 1
 * штуку. Порівняння - по КІЛЬКОСТІ штук, не по "балах" з BREEDING_FOOD_VALUES
 * (див. HumanoidEntity.getTotalFoodItemCount()/shareOneFoodItemWith()).
 */
public class ShareFoodGoal extends Goal {

    private static final double DETECTION_RADIUS = 8.0D;
    private static final double MOVE_SPEED = 0.6D;
    private static final double SHARE_DISTANCE_SQ = 4.0D; // ~2 блоки

    private final HumanoidEntity giver;
    private HumanoidEntity recipient;

    public ShareFoodGoal(HumanoidEntity giver) {
        this.giver = giver;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (giver.getTotalFoodItemCount() < 2) {
            return false; // нема чим ділитися - самому лишиться 0
        }

        Box box = giver.getBoundingBox().expand(DETECTION_RADIUS);
        List<HumanoidEntity> candidates = giver.getWorld().getEntitiesByClass(
                HumanoidEntity.class, box,
                other -> other != giver
                        && other.isAlive()
                        && other.getTotalFoodItemCount() < giver.getTotalFoodItemCount());

        recipient = candidates.stream()
                .min(Comparator.comparingInt(HumanoidEntity::getTotalFoodItemCount)
                        .thenComparingDouble(giver::squaredDistanceTo))
                .orElse(null);
        return recipient != null;
    }

    @Override
    public boolean shouldContinue() {
        return recipient != null
                && recipient.isAlive()
                && giver.getTotalFoodItemCount() >= 2
                && recipient.getTotalFoodItemCount() < giver.getTotalFoodItemCount();
    }

    @Override
    public void tick() {
        if (recipient == null) return;
        giver.getNavigation().startMovingTo(recipient, MOVE_SPEED);
        if (giver.squaredDistanceTo(recipient) <= SHARE_DISTANCE_SQ) {
            giver.shareOneFoodItemWith(recipient);
            recipient = null; // готово - наступного разу canStart() сам перевірить, чи ще треба
        }
    }

    @Override
    public void stop() {
        recipient = null;
        giver.getNavigation().stop();
    }
}
