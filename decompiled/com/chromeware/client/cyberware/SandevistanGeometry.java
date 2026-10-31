/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.geom.ModelPart
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.chromeware.client.cyberware;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class SandevistanGeometry {
    private final List<Quad> body;
    private final List<Quad> head;
    private final List<Quad> neck;
    private final float neckStart;
    private final float neckEnd;

    private SandevistanGeometry(List<Quad> body, List<Quad> head, List<Quad> neck, float start, float end) {
        this.body = body;
        this.head = head;
        this.neck = neck;
        this.neckStart = start;
        this.neckEnd = end;
    }

    public static SandevistanGeometry load(Reader reader) {
        JsonObject json = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        if (json.get("format").getAsInt() != 1) {
            throw new IllegalArgumentException("Unknown Sandevistan mesh format");
        }
        JsonArray endpoints = json.getAsJsonArray("neck_z");
        float start = endpoints.get(0).getAsFloat();
        float end = endpoints.get(1).getAsFloat();
        if (!(end > start)) {
            throw new IllegalArgumentException("Neck attachment must have length");
        }
        JsonObject parts = json.getAsJsonObject("parts");
        return new SandevistanGeometry(SandevistanGeometry.readPart(parts.getAsJsonArray("body")), SandevistanGeometry.readPart(parts.getAsJsonArray("head")), SandevistanGeometry.readPart(parts.getAsJsonArray("neck")), start, end);
    }

    private static List<Quad> readPart(JsonArray faces) {
        ArrayList<Quad> quads = new ArrayList<Quad>();
        for (JsonElement element : faces) {
            JsonObject face = element.getAsJsonObject();
            JsonArray normal = face.getAsJsonArray("normal");
            JsonArray points = face.getAsJsonArray("vertices");
            if (points.size() != 4 || normal.size() != 3) {
                throw new IllegalArgumentException("Expected a cuboid quad");
            }
            ArrayList<Vertex> vertices = new ArrayList<Vertex>();
            for (JsonElement point : points) {
                JsonArray v = point.getAsJsonArray();
                vertices.add(new Vertex(v.get(0).getAsFloat(), v.get(1).getAsFloat(), v.get(2).getAsFloat(), v.get(3).getAsFloat(), v.get(4).getAsFloat()));
            }
            quads.add(new Quad(face.get("piece").getAsString(), new Vector3f(normal.get(0).getAsFloat(), normal.get(1).getAsFloat(), normal.get(2).getAsFloat()), List.copyOf(vertices)));
        }
        return List.copyOf(quads);
    }

    List<Quad> body() {
        return this.body;
    }

    List<Quad> head() {
        return this.head;
    }

    List<Quad> neck() {
        return this.neck;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(PoseStack root, ModelPart bodyPart, ModelPart headPart, float dockOffset, boolean showHead, VertexConsumer buffer, int light, int overlay) {
        root.pushPose();
        try {
            bodyPart.translateAndRotate(root);
            SandevistanGeometry.renderRigid(this.body, root.last(), buffer, light, overlay);
        }
        finally {
            root.popPose();
        }
        if (!showHead) {
            return;
        }
        root.pushPose();
        try {
            headPart.translateAndRotate(root);
            root.translate(0.0f, 0.0f, dockOffset);
            SandevistanGeometry.renderRigid(this.head, root.last(), buffer, light, overlay);
        }
        finally {
            root.popPose();
        }
        PoseStack bodyFrame = new PoseStack();
        bodyPart.translateAndRotate(bodyFrame);
        PoseStack headFrame = new PoseStack();
        headPart.translateAndRotate(headFrame);
        headFrame.translate(0.0f, 0.0f, dockOffset);
        for (Quad quad : this.neck) {
            List<Vector3f> positions = quad.vertices().stream().map(v -> this.neckPosition((Vertex)v, bodyFrame.last().pose(), headFrame.last().pose())).toList();
            Vector3f normal = new Vector3f((Vector3fc)positions.get(1)).sub((Vector3fc)positions.get(0)).cross((Vector3fc)new Vector3f((Vector3fc)positions.get(2)).sub((Vector3fc)positions.get(0)));
            if (normal.lengthSquared() < 1.0E-12f) continue;
            normal.normalize();
            normal = root.last().transformNormal(normal, new Vector3f());
            for (int i = 0; i < 4; ++i) {
                Vector3f position = root.last().pose().transformPosition((Vector3fc)positions.get(i), new Vector3f());
                SandevistanGeometry.emit(buffer, position, quad.vertices().get(i), normal, light, overlay);
            }
        }
    }

    Vector3f neckPosition(Vertex v, Matrix4f bodyFrame, Matrix4f headFrame) {
        float amount = (v.z() - this.neckStart) / (this.neckEnd - this.neckStart);
        Vector3f start = bodyFrame.transformPosition(v.x(), v.y(), this.neckStart, new Vector3f());
        Vector3f end = headFrame.transformPosition(v.x(), v.y(), this.neckEnd, new Vector3f());
        return start.lerp((Vector3fc)end, amount);
    }

    private static void renderRigid(List<Quad> quads, PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay) {
        for (Quad quad : quads) {
            Vector3f normal = pose.transformNormal(quad.normal(), new Vector3f());
            for (Vertex v : quad.vertices()) {
                Vector3f position = pose.pose().transformPosition(v.x(), v.y(), v.z(), new Vector3f());
                SandevistanGeometry.emit(buffer, position, v, normal, light, overlay);
            }
        }
    }

    private static void emit(VertexConsumer buffer, Vector3f position, Vertex v, Vector3f normal, int light, int overlay) {
        buffer.addVertex(position.x(), position.y(), position.z(), -1, v.u(), v.v(), overlay, light, normal.x(), normal.y(), normal.z());
    }

    record Vertex(float x, float y, float z, float u, float v) {
    }

    record Quad(String piece, Vector3f normal, List<Vertex> vertices) {
    }
}

