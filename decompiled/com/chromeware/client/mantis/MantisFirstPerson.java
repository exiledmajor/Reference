/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  org.joml.Matrix3f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.chromeware.client.mantis;

import com.chromeware.client.cyberware.CyberwareRig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class MantisFirstPerson {
    static final float CAMERA_X = -115.4079f;
    static final float CAMERA_Y = 6.6918f;
    static final float CAMERA_Z = 37.7462f;
    static final float OFFSET_X = -0.8377f;
    static final float OFFSET_Y = 3.8815f;
    static final float OFFSET_Z = -2.2142f;

    public static void place(PoseStack stack, Quaternionf camera, float scale, double projectionRatio) {
        stack.last().pose().identity();
        stack.last().normal().identity();
        stack.mulPose(camera);
        stack.scale((float)projectionRatio, (float)projectionRatio, 1.0f);
        stack.translate(0.0f, -1.6571876f * scale, -0.24f * scale);
        stack.scale(scale, scale, scale);
    }

    public static void cameraArm(ModelPart arm, boolean left) {
        MantisFirstPerson.cameraArm(arm, left, 1.0f);
    }

    public static void cameraArm(ModelPart arm, boolean left, float amount) {
        float side = left ? -1.0f : 1.0f;
        float u = Math.clamp((float)amount, (float)0.0f, (float)1.0f);
        Quaternionf camera = new Quaternionf().rotationZYX(MantisFirstPerson.rad(37.7462f * side), MantisFirstPerson.rad(6.6918f * side), MantisFirstPerson.rad(-115.4079f));
        MantisFirstPerson.decompose(arm, new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot).slerp((Quaternionfc)camera, u));
        arm.x += side * -0.8377f * u;
        arm.y += 3.8815f * u;
        arm.z += -2.2142f * u;
    }

    public static void cameraArm(ModelPart arm, boolean left, float amount, CyberwareRig.Transform view) {
        MantisFirstPerson.cameraArm(arm, left, amount);
        Vector3f r = view.rotation();
        MantisFirstPerson.decompose(arm, new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot).mul((Quaternionfc)new Quaternionf().rotationZYX(r.z, r.y, r.x)));
        arm.x += view.position().x * 16.0f;
        arm.y += view.position().y * 16.0f;
        arm.z += view.position().z * 16.0f;
    }

    private static float rad(float degrees) {
        return (float)Math.toRadians(degrees);
    }

    public static void followHead(PlayerModel<?> model) {
        MantisFirstPerson.followHead(model, 1.0f);
    }

    public static void followHead(PlayerModel<?> model, float amount) {
        MantisFirstPerson.followHead(model, amount, true, true);
    }

    public static void followHead(PlayerModel<?> model, float amount, boolean right, boolean left) {
        for (ModelPart arm : new ModelPart[]{right ? model.rightArm : null, left ? model.leftArm : null}) {
            if (arm == null) continue;
            MantisFirstPerson.decompose(arm, new Quaternionf().rotationY(model.head.yRot * amount).rotateX(model.head.xRot * amount).mul((Quaternionfc)new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot)));
        }
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
    }

    private static void decompose(ModelPart arm, Quaternionf q) {
        Matrix3f m = new Matrix3f().set((Quaternionfc)q);
        double cosY = Math.hypot(m.m00(), m.m01());
        arm.yRot = (float)Math.atan2(-m.m02(), cosY);
        if (cosY < 1.0E-6) {
            arm.xRot = 0.0f;
            arm.zRot = (float)Math.atan2(-m.m10(), m.m11());
        } else {
            arm.xRot = (float)Math.atan2(m.m12(), m.m22());
            arm.zRot = (float)Math.atan2(m.m01(), m.m00());
        }
    }
}

