package hik1tka.risen_races.entity.humanoid.data;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestType;

import java.util.function.Predicate;

/**
 * Опис однієї професії: її id (те, що повертає getProfession(), використовується
 * рендерами для вибору текстури/шапки) і POI-тип робочого місця, яке її дає.
 *
 * Можна вказувати як вже існуючий ванільний POI-тип (наприклад
 * PointOfInterestTypes.MASON для каменеріза - це RegistryKey), так і власний
 * зареєстрований - ProfessionDefinition сама по собі ні на що не зав'язана.
 */
public record ProfessionDefinition(
        String id,
        Predicate<RegistryEntry<PointOfInterestType>> workstationPredicate,
        int searchRadius
) {
    public ProfessionDefinition(String id, RegistryKey<PointOfInterestType> poiType, int searchRadius) {
        this(id, entry -> entry.matchesKey(poiType), searchRadius);
    }
}
