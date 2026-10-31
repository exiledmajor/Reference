/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.TitaniumBones;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Slot.class})
public abstract class TitaniumSlotMixin {
    @Shadow
    @Final
    public Container container;

    /*
     * Enabled aggressive block sorting
     */
    @ModifyReturnValue(method={"getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I"}, at={@At(value="RETURN")})
    private int chrome$limit(int original, ItemStack stack) {
        int n;
        Container container = this.container;
        if (container instanceof Inventory) {
            Inventory inv = (Inventory)container;
            if (TitaniumBones.active(inv.player)) {
                n = Math.min(((Slot)this).getMaxStackSize(), TitaniumBones.limit(inv.player, stack));
                return n;
            }
        }
        n = Math.min(original, TitaniumBones.base(stack));
        return n;
    }

    @ModifyReturnValue(method={"getItem"}, at={@At(value="RETURN")})
    private ItemStack chrome$bind(ItemStack stack) {
        Player player;
        Container container = this.container;
        if (container instanceof Inventory) {
            Inventory inv = (Inventory)container;
            player = inv.player;
        } else {
            player = null;
        }
        TitaniumBones.bind(stack, player);
        return stack;
    }

    @Inject(method={"set"}, at={@At(value="HEAD")})
    private void chrome$set(ItemStack stack, CallbackInfo ci) {
        Player player;
        Container container = this.container;
        if (container instanceof Inventory) {
            Inventory inv = (Inventory)container;
            player = inv.player;
        } else {
            player = null;
        }
        TitaniumBones.bind(stack, player);
    }
}

