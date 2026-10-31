/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.Model
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.body.LimbRenderContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={HumanoidArmorLayer.class})
public abstract class HumanoidArmorLayerMixin {
    @WrapMethod(method={"renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V"})
    private void chrome$armorLimbs(PoseStack stack, MultiBufferSource buffer, LivingEntity entity, EquipmentSlot slot, int light, HumanoidModel<?> model, float swing, float amount, float tick, float age, float yaw, float pitch, Operation<Void> original) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            try (LimbRenderContext scope = LimbRenderContext.open(player, model, true);){
                original.call(new Object[]{stack, buffer, entity, slot, light, model, Float.valueOf(swing), Float.valueOf(amount), Float.valueOf(tick), Float.valueOf(age), Float.valueOf(yaw), Float.valueOf(pitch)});
            }
        } else {
            original.call(new Object[]{stack, buffer, entity, slot, light, model, Float.valueOf(swing), Float.valueOf(amount), Float.valueOf(tick), Float.valueOf(age), Float.valueOf(yaw), Float.valueOf(pitch)});
        }
    }

    @Inject(method={"getArmorModelHook"}, at={@At(value="RETURN")})
    private void chrome$customHumanoidArmor(LivingEntity entity, ItemStack item, EquipmentSlot slot, HumanoidModel<?> model, CallbackInfoReturnable<Model> cir) {
        Object object;
        if (entity instanceof Player && (object = cir.getReturnValue()) instanceof HumanoidModel) {
            HumanoidModel humanoid = (HumanoidModel)object;
            LimbRenderContext.addArmorModel(humanoid);
        }
    }
}

