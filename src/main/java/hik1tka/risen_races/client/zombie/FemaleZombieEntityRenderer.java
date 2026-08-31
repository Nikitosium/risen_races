package hik1tka.risen_races.client.zombie;

import hik1tka.risen_races.entity.zombie.FemaleZombieEntity;
import hik1tka.risen_races.entity.zombie.MaleZombieEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;

/**
 * Ванільний ZombieEntityRenderer<T extends ZombieEntity> вже generic -
 * FemaleZombieEntity/MaleZombieEntity йому підходять напряму, свого коду не
 * треба (текстура/модель/анімація - усе стандартне зомбі).
 */
public class FemaleZombieEntityRenderer extends ZombieEntityRenderer<FemaleZombieEntity> {
    public FemaleZombieEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }
}
