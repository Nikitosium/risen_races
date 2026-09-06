package hik1tka.risen_races.util;

import hik1tka.risen_races.entity.humanoid.HumanoidEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Обмежує розмноження кількістю "домівок" (ліжок) поблизу - той самий
 * ванільний POI-тип HOME, яким і жителі рахують собі домівку, тому рахувати
 * ліжка можна без нічого нового зі свого боку.
 *
 * Ліміт і населення рахуються ОКРЕМО для кожного конкретного класу раси
 * (HumanEntity/RynarEntity/RisenPiglinEntity) - тобто піглінячі ліжка не
 * блокують розмноження людей і навпаки. Якщо тобі треба щоб усі раси ділили
 * один спільний ліміт на регіон - скажи, поміняю на HumanoidEntity.class.
 *
 * Повідомлення в чат - throttled, максимум раз на (ігровий) день, і не
 * раніше полудня (worldTime % 24000 >= 6000). Через те, що це не рівно "о
 * 12:00", а "вперше після 12:00, коли хтось спробував розмножитись і не
 * зміг" - воно не потребує окремого тикаючого таймера, просто чіпляється до
 * вже наявних спроб розмноження.
 *
 * VillageBounds ще не підключено - getSettlementName() завжди повертає
 * "село". Точний mod id/API цього мода я не знаю, тому не гадаю навмання -
 * підстав реальний виклик, коли буде документація/клас під рукою.
 */
public class VillageCapacityHelper {

    private static final double SEARCH_RADIUS = 64.0D;
    private static final long NOON_TICK = 6000L;

    private static final Map<ServerWorld, Long> lastAnnouncedDay = new WeakHashMap<>();

    public static boolean hasRoomToBreed(HumanoidEntity self) {
        if (!(self.getWorld() instanceof ServerWorld world)) return true;

        Box area = self.getBoundingBox().expand(SEARCH_RADIUS);

        long beds = world.getPointOfInterestStorage().getInSquare(
                entry -> entry.matchesKey(PointOfInterestTypes.HOME),
                self.getBlockPos(),
                (int) SEARCH_RADIUS,
                PointOfInterestStorage.OccupationStatus.ANY
        ).count();

        long population = world.getEntitiesByClass(self.getClass(), area, e -> true).size();

        if (population < beds) return true;

        maybeAnnounce(world, (int) (population - beds + 1));
        return false;
    }

    private static void maybeAnnounce(ServerWorld world, int shortage) {
        long timeOfDay = world.getTimeOfDay() % 24000L;
        if (timeOfDay < NOON_TICK) return;

        long day = world.getTimeOfDay() / 24000L;
        Long alreadyShown = lastAnnouncedDay.get(world);
        if (alreadyShown != null && alreadyShown == day) return;
        lastAnnouncedDay.put(world, day);

        String settlement = getSettlementName();
        Text message = Text.translatable("risen_races.overpopulation", settlement, shortage, settlement);
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(message, false);
        }
    }

    private static String getSettlementName() {
        // TODO: якщо мод VillageBounds встановлено - брати справжню назву типу
        // поселення (село/місто/королівство) з його API. Приклад майбутнього виду:
        // if (FabricLoader.getInstance().isModLoaded("villagebounds")) {
        //     return VillageBoundsApi.getSettlementType(world, pos); // точна назва методу невідома
        // }
        return "село";
    }
}
