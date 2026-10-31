/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  top.ribs.scguns.common.FireMode
 *  top.ribs.scguns.common.Gun
 *  top.ribs.scguns.network.message.C2SMessageShoot
 */
package com.chromeware.mixin.compat;

import com.chromeware.compat.guns.GunCyberware;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.network.message.C2SMessageShoot;

@Pseudo
@Mixin(targets={"top.ribs.scguns.common.network.ServerPlayHandler"}, remap=false)
public abstract class ScorchedGunMixin {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"handleShoot"})
    private static void chrome$shoot(C2SMessageShoot message, ServerPlayer player, Operation<Void> original) {
        GunCyberware.Shot previous = GunCyberware.current();
        GunCyberware.current(null);
        try {
            original.call(new Object[]{message, player});
        }
        finally {
            GunCyberware.current(previous);
        }
    }

    @Inject(method={"handleShoot"}, at={@At(value="INVOKE", target="Ltop/ribs/scguns/common/ShootTracker;putCooldown(Lnet/minecraft/world/item/ItemStack;Ltop/ribs/scguns/item/GunItem;Ltop/ribs/scguns/common/Gun;)V", shift=At.Shift.AFTER)})
    private static void chrome$round(CallbackInfo ci, @Local(argsOnly=true) ServerPlayer player, @Local ItemStack weapon, @Local Gun gun) {
        boolean charged = gun.getGeneral().getFireMode() == FireMode.PULSE && gun.getGeneral().getFireTimer() > 0 && player.getPersistentData().getFloat("ChargeProgress") >= 1.0f;
        GunCyberware.current(GunCyberware.fired((Player)player, weapon, charged));
    }

    @Inject(method={"handleBeamEffects"}, at={@At(value="TAIL")})
    private static void chrome$beam(ServerPlayer player, HitResult hit, Gun gun, double damage, CallbackInfo ci) {
        if (hit.getType() != HitResult.Type.MISS) {
            GunCyberware.impactCurrent(hit.getLocation());
        }
    }
}

