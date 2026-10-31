/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.zigythebird.playeranim.animation.PlayerAnimManager
 *  com.zigythebird.playeranim.api.PlayerAnimationAccess
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.monowire.MonowireCableRenderer;
import com.chromeware.client.monowire.MonowireClient;
import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.chromeware.client.sandevistan.AfterimageRenderContext;
import com.chromeware.client.sandevistan.AfterimageSnapshot;
import com.chromeware.client.sandevistan.AfterimageTimeline;
import com.chromeware.client.sandevistan.ContraptionMesh;
import com.chromeware.client.sandevistan.EchoCache;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.client.sandevistan.SandevistanHistory;
import com.chromeware.client.sandevistan.SandevistanRenderTypes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class SandevistanAfterimages {
    private static final Map<Integer, Trail> TRAILS = new HashMap<Integer, Trail>();
    private static final Map<Integer, AfterimagePlayerEntity> PROXIES = new HashMap<Integer, AfterimagePlayerEntity>();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediate((ByteBufferBuilder)new ByteBufferBuilder(512));
    private static ClientLevel level;

    public static boolean hasTrails() {
        return !TRAILS.isEmpty();
    }

    public static void clear() {
        TRAILS.clear();
        PROXIES.clear();
        level = null;
    }

    static void prepare(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != level) {
            SandevistanAfterimages.clear();
            level = mc.level;
        }
        float tick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        HashSet<Integer> active = new HashSet<Integer>();
        for (AbstractClientPlayer p : mc.level.players()) {
            if (!SandevistanClient.isActive((Entity)p) || p.isInvisible() || p.isSpectator()) continue;
            active.add(p.getId());
            TRAILS.computeIfAbsent(p.getId(), ignored -> new Trail()).tick(p, p.getPosition(tick), tick);
        }
        TRAILS.values().forEach(t -> t.echoes.removeIf(e -> !e.frame.alive()));
        TRAILS.entrySet().removeIf(e -> !active.contains(e.getKey()) && ((Trail)e.getValue()).echoes.isEmpty());
        PROXIES.keySet().removeIf(id -> !TRAILS.containsKey(id) && !active.contains(id));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean renderEchoes(RenderLevelStageEvent event, RenderTarget target) {
        SandevistanRenderTypes.target(target);
        boolean drawn = false;
        for (Map.Entry<Integer, Trail> entry : TRAILS.entrySet()) {
            entry.getValue().echoes.removeIf(e -> !e.frame.alive());
            for (Echo echo : entry.getValue().echoes) {
                EchoCache.keep(echo.mesh);
                Vec3 position = echo.snapshot.position();
                double radius = echo.mesh.radius() + 0.5;
                if (!event.getFrustum().isVisible(new AABB(position.x - radius, position.y - radius, position.z - radius, position.x + radius, position.y + radius, position.z + radius))) continue;
                PoseStack stack = event.getPoseStack();
                Vec3 camera = event.getCamera().getPosition();
                stack.pushPose();
                try {
                    stack.translate(position.x - camera.x, position.y - camera.y, position.z - camera.z);
                    EchoCache.draw(echo.mesh, stack.last(), BUFFERS, echo.frame.color(), true);
                    drawn |= echo.mesh.vertexCount() > 0;
                }
                finally {
                    stack.popPose();
                }
                if (echo.wires.isEmpty()) continue;
                AfterimageRenderContext.Scope scope = AfterimageRenderContext.pushPalette(echo.frame.color());
                try {
                    for (Vec3[] wire : echo.wires) {
                        drawn |= SandevistanAfterimages.drawWire(event, wire);
                    }
                }
                finally {
                    if (scope == null) continue;
                    scope.close();
                }
            }
        }
        BUFFERS.endBatch();
        return drawn;
    }

    static boolean renderLive(RenderLevelStageEvent event, RenderTarget target) {
        SandevistanRenderTypes.target(target);
        Minecraft mc = Minecraft.getInstance();
        float tick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float time = (float)SandevistanClient.visualTimeAt(null, tick);
        boolean drawn = false;
        if (target != null && SandevistanClient.isLocalInsideSandevistanField()) {
            for (AbstractClientPlayer player : mc.level.players()) {
                if (!SandevistanClient.isImmune((Entity)player) || player.isInvisible() || player.isSpectator()) continue;
                if (player == event.getCamera().getEntity() && mc.options.getCameraType().isFirstPerson()) {
                    AfterimageRenderContext.Scope scope = AfterimageRenderContext.push(0xFFFFFF, 1.0f);
                    try {
                        drawn |= SandevistanAfterimages.drawCameraPlayer(event, player, tick);
                        for (Vec3[] wire : MonowireClient.currentWires(player, tick)) {
                            drawn |= SandevistanAfterimages.drawWire(event, wire);
                        }
                        continue;
                    }
                    finally {
                        if (scope != null) {
                            scope.close();
                        }
                        continue;
                    }
                }
                drawn |= SandevistanAfterimages.draw(event, player, AfterimageSnapshot.capture(player, player.getPosition(tick), 0xFFFFFF, 1.0f, time, tick), false);
            }
        }
        return drawn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean drawCameraPlayer(RenderLevelStageEvent event, AbstractClientPlayer player, float tick) {
        PlayerAnimManager manager = PlayerAnimationAccess.getPlayerAnimManager((AbstractClientPlayer)player);
        FirstPersonMode mode = FirstPersonMode.THIRD_PERSON_MODEL;
        if (manager.getFirstPersonMode() != mode || manager.getFirstPersonTransitionProgress() != 1.0f) {
            return false;
        }
        boolean previous = FirstPersonMode.isFirstPersonPass();
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        try {
            FirstPersonMode.setFirstPersonPass((boolean)true);
            Vec3 p = player.getPosition(tick).subtract(event.getCamera().getPosition());
            stack.translate(p.x, p.y, p.z);
            Minecraft.getInstance().getEntityRenderDispatcher().render((Entity)player, 0.0, 0.0, 0.0, player.yBodyRot, tick, stack, layer -> BUFFERS.getBuffer(SandevistanRenderTypes.map(layer)), Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords((Entity)player, tick));
            BUFFERS.endBatch();
            boolean bl = true;
            return bl;
        }
        finally {
            FirstPersonMode.setFirstPersonPass((boolean)previous);
            stack.popPose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean draw(RenderLevelStageEvent event, AbstractClientPlayer player, AfterimageSnapshot echo, boolean palette) {
        Minecraft mc = Minecraft.getInstance();
        AfterimagePlayerEntity proxy = PROXIES.computeIfAbsent(player.getId(), ignored -> new AfterimagePlayerEntity(player));
        Vec3 camera = event.getCamera().getPosition();
        proxy.apply(echo);
        if (!event.getFrustum().isVisible(proxy.getBoundingBox())) {
            return false;
        }
        int color = echo.color();
        try {
            AfterimageRenderContext.Scope scope = palette ? AfterimageRenderContext.pushPalette(color) : AfterimageRenderContext.push(color, echo.opacity());
            try {
                RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)(palette ? 1.0f : echo.opacity()));
                PoseStack stack = event.getPoseStack();
                stack.pushPose();
                try {
                    stack.translate(echo.position().x - camera.x, echo.position().y - camera.y, echo.position().z - camera.z);
                    mc.getEntityRenderDispatcher().render((Entity)proxy, 0.0, 0.0, 0.0, echo.yBodyRot(), proxy.renderTickDelta(echo), stack, layer -> BUFFERS.getBuffer(palette ? SandevistanRenderTypes.palette(layer, color) : SandevistanRenderTypes.map(layer)), 0xF000F0);
                    BUFFERS.endBatch();
                }
                finally {
                    stack.popPose();
                }
                for (Vec3[] wire : echo.wires().points(proxy.wireSockets, proxy.wireGrips)) {
                    SandevistanAfterimages.drawWire(event, wire);
                }
                boolean bl = true;
                if (scope != null) {
                    scope.close();
                }
                return bl;
            }
            catch (Throwable throwable) {
                if (scope != null) {
                    try {
                        scope.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
        finally {
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    private static boolean drawWire(RenderLevelStageEvent event, Vec3[] points) {
        if (points == null || points.length < 2) {
            return false;
        }
        AfterimageRenderContext.State context = AfterimageRenderContext.current();
        RenderType layer = context != null && context.palette() ? SandevistanRenderTypes.palette(MonowireCableRenderer.TYPE, context.color()) : SandevistanRenderTypes.map(MonowireCableRenderer.TYPE);
        MonowireCableRenderer.curve(event.getPoseStack().last(), BUFFERS.getBuffer(layer), points, event.getCamera().getPosition());
        BUFFERS.endBatch();
        return true;
    }

    private static Echo capture(AbstractClientPlayer player, AfterimageSnapshot snapshot, AfterimageTimeline.Frame frame) {
        AfterimagePlayerEntity proxy = PROXIES.computeIfAbsent(player.getId(), ignored -> new AfterimagePlayerEntity(player));
        proxy.apply(snapshot);
        proxy.captureOrigin = snapshot.position();
        ContraptionMesh.Builder builder = new ContraptionMesh.Builder();
        try {
            AfterimageRenderContext.Scope scope = AfterimageRenderContext.pushPalette(frame.color());
            try {
                Minecraft.getInstance().getEntityRenderDispatcher().render((Entity)proxy, 0.0, 0.0, 0.0, snapshot.yBodyRot(), proxy.renderTickDelta(snapshot), new PoseStack(), (MultiBufferSource)builder, 0xF000F0);
                List<Vec3[]> wires = snapshot.wires().points(proxy.wireSockets, proxy.wireGrips);
                Echo echo = new Echo(snapshot, frame, builder.build(), wires);
                if (scope != null) {
                    scope.close();
                }
                return echo;
            }
            catch (Throwable throwable) {
                if (scope != null) {
                    try {
                        scope.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
        finally {
            proxy.captureOrigin = null;
        }
    }

    private static final class Trail {
        private final Deque<Echo> echoes = new ArrayDeque<Echo>();
        private final AfterimageDistance distance = new AfterimageDistance();
        private long generation = -1L;

        private Trail() {
        }

        private void tick(AbstractClientPlayer player, Vec3 position, float tickDelta) {
            Vec3 placed;
            AfterimageTimeline timeline = SandevistanHistory.TIMELINE;
            UUID owner = player.getUUID();
            long slot = timeline.slot(owner);
            long currentGeneration = timeline.generation(owner);
            if (this.generation != currentGeneration) {
                this.generation = currentGeneration;
                this.distance.reset();
            }
            if ((placed = this.distance.sample(slot, position)) == null) {
                return;
            }
            AfterimageTimeline.Frame frame = timeline.frame(owner);
            AfterimageSnapshot snapshot = AfterimageSnapshot.capture(player, placed, frame.color(), 1.0f, (float)frame.time(), tickDelta);
            Echo echo = SandevistanAfterimages.capture(player, snapshot, frame);
            this.echoes.addLast(echo);
            timeline.add(frame, echo.mesh.vertexCount() + echo.wires.stream().mapToInt(wire -> ((Vec3[])wire).length * 12).sum());
        }
    }

    private record Echo(AfterimageSnapshot snapshot, AfterimageTimeline.Frame frame, ContraptionMesh mesh, List<Vec3[]> wires) {
    }

    static final class AfterimageDistance {
        private Vec3 previous;
        private Vec3 direction = Vec3.ZERO;
        private double distance;
        private long emitted = -1L;

        AfterimageDistance() {
        }

        Vec3 sample(long slot, Vec3 position) {
            if (this.previous == null) {
                this.previous = position;
                return null;
            }
            Vec3 movement = position.subtract(this.previous);
            this.previous = position;
            double step = movement.length();
            if (step > 8.0) {
                this.distance = 0.0;
                this.direction = Vec3.ZERO;
                return null;
            }
            if (step > 1.0E-5) {
                this.distance += step;
                this.direction = movement.scale(1.0 / step);
            }
            if (slot < 0L || slot == this.emitted || this.distance < 0.86) {
                return null;
            }
            this.emitted = slot;
            this.distance %= 0.86;
            return position.subtract(this.direction.scale(0.4));
        }

        void reset() {
            this.previous = null;
            this.direction = Vec3.ZERO;
            this.distance = 0.0;
            this.emitted = -1L;
        }
    }
}

