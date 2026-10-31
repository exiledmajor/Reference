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
package com.chromeware.client.monowire;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;

public final class MonowireWristCutout {
    public static final float MIN_Y = 2.0f;
    public static final float MAX_Y = 7.0f;

    public static void render(ModelPart.Cube cube, boolean left, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        for (ModelPart.Polygon face : cube.polygons) {
            float f = face.normal.x;
            int n = left ? -1 : 1;
            if (f * (float)n < 0.9f) {
                MonowireWristCutout.emitFace(face, pose, buffer, light, overlay, color);
                continue;
            }
            float minZ = Float.POSITIVE_INFINITY;
            float maxZ = Float.NEGATIVE_INFINITY;
            float minY = Float.POSITIVE_INFINITY;
            float maxY = Float.NEGATIVE_INFINITY;
            for (ModelPart.Vertex v : face.vertices) {
                minZ = Math.min(minZ, v.pos.z);
                maxZ = Math.max(maxZ, v.pos.z);
                minY = Math.min(minY, v.pos.y);
                maxY = Math.max(maxY, v.pos.y);
            }
            float z0 = Math.max(minZ, -1.15f);
            float z1 = Math.min(maxZ, 1.15f);
            float y0 = Math.max(minY, 2.0f);
            float y1 = Math.min(maxY, 7.0f);
            if (z0 >= z1 || y0 >= y1) {
                MonowireWristCutout.emitFace(face, pose, buffer, light, overlay, color);
                continue;
            }
            MonowireWristCutout.patch(face, minZ, maxZ, minY, y0, pose, buffer, light, overlay, color);
            MonowireWristCutout.patch(face, minZ, maxZ, y1, maxY, pose, buffer, light, overlay, color);
            MonowireWristCutout.patch(face, minZ, z0, y0, y1, pose, buffer, light, overlay, color);
            MonowireWristCutout.patch(face, z1, maxZ, y0, y1, pose, buffer, light, overlay, color);
        }
    }

    private static void patch(ModelPart.Polygon face, float z0, float z1, float y0, float y1, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        if (z0 >= z1 || y0 >= y1) {
            return;
        }
        float minZ = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        for (ModelPart.Vertex v : face.vertices) {
            minZ = Math.min(minZ, v.pos.z);
            minY = Math.min(minY, v.pos.y);
        }
        ModelPart.Vertex a = face.vertices[0];
        ModelPart.Vertex b = face.vertices[1];
        ModelPart.Vertex d = face.vertices[3];
        float bz = b.pos.z - a.pos.z;
        float by = b.pos.y - a.pos.y;
        float dz = d.pos.z - a.pos.z;
        float dy = d.pos.y - a.pos.y;
        float determinant = bz * dy - by * dz;
        for (ModelPart.Vertex v : face.vertices) {
            float z = Math.abs(v.pos.z - minZ) < 0.001f ? z0 : z1;
            float y = Math.abs(v.pos.y - minY) < 0.001f ? y0 : y1;
            float pz = z - a.pos.z;
            float py = y - a.pos.y;
            float s = (pz * dy - py * dz) / determinant;
            float t = (bz * py - by * pz) / determinant;
            ModelPart.Vertex mapped = new ModelPart.Vertex(a.pos.x, y, z, a.u + s * (b.u - a.u) + t * (d.u - a.u), a.v + s * (b.v - a.v) + t * (d.v - a.v));
            MonowireWristCutout.emit(mapped, face.normal, pose, buffer, light, overlay, color);
        }
    }

    private static void emitFace(ModelPart.Polygon face, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        for (ModelPart.Vertex v : face.vertices) {
            MonowireWristCutout.emit(v, face.normal, pose, buffer, light, overlay, color);
        }
    }

    private static void emit(ModelPart.Vertex vertex, Vector3f normal, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay, int color) {
        Vector3f point = pose.pose().transformPosition(vertex.pos.x / 16.0f, vertex.pos.y / 16.0f, vertex.pos.z / 16.0f, new Vector3f());
        Vector3f transformed = pose.transformNormal(normal, new Vector3f());
        buffer.addVertex(point.x, point.y, point.z, color, vertex.u, vertex.v, overlay, light, transformed.x, transformed.y, transformed.z);
    }
}

