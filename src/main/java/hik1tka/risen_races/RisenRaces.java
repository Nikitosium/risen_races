package hik1tka.risen_races;

import hik1tka.risen_races.register.ModEffect;
import hik1tka.risen_races.register.ModEntity;
import hik1tka.risen_races.register.ModItem;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RisenRaces implements ModInitializer {
	public static final String MOD_ID = "risen_races";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    //Initialise registry
	@Override
	public void onInitialize() {
        LOGGER.info("Mod has been initialised");

        ModEffect.registerModEffect();
        ModItem.registerModItem();
        ModEntity.registryModEntity();
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}
