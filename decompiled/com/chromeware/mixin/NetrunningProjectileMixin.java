/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.projectile.Projectile
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.netrunning.Netrunning;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Projectile.class})
public abstract class NetrunningProjectileMixin {
    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void chrome$lockProjectile(CallbackInfo ci) {
        Projectile projectile = (Projectile)this;
        if ((Netrunning.flags((Entity)projectile) & 1) != 0) {
            projectile.setDeltaMovement(0.0, projectile.getDeltaMovement().y, 0.0);
        }
    }
}

