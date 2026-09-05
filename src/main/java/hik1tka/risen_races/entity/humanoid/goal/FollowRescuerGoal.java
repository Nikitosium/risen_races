package hik1tka.risen_races.entity.humanoid.goal;

import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Слідування врятованого RisenPiglin за ціллю, яку повертає
 * RisenPiglinEntity.resolveFollowTarget() - гравець-рятівник, якщо цей
 * піглін "головний", або інший RisenPiglin попереду в ланцюжку ("паровозик",
 * як караван лам) - див. RisenPiglinEntity.assignTrainPosition().
 *
 * Активний лише в Незері - за межами Незеру рятувати вже нема від чого
 * тікати, і піглін повертається до звичайної поведінки раси.
 */
public class FollowRescuerGoal extends Goal {

    private static final double FOLLOW_SPEED = 1.0D;
    // Тримає дистанцію "паровозика" - не стається впритул одне на одного.
    private static final double STOP_DISTANCE_SQ = 3.0D * 3.0D;
    private static final double START_DISTANCE_SQ = 4.0D * 4.0D;

    private final RisenPiglinEntity piglin;
    @Nullable
    private LivingEntity followTarget;

    public FollowRescuerGoal(RisenPiglinEntity piglin) {
        this.piglin = piglin;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        if (!isInNether() || !piglin.hasRescuer()) {
            return false;
        }
        followTarget = piglin.resolveFollowTarget();
        return followTarget != null
                && piglin.squaredDistanceTo(followTarget) > START_DISTANCE_SQ;
    }

    @Override
    public boolean shouldContinue() {
        if (!isInNether() || !piglin.hasRescuer()) {
            return false;
        }
        followTarget = piglin.resolveFollowTarget();
        return followTarget != null
                && followTarget.isAlive()
                && piglin.squaredDistanceTo(followTarget) > STOP_DISTANCE_SQ;
    }

    @Override
    public void tick() {
        if (followTarget == null) {
            return;
        }
        piglin.getLookControl().lookAt(followTarget);
        if (piglin.getNavigation().isIdle()) {
            piglin.getNavigation().startMovingTo(followTarget, FOLLOW_SPEED);
        }
    }

    @Override
    public void stop() {
        followTarget = null;
        piglin.getNavigation().stop();
    }

    private boolean isInNether() {
        return piglin.getWorld().getRegistryKey() == World.NETHER;
    }
}
