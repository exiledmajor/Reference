/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.body.OneHanded;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={HumanoidModel.class})
public abstract class HumanoidModelBodyMixin {
    @WrapOperation(method={"setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getMainArm()Lnet/minecraft/world/entity/HumanoidArm;")})
    private HumanoidArm chrome$useArm(LivingEntity entity, Operation<HumanoidArm> original) {
        Player player;
        return entity instanceof Player ? OneHanded.effectiveMainArm(player, (player = (Player)entity).isUsingItem() ? player.getUsedItemHand() : InteractionHand.MAIN_HAND) : (HumanoidArm)original.call(new Object[]{entity});
    }

    @WrapOperation(method={"getAttackArm"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getMainArm()Lnet/minecraft/world/entity/HumanoidArm;")})
    private HumanoidArm chrome$attackArm(LivingEntity entity, Operation<HumanoidArm> original) {
        HumanoidArm humanoidArm;
        if (entity instanceof Player) {
            Player player = (Player)entity;
            humanoidArm = OneHanded.effectiveMainArm(player, player.swingingArm == null ? InteractionHand.MAIN_HAND : player.swingingArm);
        } else {
            humanoidArm = (HumanoidArm)original.call(new Object[]{entity});
        }
        return humanoidArm;
    }
}

