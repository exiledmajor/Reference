/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.ImplantCriticals;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={AbstractArrow.class})
public abstract class SandevistanArrowCriticalMixin {
    @Unique
    private int chrome$baseDamage;

    @ModifyExpressionValue(method={"onHitEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/Mth;ceil(D)I")})
    private int chrome$captureBase(int amount) {
        this.chrome$baseDamage = amount;
        return amount;
    }

    @WrapOperation(method={"onHitEntity"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")})
    private boolean chrome$criticalDamage(Entity target, DamageSource source, float amount, Operation<Boolean> original) {
        Entity entity;
        AbstractArrow arrow = (AbstractArrow)this;
        if (arrow.isCritArrow() && (entity = arrow.getOwner()) instanceof Player) {
            Player p = (Player)entity;
            amount += (float)this.chrome$baseDamage * ImplantCriticals.bonus(p, target, ImplantCriticals.Weapon.OTHER);
        }
        return (Boolean)original.call(new Object[]{target, source, Float.valueOf(amount)});
    }
}

