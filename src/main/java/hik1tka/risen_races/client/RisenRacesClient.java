package hik1tka.risen_races.client;

import hik1tka.risen_races.client.humanoid.human.HumanEntityRender;
import hik1tka.risen_races.client.humanoid.human.model.profession.hat.FarmerHatModel;
import hik1tka.risen_races.client.humanoid.human.model.profession.hat.FishermanHatModel;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import hik1tka.risen_races.register.ModModelLayers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RisenRacesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.FARMER_HAT, FarmerHatModel::getTexturedModelData);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.FISHERMAN_HAT, FishermanHatModel::getTexturedModelData);
        EntityRendererRegistry.register(HumanEntity.HUMAN, HumanEntityRender::new);
    }
}