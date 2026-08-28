package hik1tka.risen_races.entity.humanoid.goal;

import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class PanicUntilSafeGoal extends Goal {
    private final HumanoidEntity entity;
    private final double speed;
    private LivingEntity lastAttacker;

    private int safeTimer = 0;
    private final int maxSafeTicks; // Наприклад, 100 тіків (5 секунд)

    public PanicUntilSafeGoal(HumanoidEntity entity, double speed, int safeSeconds) {
        this.entity = entity;
        this.speed = speed;
        this.maxSafeTicks = safeSeconds * 20; // 1 секунда = 20 тіків
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Старт лише при отриманні шкоди
        LivingEntity attacker = this.entity.getAttacker();
        if (attacker != null && this.entity.getLastAttackedTime() > 0) {
            this.lastAttacker = attacker;
            this.safeTimer = 0; // Скидаємо таймер
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (this.lastAttacker == null || !this.lastAttacker.isAlive()) {
            return false;
        }

        if (this.lastAttacker.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            // Якщо гравець повністю голий і під інвізом — моб його НЕ бачить взагалі
            // Якщо на гравці є броня, радіус помітності зменшується (7% за кожну частину броні)
            double armorFactor = this.lastAttacker. getArmorVisibility(); // від 0.0 (голий) до 1.0 (у повній броні)

            // Якщо гравець голий під інвізом — моб одразу втрачає його з поля зору
            if (armorFactor <= 0.0D) {
                this.safeTimer++;
                return this.safeTimer < this.maxSafeTicks;
            }
        }

        // Перевіряємо, чи бачить моб нападника (чи є лінія зору і відстань менше 32 блоків)
        boolean canSeeAttacker = this.entity.getVisibilityCache().canSee(this.lastAttacker)
                && this.entity.squaredDistanceTo(this.lastAttacker) < 1024.0; // 32^2

        if (canSeeAttacker) {
            // Гравець у полі зору — таймер не цокає, продовжуємо бігти
            this.safeTimer = 0;
            return true;
        } else {
            // Гравець зник з поля зору — вмикаємо таймер
            this.safeTimer++;
            // Біжимо, поки таймер не досягне ліміту
            return this.safeTimer < this.maxSafeTicks;
        }
    }

    @Override
    public void start() {
        this.findAndMoveAway();
    }

    @Override
    public void tick() {
        // Якщо моб дійшов до точки втечі, але таймер ще цокає — шукаємо нову точку далі від гравця
        if (this.entity.getNavigation().isIdle()) {
            this.findAndMoveAway();
        }
    }

    @Override
    public void stop() {
        this.lastAttacker = null;
        this.entity.setAttacker(null); // Очищаємо нападника, щоб заспокоїтись
        this.safeTimer = 0;
    }

    private void findAndMoveAway() {
        if (this.lastAttacker == null) return;

        // Генеруємо точку в протилежному напрямку від гравця на відстані 16 блоків
        Vec3d target = NoPenaltyTargeting.findFrom(this.entity, 16, 7, this.lastAttacker.getPos());
        if (target != null) {
            this.entity.getNavigation().startMovingTo(target.x, target.y, target.z, this.speed);
        }
    }
}