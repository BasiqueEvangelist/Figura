package net.blancworks.figura.forge.mixin;

import net.blancworks.figura.PlayerDataManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "reloadResources", at = @At("TAIL"))
    public void resourcesReloadedHook(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.getReturnValue().handle((t, u) -> {
            PlayerDataManager.reloadAllTextures();
            return null;
        });
    }
}
