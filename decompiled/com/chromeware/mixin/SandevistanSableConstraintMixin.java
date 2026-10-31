/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.api.physics.PhysicsPipelineBody
 *  dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration
 *  dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.SandevistanMotorAccess;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"dev.ryanhcode.sable.physics.impl.rapier.RapierPhysicsPipeline"}, remap=false)
public abstract class SandevistanSableConstraintMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method={"addConstraint"}, at={@At(value="RETURN")})
    private void chrome$link(PhysicsPipelineBody a, PhysicsPipelineBody b, PhysicsConstraintConfiguration<?> config, CallbackInfoReturnable<PhysicsConstraintHandle> ci) {
        SandevistanContraptions.link((Level)this.level, a, b, (PhysicsConstraintHandle)ci.getReturnValue());
        Object object = ci.getReturnValue();
        if (object instanceof SandevistanMotorAccess) {
            SandevistanMotorAccess access = (SandevistanMotorAccess)object;
            access.chrome$bodies(a, b);
        }
    }
}

