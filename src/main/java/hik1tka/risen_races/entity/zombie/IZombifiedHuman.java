package hik1tka.risen_races.entity.zombie;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

/**
 * Спільний інтерфейс усіх "зомбіфікованих людей" (звичайний/кадавр/утопець).
 *
 * Навіщо: утопець тепер успадковує ВАНІЛЬНОГО DrownedEntity (щоб отримати
 * його поведінку), а звичайний зомбі і кадавр - нашого ZombifiedHumanEntity.
 * Спільного предка з нашими полями (стать/професія/пам'ять) у цих двох дерев
 * класів нема, тому спільний "контракт" описує інтерфейс - HumanEntity#tryZombify()
 * і ModZombieReplacement працюють з результатом create() як із ZombieEntity,
 * а кастують саме до цього інтерфейсу, щоб поставити стать/пам'ять незалежно
 * від того, яке це дерево класів.
 */
public interface IZombifiedHuman {
    boolean isFemale();
    void setFemale(boolean female);
    String getProfession();
    void setProfession(String profession);
    @Nullable NbtCompound getNpcMemory();
    void setNpcMemory(@Nullable NbtCompound memory);

    /**
     * Згенерувати випадкові стать/професію для "дикого" спавну
     * (природний спавнер або заміна ванільного зомбі). НЕ для конвертації
     * з живої людини - там ці значення ставлять напряму з даних людини.
     */
    void rollRandomSpawnData();
}
