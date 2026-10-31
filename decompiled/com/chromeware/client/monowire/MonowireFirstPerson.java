/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  org.joml.Matrix3f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.chromeware.client.monowire;

import com.chromeware.client.cyberware.CyberwareRig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class MonowireFirstPerson {
    public static final float PLAYER_SKIN_SCALE = 0.9375f;
    private static final float MODEL_ORIGIN = 1.501f;

    public static void prepareCameraModel(HumanoidModel<?> model) {
        model.crouching = false;
    }

    public static void cameraArm(ModelPart arm, CyberwareRig.Transform view) {
        Vector3f r = view.rotation();
        Quaternionf rotation = new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot).mul((Quaternionfc)new Quaternionf().rotationZYX(r.z, r.y, r.x));
        Matrix3f matrix = new Matrix3f().set((Quaternionfc)rotation);
        arm.xRot = (float)Math.atan2(matrix.m12(), matrix.m22());
        arm.yRot = (float)Math.asin(Math.clamp((float)(-matrix.m02()), (float)-1.0f, (float)1.0f));
        arm.zRot = (float)Math.atan2(matrix.m01(), matrix.m00());
        arm.x += view.position().x * 16.0f;
        arm.y += view.position().y * 16.0f;
        arm.z += view.position().z * 16.0f;
    }

    public static void place(PoseStack stack, Quaternionf cameraRotation, float playerScale, double projectionRatio) {
        PoseStack.Pose pose = stack.last();
        pose.pose().identity();
        pose.normal().identity();
        stack.mulPose(cameraRotation);
        stack.scale((float)projectionRatio, (float)projectionRatio, 1.0f);
        stack.translate(0.0f, -1.6571876f * playerScale, -0.24f * playerScale);
        stack.scale(playerScale, playerScale, playerScale);
    }
}

