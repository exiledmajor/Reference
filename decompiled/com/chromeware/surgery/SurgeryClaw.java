/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmAngleTarget
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package com.chromeware.surgery;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmAngleTarget;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class SurgeryClaw {
    private static final Field BASE = SurgeryClaw.field("baseAngle");
    private static final Field LOWER = SurgeryClaw.field("lowerArmAngle");
    private static final Field UPPER = SurgeryClaw.field("upperArmAngle");
    private static final Field HEAD = SurgeryClaw.field("headAngle");

    public static Vec3[] joints(BlockPos arm, boolean ceiling, float base, float lower, float upper, float head) {
        Matrix4f m = new Matrix4f().translate((float)arm.getX() + 0.5f, (float)arm.getY() + 0.5f, (float)arm.getZ() + 0.5f);
        if (ceiling) {
            m.rotateX((float)Math.PI);
        }
        m.translate(0.0f, 0.25f, 0.0f).rotateY(SurgeryClaw.rad(base));
        m.translate(0.0f, 0.125f, 0.0f);
        Vec3 shoulder = SurgeryClaw.at(m);
        m.rotateX(SurgeryClaw.rad(lower)).translate(0.0f, 0.0f, -0.875f);
        Vec3 elbow = SurgeryClaw.at(m);
        m.rotateX(SurgeryClaw.rad(upper - 180.0f)).translate(0.0f, 0.0f, -0.9375f);
        Vec3 wrist = SurgeryClaw.at(m);
        m.rotateX(SurgeryClaw.rad(head - 45.0f)).rotateX(SurgeryClaw.rad(90.0f)).translate(0.0f, -0.625f, 0.0f);
        return new Vec3[]{shoulder, elbow, wrist, SurgeryClaw.at(m)};
    }

    public static Vec3[] joints(BlockPos arm, boolean ceiling, ArmAngleTarget target) {
        return SurgeryClaw.joints(arm, ceiling, SurgeryClaw.get(BASE, target), SurgeryClaw.get(LOWER, target), SurgeryClaw.get(UPPER, target), SurgeryClaw.get(HEAD, target));
    }

    public static Vec3 position(BlockPos arm, boolean ceiling, float base, float lower, float upper, float head) {
        return SurgeryClaw.joints(arm, ceiling, base, lower, upper, head)[3];
    }

    public static Vec3 position(BlockPos arm, boolean ceiling, ArmAngleTarget target) {
        return SurgeryClaw.joints(arm, ceiling, target)[3];
    }

    private static Vec3 at(Matrix4f m) {
        Vector3f p = m.transformPosition(new Vector3f());
        return new Vec3((double)p.x, (double)p.y, (double)p.z);
    }

    private static float rad(float degrees) {
        return (float)Math.toRadians(degrees);
    }

    private static Field field(String name) {
        try {
            Field f = ArmAngleTarget.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        }
        catch (NoSuchFieldException e) {
            throw new IllegalStateException("Create's ArmAngleTarget has no " + name, e);
        }
    }

    private static float get(Field f, ArmAngleTarget target) {
        try {
            return f.getFloat(target);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}

