/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.MeshData
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexBuffer
 *  com.mojang.blaze3d.vertex.VertexBuffer$Usage
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.ShaderInstance
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.ContraptionMesh;
import com.chromeware.client.sandevistan.SandevistanRenderTypes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;

final class EchoCache {
    private static final long MEMORY_BUDGET = 0x4000000L;
    private static final EchoResourceCache<ContraptionMesh, Uploaded> CACHE = new EchoResourceCache(0x4000000L, u -> u.bytes, Uploaded::close);

    EchoCache() {
    }

    static void beginFrame() {
        CACHE.beginFrame();
    }

    static void keep(ContraptionMesh mesh) {
        CACHE.retain(mesh);
    }

    static void clear() {
        CACHE.clear();
    }

    static void endFrame() {
        CACHE.endFrame();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void draw(ContraptionMesh mesh, PoseStack.Pose pose, MultiBufferSource.BufferSource fallback, int color, boolean historical) {
        EchoCache.keep(mesh);
        if (mesh.vertexCount() == 0) {
            return;
        }
        Uploaded uploaded = CACHE.acquire(mesh, Uploaded::new);
        for (ContraptionMesh.Batch batch : mesh.batches()) {
            RenderType layer = SandevistanRenderTypes.cached(batch.layer(), historical);
            if (layer == null) {
                ContraptionMesh remaining = new ContraptionMesh(List.of(batch), mesh.radius());
                if (historical) {
                    remaining.drawPalette(pose, (MultiBufferSource)fallback, color);
                    continue;
                }
                remaining.draw(pose, (MultiBufferSource)fallback, color, 1.0f, false);
                continue;
            }
            GpuBatch gpu = uploaded.batches.stream().filter(b -> b.source == batch.layer()).findFirst().orElse(null);
            if (gpu == null) {
                VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
                try {
                    ByteBufferBuilder storage = new ByteBufferBuilder(Math.max(256, batch.vertices().size() * layer.format().getVertexSize()));
                    try {
                        BufferBuilder builder = new BufferBuilder(storage, layer.mode(), layer.format());
                        for (ContraptionMesh.Vertex vertex : batch.vertices()) {
                            builder.addVertex(vertex.x(), vertex.y(), vertex.z()).setColor(vertex.color());
                            if (layer.format() == DefaultVertexFormat.POSITION_COLOR) continue;
                            builder.setUv(vertex.u(), vertex.v()).setOverlay(vertex.overlay()).setLight(vertex.light()).setNormal(vertex.nx(), vertex.ny(), vertex.nz());
                        }
                        MeshData data = builder.build();
                        if (data == null) {
                            vbo.close();
                            continue;
                        }
                        vbo.bind();
                        vbo.upload(data);
                    }
                    finally {
                        storage.close();
                        continue;
                    }
                }
                catch (Error | RuntimeException failure) {
                    vbo.close();
                    throw failure;
                }
                finally {
                    VertexBuffer.unbind();
                    continue;
                }
                gpu = new GpuBatch(batch.layer(), vbo);
                uploaded.batches.add(gpu);
                long size = (long)batch.vertices().size() * (long)layer.format().getVertexSize();
                uploaded.bytes += size;
            }
            fallback.endBatch();
            layer.setupRenderState();
            ShaderInstance shader = SandevistanRenderTypes.prepareCached(batch.layer(), pose, color, historical);
            try {
                gpu.buffer.bind();
                gpu.buffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), shader);
            }
            finally {
                VertexBuffer.unbind();
                SandevistanRenderTypes.resetCached(shader);
                layer.clearRenderState();
            }
        }
    }

    static final class EchoResourceCache<K, V> {
        private final long budget;
        private final ToLongFunction<V> size;
        private final Consumer<V> dispose;
        private final Map<K, Entry<V>> entries = new IdentityHashMap<K, Entry<V>>();
        private final Set<K> retained = Collections.newSetFromMap(new IdentityHashMap());
        private long frame;

        EchoResourceCache(long budget, ToLongFunction<V> size, Consumer<V> dispose) {
            this.budget = budget;
            this.size = size;
            this.dispose = dispose;
        }

        void beginFrame() {
            ++this.frame;
            this.retained.clear();
        }

        void retain(K key) {
            this.retained.add(key);
        }

        V acquire(K key, Supplier<V> factory) {
            this.retain(key);
            Entry entry = this.entries.computeIfAbsent(key, ignored -> new Entry(factory.get()));
            entry.used = this.frame;
            return entry.value;
        }

        long bytes() {
            return this.entries.values().stream().mapToLong(e -> this.size.applyAsLong(e.value)).sum();
        }

        void endFrame() {
            Map.Entry oldest;
            Iterator<Map.Entry<K, Entry<V>>> iterator = this.entries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<K, Entry<V>> entry = iterator.next();
                if (this.retained.contains(entry.getKey())) continue;
                this.dispose.accept(entry.getValue().value);
                iterator.remove();
            }
            while (this.bytes() > this.budget && this.entries.size() > 1 && (oldest = (Map.Entry)this.entries.entrySet().stream().filter(e -> ((Entry)e.getValue()).used < this.frame).min(Comparator.comparingLong(e -> ((Entry)e.getValue()).used)).orElse(null)) != null) {
                this.dispose.accept(((Entry)oldest.getValue()).value);
                this.entries.remove(oldest.getKey());
            }
        }

        void clear() {
            this.entries.values().forEach(e -> this.dispose.accept(e.value));
            this.entries.clear();
            this.retained.clear();
            this.frame = 0L;
        }

        private static final class Entry<V> {
            final V value;
            long used;

            Entry(V value) {
                this.value = value;
            }
        }
    }

    private static final class Uploaded
    implements AutoCloseable {
        final List<GpuBatch> batches = new ArrayList<GpuBatch>();
        long bytes;

        private Uploaded() {
        }

        @Override
        public void close() {
            this.batches.forEach(b -> b.buffer.close());
        }
    }

    private record GpuBatch(RenderType source, VertexBuffer buffer) {
    }
}

