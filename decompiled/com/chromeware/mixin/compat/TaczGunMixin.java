/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.tacz.guns.api.entity.ReloadState
 *  com.tacz.guns.api.item.IGun
 *  com.tacz.guns.entity.shooter.ShooterDataHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat;

import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.cyberware.ZetatechBerserk;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.tacz.guns.item.ModernKineticGunItem"}, remap=false)
public abstract class TaczGunMixin {
    @Inject(method={"shoot"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$berserk(ShooterDataHolder holder, ItemStack gun, Supplier<Float> pitch, Supplier<Float> yaw, LivingEntity shooter, CallbackInfo ci) {
        Player p;
        if (shooter instanceof Player && !ZetatechBerserk.rangedAllowed(p = (Player)shooter)) {
            ci.cancel();
        }
    }

    @Inject(method={"doBulletSpread"}, at={@At(value="HEAD")})
    private void chrome$round(ShooterDataHolder holder, ItemStack gun, LivingEntity shooter, Projectile bullet, int index, float speed, float spread, float pitch, float yaw, CallbackInfo ci) {
        if (!shooter.level().isClientSide() && shooter instanceof Player) {
            Player p = (Player)shooter;
            if (index == 0) {
                GunCyberware.attach(GunCyberware.fired(p, gun), (Entity)bullet);
            }
        }
    }

    @ModifyVariable(method={"doBulletSpread"}, at=@At(value="HEAD"), argsOnly=true, index=7)
    private float chrome$spread(float spread, ShooterDataHolder holder, ItemStack gun, LivingEntity shooter, Projectile bullet, int index, float speed, float inaccuracy, float pitch, float yaw) {
        float f;
        if (shooter instanceof Player) {
            Player p = (Player)shooter;
            f = spread * (float)GunCyberware.spread(p);
        } else {
            f = spread;
        }
        return f;
    }

    @WrapMethod(method={"tickReload"})
    private ReloadState chrome$reload(ShooterDataHolder holder, ItemStack gun, LivingEntity shooter, Operation<ReloadState> original) {
        IGun item = (IGun)gun.getItem();
        int before = item.getCurrentAmmoCount(gun) + (item.hasBulletInBarrel(gun) ? 1 : 0);
        boolean emptyReload = holder.reloadStateType.isReloadingEmpty();
        ReloadState result = (ReloadState)original.call(new Object[]{holder, gun, shooter});
        if (!shooter.level().isClientSide() && shooter instanceof Player) {
            int after;
            Player p = (Player)shooter;
            if ((before == 0 || emptyReload) && (after = item.getCurrentAmmoCount(gun) + (item.hasBulletInBarrel(gun) ? 1 : 0)) > before) {
                GunCyberware.reloadStarted(p, gun, 0);
                GunCyberware.reloadFinished(p, gun, after);
            }
        }
        return result;
    }
}

