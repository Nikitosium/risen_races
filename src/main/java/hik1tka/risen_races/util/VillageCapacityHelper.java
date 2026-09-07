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
 *
 * ВАЖЛИВО: "вільні місця" тут - НЕ точний claim-based підрахунок (як у
 * AcquireProfessionGoal, де кожен ентіті "застовбовує" собі конкретний
 * jobSite) - це просто "скільки POI типу HOME проти скільки живих ентіті
 * того ж класу поруч", без прив'язки хто саме в якому ліжку. Спрощення
 * свідоме, але саме тому число вільних місць треба перераховувати
 * ЩОРАЗУ, коли воно на щось впливає (canBreedWith і фактичний спавн
 * дітей - різні моменти часу, між ними хтось інший міг зайняти місце).
 */
public class VillageCapacityHelper {

    private static final double SEARCH_RADIUS = 64.0D;
    private static final long NOON_TICK = 6000L;

    private static final Map<ServerWorld, Long> lastAnnouncedDay = new WeakHashMap<>();

    /**
     * Скільки вільних місць лишається ЗАРАЗ (ліжка мінус живі ентіті того ж
     * класу поблизу), не менше 0. MAX_VALUE поза ServerWorld (клієнт) -
     * там розмноження й так ніколи не відбувається, просто щоб виклик
     * нічого штучно не обмежував.
     *
     * Публічний - потрібен і HumanoidEntity#tick() (перевірка черги
     * pendingBabies), не лише breedWith()/hasRoomToBreed() тут же.
     */
    public static int getAvailableRoom(HumanoidEntity self) {
        if (!(self.getWorld() instanceof ServerWorld world)) return Integer.MAX_VALUE;

        Box area = self.getBoundingBox().expand(SEARCH_RADIUS);

        long beds = world.getPointOfInterestStorage().getInSquare(
                entry -> entry.matchesKey(PointOfInterestTypes.HOME),
                self.getBlockPos(),
                (int) SEARCH_RADIUS,
                PointOfInterestStorage.OccupationStatus.ANY
        ).count();

        long population = world.getEntitiesByClass(self.getClass(), area, e -> true).size();

        return (int) Math.max(0L, beds - population);
    }

    public static boolean hasRoomToBreed(HumanoidEntity self) {
        int room = getAvailableRoom(self);
        if (room > 0) return true;

        // room <= 0: -room - це "на скільки вже перебор", +1 - бо навіть
        // рівно "впритул" (room == 0) вже означає "не вистачає місця ще
        // для 1". Той самий сенс, що й раніше (population - beds + 1).
        maybeAnnounce(self.getWorld(), -room + 1);
        return false;
    }

    /**
     * Обрізає кількість дитинчат, "заслужену" їжею (FoodInfo.babies() -
     * напр. яблуко Нотча "обіцяє" 4), до фактично вільних місць. Їжа вже
     * списана на requestedBabies повністю в HumanoidEntity#breedWith() -
     * золоте яблуко коштує стільки ж, навіть якщо реально народиться
     * менше дітей, ніж воно давало б у переповненому селі. Викликати
     * ПРЯМО ПЕРЕД спавном дітей, не раніше: FindMateGoal.canStart() уже
     * перевірив hasRoomToBreed() на старті пошуку пари, але поки пара
     * йшла назустріч одне одному, місце міг зайняти хтось інший.
     */
    public static int capBabyCount(HumanoidEntity self, int requestedBabies) {
        return Math.min(requestedBabies, getAvailableRoom(self));
    }

    /**
     * Миттєве ОДНОРАЗОВЕ сповіщення - викликати одразу після спавну дітей
     * (breedWith), а не чекати щоденної перевірки о 12:00 (maybeAnnounce,
     * через hasRoomToBreed). Якщо саме ЦІ пологи заповнили село "під
     * зав'язку" (room <= 0 після спавну) - гравець дізнається зараз.
     * Позначає день як "уже показано" через той самий lastAnnouncedDay,
     * що й денне нагадування - щоб сьогодні воно не продублювалось ще й
     * опівдні; завтра, якщо село й досі переповнене, нагадування піде
     * знову як завжди.
     */
    public static void announceIfFull(HumanoidEntity self) {
        if (!(self.getWorld() instanceof ServerWorld world)) return;

        int room = getAvailableRoom(self);
        if (room > 0) return;

        announceNow(world, -room + 1);
    }

    /**
     * Миттєве повідомлення з ТОЧНИМ числом дітей, яких довелось поставити
     * в чергу (HumanoidEntity#queuePendingBabies) - на відміну від
     * announceIfFull() (загальне "рівно заповнено, не вистачає ще для 1"),
     * тут число - саме те, скільки дитинчат з ЦЬОГО конкретного обряду не
     * влізло одразу (напр. яблуко Нотча дало 4, влізло 2 -> тут буде 2).
     * Той самий lastAnnouncedDay - не дублюється з денним нагадуванням.
     */
    public static void announceQueuedBirths(HumanoidEntity self, int queuedCount) {
        if (queuedCount <= 0) return;
        if (!(self.getWorld() instanceof ServerWorld world)) return;
        announceNow(world, queuedCount);
    }

    private static void maybeAnnounce(net.minecraft.world.World genericWorld, int shortage) {
        if (!(genericWorld instanceof ServerWorld world)) return;

        long timeOfDay = world.getTimeOfDay() % 24000L;
        if (timeOfDay < NOON_TICK) return;

        announceNow(world, shortage);
    }

    private static void announceNow(ServerWorld world, int shortage) {
        long day = world.getTimeOfDay() / 24000L;
        Long alreadyShown = lastAnnouncedDay.get(world);
        if (alreadyShown != null && alreadyShown == day) return;
        lastAnnouncedDay.put(world, day);

        Text settlement = getSettlementName();
        Text message = Text.translatable("risen_races.overpopulation", settlement, shortage, settlement);
        for (ServerPlayerEntity player : world.getPlayers()) {
            player.sendMessage(message, false);
        }
    }

    private static Text getSettlementName() {
        // TODO: якщо мод VillageBounds встановлено - брати справжню назву типу
        // поселення (село/місто/королівство) з його API, скоріш за все теж як
        // Text (або обгорнути String в Text.literal(...), якщо API повертає
        // сирий рядок). Приклад майбутнього виду:
        // if (FabricLoader.getInstance().isModLoaded("villagebounds")) {
        //     return VillageBoundsApi.getSettlementType(world, pos); // точна назва методу невідома
        // }
        return Text.translatable("risen_races.settlement.default");
    }
}