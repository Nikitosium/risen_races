package hik1tka.risen_races.entity.humanoid.goal;

import net.minecraft.entity.ai.goal.Goal;

import java.util.function.BooleanSupplier;

/**
 * Обгортка, що вмикає/вимикає інший Goal (ванільний або кастомний) за
 * умовою, яка перевіряється щотика - а не один раз при побудові
 * goalSelector.
 *
 * Навіщо: HumanoidEntity.initGoals() викликається з конструктора,
 * ДО того як HumanoidRace фактично встановлюється (це відбувається
 * пізніше - в initialize() при спавні або readCustomDataFromNbt()
 * при завантаженні). Якщо вирішувати flee-vs-fight один раз через
 * switch під час initGoals(), ентіті назавжди застрягає з поведінкою
 * дефолтної раси (HUMAN), яка була виставлена в initDataTracker().
 *
 * ConditionalGoal вирішує це: обидва варіанти (флі та файт) додаються
 * в goalSelector завжди, а яка саме активна - вирішується щотика,
 * на основі актуальної rase() на момент перевірки.
 */
public class ConditionalGoal extends Goal {

    private final Goal delegate;
    private final BooleanSupplier condition;

    public ConditionalGoal(Goal delegate, BooleanSupplier condition) {
        this.delegate = delegate;
        this.condition = condition;
        this.setControls(delegate.getControls());
    }

    @Override
    public boolean canStart() {
        return condition.getAsBoolean() && delegate.canStart();
    }

    @Override
    public boolean shouldContinue() {
        // якщо умова перестала виконуватись (раса/поведінка змінилась,
        // теоретично) - одразу гасимо делегата, а не чекаємо його власний
        // shouldContinue().
        return condition.getAsBoolean() && delegate.shouldContinue();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return delegate.shouldRunEveryTick();
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public void tick() {
        delegate.tick();
    }
}
