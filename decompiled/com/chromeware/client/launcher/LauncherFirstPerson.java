/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.world.entity.HumanoidArm
 *  org.joml.Matrix3f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package com.chromeware.client.launcher;

import com.chromeware.client.cyberware.ArmFirstPerson;
import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.monowire.MonowireFirstPerson;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class LauncherFirstPerson {
    public static void place(PoseStack stack, Quaternionf cameraRotation, float scale, double projectionRatio) {
        ArmFirstPerson.place(stack, cameraRotation, scale, projectionRatio);
    }

    public static void cameraArm(ModelPart arm, CyberwareRig.Transform view) {
        MonowireFirstPerson.cameraArm(arm, view);
    }

    public static void followHead(PlayerModel<?> model, HumanoidArm side) {
        ModelPart arm = side == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
        LauncherFirstPerson.aim(arm, model.head.xRot, model.head.yRot);
        (side == HumanoidArm.LEFT ? model.leftSleeve : model.rightSleeve).copyFrom(arm);
    }

    public static void aim(ModelPart arm, float pitch, float yaw) {
        float amount = (float)Math.clamp((double)((double)(-arm.xRot) / 1.5707963267948966), (double)0.0, (double)1.0);
        Quaternionf rotation = new Quaternionf().rotationY(yaw * amount).rotateX(pitch * amount).mul((Quaternionfc)new Quaternionf().rotationZYX(arm.zRot, arm.yRot, arm.xRot));
        Matrix3f m = new Matrix3f().set((Quaternionfc)rotation);
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

