package hik1tka.risen_races.entity.humanoid.ai;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;

public class HumanoidFleeTask extends Task<AbstractHumanoidEntity> {
    private final float maxDistance;
    private final double speed;
    private LivingEntity dangerTarget;

    public HumanoidFleeTask(float maxDistance, double speed) {
        // Прописуємо, що таск взаємодіє з модулем ходьби та погляду
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.BREED_TARGET, MemoryModuleState.REGISTERED // Щоб скинути розмноження при шухері
        ));
        this.maxDistance = maxDistance;
        this.speed = speed;
    }

    // 1. ПЕРЕВІРКА: Чи є небезпека поруч?
    @Override
    protected boolean shouldRun(ServerWorld world, AbstractHumanoidEntity entity) {
        // Якщо це Піглін — у нього може бути інша логіка (він б'ється), тому перевіряємо тільки Людей/Ринарів
        if (entity.getRace() == AbstractHumanoidEntity.Race.PIGLIN) {
            return false;
        }

        // Шукаємо ворогів у коробці навколо моба
        Box searchBox = entity.getBoundingBox().expand(this.maxDistance, 4.0, this.maxDistance);
        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, searchBox, (living) -> true);

        for (LivingEntity potentialDanger : nearbyEntities) {
            // Твій список небезпечних тварин
            if (potentialDanger instanceof ZombieEntity ||
                    potentialDanger instanceof SkeletonEntity ||
                    potentialDanger instanceof CreeperEntity ||
                    potentialDanger instanceof EndermanEntity ||
                    potentialDanger instanceof SpiderEntity ||
                    potentialDanger instanceof WitchEntity) {

                this.dangerTarget = potentialDanger;
                return true; // Знайшли ворога — запускаємо таск втікання!
            }
        }
        return false;
    }

    // 2. СТАРТ: Що робимо в перший момент?
    @Override
    protected void run(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        Brain<?> brain = entity.getBrain();

        // Якщо моб у цей момент думав про секс (розмноження) — миттєво очищаємо цю пам'ять! БЕЗПЕКА НАЙВАЖЛИВІША.
        if (brain.hasMemoryModule(MemoryModuleType.BREED_TARGET)) {
            brain.forget(MemoryModuleType.BREED_TARGET);
        }

        this.fleeFromDanger(entity);
    }

    // 3. КОЖЕН ТІК: Оновлюємо маршрут втікання
    @Override
    protected void keepRunning(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        // Якщо ворог підійшов ближче або все ще в радіусі — продовжуємо рахувати шлях ВІД нього
        if (this.dangerTarget != null && entity.squaredDistanceTo(this.dangerTarget) < (this.maxDistance * this.maxDistance)) {
            this.fleeFromDanger(entity);
        }
    }

    // Логіка прорахунку координат безпечної точки (протилежний вектор від ворога)
    private void fleeFromDanger(AbstractHumanoidEntity entity) {
        if (this.dangerTarget == null) return;

        // Рахуємо вектор: куди бігти, щоб віддалитися від ворога
        double diffX = entity.getX() - this.dangerTarget.getX();
        double diffZ = entity.getZ() - this.dangerTarget.getZ();

        // Проектуємо точку втікання на відстань 10 блоків від ворога
        double targetX = entity.getX() + Math.signum(diffX) * 10;
        double targetZ = entity.getZ() + Math.signum(diffZ) * 10;

        // Створюємо WalkTarget (модуль пам'яті для ходьби в системі Brain)
        // Задаємо точку, швидкість бігу (this.speed) і точність наближення (за 2 блоки до точки зупинитись)
        entity.getBrain().remember(MemoryModuleType.WALK_TARGET, new WalkTarget(
                new net.minecraft.util.math.BlockPos((int)targetX, (int)entity.getY(), (int)targetZ),
                (float)this.speed,
                2
        ));
    }

    // 4. ФІНІШ: Коли ворог далеко, заспокоюємось
    @Override
    protected boolean shouldKeepRunning(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        return this.dangerTarget != null && this.dangerTarget.isAlive() && entity.squaredDistanceTo(this.dangerTarget) < (this.maxDistance * this.maxDistance);
    }

    @Override
    protected void finishRunning(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        this.dangerTarget = null;
        entity.getBrain().forget(MemoryModuleType.WALK_TARGET);
    }
}