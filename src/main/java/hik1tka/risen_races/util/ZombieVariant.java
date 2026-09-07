package hik1tka.risen_races.util;

/**
 * Підвид зомбі, у якого перетворюється людина/дикий зомбі - залежить від
 * біома (і поточної присутності у воді) в момент перетворення/спавну.
 * Див. ZombieVariantHelper.resolveVariant().
 */
public enum ZombieVariant {
    NORMAL,
    HUSK,
    DROWNED
}
