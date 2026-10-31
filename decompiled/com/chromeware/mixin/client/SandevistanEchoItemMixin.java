/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.AfterimageRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ItemRenderer.class})
public abstract class SandevistanEchoItemMixin {
    @Inject(method={"getFoilBuffer", "getFoilBufferDirect"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$item(MultiBufferSource buffers, RenderType layer, boolean item, boolean foil, CallbackInfoReturnable<VertexConsumer> ci) {
        if (AfterimageRenderContext.isRendering()) {
            ci.setReturnValue((Object)buffers.getBuffer(layer));
        }
    }

    @Inject(method={"getArmorFoilBuffer"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$armor(MultiBufferSource buffers, RenderType layer, boolean foil, CallbackInfoReturnable<VertexConsumer> ci) {
        if (AfterimageRenderContext.isRendering()) {
            ci.setReturnValue((Object)buffers.getBuffer(layer));
        }
    }

    @Inject(method={"getCompassFoilBuffer"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$compass(MultiBufferSource buffers, RenderType layer, PoseStack.Pose pose, CallbackInfoReturnable<VertexConsumer> ci) {
        if (AfterimageRenderContext.isRendering()) {
            ci.setReturnValue((Object)buffers.getBuffer(layer));
        }
    }
}

