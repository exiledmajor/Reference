/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Window
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.AbstractTickableSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec2
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 */
package com.chromeware.client.netrunning;

import com.chromeware.netrunning.NetrunningPackets;
import com.chromeware.netrunning.NetrunningSounds;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public final class PingMarkers {
    private static final ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"textures/gui/ping_arrow.png");
    private static final int WHITE = -1;
    private static final int SHADOW_BLACK = FastColor.ARGB32.color((int)64, (int)0, (int)0, (int)0);
    private static final float PING_SIZE = 1.0f;
    private static final int SAFE_LEFT = 5;
    private static final int SAFE_RIGHT = 5;
    private static final int SAFE_TOP = 5;
    private static final int SAFE_BOTTOM = 60;
    private static final Map<UUID, Marker> MARKERS = new LinkedHashMap<UUID, Marker>();

    public static void receive(NetrunningPackets.Marker packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Marker marker = new Marker();
        marker.pos = new Vec3(packet.x(), packet.y(), packet.z());
        marker.entity = packet.entity();
        marker.until = mc.level.getGameTime() + (long)packet.ticks();
        MARKERS.put(packet.key(), marker);
        mc.getSoundManager().play((SoundInstance)new DirectionalSound(marker.pos));
    }

    public static void clear() {
        MARKERS.clear();
    }

    public static void project(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || MARKERS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        long now = mc.level.getGameTime();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Camera camera = event.getCamera();
        Iterator<Marker> it = MARKERS.values().iterator();
        while (it.hasNext()) {
            Entity followed;
            Marker marker = it.next();
            if (marker.until <= now) {
                it.remove();
                continue;
            }
            Entity entity = followed = marker.entity < 0 ? null : mc.level.getEntity(marker.entity);
            if (followed != null) {
                marker.pos = followed.getPosition(partial).add(0.0, followed.getBoundingBox().getYsize(), 0.0);
            }
            marker.screen = PingMarkers.worldToScreen(marker.pos, event.getModelViewMatrix(), event.getProjectionMatrix(), camera.getPosition());
            marker.distance = camera.getPosition().distanceTo(marker.pos);
            marker.scale = (float)Math.max(1.0, 2.0 / Math.pow(marker.distance, 0.3)) * 0.5f * 1.0f;
        }
    }

    private static ScreenPos worldToScreen(Vec3 worldPos, Matrix4f modelView, Matrix4f projection, Vec3 camera) {
        Window window = Minecraft.getInstance().getWindow();
        Vector4f rel = new Vector4f((Vector3fc)camera.reverse().add(worldPos).toVector3f(), 1.0f);
        rel.mul((Matrix4fc)modelView);
        rel.mul((Matrix4fc)projection);
        float depth = rel.w;
        if (depth != 0.0f) {
            rel.div(depth);
        }
        return new ScreenPos((float)window.getGuiScaledWidth() * (0.5f + rel.x * 0.5f), (float)window.getGuiScaledHeight() * (0.5f - rel.y * 0.5f), depth);
    }

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || MARKERS.isEmpty()) {
            return;
        }
        Window wnd = mc.getWindow();
        Vec2 screenSize = new Vec2((float)wnd.getGuiScaledWidth(), (float)wnd.getGuiScaledHeight());
        Vec2 topLeft = new Vec2(5.0f, 5.0f);
        Vec2 bottomRight = new Vec2(screenSize.x - 5.0f, screenSize.y - 60.0f);
        Vec2 safeCenter = new Vec2((bottomRight.x - topLeft.x) * 0.5f, (bottomRight.y - topLeft.y) * 0.5f);
        PoseStack m = g.pose();
        m.pushPose();
        m.translate(0.0f, 0.0f, (float)(-MARKERS.size()) * 16.0f);
        for (Marker marker : MARKERS.values()) {
            if (marker.screen == null) continue;
            m.translate(0.0f, 0.0f, 16.0f);
            PingMarkers.indicator(g, marker, screenSize, topLeft, bottomRight, safeCenter);
            if (marker.screen.behindCamera()) continue;
            PingMarkers.location(g, marker);
        }
        m.popPose();
    }

    private static void location(GuiGraphics g, Marker marker) {
        PoseStack m = g.pose();
        m.pushPose();
        m.translate(marker.screen.x, marker.screen.y, 0.0f);
        m.scale(marker.scale, marker.scale, 1.0f);
        PingMarkers.label(g, (Component)Component.literal((String)"%,.1f m".formatted(marker.distance)), -1.5f, -1);
        PingMarkers.diamond(g, -1);
        m.popPose();
    }

    private static void indicator(GuiGraphics g, Marker marker, Vec2 screenSize, Vec2 topLeft, Vec2 bottomRight, Vec2 safeCenter) {
        ScreenPos screen = marker.screen;
        boolean behind = screen.behindCamera();
        if (!behind && screen.inBounds(Vec2.ZERO, screenSize)) {
            return;
        }
        Vec2 direction = new Vec2(screen.x - topLeft.x - safeCenter.x, screen.y - topLeft.y - safeCenter.y);
        if (behind) {
            direction = direction.scale(-1.0f);
        }
        float angle = (float)Math.atan2(direction.y, direction.x);
        Vec2 edge = PingMarkers.edge(angle, topLeft, bottomRight);
        double offsetX = Math.cos((double)angle + Math.PI) * 12.0;
        double offsetY = Math.sin((double)angle + Math.PI) * 12.0;
        PoseStack m = g.pose();
        m.pushPose();
        m.translate(edge.x, edge.y, 0.0f);
        m.pushPose();
        m.scale(marker.scale, marker.scale, 1.0f);
        m.translate(offsetX, offsetY, 0.0);
        PingMarkers.diamond(g, -1);
        m.popPose();
        m.pushPose();
        m.mulPose(new Matrix4f().rotateZ(angle));
        m.scale(1.0f, 1.0f, 1.0f);
        m.scale(0.25f, 0.25f, 1.0f);
        m.translate(-5.0f, 0.0f, 0.0f);
        PingMarkers.texture(g, ARROW, 10, -1);
        m.popPose();
        m.popPose();
    }

    private static Vec2 edge(float angle, Vec2 leftTop, Vec2 rightBottom) {
        float width = rightBottom.x - leftTop.x;
        float height = rightBottom.y - leftTop.y;
        Vec3 direction = new Vec3(Math.cos(angle), Math.sin(angle), 0.0).multiply((double)(1.0f / width), (double)(1.0f / height), 0.0);
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        double cx = width * 0.5f;
        double cy = height * 0.5f;
        if (Math.abs(direction.x) < Math.abs(direction.y)) {
            if (direction.y < 0.0) {
                return new Vec2((float)(cx + -cy / dy * dx) + leftTop.x, leftTop.y);
            }
            return new Vec2((float)(cx + cy / dy * dx) + leftTop.x, rightBottom.y);
        }
        if (direction.x < 0.0) {
            return new Vec2(leftTop.x, (float)(cy + -cx / dx * dy) + leftTop.y);
        }
        return new Vec2(rightBottom.x, (float)(cy + cx / dx * dy) + leftTop.y);
    }

    private static void label(GuiGraphics g, Component text, float yOffset, int color) {
        Font font = Minecraft.getInstance().font;
        float f = font.width((FormattedText)text);
        Objects.requireNonNull(font);
        Vec2 metrics = new Vec2(f, 9.0f);
        Vec2 offset = metrics.scale(-0.5f).add(new Vec2(0.0f, metrics.y * yOffset));
        PoseStack m = g.pose();
        m.pushPose();
        m.translate(offset.x, offset.y, 0.0f);
        g.fill(-2, -2, (int)metrics.x + 1, (int)metrics.y, SHADOW_BLACK);
        g.drawString(font, text, 0, 0, color, false);
        m.popPose();
    }

    private static void diamond(GuiGraphics g, int color) {
        PoseStack m = g.pose();
        m.pushPose();
        m.mulPose(new Matrix4f().rotateZ(0.7853982f));
        m.translate(-2.5, -2.5, 0.0);
        g.fill(0, 0, 5, 5, color);
        m.popPose();
    }

    private static void texture(GuiGraphics g, ResourceLocation texture, int size, int color) {
        int offset = size / -2;
        RenderSystem.setShaderColor((float)((float)FastColor.ARGB32.red((int)color) / 255.0f), (float)((float)FastColor.ARGB32.green((int)color) / 255.0f), (float)((float)FastColor.ARGB32.blue((int)color) / 255.0f), (float)((float)FastColor.ARGB32.alpha((int)color) / 255.0f));
        RenderSystem.enableBlend();
        g.blit(texture, offset, offset, 0, 0.0f, 0.0f, size, size, size, size);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private static final class Marker {
        Vec3 pos;
        int entity;
        long until;
        ScreenPos screen;
        double distance;
        float scale;

        private Marker() {
        }
    }

    private static final class DirectionalSound
    extends AbstractTickableSoundInstance {
        private final Vec3 pos;

        DirectionalSound(Vec3 pos) {
            super(NetrunningSounds.PING, SoundSource.MASTER, RandomSource.create());
            this.volume = 1.0f;
            this.pitch = 1.0f;
            this.pos = pos;
            this.tick();
        }

        public void tick() {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                this.stop();
                return;
            }
            Vec3 playerPos = player.position();
            Vec3 between = playerPos.vectorTo(this.pos);
            double mapped = Math.min(between.length(), 64.0) / 64.0 * 14.0;
            Vec3 at = playerPos.add(between.normalize().scale(mapped));
            this.x = at.x;
            this.y = at.y;
            this.z = at.z;
        }
    }

    private record ScreenPos(float x, float y, float depth) {
        boolean behindCamera() {
            return this.depth <= 0.0f;
        }

        boolean inBounds(Vec2 topLeft, Vec2 bottomRight) {
            return this.x >= topLeft.x && this.x <= bottomRight.x && this.y >= topLeft.y && this.y <= bottomRight.y;
        }
    }
}

