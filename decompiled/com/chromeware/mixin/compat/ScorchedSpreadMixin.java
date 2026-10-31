/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.compat;

import com.chromeware.compat.guns.GunCyberware;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"top.ribs.scguns.util.GunModifierHelper"}, remap=false)
public abstract class ScorchedSpreadMixin {
    @Inject(method={"getModifiedSpread(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;F)F"}, at={@At(value="RETURN")}, cancellable=true)
    private static void chrome$spread(Player player, ItemStack weapon, float spread, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((Object)Float.valueOf(cir.getReturnValueF() * (float)GunCyberware.spread(player)));
    }
}

