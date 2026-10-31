/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.humanity.SyringeLiquidRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemRenderer.class})
public abstract class SyringeLiquidItemRendererMixin {
    @Unique
    private static final ThreadLocal<LivingEntity> chrome$syringeHolder = new ThreadLocal();

    @Inject(method={"renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V"}, at={@At(value="HEAD")})
    private void chrome$captureSyringeHolder(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poses, MultiBufferSource buffers, Level level, int light, int overlay, int seed, CallbackInfo ci) {
        if (entity == null) {
            chrome$syringeHolder.remove();
        } else {
            chrome$syringeHolder.set(entity);
        }
    }

    @Inject(method={"renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V"}, at={@At(value="RETURN")})
    private void chrome$releaseSyringeHolder(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poses, MultiBufferSource buffers, Level level, int light, int overlay, int seed, CallbackInfo ci) {
        chrome$syringeHolder.remove();
    }

    @Inject(method={"render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")})
    private void chrome$renderSyringeLiquid(ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poses, MultiBufferSource buffers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        SyringeLiquidRenderer.render(stack, chrome$syringeHolder.get(), poses, buffers, light, overlay);
    }
}

