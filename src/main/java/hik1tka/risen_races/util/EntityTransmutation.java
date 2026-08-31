package hik1tka.risen_races.util;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

/**
 * Спільна логіка "перетворення" однієї сутності на іншу зі збереженням позиції
 * та (опційно) даних. Раніше цей самий блок коду (створити нову сутність,
 * скопіювати позицію, скопіювати NBT, прибрати UUID, програти звук, заспавнити,
 * видалити стару) був продубльований окремо в PurifVilMixin, MutVilMixin,
 * PurifEntitiesMixin (кінь) і MutEntitiesMixin (кінь/хоглін/піглін) - тепер
 * усі вони викликають цей один метод.
 */
public final class EntityTransmutation {

    private EntityTransmutation() {}

    /**
     * @param source          сутність, яку перетворюємо (буде видалена через discard())
     * @param targetType      тип, у який перетворюємо
     * @param world           серверний світ
     * @param sound           звук перетворення
     * @param volume          гучність звуку
     * @param pitch           висота звуку
     * @param fullNbt         true - копіювати ПОВНИЙ NBT через writeNbt/readNbt
     *                        (потрібно для жителів: торгівля, репутація, професія);
     *                        false - лише writeCustomDataToNbt/readCustomDataFromNbt
     *                        (легший варіант для тварин/мобів - здоров'я, вік, спорядження)
     * @param restoreFullHealth true - після перетворення виставити повне здоров'я
     *                        (потрібно для мутацій "у гіршу сторону", щоб моб не
     *                        з'явився напівмертвим; для очищення зазвичай не треба -
     *                        зберігаємо те здоров'я, що було)
     * @return нову сутність, або null якщо targetType.create() не вдався
     */
    public static <T extends LivingEntity> T swap(LivingEntity source, EntityType<T> targetType, ServerWorld world,
                                                    SoundEvent sound, float volume, float pitch,
                                                    boolean fullNbt, boolean restoreFullHealth) {
        T target = targetType.create(world);
        if (target == null) return null;

        target.refreshPositionAndAngles(source.getX(), source.getY(), source.getZ(), source.getYaw(), source.getPitch());

        NbtCompound nbt = new NbtCompound();
        if (fullNbt) {
            source.writeNbt(nbt);
            nbt.remove("UUID");
            target.readNbt(nbt);
        } else {
            source.writeCustomDataToNbt(nbt);
            nbt.remove("UUID");
            target.readCustomDataFromNbt(nbt);
        }

        if (restoreFullHealth) {
            target.setHealth(target.getMaxHealth());
        }

        world.playSound(null, source.getX(), source.getY(), source.getZ(), sound, SoundCategory.NEUTRAL, volume, pitch);
        world.spawnEntity(target);
        source.discard();
        return target;
    }
}
