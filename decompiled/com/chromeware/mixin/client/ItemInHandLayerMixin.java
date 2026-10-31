/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.ArmedModel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.layers.ItemInHandLayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.body.OneHanded;
import com.chromeware.client.extendo.ExtendoClient;
import com.chromeware.cyberware.HardwareHands;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ItemInHandLayer.class})
public abstract class ItemInHandLayerMixin {
    @WrapOperation(method={"render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/layers/ItemInHandLayer;renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")})
    private void chrome$hideFloatingItems(ItemInHandLayer<?, ?> layer, LivingEntity entity, ItemStack item, ItemDisplayContext display, HumanoidArm arm, PoseStack stack, MultiBufferSource buffer, int light, Operation<Void> original) {
        if (!(entity instanceof Player)) {
            original.call(new Object[]{layer, entity, item, display, arm, stack, buffer, light});
            return;
        }
        Player player = (Player)entity;
        InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        HumanoidArm effective = OneHanded.resolve(player, hand);
        if (HardwareHands.locked(player, hand)) {
            return;
        }
        if (effective != null) {
            original.call(new Object[]{layer, entity, item, effective == HumanoidArm.LEFT ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, effective, stack, buffer, light});
        }
    }

    @WrapOperation(method={"renderArmWithItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V")})
    private void chrome$extendedHand(ArmedModel model, HumanoidArm arm, PoseStack stack, Operation<Void> original, @Local(argsOnly=true) LivingEntity entity) {
        original.call(new Object[]{model, arm, stack});
        float offset = ExtendoClient.handOffset(entity, arm);
        if (offset != 0.0f) {
            stack.translate(0.0f, offset, 0.0f);
        }
    }
}

