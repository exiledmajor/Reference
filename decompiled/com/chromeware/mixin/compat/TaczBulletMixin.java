/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.tacz.guns.util.TacHitResult
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat;

import com.chromeware.compat.guns.GunCyberware;
import com.tacz.guns.util.TacHitResult;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.tacz.guns.entity.EntityKineticBullet"}, remap=false)
public abstract class TaczBulletMixin {
    @Inject(method={"onHitEntity"}, at={@At(value="TAIL")})
    private void chrome$entityImpact(TacHitResult hit, Vec3 start, Vec3 end, CallbackInfo ci) {
        Entity entity;
        Projectile bullet = (Projectile)this;
        if (!bullet.level().isClientSide() && (entity = bullet.getOwner()) instanceof Player) {
            Player p = (Player)entity;
            GunCyberware.impact((Entity)bullet, p, hit.getLocation());
        }
    }

    @Inject(method={"onHitBlock"}, at={@At(value="TAIL")})
    private void chrome$blockImpact(BlockHitResult hit, Vec3 start, Vec3 end, CallbackInfo ci) {
        Entity entity;
        Projectile bullet = (Projectile)this;
        if (!bullet.level().isClientSide() && hit.getType() != HitResult.Type.MISS && (entity = bullet.getOwner()) instanceof Player) {
            Player p = (Player)entity;
            GunCyberware.impact((Entity)bullet, p, hit.getLocation().add(Vec3.atLowerCornerOf((Vec3i)hit.getDirection().getNormal()).scale(0.02)));
        }
    }
}

