/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.client.compat.CompatHands;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"net.bettercombat.logic.WeaponRegistry"}, remap=false)
public abstract class BetterCombatWeaponMixin {
    @Inject(method={"getAttributes(Lnet/minecraft/world/item/ItemStack;)Lnet/bettercombat/api/WeaponAttributes;"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$lockedHand(ItemStack stack, CallbackInfoReturnable<Object> cir) {
        if (CompatHands.lockedStack(stack)) {
            cir.setReturnValue(null);
        }
    }
}

