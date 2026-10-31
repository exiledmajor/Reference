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
@Mixin(targets={"dev.tr7zw.firstperson.LogicHandler"}, remap=false)
public abstract class FirstPersonModelMixin {
    @Inject(method={"shouldApplyThirdPerson(Z)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$hardwareBody(boolean thirdPerson, CallbackInfoReturnable<Boolean> cir) {
        if (!thirdPerson && CompatHands.hardwareFirstPerson()) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"showVanillaHands(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$freeHands(ItemStack main, ItemStack off, CallbackInfoReturnable<Boolean> cir) {
        if (CompatHands.hardwareFirstPerson()) {
            cir.setReturnValue((Object)true);
        }
    }
}

