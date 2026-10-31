/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  org.joml.Vector3f
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.SandevistanRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;

public record ContraptionMesh(List<Batch> batches, double radius, int vertexCount) {
    private static final ContraptionMesh EMPTY = new ContraptionMesh(List.of(), 0.0, 0);

    public ContraptionMesh {
        batches = List.copyOf(batches);
    }

    public ContraptionMesh(List<Batch> batches, double radius) {
        this(batches, radius, batches.stream().mapToInt(b -> b.vertices.size()).sum());
    }

    public void draw(PoseStack.Pose pose, MultiBufferSource buffers, int tint, float alpha, boolean fullBright) {
        this.draw(pose, buffers, tint, alpha, fullBright, false);
    }

    public void drawPalette(PoseStack.Pose pose, MultiBufferSource buffers, int color) {
        this.draw(pose, buffers, color, 1.0f, true, true);
    }

    private void draw(PoseStack.Pose pose, MultiBufferSource buffers, int tint, float alpha, boolean fullBright, boolean palette) {
        Vector3f p = new Vector3f();
        Vector3f n = new Vector3f();
        for (Batch batch : this.batches) {
            VertexConsumer buffer = buffers.getBuffer(palette ? SandevistanRenderTypes.palette(batch.layer, tint) : SandevistanRenderTypes.map(batch.layer));
            for (Vertex v : batch.vertices) {
                pose.pose().transformPosition(v.x, v.y, v.z, p);
                pose.normal().transform(v.nx, v.ny, v.nz, n);
                int r = (v.color >> 16 & 0xFF) * (palette ? 255 : tint >> 16 & 0xFF) / 255;
                int g = (v.color >> 8 & 0xFF) * (palette ? 255 : tint >> 8 & 0xFF) / 255;
                int b = (v.color & 0xFF) * (palette ? 255 : tint & 0xFF) / 255;
                int a = (int)((float)(v.color >>> 24 & 0xFF) * alpha);
                buffer.addVertex(p.x, p.y, p.z).setColor(r, g, b, a).setUv(v.u, v.v).setOverlay(v.overlay).setLight(fullBright ? 0xF000F0 : v.light).setNormal(n.x, n.y, n.z);
            }
        }
    }

    public record Batch(RenderType layer, List<Vertex> vertices) {
        public Batch {
            vertices = List.copyOf(vertices);
        }
    }

    public record Vertex(float x, float y, float z, int color, float u, float v, int overlay, int light, float nx, float ny, float nz) {
    }

    private static final class Capture
    implements VertexConsumer {
        final List<Vertex> vertices = new ArrayList<Vertex>();
        float x;
        float y;
        float z;
        float u;
        float v;
        float nx;
        float ny;
        float nz;
        int color = -1;
        int overlay = OverlayTexture.NO_OVERLAY;
        int light = 0xF000F0;
        boolean pending;

        private Capture() {
        }

        void flush() {
            if (this.pending) {
                this.vertices.add(new Vertex(this.x, this.y, this.z, this.color, this.u, this.v, this.overlay, this.light, this.nx, this.ny, this.nz));
                this.pending = false;
            }
        }

        public VertexConsumer addVertex(float x, float y, float z) {
            this.flush();
            this.x = x;
            this.y = y;
            this.z = z;
            this.pending = true;
            return this;
        }

        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.color = a << 24 | r << 16 | g << 8 | b;
            return this;
        }

        public VertexConsumer setUv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        public VertexConsumer setUv1(int u, int v) {
            this.overlay = u | v << 16;
            return this;
        }

        public VertexConsumer setUv2(int u, int v) {
            this.light = u | v << 16;
            return this;
        }

        public VertexConsumer setNormal(float x, float y, float z) {
            this.nx = x;
            this.ny = y;
            this.nz = z;
            return this;
        }
    }

    public static final class Builder
    implements MultiBufferSource {
        private final Map<RenderType, Capture> layers = new LinkedHashMap<RenderType, Capture>();

        public VertexConsumer getBuffer(RenderType type) {
            return this.layers.computeIfAbsent(type, ignored -> new Capture());
        }

        public ContraptionMesh build() {
            ArrayList batches = new ArrayList();
            this.layers.forEach((layer, c) -> {
                c.flush();
                if (!c.vertices.isEmpty()) {
                    batches.add(new Batch((RenderType)layer, List.copyOf(c.vertices)));
                }
            });
            if (batches.isEmpty()) {
                return EMPTY;
            }
            double radiusSquared = 0.0;
            for (Batch batch : batches) {
                for (Vertex v : batch.vertices) {
                    radiusSquared = Math.max(radiusSquared, (double)v.x * (double)v.x + (double)v.y * (double)v.y + (double)v.z * (double)v.z);
                }
            }
            return new ContraptionMesh(List.copyOf(batches), Math.sqrt(radiusSquared));
        }
    }
}

