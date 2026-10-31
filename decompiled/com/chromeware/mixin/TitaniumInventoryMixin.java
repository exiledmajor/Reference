/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.TitaniumBones;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Inventory.class})
public abstract class TitaniumInventoryMixin {
    @Shadow
    @Final
    public Player player;

    @ModifyReturnValue(method={"getItem"}, at={@At(value="RETURN")})
    private ItemStack chrome$owned(ItemStack stack) {
        TitaniumBones.bind(stack, this.player);
        return stack;
    }

    @Inject(method={"setItem"}, at={@At(value="HEAD")})
    private void chrome$set(int index, ItemStack stack, CallbackInfo ci) {
        TitaniumBones.bind(stack, this.player);
    }

    @Inject(method={"add(ILnet/minecraft/world/item/ItemStack;)Z"}, at={@At(value="HEAD")})
    private void chrome$pickup(int index, ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        TitaniumBones.refresh(this.player);
        TitaniumBones.bind(stack, this.player);
    }

    @Inject(method={"add(ILnet/minecraft/world/item/ItemStack;)Z"}, at={@At(value="RETURN")})
    private void chrome$pickupLeftover(int index, ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        TitaniumBones.bind(stack, null);
    }

    @ModifyReturnValue(method={"removeItem", "removeItemNoUpdate"}, at={@At(value="RETURN")})
    private ItemStack chrome$extracted(ItemStack stack) {
        TitaniumBones.bind(stack, null);
        return stack;
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void chrome$beforeTick(CallbackInfo ci) {
        TitaniumBones.refresh(this.player);
    }
}

