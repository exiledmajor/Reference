/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelPart$Cube
 */
package com.chromeware.client.launcher;

import com.chromeware.client.body.ClippedLimbRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

public final class LauncherForearmCutout {
    public static final float MIN_Y = 4.0f;
    public static final float MAX_Y = 10.0f;

    public static void render(ModelPart.Cube cube, boolean left, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        ClippedLimbRenderer.render(cube, 4.0f - cube.minY, pose, buffer, light, overlay, color);
    }
}

