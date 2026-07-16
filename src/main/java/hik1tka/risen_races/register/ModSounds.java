package hik1tka.risen_races.register;

import hik1tka.risen_races.RisenRaces;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static SoundEvent ENTITY_DEATH = registerSoundEvent("death");
    public static SoundEvent MALE_HURT = registerSoundEvent("male_hurt");
    public static SoundEvent MALE_AMBIENT = registerSoundEvent("male_ambient");
    public static SoundEvent FEMALE_HURT = registerSoundEvent("female_hurt");
    public static SoundEvent FEMALE_AMBIENT = registerSoundEvent("female_ambient");

    public static SoundEvent MALE_NO = registerSoundEvent("male_no");
    public static SoundEvent FEMALE_NO = registerSoundEvent("female_no");
    public static SoundEvent MALE_NO_GOD = registerSoundEvent("male_no_god");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(RisenRaces.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        RisenRaces.LOGGER.info("Registering Mod Sounds for " + RisenRaces.MOD_ID);
    }
}