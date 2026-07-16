package hik1tka.risen_races.util;

import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.random.Random;

public interface IGenderedEntity {
    boolean isFemale();
    void setFemale(boolean female);
    boolean isInLove();
    void setLoveTicks(int ticks);
    boolean canBreedWith(PassiveEntity other);
    String getRaceId();

    static void generateRandomGender(IGenderedEntity entity, Random random) {
        boolean isFemale = random.nextFloat() < 0.51f;
        entity.setFemale(isFemale);
    }

    default boolean canBreedWithGendered(PassiveEntity other) {
        // 1. Перевіряємо, чи є партнер сутністю з підтримкою нашого інтерфейсу
        if (other instanceof IGenderedEntity otherGendered) {

            // 2. Умова однієї раси: якщо ID рас не збігаються — розмноження заборонено
            if (!this.getRaceId().equals(otherGendered.getRaceId())) {
                return false;
            }

            // 3. Умова протилежної статі: true != false поверне true (статі різні)
            // Якщо обоє чоловіки (false != false) або обоє жінки (true != true) — поверне false
            return this.isFemale() != otherGendered.isFemale();
        }

        // Якщо у партнера немає нашої гендерної системи — ніяких дітей
        return false;
    }

}