package net.blancworks.figura.fabric.mixin;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.gui.EmoteWheel;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void disableCrosshairIfNeeded(MatrixStack matrices, CallbackInfo ci) {
        if (FiguraMod.emoteWheel.isPressed() && EmoteWheel.enabled)
            ci.cancel();
    }
}
