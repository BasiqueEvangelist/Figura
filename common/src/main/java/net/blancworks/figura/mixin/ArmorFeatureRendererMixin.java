package net.blancworks.figura.mixin;

import net.blancworks.figura.FiguraMod;
import net.blancworks.figura.PlayerData;
import net.blancworks.figura.access.ModelAccess;
import net.blancworks.figura.access.ModelPartAccess;
import net.blancworks.figura.lua.api.model.ArmorModelAPI;
import net.blancworks.figura.lua.api.model.VanillaModelPartCustomization;
import net.blancworks.figura.trust.PlayerTrustManager;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> extends FeatureRenderer<T, M>{
    public ArmorFeatureRendererMixin(FeatureRendererContext<T, M> context) {
        super(context);
    }

    private ArrayList<ModelPart> figura$customizedParts = new ArrayList<>();

    private HashMap<EquipmentSlot, String> partMap = new HashMap<EquipmentSlot, String>();
    
    @Redirect(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAttributes(Lnet/minecraft/client/render/entity/model/BipedEntityModel;)V"))
    private void onRenderArmor(M otherModel, BipedEntityModel<T> actualModel, MatrixStack matrices, VertexConsumerProvider vertexConsumers, T livingEntity, EquipmentSlot equipmentSlot, int i, A bipedEntityModel) {
        if(partMap.size() == 0){
            partMap.put(EquipmentSlot.HEAD, ArmorModelAPI.VANILLA_HELMET);
            partMap.put(EquipmentSlot.CHEST, ArmorModelAPI.VANILLA_CHESTPLATE);
            partMap.put(EquipmentSlot.LEGS, ArmorModelAPI.VANILLA_LEGGINGS);
            partMap.put(EquipmentSlot.FEET, ArmorModelAPI.VANILLA_BOOTS);
        }
        
        String partID = partMap.get(equipmentSlot);
        
        if(partID != null) {
            PlayerData data = FiguraMod.currentData;
            
            if(data != null) {
                if (data.getTrustContainer().getBoolSetting(PlayerTrustManager.ALLOW_VANILLA_MOD_ID)) {
                    for (ModelPart part : ((ModelAccess) actualModel).figura$getModelParts()) {
                        figura$applyPartCustomization(partID, part);
                    }
                }
            }
        }

        otherModel.setAttributes(actualModel);
    }

    @Inject(at = @At("RETURN"), method = "renderArmor")
    private void postRenderArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T livingEntity, EquipmentSlot equipmentSlot, int i, A bipedEntityModel, CallbackInfo ci) {
        figura$clearAllPartCustomizations();
    }

    @Shadow
    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        
    }

    public void figura$applyPartCustomization(String id, ModelPart part){
        PlayerData data = FiguraMod.currentData;

        if(data != null && data.script != null && data.script.allCustomizations != null) {
            VanillaModelPartCustomization customization = data.script.allCustomizations.get(id);

            if(customization != null) {
                ((ModelPartAccess)part).figura$setPartCustomization(customization);
                figura$customizedParts.add(part);
            }
        }
    }

    public void figura$clearAllPartCustomizations(){
        for (ModelPart part : figura$customizedParts) {
            ((ModelPartAccess)part).figura$setPartCustomization(null);
        }
        
        figura$customizedParts.clear();
    }
}
