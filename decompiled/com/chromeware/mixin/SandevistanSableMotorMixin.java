/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  dev.ryanhcode.sable.api.physics.PhysicsPipelineBody
 *  dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.SandevistanMotorAccess;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.constraint.ConstraintJointAxis;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.EnumMap;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets={"dev.ryanhcode.sable.physics.impl.rapier.constraint.RapierConstraintHandle"}, remap=false)
public abstract class SandevistanSableMotorMixin
implements SandevistanMotorAccess {
    @Unique
    private PhysicsPipelineBody chrome$a;
    @Unique
    private PhysicsPipelineBody chrome$b;
    @Unique
    private final Map<ConstraintJointAxis, SandevistanMotorAccess.Motor> chrome$motors = new EnumMap<ConstraintJointAxis, SandevistanMotorAccess.Motor>(ConstraintJointAxis.class);

    @Override
    public void chrome$bodies(PhysicsPipelineBody a, PhysicsPipelineBody b) {
        this.chrome$a = a;
        this.chrome$b = b;
    }

    @Shadow
    public abstract void setMotor(ConstraintJointAxis var1, double var2, double var4, double var6, boolean var8, double var9);

    @Override
    public void chrome$refreshMotors() {
        for (Map.Entry<ConstraintJointAxis, SandevistanMotorAccess.Motor> e : new EnumMap<ConstraintJointAxis, SandevistanMotorAccess.Motor>(this.chrome$motors).entrySet()) {
            SandevistanMotorAccess.Motor m = e.getValue();
            this.setMotor(e.getKey(), m.target(), m.stiffness(), m.damping(), m.limited(), m.maximum());
        }
    }

    @WrapMethod(method={"setMotor"})
    private void chrome$motor(ConstraintJointAxis axis, double target, double stiffness, double damping, boolean limited, double maximum, Operation<Void> original) {
        double d;
        this.chrome$motors.put(axis, new SandevistanMotorAccess.Motor(target, stiffness, damping, limited, maximum));
        PhysicsPipelineBody physicsPipelineBody = this.chrome$a;
        if (physicsPipelineBody instanceof SubLevel) {
            SubLevel body = (SubLevel)physicsPipelineBody;
            d = SandevistanContraptions.scale(body);
        } else {
            physicsPipelineBody = this.chrome$b;
            if (physicsPipelineBody instanceof SubLevel) {
                SubLevel body = (SubLevel)physicsPipelineBody;
                d = SandevistanContraptions.scale(body);
            } else {
                d = 1.0;
            }
        }
        double s = d;
        original.call(new Object[]{axis, target, stiffness * s * s, damping * s, limited, maximum * s * s});
    }
}

