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
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record CyberwareRig(List<Bone> bones, Map<String, Locator> locators) {
    private static Vector3f vector(JsonArray a) {
        return new Vector3f(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    public static CyberwareRig load(Reader reader) {
        JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        if (root.get("format").getAsInt() != 1) {
            throw new IllegalArgumentException("Unsupported cyberware rig");
        }
        ArrayList<Bone> bones = new ArrayList<Bone>();
        HashSet<String> names = new HashSet<String>();
        for (JsonElement entry : root.getAsJsonArray("bones")) {
            String parent;
            JsonObject b = entry.getAsJsonObject();
            String name = b.get("name").getAsString();
            String string = parent = b.get("parent").isJsonNull() ? null : b.get("parent").getAsString();
            if (names.contains(name) || parent != null && !names.contains(parent)) {
                throw new IllegalArgumentException("Rig bones must have unique names and precede their children");
            }
            names.add(name);
            ArrayList<Quad> quads = new ArrayList<Quad>();
            for (JsonElement value : b.getAsJsonArray("quads")) {
                JsonObject q = value.getAsJsonObject();
                ArrayList<Vertex> vertices = new ArrayList<Vertex>();
                for (JsonElement v : q.getAsJsonArray("vertices")) {
                    JsonArray a = v.getAsJsonArray();
                    vertices.add(new Vertex(CyberwareRig.vector(a), a.get(3).getAsFloat(), a.get(4).getAsFloat()));
                }
                if (vertices.size() != 4) {
                    throw new IllegalArgumentException("Rig face must be a quad");
                }
                quads.add(new Quad(q.get("piece").getAsString(), CyberwareRig.vector(q.getAsJsonArray("normal")), List.copyOf(vertices)));
            }
            bones.add(new Bone(name, parent, CyberwareRig.vector(b.getAsJsonArray("pivot")), CyberwareRig.vector(b.getAsJsonArray("rotation")), List.copyOf(quads)));
        }
        HashMap<String, Locator> locators = new HashMap<String, Locator>();
        for (Map.Entry entry : root.getAsJsonObject("locators").entrySet()) {
            JsonObject l = ((JsonElement)entry.getValue()).getAsJsonObject();
            String bone = l.get("bone").getAsString();
            if (!names.contains(bone)) {
                throw new IllegalArgumentException("Unknown locator bone: " + bone);
            }
            locators.put((String)entry.getKey(), new Locator(bone, CyberwareRig.vector(l.getAsJsonArray("position"))));
        }
        return new CyberwareRig(List.copyOf(bones), Map.copyOf(locators));
    }

    public Map<String, Vec3> render(boolean left, boolean slim, ModelPart arm, float center, PoseStack stack, Function<String, Transform> animation, VertexConsumer buffer, int light, int overlay) {
        return this.render(left, slim, arm, center, stack, animation, buffer, light, overlay, name -> true, 0.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Map<String, Vec3> render(boolean left, boolean slim, ModelPart arm, float center, PoseStack stack, Function<String, Transform> animation, VertexConsumer buffer, int light, int overlay, Predicate<String> draw, float along) {
        stack.pushPose();
        try {
            arm.translateAndRotate(stack);
            stack.translate(center, 0.0f, 0.0f);
            stack.scale((float)(left ? -1 : 1) * (slim ? 0.75f : 1.0f), 1.0f, 1.0f);
            stack.translate(0.0f, along, 0.0f);
            HashMap<String, Vec3> result = new HashMap<String, Vec3>();
            for (Bone bone : this.bones) {
                if (bone.parent() != null) continue;
                this.renderBone(bone, stack, animation, buffer, light, overlay, result, draw);
            }
            Map map = Map.copyOf(result);
            return map;
        }
        finally {
            stack.popPose();
        }
    }

    public Map<String, Matrix4f> poses(Function<String, Transform> animation) {
        HashMap<String, Matrix4f> result = new HashMap<String, Matrix4f>();
        for (Bone bone : this.bones) {
            if (bone.parent() != null) continue;
            this.pose(bone, new Matrix4f(), animation, result);
        }
        return result;
    }

    private void pose(Bone bone, Matrix4f parent, Function<String, Transform> animation, Map<String, Matrix4f> result) {
        Matrix4f m = new Matrix4f((Matrix4fc)parent);
        Vector3f pivot = bone.pivot();
        m.translate(pivot.x, pivot.y, pivot.z);
        Transform transform = animation.apply(bone.name());
        Vector3f p = transform.position();
        m.translate(p.x, p.y, p.z);
        Vector3f r = new Vector3f((Vector3fc)bone.rotation()).add((Vector3fc)transform.rotation());
        m.rotate((Quaternionfc)new Quaternionf().rotationZYX(r.z, r.y, r.x));
        Vector3f s = transform.scale();
        m.scale(s.x, s.y, s.z);
        result.put(bone.name(), m);
        for (Bone child : this.bones) {
            if (!bone.name().equals(child.parent())) continue;
            this.pose(child, m, animation, result);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderBone(Bone bone, PoseStack stack, Function<String, Transform> animation, VertexConsumer buffer, int light, int overlay, Map<String, Vec3> result, Predicate<String> draw) {
        stack.pushPose();
        try {
            Vector3f pivot = bone.pivot();
            stack.translate(pivot.x, pivot.y, pivot.z);
            Transform transform = animation.apply(bone.name());
            Vector3f p = transform.position();
            stack.translate(p.x, p.y, p.z);
            Vector3f r = new Vector3f((Vector3fc)bone.rotation()).add((Vector3fc)transform.rotation());
            stack.mulPose(new Quaternionf().rotationZYX(r.z, r.y, r.x));
            Vector3f s = transform.scale();
            stack.scale(s.x, s.y, s.z);
            PoseStack.Pose pose = stack.last();
            if (buffer != null && draw.test(bone.name())) {
                for (Quad quad : bone.quads()) {
                    Vector3f normal = pose.transformNormal(quad.normal(), new Vector3f());
                    for (Vertex v : quad.vertices()) {
                        Vector3f point = pose.pose().transformPosition((Vector3fc)v.point(), new Vector3f());
                        buffer.addVertex(point.x, point.y, point.z, -1, v.u(), v.v(), overlay, light, normal.x, normal.y, normal.z);
                    }
                }
            }
            for (Map.Entry entry : this.locators.entrySet()) {
                if (!((Locator)entry.getValue()).bone().equals(bone.name())) continue;
                Vector3f point = pose.pose().transformPosition((Vector3fc)((Locator)entry.getValue()).point(), new Vector3f());
                result.put((String)entry.getKey(), new Vec3((double)point.x, (double)point.y, (double)point.z));
            }
            for (Bone bone2 : this.bones) {
                if (!bone.name().equals(bone2.parent())) continue;
                this.renderBone(bone2, stack, animation, buffer, light, overlay, result, draw);
            }
        }
        finally {
            stack.popPose();
        }
    }

    public record Vertex(Vector3f point, float u, float v) {
    }

    public record Quad(String piece, Vector3f normal, List<Vertex> vertices) {
    }

    public record Bone(String name, String parent, Vector3f pivot, Vector3f rotation, List<Quad> quads) {
    }

    public record Locator(String bone, Vector3f point) {
    }

    public record Transform(Vector3f position, Vector3f rotation, Vector3f scale) {
        public static final Transform IDENTITY = new Transform(new Vector3f(), new Vector3f(), new Vector3f(1.0f));
    }
}

