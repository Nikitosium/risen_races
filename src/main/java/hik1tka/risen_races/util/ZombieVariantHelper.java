package hik1tka.risen_races.util;

import hik1tka.risen_races.entity.zombie.ZombifiedHumanDrownedEntity;
import hik1tka.risen_races.entity.zombie.ZombifiedHumanEntity;
import hik1tka.risen_races.entity.zombie.ZombifiedHumanHuskEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Визначає, у який підвид зомбі перетворюється людина (HumanEntity#tryZombify)
 * або дикий ванільний зомбі (ModZombieReplacement) - за біомом на позиції
 * ентіті, і водночас створює відповідний ентіті одним викликом (create()).
 *
 * Пріоритет перевірки: спершу "фізично у воді ЗАРАЗ" (найширша умова -
 * покриває "інші водойми", яких немає в явному списку біомів нижче,
 * наприклад ставок посеред рівнини), потім конкретні водні біоми (утопець),
 * і лише потім сухі спекотні біоми (кадавр). Якщо жоден варіант не підійшов -
 * звичайний ZombifiedHumanEntity.
 */
public final class ZombieVariantHelper {

    private ZombieVariantHelper() {
    }

    private static final Set<RegistryKey<Biome>> HUSK_BIOMES = Set.of(
            BiomeKeys.DESERT,
            BiomeKeys.SAVANNA, BiomeKeys.SAVANNA_PLATEAU, BiomeKeys.WINDSWEPT_SAVANNA,
            BiomeKeys.JUNGLE, BiomeKeys.SPARSE_JUNGLE, BiomeKeys.BAMBOO_JUNGLE
    );

    private static final Set<RegistryKey<Biome>> DROWNED_BIOMES = Set.of(
            BiomeKeys.OCEAN, BiomeKeys.DEEP_OCEAN,
            BiomeKeys.COLD_OCEAN, BiomeKeys.DEEP_COLD_OCEAN,
            BiomeKeys.LUKEWARM_OCEAN, BiomeKeys.DEEP_LUKEWARM_OCEAN,
            BiomeKeys.WARM_OCEAN,
            BiomeKeys.FROZEN_OCEAN, BiomeKeys.DEEP_FROZEN_OCEAN,
            BiomeKeys.RIVER, BiomeKeys.FROZEN_RIVER,
            BiomeKeys.SWAMP, BiomeKeys.MANGROVE_SWAMP,
            BiomeKeys.BEACH, BiomeKeys.SNOWY_BEACH, BiomeKeys.STONY_SHORE
    );

    public static ZombieVariant resolveVariant(ServerWorld world, Entity entity) {
        // Найширша умова - фізично у воді просто зараз, незалежно від
        // біома (ставок/канал/будь-яка інша водойма, не перелічена нижче).
        if (entity.isTouchingWater()) {
            return ZombieVariant.DROWNED;
        }

        BlockPos pos = entity.getBlockPos();
        RegistryEntry<Biome> biome = world.getBiome(pos);

        for (RegistryKey<Biome> key : DROWNED_BIOMES) {
            if (biome.matchesKey(key)) {
                return ZombieVariant.DROWNED;
            }
        }
        for (RegistryKey<Biome> key : HUSK_BIOMES) {
            if (biome.matchesKey(key)) {
                return ZombieVariant.HUSK;
            }
        }
        return ZombieVariant.NORMAL;
    }

    /**
     * Створює конкретний ентіті-зомбі під переданий варіант. Повертає null,
     * якщо create() з якоїсь причини не вдався (ваніль так само може
     * повернути null - наприклад, якщо реєстр ще не готовий).
     */
    @Nullable
    public static ZombieEntity create(ServerWorld world, ZombieVariant variant) {
        return switch (variant) {
            case HUSK -> ZombifiedHumanHuskEntity.ZOMBIFIED_HUMAN_HUSK.create(world);
            case DROWNED -> ZombifiedHumanDrownedEntity.ZOMBIFIED_HUMAN_DROWNED.create(world);
            case NORMAL -> ZombifiedHumanEntity.ZOMBIFIED_HUMAN.create(world);
        };
    }
}
