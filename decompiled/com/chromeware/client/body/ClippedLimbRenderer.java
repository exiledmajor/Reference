/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelPart$Cube
 *  net.minecraft.client.model.geom.ModelPart$Polygon
 *  net.minecraft.client.model.geom.ModelPart$Vertex
 *  org.joml.Vector3f
 */
package com.chromeware.client.body;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

public final class ClippedLimbRenderer {
    public static void stretched(ModelPart.Cube cube, float scale, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        Vector3f scratch = new Vector3f();
        for (ModelPart.Polygon face : cube.polygons) {
            Vector3f normal = pose.transformNormal(face.normal, new Vector3f());
            for (ModelPart.Vertex vertex : face.vertices) {
                float y = cube.minY + (vertex.pos.y() - cube.minY) * scale;
                Vector3f position = pose.pose().transformPosition(vertex.pos.x() / 16.0f, y / 16.0f, vertex.pos.z() / 16.0f, scratch);
                buffer.addVertex(position.x(), position.y(), position.z(), color, vertex.u, vertex.v, overlay, light, normal.x(), normal.y(), normal.z());
            }
        }
    }

    public static void render(ModelPart.Cube cube, float length, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        float cutoff = cube.minY + Math.min(length, cube.maxY - cube.minY);
        Vector3f scratch = new Vector3f();
        for (ModelPart.Polygon face : cube.polygons) {
            Vector3f normal = pose.transformNormal(face.normal, new Vector3f());
            boolean side = Math.abs(face.normal.y()) < 0.5f;
            for (ModelPart.Vertex vertex : face.vertices) {
                float y = Math.min(vertex.pos.y(), cutoff);
                float u = vertex.u;
                float v = vertex.v;
                if (side && y < vertex.pos.y()) {
                    for (ModelPart.Vertex upper : face.vertices) {
                        if (!(upper.pos.y() < vertex.pos.y()) || !(Math.abs(upper.pos.x() - vertex.pos.x()) < 0.001f) || !(Math.abs(upper.pos.z() - vertex.pos.z()) < 0.001f)) continue;
                        float fraction = (y - upper.pos.y()) / (vertex.pos.y() - upper.pos.y());
                        u = upper.u + (vertex.u - upper.u) * fraction;
                        v = upper.v + (vertex.v - upper.v) * fraction;
                        break;
                    }
                }
                Vector3f position = pose.pose().transformPosition(vertex.pos.x() / 16.0f, y / 16.0f, vertex.pos.z() / 16.0f, scratch);
                buffer.addVertex(position.x(), position.y(), position.z(), color, u, v, overlay, light, normal.x(), normal.y(), normal.z());
            }
        }
    }
}

