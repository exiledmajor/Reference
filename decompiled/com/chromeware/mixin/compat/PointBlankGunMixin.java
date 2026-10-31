/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.vicmatskiv.pointblank.item.FireModeInstance
 *  com.vicmatskiv.pointblank.item.GunItem
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.compat;

import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.cyberware.ZetatechBerserk;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"com.vicmatskiv.pointblank.item.GunItem"}, remap=false)
public abstract class PointBlankGunMixin {
    @WrapMethod(method={"reloadGun"})
    private int chrome$reload(ItemStack gun, Player player, FireModeInstance mode, Operation<Integer> original) {
        int before = GunItem.getAmmo((ItemStack)gun, (FireModeInstance)mode);
        GunCyberware.reloadStarted(player, gun, before);
        int result = (Integer)original.call(new Object[]{gun, player, mode});
        GunCyberware.reloadFinished(player, gun, result);
        return result;
    }

    @Inject(method={"adjustInaccuracy"}, at={@At(value="RETURN")}, cancellable=true)
    private void chrome$spread(Player player, ItemStack gun, boolean aiming, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue((Object)(cir.getReturnValueD() * GunCyberware.spread(player)));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"handleClientHitScanFireRequest"})
    private void chrome$hitscan(ServerPlayer p, FireModeInstance mode, UUID id, int slot, int correlation, boolean aiming, boolean alternate, long seed, Operation<Void> original) {
        if (!ZetatechBerserk.rangedAllowed((Player)p)) {
            return;
        }
        GunCyberware.Shot previous = GunCyberware.current();
        try {
            original.call(new Object[]{p, mode, id, slot, correlation, aiming, alternate, seed});
        }
        finally {
            GunCyberware.current(previous);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"handleClientProjectileFireRequest"})
    private void chrome$projectile(ServerPlayer p, FireModeInstance mode, UUID id, int slot, int correlation, boolean aiming, double x, double y, double z, double dx, double dy, double dz, int target, long seed, Operation<Void> original) {
        if (!ZetatechBerserk.rangedAllowed((Player)p)) {
            return;
        }
        GunCyberware.Shot previous = GunCyberware.current();
        try {
            original.call(new Object[]{p, mode, id, slot, correlation, aiming, x, y, z, dx, dy, dz, target, seed});
        }
        finally {
            GunCyberware.current(previous);
        }
    }

    @Inject(method={"handleClientHitScanFireRequest", "handleClientProjectileFireRequest"}, at={@At(value="INVOKE", target="Lcom/vicmatskiv/pointblank/feature/SoundFeature;playFireSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V")})
    private void chrome$acceptedRound(CallbackInfo ci, @Local(argsOnly=true) ServerPlayer player, @Local ItemStack gun) {
        GunCyberware.current(GunCyberware.fired((Player)player, gun));
    }

    @Inject(method={"hitScanTarget"}, at={@At(value="TAIL")})
    private void chrome$impact(CallbackInfo ci, @Local(argsOnly=true) HitResult hit) {
        if (hit.getType() != HitResult.Type.MISS) {
            GunCyberware.impactCurrent(hit.getLocation());
        }
    }
}

