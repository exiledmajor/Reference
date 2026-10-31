/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  dev.ryanhcode.sable.api.physics.PhysicsPipeline
 *  dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle
 *  dev.ryanhcode.sable.api.sublevel.SubLevelContainer
 *  dev.ryanhcode.sable.sublevel.ServerSubLevel
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.SandevistanPhysics;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value={SubLevelPhysicsSystem.class}, remap=false)
public abstract class SandevistanSablePhysicsMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void chrome$domains(SubLevelContainer container, CallbackInfo ci) {
        SandevistanContraptions.refresh((Level)this.level);
    }

    @WrapOperation(method={"tickPipelinePhysics"}, at={@At(value="INVOKE", target="Ldev/ryanhcode/sable/sublevel/ServerSubLevel;prePhysicsTick(Ldev/ryanhcode/sable/sublevel/system/SubLevelPhysicsSystem;Ldev/ryanhcode/sable/api/physics/handle/RigidBodyHandle;D)V")})
    private void chrome$actorTime(ServerSubLevel body, SubLevelPhysicsSystem system, RigidBodyHandle handle, double dt, Operation<Void> original) {
        original.call(new Object[]{body, system, handle, dt * SandevistanContraptions.scale((SubLevel)body)});
    }

    @WrapOperation(method={"tickPipelinePhysics"}, at={@At(value="INVOKE", target="Ldev/ryanhcode/sable/api/physics/PhysicsPipeline;physicsTick(D)V")})
    private void chrome$step(PhysicsPipeline pipeline, double dt, Operation<Void> original) {
        SandevistanPhysics.step(SubLevelContainer.getContainer((ServerLevel)this.level), pipeline, dt, () -> original.call(new Object[]{pipeline, dt}));
    }
}

