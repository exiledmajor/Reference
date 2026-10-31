/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.ZetatechBerserk;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Player.class})
public abstract class BerserkPlayerMixin {
    @WrapOperation(method={"actuallyHurt"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;setHealth(F)V")})
    private void chrome$healthFloor(Player entity, float health, Operation<Void> original, @Local(argsOnly=true) DamageSource source) {
        original.call(new Object[]{entity, Float.valueOf(ZetatechBerserk.protectedHealth(entity, source, health))});
    }
}

