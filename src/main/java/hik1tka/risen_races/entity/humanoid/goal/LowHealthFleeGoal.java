package hik1tka.risen_races.entity.humanoid.goal;

import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Коли хп критично мале (< 5), гуманоїд тікає від того, хто його востаннє
 * вдарив - навіть якщо в звичайному стані мав би битися (FIGHT-раса) чи
 * ігнорувати цього конкретного нападника (наприклад, рятівника - див.
 * RisenPiglinEntity.reactToDamage()).
 *
 * Реєструється з ВИЩИМ пріоритетом (менше число), ніж MeleeAttackGoal -
 * поки цей гоул активний, MeleeAttackGoal фізично не може отримати
 * Control.MOVE, тому бійка зупиняється сама собою.
 *
 * ПРИМІТКА: замість net.minecraft.entity.ai.NoPenaltyTargeting (сигнатура
 * "away from" відрізняється між версіями/мапінгами і в 1.20.1 викликала
 * помилку компіляції) точка втечі рахується вручну - просто вектор від
 * нападника на фіксовану дистанцію. Навігація сама відкине точку, якщо
 * шлях туди не існує (просто не зрушить з місця), без винятків.
 */
public class LowHealthFleeGoal extends Goal {

    private static final float HEALTH_THRESHOLD = 5.0f;
    private static final double FLEE_SPEED = 1.4D;
    private static final double FLEE_DISTANCE = 10.0D;
    private static final int RECENT_ATTACK_TICKS = 100; // ~5 сек - щоб не тікати вічно від старої атаки

    private final HumanoidEntity entity;

    public LowHealthFleeGoal(HumanoidEntity entity) {
        this.entity = entity;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        LivingEntity attacker = entity.getAttacker();
        return entity.getHealth() < HEALTH_THRESHOLD
                && attacker != null
                && attacker.isAlive()
                && entity.getWorld().getTime() - entity.getLastAttackedTime() < RECENT_ATTACK_TICKS;
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void start() {
        recalculateFleePoint();
    }

    @Override
    public void tick() {
        // Перераховуємо напрямок втечі, лише коли попередній шлях завершився -
        // нападник міг зрушити з місця, поки ми йшли.
        if (entity.getNavigation().isIdle()) {
            recalculateFleePoint();
        }
    }

    @Override
    public void stop() {
        entity.getNavigation().stop();
    }

    private void recalculateFleePoint() {
        LivingEntity attacker = entity.getAttacker();
        if (attacker == null) return;

        Vec3d fromAttacker = entity.getPos().subtract(attacker.getPos());
        if (fromAttacker.lengthSquared() < 1.0E-4) {
            // нападник стоїть впритул/на тій самій точці - беремо довільний напрямок
            fromAttacker = new Vec3d(entity.getRandom().nextDouble() - 0.5, 0, entity.getRandom().nextDouble() - 0.5);
        }
        Vec3d direction = fromAttacker.normalize();
        Vec3d fleeTarget = entity.getPos().add(direction.multiply(FLEE_DISTANCE));

        entity.getNavigation().startMovingTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, FLEE_SPEED);
    }
}
