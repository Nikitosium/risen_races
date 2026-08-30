package hik1tka.risen_races.client;

import hik1tka.risen_races.client.humanoid.human.HumanEntityRender;
import hik1tka.risen_races.client.humanoid.human.model.profession.hat.FarmerHatModel;
import hik1tka.risen_races.client.humanoid.human.model.profession.hat.FishermanHatModel;
import hik1tka.risen_races.client.humanoid.piglin.RisenPiglinEntityRender;
import hik1tka.risen_races.client.humanoid.piglin.model.RisenFemalePiglinModel;
import hik1tka.risen_races.client.humanoid.rynar.RynarEntityRender;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import hik1tka.risen_races.entity.humanoid.risen_piglin.RisenPiglinEntity;
import hik1tka.risen_races.entity.humanoid.rynar.RynarEntity;
import hik1tka.risen_races.register.ModModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RisenRacesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Шари капелюхів (людина)
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.FARMER_HAT, FarmerHatModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.FISHERMAN_HAT, FishermanHatModel::getTexturedModelData);
        // Власна модель жіночого пігліна - обов'язково, RisenPiglinEntityRender
        // їх запитує в конструкторі, інакше саме тут і буде "entityRenderer is null"
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.RISEN_PIGLIN_FEMALE, RisenFemalePiglinModel::getTexturedModelData);

        EntityRendererRegistry.register(HumanEntity.HUMAN, HumanEntityRender::new);
        EntityRendererRegistry.register(RisenPiglinEntity.RISEN_PIGLIN, RisenPiglinEntityRender::new);
        EntityRendererRegistry.register(RynarEntity.RYNAR, RynarEntityRender::new);
    }
}