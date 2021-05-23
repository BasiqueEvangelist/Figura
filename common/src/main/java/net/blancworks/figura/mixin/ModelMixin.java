package net.blancworks.figura.mixin;

import net.blancworks.figura.access.ModelAccess;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Model.class)
public class ModelMixin implements ModelAccess {
    @Unique
    private final List<ModelPart> addedParts = new ArrayList<>();

    @Inject(method = "accept", at = @At("HEAD"))
    private void addModelPart(ModelPart part, CallbackInfo ci) {
        addedParts.add(part);
    }

    @Override
    public List<ModelPart> figura$getModelParts() {
        return addedParts;
    }
}
