package net.blancworks.figura.forge.mixin;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.gui.ActionWheel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeIngameGui.class)
public abstract class ForgeInGameGuiMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void disableCrosshairIfNeeded(MatrixStack matrices, CallbackInfo ci) {
        if (FiguraMod.actionWheel.isPressed() && ActionWheel.enabled)
            ci.cancel();
    }
}
