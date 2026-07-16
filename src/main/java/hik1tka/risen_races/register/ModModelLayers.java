package hik1tka.risen_races.register;

import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class ModModelLayers {
    // Реєструємо шар для капелюха фермера
    public static final EntityModelLayer FARMER_HAT =
            new EntityModelLayer(new Identifier("risen_races", "farmer_hat"), "main");

    public static final EntityModelLayer FISHERMAN_HAT =
            new EntityModelLayer(new Identifier("risen_races", "fisherman_hat"), "main");
}