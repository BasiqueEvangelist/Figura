package net.blancworks.figura.mixin;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.gui.ActionWheel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    private ActionWheel actionWheel;

    @Inject(at = @At ("RETURN"), method = "<init>")
    public void init(MinecraftClient client, CallbackInfo ci) {
        actionWheel = new ActionWheel(client);
    }

    @Inject(at = @At ("RETURN"), method = "render")
    public void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (FiguraMod.actionWheel.isPressed()) {
            if (ActionWheel.enabled)
                actionWheel.render(matrices);
        }
        else {
            ActionWheel.enabled = true;
        }
    }

    @Inject(at = @At ("HEAD"), method = "renderCrosshair", cancellable = true)
    private void renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
        if (FiguraMod.actionWheel.isPressed() && ActionWheel.enabled) {
            ci.cancel();
        }
    }
}
