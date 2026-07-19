package hik1tka.risen_races.entity.humanoid.ai;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Brain-таск розмноження для гуманоїдів (Human, Rynar, ...).
 * Аналог вбудованого VillagerEntity-breeding, але без POI/professions —
 * лише пошук закоханого партнера тієї ж раси, підхід і спавн дитини.
 */
public class HumanoidBreedTask extends MultiTickTask<AbstractHumanoidEntity> {

    private static final double BREED_RANGE = 8.0;      // радіус пошуку партнера
    private static final double BREED_DISTANCE_SQ = 2.5 * 2.5; // на якій відстані вже "спарюються"

    private AbstractHumanoidEntity mate;

    public HumanoidBreedTask() {
        super(ImmutableMap.of(
                MemoryModuleType.BREED_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED
        ));
    }

    // 1. ПЕРЕВІРКА: чи закохані і чи є поруч підходящий партнер?
    @Override
    protected boolean shouldRun(ServerWorld world, AbstractHumanoidEntity entity) {
        if (!entity.isInLove()) {
            return false;
        }

        AbstractHumanoidEntity found = findMate(world, entity);
        if (found == null) {
            return false;
        }

        this.mate = found;
        return true;
    }

    // 2. СТАРТ: запам'ятовуємо партнера і йдемо до нього
    @Override
    protected void run(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        entity.getBrain().remember(MemoryModuleType.BREED_TARGET, this.mate);
        entity.getBrain().remember(MemoryModuleType.LOOK_TARGET,
                new net.minecraft.entity.ai.brain.LookTarget(this.mate));
        moveTowardsMate(entity);
    }

    // 3. КОЖЕН ТІК: підходимо ближче, і коли впритул — спарюємось
    @Override
    protected void keepRunning(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        if (this.mate == null) {
            return;
        }

        if (entity.squaredDistanceTo(this.mate) <= BREED_DISTANCE_SQ) {
            entity.getNavigation().stop();
            entity.getLookControl().lookAt(this.mate);
            breed(world, entity, this.mate);
        } else {
            moveTowardsMate(entity);
        }
    }

    // 4. ЧИ ПРОДОВЖУВАТИ: партнер живий, все ще закоханий, і ми самі ще не витратили любов
    @Override
    protected boolean shouldKeepRunning(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        return this.mate != null
                && this.mate.isAlive()
                && this.mate.isInLove()
                && entity.isInLove()
                && entity.squaredDistanceTo(this.mate) <= BREED_RANGE * BREED_RANGE;
    }

    // 5. ФІНІШ: чистимо пам'ять
    @Override
    protected void finishRunning(ServerWorld world, AbstractHumanoidEntity entity, long time) {
        entity.getBrain().forget(MemoryModuleType.BREED_TARGET);
        entity.getBrain().forget(MemoryModuleType.WALK_TARGET);
        this.mate = null;
    }

    // ---- Допоміжні методи ----

    private AbstractHumanoidEntity findMate(ServerWorld world, AbstractHumanoidEntity entity) {
        List<AbstractHumanoidEntity> candidates = world.getEntitiesByClass(
                AbstractHumanoidEntity.class,
                entity.getBoundingBox().expand(BREED_RANGE),
                other -> other != entity && other.isInLove() && entity.canBreedWith(other)
        );

        if (candidates.isEmpty()) {
            return null;
        }

        // Беремо найближчого кандидата
        AbstractHumanoidEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (AbstractHumanoidEntity candidate : candidates) {
            double dist = entity.squaredDistanceTo(candidate);
            if (dist < closestDist) {
                closestDist = dist;
                closest = candidate;
            }
        }
        return closest;
    }

    private void moveTowardsMate(AbstractHumanoidEntity entity) {
        if (this.mate == null) return;
        entity.getBrain().remember(MemoryModuleType.WALK_TARGET,
                new WalkTarget(this.mate.getPos(), 0.5f, 1));
    }

    private void breed(ServerWorld world, AbstractHumanoidEntity mother, AbstractHumanoidEntity father) {
        // Захист від подвійного спавну: якщо котрийсь із батьків раптом
        // вже встиг "розлюбитись" на попередньому кроці цього ж тіку — виходимо.
        if (!mother.isInLove() || !father.isInLove()) {
            return;
        }

        PassiveEntity child = mother.createChild(world, father);
        if (child != null) {
            world.spawnEntity(child);

            // Частинки серденьок, як у ванільному розмноженні
            Vec3d pos = mother.getPos().add(0, mother.getHeight() / 2.0, 0);
            world.spawnParticles(ParticleTypes.HEART, pos.x, pos.y, pos.z, 8, 0.3, 0.3, 0.3, 0.0);

            world.playSound(null, mother.getBlockPos(), net.minecraft.sound.SoundEvents.ENTITY_VILLAGER_CELEBRATE,
                    net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);
        }

        // Скидаємо закоханість ОБОМ одразу і синхронно (важливо: сутності тікають
        // послідовно в один потік, тому другий батько на своєму кроці цього ж тіку
        // вже побачить isInLove() == false і не спробує заспавнити другу дитину)
        mother.setLoveTicks(0);
        father.setLoveTicks(0);
    }
}
