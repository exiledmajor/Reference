/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.TitaniumBones;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={AbstractContainerMenu.class})
public abstract class TitaniumMenuMixin {
    @ModifyReturnValue(method={"getCarried"}, at={@At(value="RETURN")})
    private ItemStack chrome$carried(ItemStack stack) {
        TitaniumBones.bind(stack, TitaniumBones.owner((AbstractContainerMenu)this));
        return stack;
    }
}

