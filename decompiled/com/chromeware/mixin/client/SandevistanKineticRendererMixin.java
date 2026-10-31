/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.base.KineticBlockEntity
 *  com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction$Axis
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanKinetics;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={KineticBlockEntityRenderer.class}, remap=false)
public abstract class SandevistanKineticRendererMixin {
    @Inject(method={"getAngleForBe"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$angle(KineticBlockEntity be, BlockPos pos, Direction.Axis axis, CallbackInfoReturnable<Float> ci) {
        if (SandevistanKinetics.affected(be)) {
            ci.setReturnValue((Object)Float.valueOf(SandevistanKinetics.angle(be, pos, axis)));
        }
    }
}

