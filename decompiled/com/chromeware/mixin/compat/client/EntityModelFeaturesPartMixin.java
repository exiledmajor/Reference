/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelPart
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.client.body.LimbRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"traben.entity_model_features.models.parts.EMFModelPart"}, remap=false)
public abstract class EntityModelFeaturesPartMixin {
    @Inject(method={"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$hiddenLimb(PoseStack stack, VertexConsumer buffer, int light, int overlay, int color, CallbackInfo ci) {
        LimbRenderContext.Rule rule = LimbRenderContext.ruleFor((ModelPart)this);
        if (rule != null && rule.hidden()) {
            ci.cancel();
        }
    }
}

