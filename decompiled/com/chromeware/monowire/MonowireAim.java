/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.monowire;

import net.minecraft.world.phys.Vec3;

public record MonowireAim(Vec3 forward, Vec3 right, Vec3 up) {
    public static MonowireAim of(Vec3 look, float yaw) {
        Vec3 forward = look.normalize();
        if (forward.lengthSqr() < 0.5) {
            forward = new Vec3(0.0, 0.0, 1.0);
        }
        double angle = Math.toRadians(yaw);
        Vec3 right = new Vec3(-Math.cos(angle), 0.0, -Math.sin(angle));
        if ((right = right.subtract(forward.scale(right.dot(forward))).normalize()).lengthSqr() < 0.5) {
            Vec3 reference = Math.abs(forward.y) > 0.9 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 1.0, 0.0);
            right = forward.cross(reference).normalize();
        }
        return new MonowireAim(forward, right, right.cross(forward).normalize());
    }
}

