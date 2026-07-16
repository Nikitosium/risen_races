package hik1tka.risen_races.client;

import hik1tka.risen_races.client.humanoid.human.HumanEntityRender;
import hik1tka.risen_races.entity.humanoid.human.HumanEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class RisenRacesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(HumanEntity.HUMAN, HumanEntityRender::new);
    }
}