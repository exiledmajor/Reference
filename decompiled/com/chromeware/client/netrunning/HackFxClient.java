/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 *  net.neoforged.neoforge.client.event.RenderLivingEvent$Pre
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.netrunning;

import com.chromeware.client.monowire.MonowireCableRenderer;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.netrunning.HackFx;
import com.chromeware.netrunning.Quickhack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class HackFxClient {
    private static final Map<Integer, Shake> SHAKES = new HashMap<Integer, Shake>();
    private static final List<Burst> BURSTS = new ArrayList<Burst>();
    private static final EnumMap<Quickhack, Overlay> OVERLAYS = new EnumMap(Quickhack.class);
    private static final String GLYPHS = "#%&@$01ABCDEF<>/\\|=+*?!";
    private static Object lastLevel;

    public static void register() {
        HackFx.receiver = HackFxClient::receive;
        NeoForge.EVENT_BUS.addListener(HackFxClient::world);
        NeoForge.EVENT_BUS.addListener(event -> HackFxClient.screen(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false)));
        NeoForge.EVENT_BUS.addListener(event -> HackFxClient.tick());
        NeoForge.EVENT_BUS.addListener(HackFxClient::convulse);
    }

    private static void receive(HackFx.Fx fx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        long now = mc.level.getGameTime();
        Quickhack hack = Quickhack.values()[Mth.clamp((int)fx.hack(), (int)0, (int)(Quickhack.values().length - 1))];
        int color = HackFx.color(hack);
        switch (fx.kind()) {
            case 0: {
                long seed = mc.level.random.nextLong();
                switch (hack) {
                    case SHORT_CIRCUIT: {
                        SHAKES.put(fx.to(), new Shake(now, now + 14L, 0.07f));
                        break;
                    }
                    case OVERHEAT: {
                        SHAKES.put(fx.to(), new Shake(now, now + 40L, 0.03f));
                        break;
                    }
                    case CONTAGION: {
                        SHAKES.put(fx.to(), new Shake(now, now + 10L, 0.04f));
                        break;
                    }
                    case BLACKWALL_GATEWAY: {
                        SHAKES.put(fx.to(), new Shake(now, now + 30L, 0.09f));
                        break;
                    }
                }
                if (hack == Quickhack.SHORT_CIRCUIT) {
                    BURSTS.add(new Burst(Shape.ARCS, fx.to(), null, null, color, now, fx.ticks(), seed));
                }
                if (hack == Quickhack.BLACKWALL_GATEWAY) {
                    BURSTS.add(new Burst(Shape.TENDRILS, fx.to(), null, null, color, now, 30, seed));
                }
                if (mc.player == null || fx.to() != mc.player.getId() || fx.ticks() <= 0) break;
                OVERLAYS.put(hack, new Overlay(now, now + (long)fx.ticks()));
                break;
            }
            case 1: {
                Entity from = mc.level.getEntity(fx.from());
                Entity to = mc.level.getEntity(fx.to());
                if (from == null || to == null) {
                    return;
                }
                Shape shape = hack == Quickhack.CONTAGION ? Shape.PACKETS : Shape.BOLT;
                BURSTS.add(new Burst(shape, -1, HackFxClient.centre(from, 1.0f), HackFxClient.centre(to, 1.0f), color, now, shape == Shape.PACKETS ? 16 : 10, mc.level.random.nextLong()));
                break;
            }
        }
    }

    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != lastLevel) {
            SHAKES.clear();
            BURSTS.clear();
            OVERLAYS.clear();
            lastLevel = mc.level;
        }
        if (mc.level == null) {
            return;
        }
        long now = mc.level.getGameTime();
        SHAKES.values().removeIf(s -> s.until <= now);
        BURSTS.removeIf(b -> b.start + (long)b.life <= now || b.target >= 0 && mc.level.getEntity(b.target) == null);
        OVERLAYS.values().removeIf(o -> o.until <= now);
        if (mc.player != null && OVERLAYS.containsKey((Object)Quickhack.OVERHEAT) && !mc.player.isOnFire() && now - HackFxClient.OVERLAYS.get((Object)((Object)Quickhack.OVERHEAT)).start > 10L) {
            OVERLAYS.remove((Object)Quickhack.OVERHEAT);
        }
        if (mc.player != null && OVERLAYS.containsKey((Object)Quickhack.CONTAGION) && !mc.player.hasEffect(MobEffects.POISON) && now - HackFxClient.OVERLAYS.get((Object)((Object)Quickhack.CONTAGION)).start > 10L) {
            OVERLAYS.remove((Object)Quickhack.CONTAGION);
        }
        if (mc.player == null || !mc.player.isAlive()) {
            OVERLAYS.keySet().removeIf(h -> h != Quickhack.BLACKWALL_GATEWAY);
        }
    }

    private static Vec3 centre(Entity e, float partial) {
        return e.getPosition(partial).add(0.0, (double)e.getBbHeight() * 0.55, 0.0);
    }

    private static void world(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || BURSTS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        long now = mc.level.getGameTime();
        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(MonowireCableRenderer.TYPE);
        Vec3 eye = event.getCamera().getPosition();
        for (Burst burst : BURSTS) {
            float age = (float)(now - burst.start) + partial;
            float fade = Mth.clamp((float)(1.0f - age / (float)burst.life), (float)0.0f, (float)1.0f);
            if (burst.shape == Shape.BOLT || burst.shape == Shape.PACKETS) {
                if (burst.shape == Shape.BOLT) {
                    HackFxClient.bolt(pose, buffer, eye, burst.a, burst.b, burst.color, fade, burst.seed + now, 0.22);
                    continue;
                }
                HackFxClient.packets(pose, buffer, eye, burst.a, burst.b, burst.color, age / (float)burst.life);
                continue;
            }
            Entity target = mc.level.getEntity(burst.target);
            if (target == null) continue;
            Vec3 at = HackFxClient.centre(target, partial);
            switch (burst.shape.ordinal()) {
                case 0: {
                    HackFxClient.arcs(pose, buffer, eye, target, at, burst.color, fade, burst.seed + now / 2L);
                    break;
                }
                case 1: {
                    HackFxClient.tendrils(pose, buffer, eye, target, at, burst.color, fade, age, burst.seed, now);
                    break;
                }
            }
        }
        buffers.endBatch(MonowireCableRenderer.TYPE);
    }

    private static void convulse(RenderLivingEvent.Pre<?, ?> event) {
        Shake shake = SHAKES.get(event.getEntity().getId());
        ClientLevel level = Minecraft.getInstance().level;
        if (shake == null || level == null) {
            return;
        }
        long now = level.getGameTime();
        float fade = Mth.clamp((float)(((float)(shake.until - now) - event.getPartialTick()) / (float)Math.max(1L, shake.until - shake.start)), (float)0.0f, (float)1.0f);
        RandomSource random = RandomSource.create((long)((long)event.getEntity().getId() * 31L + now));
        float s = shake.strength * fade * event.getEntity().getBbHeight() / 1.8f;
        event.getPoseStack().translate((random.nextFloat() - 0.5f) * 2.0f * s, random.nextFloat() * s * 0.5f, (random.nextFloat() - 0.5f) * 2.0f * s);
        event.getPoseStack().mulPose(Axis.YP.rotationDegrees((random.nextFloat() - 0.5f) * 60.0f * s));
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees((random.nextFloat() - 0.5f) * 40.0f * s));
    }

    private static void arcs(PoseStack.Pose pose, VertexConsumer buffer, Vec3 eye, Entity target, Vec3 at, int color, float fade, long seed) {
        RandomSource random = RandomSource.create((long)seed);
        double w = (double)target.getBbWidth() * 0.6;
        double h = (double)target.getBbHeight() * 0.5;
        for (int arc = 0; arc < 4; ++arc) {
            Vec3 a = at.add((random.nextDouble() - 0.5) * 2.0 * w, (random.nextDouble() - 0.5) * 2.0 * h, (random.nextDouble() - 0.5) * 2.0 * w);
            Vec3 b = at.add((random.nextDouble() - 0.5) * 2.0 * w, (random.nextDouble() - 0.5) * 2.0 * h, (random.nextDouble() - 0.5) * 2.0 * w);
            HackFxClient.jagged(pose, buffer, eye, a, b, color, fade, random, 0.09, 7, 0.022, 0.007);
        }
    }

    private static void tendrils(PoseStack.Pose pose, VertexConsumer buffer, Vec3 eye, Entity target, Vec3 at, int color, float fade, float age, long seed, long frame) {
        RandomSource shape = RandomSource.create((long)seed);
        RandomSource jitter = RandomSource.create((long)(seed ^ frame * 31L));
        double reach = (1.1 + (double)target.getBbWidth()) * (double)Math.min(1.0f, age / 8.0f);
        for (int t = 0; t < 7; ++t) {
            Vec3 dir = new Vec3(shape.nextDouble() - 0.5, shape.nextDouble() * 0.9 - 0.35, shape.nextDouble() - 0.5).normalize();
            Vec3 end = at.add(dir.scale(reach * (0.7 + 0.5 * shape.nextDouble())));
            float flicker = jitter.nextFloat() < 0.15f ? 0.2f : 1.0f;
            HackFxClient.jagged(pose, buffer, eye, at, end, 5898248, fade * flicker, jitter, 0.14, 8, 0.05, 0.02);
            HackFxClient.jagged(pose, buffer, eye, at, end, color, fade * flicker, jitter, 0.1, 8, 0.018, 0.006);
        }
        double radius = 0.45 + (double)target.getBbWidth() * 0.8;
        for (int i = 0; i < 26; ++i) {
            Vec3 n = new Vec3(jitter.nextDouble() - 0.5, jitter.nextDouble() - 0.5, jitter.nextDouble() - 0.5).normalize();
            Vec3 p = at.add(n.scale(radius));
            Vec3 q = p.add(new Vec3(jitter.nextDouble() - 0.5, jitter.nextDouble() - 0.5, jitter.nextDouble() - 0.5).scale(0.18));
            HackFxClient.segment(pose, buffer, eye, p, q, 0.012, HackFxClient.argb(color, 0.8f * fade));
        }
    }

    private static void bolt(PoseStack.Pose pose, VertexConsumer buffer, Vec3 eye, Vec3 a, Vec3 b, int color, float fade, long seed, double spread) {
        RandomSource random = RandomSource.create((long)seed);
        HackFxClient.jagged(pose, buffer, eye, a, b, color, fade, random, spread, 12, 0.05, 0.014);
    }

    private static void packets(PoseStack.Pose pose, VertexConsumer buffer, Vec3 eye, Vec3 a, Vec3 b, int color, float age) {
        for (int i = 0; i < 4; ++i) {
            float f = age * 1.6f - (float)i * 0.2f;
            if (f < 0.0f || f > 1.0f) continue;
            Vec3 at = a.lerp(b, (double)f).add(0.0, Math.sin((double)f * Math.PI) * 0.4, 0.0);
            HackFxClient.dot(pose, buffer, eye, at, 0.09, HackFxClient.argb(color, 0.3f));
            HackFxClient.dot(pose, buffer, eye, at, 0.035, HackFxClient.argb(color, 0.95f));
        }
        HackFxClient.segment(pose, buffer, eye, a, b, 0.006, HackFxClient.argb(color, 0.25f * (1.0f - age)));
    }

    private static void jagged(PoseStack.Pose pose, VertexConsumer buffer, Vec3 eye, Vec3 a, Vec3 b, int color, float fade, RandomSource random, double spread, int steps, double glow, double core) {
        Vec3 previous = a;
        for (int i = 1; i <= steps; ++i) {
            Vec3 next = a.lerp(b, (double)i / (double)steps);
            if (i < steps) {
                next = next.add((random.nextDouble() - 0.5) * spread, (random.nextDouble() - 0.5) * spread, (random.nextDouble() - 0.5) * spread);
            }
            HackFxClient.segment(pose, buffer, eye, previous, next, glow, HackFxClient.argb(color, 0.3f * fade));
            HackFxClient.segment(pose, buffer, eye, previous, next, core, HackFxClient.argb(0xFFFFFF, 0.75f * fade));
            HackFxClient.segment(pose, buffer, eye, previous, next, core * 1.8, HackFxClient.argb(color, 0.8f * fade));
            previous = next;
        }
    }

    private static void segment(PoseStack.Pose pose, VertexConsumer buffer, Vec3 eye, Vec3 p, Vec3 q, double width, int color) {
        Vec3 side = q.subtract(p).cross(eye.subtract(p));
        if (side.lengthSqr() < 1.0E-10) {
            return;
        }
        side = side.normalize().scale(width);
        HackFxClient.vertex(pose, buffer, p.add(side).subtract(eye), color);
        HackFxClient.vertex(pose, buffer, q.add(side).subtract(eye), color);
        HackFxClient.vertex(pose, buffer, q.subtract(side).subtract(eye), color);
        HackFxClient.vertex(pose, buffer, p.subtract(side).subtract(eye), color);
    }

    private static void dot(PoseStack.Pose pose, VertexConsumer buffer, Vec3 eye, Vec3 at, double radius, int color) {
        Vec3 view = eye.subtract(at).normalize();
        Vec3 right = view.cross(Math.abs(view.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0)).normalize().scale(radius);
        Vec3 up = right.cross(view);
        Vec3 c = at.subtract(eye);
        HackFxClient.vertex(pose, buffer, c.subtract(right).subtract(up), color);
        HackFxClient.vertex(pose, buffer, c.add(right).subtract(up), color);
        HackFxClient.vertex(pose, buffer, c.add(right).add(up), color);
        HackFxClient.vertex(pose, buffer, c.subtract(right).add(up), color);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 p, int color) {
        buffer.addVertex(pose.pose(), (float)p.x, (float)p.y, (float)p.z).setColor(color);
    }

    private static int argb(int rgb, float alpha) {
        return Mth.clamp((int)((int)(alpha * 255.0f)), (int)0, (int)255) << 24 | rgb & 0xFFFFFF;
    }

    private static void screen(GuiGraphics g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (OVERLAYS.isEmpty() || mc.level == null || mc.player == null || !ChromewareConfig.quickhackVfx() || mc.getCameraEntity() != mc.player) {
            return;
        }
        long now = mc.level.getGameTime();
        float time = (float)(now % 72000L) + partial;
        int w = g.guiWidth();
        int h = g.guiHeight();
        RandomSource random = RandomSource.create((long)(now * 7919L));
        block6: for (Map.Entry<Quickhack, Overlay> entry : OVERLAYS.entrySet()) {
            Overlay o = entry.getValue();
            float elapsed = (float)(now - o.start) + partial;
            float f = Mth.clamp((float)(elapsed / (float)Math.max(1L, o.until - o.start)), (float)0.0f, (float)1.0f);
            switch (entry.getKey()) {
                case SHORT_CIRCUIT: {
                    int i;
                    float strength = (1.0f - f) * (1.0f - f);
                    g.fill(0, 0, w, h, HackFxClient.argb(15267583, 0.45f * strength));
                    for (i = 0; i < 26; ++i) {
                        int y = random.nextInt(h);
                        int x = random.nextInt(w);
                        int len = 20 + random.nextInt(w / 2);
                        g.fill(x, y, Math.min(w, x + len), y + 1 + random.nextInt(2), HackFxClient.argb(0xFFFFFF, 0.5f * strength));
                    }
                    for (i = 0; i < 4; ++i) {
                        int x = random.nextInt(w);
                        int y = random.nextInt(h);
                        g.fill(x, y, x + 10 + random.nextInt(50), y + 3 + random.nextInt(8), HackFxClient.argb(10148095, 0.35f * strength));
                    }
                    continue block6;
                }
                case OVERHEAT: {
                    HackFxClient.edges(g, w, h, 16734736, 0.3f + 0.08f * Mth.sin((float)(time * 0.35f)));
                    break;
                }
                case CONTAGION: {
                    g.fill(0, 0, w, h, HackFxClient.argb(5222448, 0.05f));
                    HackFxClient.edges(g, w, h, 5230640, 0.26f + 0.1f * Mth.sin((float)(time * 0.18f)));
                    break;
                }
                case BLACKWALL_GATEWAY: {
                    int i;
                    float strength = Math.min(1.0f, elapsed / 10.0f);
                    g.fill(0, 0, w, h, HackFxClient.argb(9043984, 0.12f + 0.3f * strength));
                    HackFxClient.edges(g, w, h, 0x30000A, 0.7f * strength);
                    for (i = 0; i < 40; ++i) {
                        String glyph = String.valueOf(GLYPHS.charAt(random.nextInt(GLYPHS.length())));
                        g.drawString(mc.font, glyph, random.nextInt(w), random.nextInt(h), HackFxClient.argb(random.nextFloat() < 0.2f ? 0xFFFFFF : 16719936, 0.9f * strength), false);
                    }
                    for (i = 0; i < 3; ++i) {
                        int y = random.nextInt(h);
                        g.fill(0, y, w, y + 2 + random.nextInt(6), HackFxClient.argb(16715824, 0.25f * strength));
                    }
                    continue block6;
                }
            }
        }
    }

    private static void edges(GuiGraphics g, int w, int h, int rgb, float strength) {
        int bands = 14;
        int step = Math.max(1, Math.min(w, h) / 56);
        for (int i = 0; i < bands; ++i) {
            float a = strength * (1.0f - (float)i / (float)bands) * (1.0f - (float)i / (float)bands);
            int c = HackFxClient.argb(rgb, a);
            int inset = i * step;
            g.fill(inset, inset, w - inset, inset + step, c);
            g.fill(inset, h - inset - step, w - inset, h - inset, c);
            g.fill(inset, inset + step, inset + step, h - inset - step, c);
            g.fill(w - inset - step, inset + step, w - inset, h - inset - step, c);
        }
    }

    private record Shake(long start, long until, float strength) {
    }

    private record Burst(Shape shape, int target, Vec3 a, Vec3 b, int color, long start, int life, long seed) {
    }

    private static enum Shape {
        ARCS,
        TENDRILS,
        BOLT,
        PACKETS;

    }

    private record Overlay(long start, long until) {
    }
}

