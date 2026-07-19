package hik1tka.risen_races.entity.humanoid.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.Schedule;
import net.minecraft.entity.ai.brain.task.*;

public class HumanoidBrain {

    public static void init(Brain<AbstractHumanoidEntity> brain, AbstractHumanoidEntity entity) {
        // 1. Встановлюємо РОЗКЛАД як у жителя (Vanilla Villager Schedule)
        brain.setSchedule(Schedule.VILLAGER_DEFAULT);

        // 2. Реєструємо активності
        registerCoreActivities(brain); // Базові речі: плавання, ПАНІКА/ВТЕЧА
        registerIdleActivities(brain); // Коли нічого робити (РОЗМНОЖЕННЯ, блукання)
        registerRestActivities(brain); // Коли час спати (REST)

        // Підключаємо роботу і збори
        registerWorkActivities(brain);
        registerMeetActivities(brain);

        // 3. Запускаємо мозок
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(entity.getWorld().getTimeOfDay(), entity.getWorld().getTime());
    }

    public static void updateActivities(AbstractHumanoidEntity entity) {
        entity.getBrain().refreshActivities(entity.getWorld().getTimeOfDay(), entity.getWorld().getTime());
    }

    // ==========================================
    // ТАСКИ ДЛЯ РІЗНИХ РЕЖИМІВ
    // ==========================================

    private static void registerCoreActivities(Brain<AbstractHumanoidEntity> brain) {
        // Додаємо твій HumanoidFleeTask у CORE.
        // CORE працює ЗАВЖДИ (навіть якщо моб спить чи працює),
        // тому якщо він побачить ворога, він негайно почне тікати.
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(
                new StayAboveWaterTask(0.8F),
                new HumanoidFleeTask(12.0f, 1.25), // <--- Додано втечу від ворогів
                WakeUpTask.create(),
                new LookAroundTask(45, 90),
                new WanderAroundTask()
        ));
    }

    private static void registerIdleActivities(Brain<AbstractHumanoidEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(
                new HumanoidBreedTask(), // <--- Додано розмноження, коли моб вільний
                new RandomTask<>(ImmutableList.of(
                        Pair.of(StrollTask.create(0.6F), 2),
                        Pair.of(new WaitTask(30, 60), 1)
                ))
        ));
    }

    private static void registerRestActivities(Brain<AbstractHumanoidEntity> brain) {
        brain.setTaskList(Activity.REST, 10, ImmutableList.of(
                new SleepTask()
        ));
    }

    private static void registerWorkActivities(Brain<AbstractHumanoidEntity> brain) {
        // Коли настане час працювати за розкладом (Day)
        brain.setTaskList(Activity.WORK, 10, ImmutableList.of(
                // TODO: В майбутньому тут буде твій кастомний HumanoidWorkTask
                // Поки що нехай просто блукають
                new WanderAroundTask()
        ));
    }

    private static void registerMeetActivities(Brain<AbstractHumanoidEntity> brain) {
        // Коли настане час іти до колокола (Late afternoon)
        brain.setTaskList(Activity.MEET, 10, ImmutableList.of(
                // TODO: В майбутньому тут буде таск для пошуку колокола та соціалізації
                // Поки що нехай просто дивляться один на одного та гуляють
                new RandomTask<>(ImmutableList.of(
                        Pair.of(StrollTask.create(0.6F), 2),
                        Pair.of(new LookAroundTask(45, 90), 2)
                ))
        ));
    }
}