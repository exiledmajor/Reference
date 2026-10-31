/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat;

import com.chromeware.compat.guns.GunCyberware;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.vicmatskiv.pointblank.item.HurtingItem"}, remap=false)
public abstract class PointBlankDamageMixin {
    @Inject(method={"handleBlockHit"}, at={@At(value="TAIL")})
    private void chrome$blockImpact(LivingEntity shooter, BlockHitResult hit, Entity projectile, CallbackInfo ci) {
        if (projectile != null && shooter instanceof Player) {
            Player p = (Player)shooter;
            GunCyberware.impact(projectile, p, hit.getLocation().add(Vec3.atLowerCornerOf((Vec3i)hit.getDirection().getNormal()).scale(0.02)));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"hurtEntity"})
    private float chrome$firearmHit(LivingEntity shooter, EntityHitResult hit, Entity projectile, ItemStack gun, Operation<Float> original) {
        GunCyberware.enterHit();
        try {
            float damage = ((Float)original.call(new Object[]{shooter, hit, projectile, gun})).floatValue();
            if (damage > 0.0f && projectile != null && shooter instanceof Player) {
                Player p = (Player)shooter;
                GunCyberware.impact(projectile, p, hit.getLocation());
            }
            float f = damage;
            return f;
        }
        finally {
            GunCyberware.leaveHit();
        }
    }
}

