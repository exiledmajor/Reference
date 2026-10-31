/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.BundleContents$Mutable
 *  org.spongepowered.asm.mixin.Mixin
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.TitaniumBones;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BundleContents.Mutable.class})
public abstract class TitaniumBundleMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"tryInsert"})
    private int chrome$normalWeight(ItemStack stack, Operation<Integer> original) {
        Player owner = TitaniumBones.owner(stack);
        TitaniumBones.bind(stack, null);
        try {
            int n = (Integer)original.call(new Object[]{stack});
            return n;
        }
        finally {
            TitaniumBones.bind(stack, owner);
        }
    }
}

