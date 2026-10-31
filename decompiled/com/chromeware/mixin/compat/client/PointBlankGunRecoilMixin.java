/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.vicmatskiv.pointblank.client.GunClientState
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.compat.guns.GunCyberware;
import com.vicmatskiv.pointblank.client.GunClientState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.vicmatskiv.pointblank.client.controller.GunRecoilAnimationController"}, remap=false)
public abstract class PointBlankGunRecoilMixin {
    @Shadow
    private double initialAmplitudeAdj;

    @Inject(method={"onStartFiring"}, at={@At(value="TAIL")})
    private void chrome$recoil(LivingEntity shooter, GunClientState state, ItemStack gun, CallbackInfo ci) {
        if (shooter instanceof Player) {
            Player p = (Player)shooter;
            this.initialAmplitudeAdj *= GunCyberware.recoil(p);
        }
    }
}

