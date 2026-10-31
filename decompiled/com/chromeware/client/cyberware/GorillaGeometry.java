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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record GorillaGeometry(Map<String, Map<String, List<Quad>>> variants) {
    private static final Map<String, float[]> FRAME_BOUNDS = Map.of("upper_actuator_mount", new float[]{-2.35f, 4.875f, -1.125f, -1.95f, 5.25f, 1.125f}, "lower_actuator_mount", new float[]{-2.35f, 6.875f, -1.125f, -1.95f, 7.25f, 1.125f}, "outer_copper_actuator", new float[]{-2.25f, 5.0f, 0.375f, -1.95f, 7.125f, 0.75f}, "inner_copper_actuator", new float[]{-2.25f, 5.0f, -0.75f, -1.95f, 7.125f, -0.375f}, "outer_brass_actuator_sleeve", new float[]{-2.35f, 5.125f, 0.25f, -1.9f, 5.625f, 0.875f}, "inner_brass_actuator_sleeve", new float[]{-2.35f, 5.125f, -0.875f, -1.9f, 5.625f, -0.25f});

    public static GorillaGeometry load(Reader reader) {
        JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        if (root.get("format").getAsInt() != 2) {
            throw new IllegalArgumentException("Unsupported Gorilla mesh format");
        }
        HashMap all = new HashMap();
        for (String variant : List.of("right", "left", "slim_right", "slim_left")) {
            JsonObject parts = root.getAsJsonObject("variants").getAsJsonObject(variant);
            HashMap result = new HashMap();
            for (String key : List.of("upper", "forearm", "hand")) {
                ArrayList<Quad> quads = new ArrayList<Quad>();
                for (JsonElement element : parts.getAsJsonArray(key)) {
                    JsonObject q = element.getAsJsonObject();
                    JsonArray n = q.getAsJsonArray("normal");
                    ArrayList<Vertex> vertices = new ArrayList<Vertex>();
                    for (JsonElement value : q.getAsJsonArray("vertices")) {
                        JsonArray v = value.getAsJsonArray();
                        vertices.add(new Vertex(new Vector3f(v.get(0).getAsFloat(), v.get(1).getAsFloat(), v.get(2).getAsFloat()), v.get(3).getAsFloat(), v.get(4).getAsFloat()));
                    }
                    quads.add(new Quad(q.get("piece").getAsString(), new Vector3f(n.get(0).getAsFloat(), n.get(1).getAsFloat(), n.get(2).getAsFloat()), List.copyOf(vertices), q.get("material").getAsString()));
                }
                result.put(key, List.copyOf(quads));
            }
            result.put("frame", GorillaGeometry.fitFrame((List)result.get("forearm"), variant.startsWith("slim_")));
            all.put(variant, Map.copyOf(result));
        }
        return new GorillaGeometry(Map.copyOf(all));
    }

    /*
     * Exception decompiling
     */
    private static List<Quad> fitFrame(List<Quad> forearm, boolean slim) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * java.lang.UnsupportedOperationException
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray.getDimSize(NewAnonymousArray.java:142)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.isNewArrayLambda(LambdaRewriter.java:455)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:409)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteDynamicExpression(LambdaRewriter.java:167)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:105)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterHelper.applyForwards(ExpressionRewriterHelper.java:12)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriterToArgs(AbstractMemberFunctionInvokation.java:101)
         *     at org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMemberFunctionInvokation.applyExpressionRewriter(AbstractMemberFunctionInvokation.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewriteExpression(LambdaRewriter.java:103)
         *     at org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment.rewriteExpressions(StructuredAssignment.java:146)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter.rewrite(LambdaRewriter.java:88)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.rewriteLambdas(Op04StructuredStatement.java:1137)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:912)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    public List<Quad> part(boolean left, boolean slim, String part) {
        return this.variants.get((slim ? "slim_" : "") + (left ? "left" : "right")).get(part);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderArm(boolean left, boolean slim, ModelPart arm, float center, PoseStack stack, Function<String, VertexConsumer> buffers, boolean sleeve, int light, int overlay) {
        stack.pushPose();
        try {
            arm.translateAndRotate(stack);
            stack.translate(center, 0.0f, 0.0f);
            if (left) {
                stack.scale(-1.0f, 1.0f, 1.0f);
            }
            for (String part : List.of("upper", "forearm", "hand")) {
                this.renderPart(left, slim, part, stack.last(), buffers, sleeve, light, overlay);
            }
        }
        finally {
            stack.popPose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderFrameHardware(boolean left, boolean slim, ModelPart arm, float center, PoseStack stack, VertexConsumer buffer, int light, int overlay) {
        stack.pushPose();
        try {
            arm.translateAndRotate(stack);
            stack.translate(center, 0.0f, 0.0f);
            if (left) {
                stack.scale(-1.0f, 1.0f, 1.0f);
            }
            this.renderPart(left, slim, "frame", stack.last(), ignored -> buffer, false, light, overlay);
        }
        finally {
            stack.popPose();
        }
    }

    public void renderPart(boolean left, boolean slim, String part, PoseStack.Pose pose, Function<String, VertexConsumer> buffers, boolean sleeve, int light, int overlay) {
        for (Quad quad : this.part(left, slim, part)) {
            VertexConsumer buffer;
            if (quad.material().equals("sleeve") && !sleeve || (buffer = buffers.apply(quad.material())) == null) continue;
            Vector3f normal = pose.transformNormal(quad.normal(), new Vector3f());
            for (Vertex v : quad.vertices()) {
                Vector3f p = pose.pose().transformPosition((Vector3fc)v.position(), new Vector3f());
                buffer.addVertex(p.x, p.y, p.z, -1, v.u(), v.v(), overlay, light, normal.x, normal.y, normal.z);
            }
        }
    }

    public record Vertex(Vector3f position, float u, float v) {
    }

    public record Quad(String piece, Vector3f normal, List<Vertex> vertices, String material) {
    }
}

