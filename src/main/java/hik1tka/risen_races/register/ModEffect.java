package hik1tka.risen_races.register;

import hik1tka.risen_races.RisenRaces;
import hik1tka.risen_races.effect.PurificationEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import hik1tka.risen_races.effect.ZombificationEffect;

public class ModEffect {
    public static void registerModEffect(){
        RisenRaces.LOGGER.info("Register Mod Effects for "+ RisenRaces.MOD_ID);
    }
    public static final StatusEffect PURIFICATION = new PurificationEffect(StatusEffectCategory.BENEFICIAL, 0xFFFFFF);
    public static final StatusEffect ZOMBIFICATION = new ZombificationEffect(StatusEffectCategory.HARMFUL, 0x4B5320);

    // 2. Метод для реєстрації ефектів у грі
    public static void registerEffects() {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(RisenRaces.MOD_ID, "purification"), PURIFICATION);
        Registry.register(Registries.STATUS_EFFECT, new Identifier(RisenRaces.MOD_ID, "zombification"), ZOMBIFICATION);
    }
}
