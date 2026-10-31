/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.food.FoodData
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.ZetatechBerserk;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={FoodData.class})
public abstract class FoodMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"tick"})
    private void chrome$berserkHunger(Player player, Operation<Void> original) {
        if (!ZetatechBerserk.active(player)) {
            original.call(new Object[]{player});
            return;
        }
        FoodData food = (FoodData)this;
        float pending = food.getExhaustionLevel();
        food.setExhaustion(0.0f);
        try {
            original.call(new Object[]{player});
        }
        finally {
            food.setExhaustion(pending);
        }
    }

    @ModifyArg(method={"tick"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;heal(F)V"), index=0)
    private float chrome$naturalRegeneration(float amount, @Local(argsOnly=true) Player player) {
        return ImplantEffects.naturalRegeneration(player, amount);
    }
}

