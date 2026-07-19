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
        // Ранок/День - WORK
        // Вечір - MEET (збори біля колокола)
        // Ніч - REST (сон)
        brain.setSchedule(Schedule.VILLAGER_DEFAULT);

        // 2. Реєструємо, ЩО саме вони мають робити під час кожної активності
        registerCoreActivities(brain); // Базові речі: плавання, паніка
        registerIdleActivities(brain); // Коли нічого робити
        registerRestActivities(brain); // Коли час спати (REST)
        // TODO: Пізніше додамо сюди registerWorkActivities і registerMeetActivities!

        // 3. Запускаємо мозок
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.doExclusively(Activity.IDLE);
        brain.refreshActivities(entity.getWorld().getTimeOfDay(), entity.getWorld().getTime());
    }

    public static void updateActivities(AbstractHumanoidEntity entity) {
        // Цей метод викликається кожен тік. Він перевіряє час і перемикає розклад (напр. з IDLE на REST)
        entity.getBrain().refreshActivities(entity.getWorld().getTimeOfDay(), entity.getWorld().getTime());
    }

    // ==========================================
    // ТАСКИ ДЛЯ РІЗНИХ РЕЖИМІВ
    // ==========================================

    private static void registerCoreActivities(Brain<AbstractHumanoidEntity> brain) {
        // Те, що працює ЗАВЖДИ (випливти з води, прокинутись вранці, дивитись навколо)
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(
                new StayAboveWaterTask(0.8F),
                WakeUpTask.create(),
                new LookAroundTask(45, 90),
                new WanderAroundTask()
        ));
    }

    private static void registerIdleActivities(Brain<AbstractHumanoidEntity> brain) {
        // Коли час IDLE (вільний час), вони блукають туди-сюди і роздивляються
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(
                new RandomTask<>(ImmutableList.of(
                        Pair.of(StrollTask.create(0.6F), 2),
                        Pair.of(new WaitTask(30, 60), 1) // Просто стояти
                ))
        ));
    }

    private static void registerRestActivities(Brain<AbstractHumanoidEntity> brain) {
        // Коли ніч (REST), треба шукати ліжко і спати
        brain.setTaskList(Activity.REST, 10, ImmutableList.of(
                // Це ванільні таски для пошуку ліжка
                new SleepTask()
        ));
    }
}