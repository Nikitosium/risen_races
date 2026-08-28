package hik1tka.risen_races.entity.humanoid;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

/**
 * Раса для HumanoidEntity.
 * Тут зберігається все, що відрізняє расу: текстура, звуки, тип поведінки
 * на небезпеку. Базовий інтелект (торгівля, розмноження) спільний і лежить
 * у самому HumanoidEntity — раса лише "перемикає" деталі.
 */
public enum HumanoidRace {

    HUMAN(
            new Identifier("yourmod", "textures/entity/humanoid/human.png"),
            SoundEvents.ENTITY_VILLAGER_AMBIENT,
            SoundEvents.ENTITY_VILLAGER_HURT,
            SoundEvents.ENTITY_VILLAGER_DEATH,
            DangerBehavior.FLEE
    ),
    RIZEN_PIGLIN(
            new Identifier("yourmod", "textures/entity/humanoid/rizen_piglin.png"),
            SoundEvents.ENTITY_PIGLIN_AMBIENT,
            SoundEvents.ENTITY_PIGLIN_HURT,
            SoundEvents.ENTITY_PIGLIN_DEATH,
            DangerBehavior.FIGHT
    ),
    RYNAR(
            new Identifier("yourmod", "textures/entity/humanoid/rynar.png"),
            SoundEvents.ENTITY_VILLAGER_AMBIENT, // заміни на свій кастомний звук пізніше
            SoundEvents.ENTITY_VILLAGER_HURT,
            SoundEvents.ENTITY_VILLAGER_DEATH,
            DangerBehavior.FLEE
    );

    private final Identifier texture;
    private final SoundEvent ambientSound;
    private final SoundEvent hurtSound;
    private final SoundEvent deathSound;
    private final DangerBehavior dangerBehavior;

    HumanoidRace(Identifier texture, SoundEvent ambientSound, SoundEvent hurtSound,
                 SoundEvent deathSound, DangerBehavior dangerBehavior) {
        this.texture = texture;
        this.ambientSound = ambientSound;
        this.hurtSound = hurtSound;
        this.deathSound = deathSound;
        this.dangerBehavior = dangerBehavior;
    }

    public Identifier getTexture() {
        return texture;
    }

    public SoundEvent getAmbientSound() {
        return ambientSound;
    }

    public SoundEvent getHurtSound() {
        return hurtSound;
    }

    public SoundEvent getDeathSound() {
        return deathSound;
    }

    public DangerBehavior getDangerBehavior() {
        return dangerBehavior;
    }

    /**
     * Тип поведінки на появу небезпеки.
     * FLEE  -> Human, Rynar
     * FIGHT -> RizenPiglin (з логікою "1-2 вороги = б'ється сам, 3+ = кличе на допомогу")
     */
    public enum DangerBehavior {
        FLEE,
        FIGHT
    }
}
