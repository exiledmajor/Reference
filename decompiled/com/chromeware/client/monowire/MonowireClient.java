/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Camera
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 *  net.neoforged.neoforge.client.settings.IKeyConflictContext
 *  net.neoforged.neoforge.client.settings.KeyConflictContext
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package com.chromeware.client.monowire;

import com.chromeware.client.cyberware.ArmInput;
import com.chromeware.client.monowire.MonowireAnimation;
import com.chromeware.client.monowire.MonowireCableRenderer;
import com.chromeware.client.monowire.MonowireProjection;
import com.chromeware.client.monowire.MonowireWristRenderer;
import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.chromeware.client.sandevistan.AfterimageRenderContext;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.monowire.MonowireAnchors;
import com.chromeware.monowire.MonowireImplant;
import com.chromeware.monowire.MonowireObstacles;
import com.chromeware.monowire.MonowirePhysics;
import com.chromeware.monowire.MonowireState;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class MonowireClient {
    public static final KeyMapping TOGGLE = new KeyMapping("key.chromeware.monowire", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 82, "key.categories.chromeware");
    private static final Map<Key, View> VIEWS = new HashMap<Key, View>();
    private static Object levelIdentity;
    private static final Map<HumanoidArm, MonowireObstacles.Box> CAMERA_ARMS;
    private static long cameraArmsTick;
    private static long renderFrame;
    public static final int HOLD_TICKS = 5;
    private static boolean holding;
    private static boolean charging;
    private static int held;

    public static WireSnapshot snapshot(AbstractClientPlayer player, HumanoidArm side, float tick) {
        View v = VIEWS.get(new Key(player.getUUID(), side));
        if (v == null || v.feed <= 0.0 || !v.physics.initialized() || !MonowireImplant.hasHardware((Player)player, side) || !MonowireAnimation.outletOpen(player, side)) {
            return null;
        }
        Vec3 socket = MonowireClient.root(player, v);
        Vec3[] points = MonowireRenderCurve.released(v.physics, socket, tick);
        return new WireSnapshot(MonowireAnimation.gripping(player, side), Arrays.stream(points).map(p -> p.subtract(socket)).toList());
    }

    public static List<Vec3[]> currentWires(AbstractClientPlayer player, float tick) {
        ArrayList<Vec3[]> wires = new ArrayList<Vec3[]>();
        for (HumanoidArm side : HumanoidArm.values()) {
            Vec3[] wire = MonowireClient.currentWire(player, side, tick);
            if (wire == null) continue;
            wires.add(wire);
        }
        return List.copyOf(wires);
    }

    private static Vec3[] currentWire(AbstractClientPlayer player, HumanoidArm side, float tick) {
        View v = VIEWS.get(new Key(player.getUUID(), side));
        if (v == null || v.feed <= 0.0 || !v.physics.initialized() || !MonowireImplant.hasHardware((Player)player, side) || !MonowireAnimation.outletOpen(player, side)) {
            return null;
        }
        boolean held = MonowireAnimation.gripping(player, side);
        if (MonowireClient.firstPerson(player) && (v.socketFrame != renderFrame || held && v.gripFrame != renderFrame)) {
            return null;
        }
        Vec3 down = MonowireClient.firstPerson(player) ? MonowireProjection.toWorld(new Vec3(0.0, -1.0, 0.0), Minecraft.getInstance().gameRenderer.getMainCamera()) : new Vec3(0.0, -1.0, 0.0);
        return held ? MonowireRenderCurve.held(MonowireClient.root(player, v), MonowireClient.grip(player, v), down) : MonowireRenderCurve.released(v.physics, MonowireClient.root(player, v), tick);
    }

    public static void register(IEventBus bus) {
        bus.addListener(e -> e.register(TOGGLE));
        bus.addListener(MonowireWristRenderer::reload);
        MonowireAnimation.register();
        NeoForge.EVENT_BUS.addListener(MonowireClient::tick);
        NeoForge.EVENT_BUS.addListener(MonowireClient::world);
        NeoForge.EVENT_BUS.addListener(e -> VIEWS.clear());
    }

    private static View view(AbstractClientPlayer player, HumanoidArm side) {
        return VIEWS.computeIfAbsent(new Key(player.getUUID(), side), k -> new View(side));
    }

    private static boolean firstPerson(AbstractClientPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        return mc.getCameraEntity() == player && mc.options.getCameraType().isFirstPerson();
    }

    public static boolean pairDeployed(Player p) {
        return MonowireImplant.deployed(p, HumanoidArm.RIGHT) && MonowireImplant.deployed(p, HumanoidArm.LEFT);
    }

    public static void press() {
        if (!holding) {
            holding = true;
            charging = false;
            held = 0;
        }
    }

    private static void hold(Minecraft mc) {
        if (!holding) {
            return;
        }
        if (mc.player == null || mc.screen != null || !MonowireClient.pairDeployed((Player)mc.player)) {
            if (charging && mc.player != null) {
                MonowireClient.send(6);
            }
            charging = false;
            holding = false;
        } else if (mc.options.keyAttack.isDown()) {
            if (++held >= 5 && (!charging || !MonowireClient.charged((Player)mc.player) && held % 2 == 0)) {
                charging = true;
                MonowireClient.send(4);
            }
        } else {
            if (charging && MonowireClient.charged((Player)mc.player)) {
                MonowireClient.send(5);
            } else {
                if (charging) {
                    MonowireClient.send(6);
                }
                PacketDistributor.sendToServer((CustomPacketPayload)ImplantPackets.Arm.of(1, mc.player.getMainArm()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            charging = false;
            holding = false;
        }
    }

    private static boolean charged(Player p) {
        return MonowireImplant.state(p, HumanoidArm.RIGHT).action() == 2;
    }

    private static void send(int action) {
        PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.Arm(action, 0), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (levelIdentity != mc.level) {
            VIEWS.clear();
            levelIdentity = mc.level;
        }
        if (!mc.isPaused()) {
            MonowireClient.hold(mc);
        }
        while (TOGGLE.consumeClick()) {
            if (mc.player == null || mc.screen != null) continue;
            ArmInput.toggle();
        }
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        VIEWS.keySet().removeIf(key -> mc.level.players().stream().noneMatch(p -> p.getUUID().equals(key.player()) && MonowireImplant.hasHardware((Player)p, key.side())));
        for (AbstractClientPlayer player : mc.level.players()) {
            MonowireAnimation.update(player);
            for (HumanoidArm side : HumanoidArm.values()) {
                if (!MonowireImplant.hasHardware((Player)player, side)) continue;
                View v = MonowireClient.view(player, side);
                if (v.lastEntityTick == player.tickCount) continue;
                v.lastEntityTick = player.tickCount;
                boolean deployed = MonowireImplant.deployed((Player)player, side);
                if (!deployed) {
                    v.physics.cancelSlash();
                }
                v.feed = Math.clamp((double)(v.feed + (deployed ? 0.125 : -0.125)), (double)0.0, (double)1.0);
                MonowireState state = MonowireImplant.state((Player)player, side);
                int stroke = state.stroke();
                if (deployed && stroke != v.stroke && stroke > 0 && (state.action() == 1 || state.action() == 3)) {
                    v.physics.slash(player.getLookAngle(), side == HumanoidArm.LEFT, stroke, state.action() == 3 ? MonowirePhysics.Lash.CROSS : MonowirePhysics.Lash.of(stroke));
                } else if (state.action() == 0 && v.physics.slashing() && stroke == v.stroke && MonowireClient.otherStarted(player, side)) {
                    v.physics.cancelSlash();
                }
                v.physics.aim(player.getLookAngle(), player.getYRot());
                v.physics.bodies(MonowireObstacles.of((Player)player, side, !v.physics.slashing(), 1.0f, MonowireClient.drawnArms(player)));
                v.stroke = stroke;
                if (!(v.feed > 0.0)) continue;
                v.physics.tick(MonowireClient.root(player, v), v.feed, MonowireAnimation.gripping(player, side) ? MonowireClient.grip(player, v) : null, (Level)mc.level);
            }
        }
    }

    private static boolean otherStarted(AbstractClientPlayer player, HumanoidArm side) {
        return ArmHardware.striking((Player)player, side.getOpposite());
    }

    private static Vec3 root(AbstractClientPlayer player, View v) {
        Minecraft mc = Minecraft.getInstance();
        if (player.level().getGameTime() - v.captureTick <= 2L) {
            if (MonowireClient.firstPerson(player) && v.cameraSocket != null) {
                return mc.gameRenderer.getMainCamera().getPosition().add(MonowireProjection.toWorld(v.cameraSocket, mc.gameRenderer.getMainCamera()));
            }
            if (v.worldSocket != null) {
                return v.worldSocket;
            }
        }
        return MonowireAnchors.wrist((Player)player, v.side);
    }

    private static Vec3 grip(AbstractClientPlayer player, View view) {
        Minecraft mc = Minecraft.getInstance();
        if (player.level().getGameTime() - view.captureTick <= 2L) {
            if (MonowireClient.firstPerson(player) && view.cameraGrip != null) {
                return mc.gameRenderer.getMainCamera().getPosition().add(MonowireProjection.toWorld(view.cameraGrip, mc.gameRenderer.getMainCamera()));
            }
            if (view.worldGrip != null) {
                return view.worldGrip;
            }
        }
        return MonowireAnchors.grip((Player)player, view.side);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void captureArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player || !MonowireClient.firstPerson(player) || AfterimageRenderContext.isRendering() || mc.level == null) {
            return;
        }
        float[] c = null;
        ArrayList cubes = new ArrayList();
        arm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                cubes.add(new float[]{cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ});
            }
        });
        if (cubes.isEmpty()) {
            return;
        }
        c = (float[])cubes.get(0);
        stack.pushPose();
        try {
            arm.translateAndRotate(stack);
            Matrix4f m = stack.last().pose();
            Vector3f center = m.transformPosition(new Vector3f((c[0] + c[3]) / 32.0f, (c[1] + c[4]) / 32.0f, (c[2] + c[5]) / 32.0f));
            Vector3f x = m.transformDirection(new Vector3f(1.0f, 0.0f, 0.0f));
            Vector3f y = m.transformDirection(new Vector3f(0.0f, 1.0f, 0.0f));
            Vector3f z = m.transformDirection(new Vector3f(0.0f, 0.0f, 1.0f));
            Camera camera = mc.gameRenderer.getMainCamera();
            CAMERA_ARMS.put(side, new MonowireObstacles.Box(MonowireProjection.toCamera(new Vec3((double)center.x, (double)center.y, (double)center.z), camera), MonowireProjection.toCamera(new Vec3((double)x.x, (double)x.y, (double)x.z).normalize(), camera), MonowireProjection.toCamera(new Vec3((double)y.x, (double)y.y, (double)y.z).normalize(), camera), MonowireProjection.toCamera(new Vec3((double)z.x, (double)z.y, (double)z.z).normalize(), camera), new Vec3((double)((c[3] - c[0]) / 32.0f * x.length()), (double)((c[4] - c[1]) / 32.0f * y.length()), (double)((c[5] - c[2]) / 32.0f * z.length()))));
            cameraArmsTick = mc.level.getGameTime();
        }
        finally {
            stack.popPose();
        }
    }

    private static List<MonowireObstacles.Box> drawnArms(AbstractClientPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player || !MonowireClient.firstPerson(player) || mc.level == null || mc.level.getGameTime() - cameraArmsTick > 2L || CAMERA_ARMS.isEmpty()) {
            return null;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        ArrayList<MonowireObstacles.Box> out = new ArrayList<MonowireObstacles.Box>();
        for (MonowireObstacles.Box b : CAMERA_ARMS.values()) {
            out.add(new MonowireObstacles.Box(camera.getPosition().add(MonowireProjection.toWorld(b.center(), camera)), MonowireProjection.toWorld(b.x(), camera), MonowireProjection.toWorld(b.y(), camera), MonowireProjection.toWorld(b.z(), camera), b.half()));
        }
        return out;
    }

    public static void capture(AbstractClientPlayer player, HumanoidArm side, Vec3 relative, boolean handProjection, boolean grip) {
        Vec3 projected;
        Minecraft mc = Minecraft.getInstance();
        if (player instanceof AfterimagePlayerEntity) {
            AfterimagePlayerEntity echo = (AfterimagePlayerEntity)player;
            if (relative != null && AfterimageRenderContext.isRendering()) {
                Vec3 world = relative.add(echo.captureOrigin != null ? echo.captureOrigin : mc.gameRenderer.getMainCamera().getPosition());
                (grip ? echo.wireGrips : echo.wireSockets).put(side, world);
            }
            return;
        }
        if (AfterimageRenderContext.isRendering()) {
            return;
        }
        if (relative == null || mc.level == null || mc.level.getPlayerByUUID(player.getUUID()) != player) {
            return;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        float tick = mc.getTimer().getGameTimeDeltaPartialTick(false);
        Vec3 world = projected = handProjection ? MonowireProjection.handToWorld(relative, camera, MonowireProjection.ratio(tick)) : relative;
        if ((world = world.add(camera.getPosition())).distanceToSqr(player.getEyePosition(tick)) > 16.0) {
            return;
        }
        View v = MonowireClient.view(player, side);
        if (grip) {
            v.worldGrip = world;
            v.gripFrame = renderFrame;
        } else {
            v.worldSocket = world;
            v.socketFrame = renderFrame;
        }
        if (MonowireClient.firstPerson(player)) {
            if (grip) {
                v.cameraGrip = MonowireProjection.toCamera(projected, camera);
            } else {
                v.cameraSocket = MonowireProjection.toCamera(projected, camera);
            }
        }
        v.captureTick = player.level().getGameTime();
    }

    private static void world(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            ++renderFrame;
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(MonowireCableRenderer.TYPE);
        float tick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (AbstractClientPlayer player : mc.level.players()) {
            if (MonowireClient.firstPerson(player)) continue;
            MonowireClient.drawPlayer(player, event.getPoseStack(), event.getCamera(), tick, consumer);
        }
        buffers.endBatch(MonowireCableRenderer.TYPE);
    }

    public static void firstPerson(Camera camera, float tick, PoseStack stack) {
        AbstractClientPlayer player;
        Entity entity;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !((entity = mc.getCameraEntity()) instanceof AbstractClientPlayer) || !MonowireClient.firstPerson(player = (AbstractClientPlayer)entity)) {
            return;
        }
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        MonowireClient.drawPlayer(player, stack, camera, tick, buffers.getBuffer(MonowireCableRenderer.TYPE));
        buffers.endBatch(MonowireCableRenderer.TYPE);
    }

    private static void drawPlayer(AbstractClientPlayer player, PoseStack stack, Camera viewCamera, float tick, VertexConsumer consumer) {
        Vec3 camera = viewCamera.getPosition();
        boolean linked = MonowireClient.linked(player);
        int rgb = MonowireCableRenderer.color((Player)player);
        for (HumanoidArm side : HumanoidArm.values()) {
            Vec3 down;
            View v = VIEWS.get(new Key(player.getUUID(), side));
            if (!MonowireClient.drawable(player, v)) continue;
            boolean held = MonowireAnimation.gripping(player, side);
            if (MonowireClient.firstPerson(player) && (v.socketFrame != renderFrame || held && v.gripFrame != renderFrame)) continue;
            Vec3 vec3 = down = MonowireClient.firstPerson(player) ? MonowireProjection.toWorld(new Vec3(0.0, -1.0, 0.0), viewCamera) : new Vec3(0.0, -1.0, 0.0);
            if (linked) {
                View other = VIEWS.get(new Key(player.getUUID(), side.getOpposite()));
                View right = side == HumanoidArm.RIGHT ? v : other;
                View left = side == HumanoidArm.RIGHT ? other : v;
                Vec3[] points = MonowireRenderCurve.held(MonowireClient.root(player, right), MonowireClient.root(player, left), down);
                int half = (int)Math.round(20.0 * v.feed);
                if (side == HumanoidArm.RIGHT) {
                    MonowireCableRenderer.curve(stack.last(), consumer, points, camera, 0, half, rgb);
                    continue;
                }
                MonowireCableRenderer.curve(stack.last(), consumer, points, camera, 40 - half, 40, rgb);
                continue;
            }
            if (!held && v.physics.standby()) {
                MonowireCableRenderer.dot(stack.last(), consumer, MonowireClient.root(player, v), camera, rgb);
                continue;
            }
            Vec3[] points = held ? MonowireRenderCurve.held(MonowireClient.root(player, v), MonowireClient.grip(player, v), down) : MonowireRenderCurve.released(v.physics, MonowireClient.root(player, v), SandevistanClient.tickDelta((Entity)player, tick));
            MonowireCableRenderer.curve(stack.last(), consumer, points, camera, 0, points.length - 1, rgb);
        }
    }

    private static boolean drawable(AbstractClientPlayer player, View v) {
        return v != null && v.feed > 0.0 && v.physics.initialized() && MonowireImplant.hasHardware((Player)player, v.side) && !player.isInvisible() && MonowireAnimation.outletOpen(player, v.side);
    }

    public static boolean linked(AbstractClientPlayer player) {
        for (HumanoidArm side : HumanoidArm.values()) {
            View v = VIEWS.get(new Key(player.getUUID(), side));
            if (!MonowireClient.drawable(player, v) || !MonowireImplant.deployed((Player)player, side) || MonowireAnimation.gripping(player, side) || !v.physics.standby()) {
                return false;
            }
            if (!MonowireClient.firstPerson(player) || v.socketFrame == renderFrame) continue;
            return false;
        }
        return true;
    }

    static {
        CAMERA_ARMS = new EnumMap<HumanoidArm, MonowireObstacles.Box>(HumanoidArm.class);
        cameraArmsTick = -100L;
    }

    private record Key(UUID player, HumanoidArm side) {
    }

    private static final class View {
        final HumanoidArm side;
        final MonowirePhysics physics = new MonowirePhysics();
        double feed;
        int stroke;
        int lastEntityTick = Integer.MIN_VALUE;
        Vec3 cameraSocket;
        Vec3 cameraGrip;
        Vec3 worldSocket;
        Vec3 worldGrip;
        long captureTick = -100L;
        long socketFrame = -1L;
        long gripFrame = -1L;

        View(HumanoidArm side) {
            this.side = side;
        }
    }

    public static final class MonowireRenderCurve {
        public static Vec3[] held(Vec3 socket, Vec3 grip, Vec3 down) {
            Vec3[] points = new Vec3[41];
            double sag = Math.min(0.025, socket.distanceTo(grip) * 0.025);
            for (int i = 0; i < points.length; ++i) {
                double t = (double)i / 40.0;
                points[i] = socket.lerp(grip, t).add(down.scale(4.0 * t * (1.0 - t) * sag));
            }
            return points;
        }

        public static Vec3[] released(MonowirePhysics physics, Vec3 socket, float tick) {
            Vec3[] points = new Vec3[41];
            Vec3 correction = socket.subtract(physics.point(0, tick));
            for (int i = 0; i < points.length; ++i) {
                points[i] = physics.point(i, tick).add(correction.scale(1.0 - (double)i / 40.0));
                double max = physics.length() * (double)i / 40.0 + 1.0E-4;
                Vec3 offset = points[i].subtract(socket);
                if (!(offset.lengthSqr() > max * max)) continue;
                points[i] = socket.add(offset.normalize().scale(max));
            }
            return points;
        }
    }

    public record WireSnapshot(boolean held, List<Vec3> offsets) {
        public WireSnapshot {
            offsets = List.copyOf(offsets);
        }

        public Vec3[] points(Vec3 socket, Vec3 grip) {
            if (this.held && grip != null) {
                return MonowireRenderCurve.held(socket, grip, new Vec3(0.0, -1.0, 0.0));
            }
            return (Vec3[])this.offsets.stream().map(arg_0 -> ((Vec3)socket).add(arg_0)).toArray(Vec3[]::new);
        }
    }
}

