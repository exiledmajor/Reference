/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.SandevistanCombat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={LivingEntity.class})
public abstract class SandevistanCombatMixin {
    @WrapOperation(method={"hurt"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z")})
    private boolean chrome$combo(DamageSource source, TagKey<DamageType> tag, Operation<Boolean> original) {
        return (Boolean)original.call(new Object[]{source, tag}) != false || tag.equals((Object)DamageTypeTags.BYPASSES_COOLDOWN) && SandevistanCombat.combo(source, (LivingEntity)this);
    }
}

