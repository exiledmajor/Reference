/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.client.cyberware.FreeHands;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererHandsMixin {
    @WrapOperation(method={"renderItemInHand"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ItemInHandRenderer;renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V")})
    private void chrome$freeHands(ItemInHandRenderer renderer, float tick, PoseStack stack, MultiBufferSource.BufferSource buffers, LocalPlayer player, int light, Operation<Void> original) {
        original.call(new Object[]{renderer, Float.valueOf(tick), stack, buffers, player, light});
        FreeHands.render(renderer, tick, stack, buffers, player, light);
    }
}

