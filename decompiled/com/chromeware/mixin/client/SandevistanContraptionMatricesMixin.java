/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  com.simibubi.create.content.contraptions.render.ContraptionMatrices
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ContraptionMatrices.class}, remap=false)
public abstract class SandevistanContraptionMatricesMixin {
    @WrapOperation(method={"setup"}, at={@At(value="INVOKE", target="Lnet/createmod/catnip/animation/AnimationTickHolder;getPartialTicks()F")})
    private float chrome$partial(Operation<Float> original, @Local(argsOnly=true) AbstractContraptionEntity entity) {
        return SandevistanClient.tickDelta((Entity)entity, ((Float)original.call(new Object[0])).floatValue());
    }
}

