/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.RangedHits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BowItem.class, CrossbowItem.class})
public abstract class ChargedRangedWeaponMixin {
    @Inject(method={"shootProjectile"}, at={@At(value="HEAD")})
    private void chrome$charge(LivingEntity shooter, Projectile projectile, int index, float speed, float inaccuracy, float angle, LivingEntity target, CallbackInfo ci) {
        if (!shooter.level().isClientSide() && shooter instanceof Player && index == 0 && (!(this instanceof BowItem) || speed >= 3.0f)) {
            RangedHits.charged(projectile);
        }
    }
}

