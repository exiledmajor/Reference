/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.base.KineticBlockEntity
 *  com.simibubi.create.content.kinetics.base.RotatingInstance
 *  net.minecraft.core.Direction$Axis
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanKinetics;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RotatingInstance.class}, remap=false)
public abstract class SandevistanRotatingInstanceMixin {
    @Inject(method={"setup(Lcom/simibubi/create/content/kinetics/base/KineticBlockEntity;Lnet/minecraft/core/Direction$Axis;F)Lcom/simibubi/create/content/kinetics/base/RotatingInstance;"}, at={@At(value="RETURN")})
    private void chrome$bind(KineticBlockEntity be, Direction.Axis axis, float speed, CallbackInfoReturnable<RotatingInstance> ci) {
        RotatingInstance instance = (RotatingInstance)this;
        SandevistanKinetics.bind(instance, be, speed, instance.rotationOffset);
    }

    @Inject(method={"setRotationalSpeed"}, at={@At(value="HEAD")})
    private void chrome$speed(float speed, CallbackInfoReturnable<RotatingInstance> ci) {
        SandevistanKinetics.speed((RotatingInstance)this, speed);
    }

    @Inject(method={"setRotationOffset"}, at={@At(value="HEAD")})
    private void chrome$offset(float offset, CallbackInfoReturnable<RotatingInstance> ci) {
        SandevistanKinetics.offset((RotatingInstance)this, offset);
    }
}

