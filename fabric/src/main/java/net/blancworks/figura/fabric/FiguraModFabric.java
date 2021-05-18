package net.blancworks.figura.fabric;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.PlayerDataManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public class FiguraModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FiguraMod.init();

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            FiguraMod.renderFirstPersonWorldParts(context.camera(), context.matrixStack(), context.tickDelta());
        });

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("figura", "reloadtextures");
            }

            @Override
            public void apply(ResourceManager manager) {
                PlayerDataManager.reloadAllTextures();
            }
        });
    }
}
