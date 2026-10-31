/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.api.physics.PhysicsPipelineBody
 */
package com.chromeware.sandevistan;

import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;

public interface SandevistanMotorAccess {
    public void chrome$bodies(PhysicsPipelineBody var1, PhysicsPipelineBody var2);

    public void chrome$refreshMotors();

    public record Motor(double target, double stiffness, double damping, boolean limited, double maximum) {
    }
}

