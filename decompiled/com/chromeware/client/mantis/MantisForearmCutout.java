/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelPart$Cube
 */
package com.chromeware.client.mantis;

import com.chromeware.client.body.ClippedLimbRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;

public final class MantisForearmCutout {
    public static final float CUT_Y = 4.0f;

    public static void render(ModelPart.Cube cube, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        ClippedLimbRenderer.render(cube, 4.0f - cube.minY, pose, buffer, light, overlay, color);
    }
}

