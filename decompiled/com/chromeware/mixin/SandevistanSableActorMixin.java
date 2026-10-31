/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor
 *  dev.ryanhcode.sable.sublevel.ServerSubLevel
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.Sandevistan;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets={"dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem"}, remap=false)
public abstract class SandevistanSableActorMixin {
    @WrapOperation(method={"tick"}, at={@At(value="INVOKE", target="Ldev/ryanhcode/sable/api/block/BlockEntitySubLevelActor;sable$tick(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)V")})
    private void chrome$actor(BlockEntitySubLevelActor actor, ServerSubLevel body, Operation<Void> original) {
        if (Sandevistan.shouldAdvance(body)) {
            original.call(new Object[]{actor, body});
        }
    }
}

