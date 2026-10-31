/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  com.zigythebird.playeranimcore.animation.Animation
 *  com.zigythebird.playeranimcore.animation.AnimationController
 *  com.zigythebird.playeranimcore.animation.AnimationController$AnimationStateHandler
 *  com.zigythebird.playeranimcore.animation.AnimationData
 *  com.zigythebird.playeranimcore.bones.PlayerAnimBone
 *  com.zigythebird.playeranimcore.enums.PlayState
 *  com.zigythebird.playeranimcore.math.Vec3f
 *  com.zigythebird.playeranimcore.molang.MolangLoader
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.cyberware.CyberwareRig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class HardwareItemGeometry {
    public static final int WIRE = -36808;
    private static final ThreadLocal<Collection<String>> REGISTERING = new ThreadLocal();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void draw(Kind kind, CyberwareRig rig, Function<String, CyberwareRig.Transform> animation, PoseStack pose, VertexConsumer hardware, VertexConsumer glow, int light, int overlay) {
        Bounds bounds = new Bounds();
        HardwareItemGeometry.place(kind, rig, animation, new PoseStack(), bounds, bounds, light, overlay);
        float size = Math.max(bounds.max.x - bounds.min.x, Math.max(bounds.max.y - bounds.min.y, bounds.max.z - bounds.min.z));
        if (!(size > 0.0f)) {
            return;
        }
        pose.pushPose();
        try {
            pose.translate(0.5f, 0.5f, 0.5f);
            pose.scale(1.0f / size, 1.0f / size, 1.0f / size);
            pose.translate(-(bounds.min.x + bounds.max.x) / 2.0f, -(bounds.min.y + bounds.max.y) / 2.0f, -(bounds.min.z + bounds.max.z) / 2.0f);
            HardwareItemGeometry.place(kind, rig, animation, pose, hardware, glow, light, overlay);
        }
        finally {
            pose.popPose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void place(Kind kind, CyberwareRig rig, Function<String, CyberwareRig.Transform> animation, PoseStack pose, VertexConsumer hardware, VertexConsumer glow, int light, int overlay) {
        ModelPart arm = new ModelPart(List.of(), Map.of());
        pose.pushPose();
        try {
            pose.mulPose(Axis.ZP.rotationDegrees(180.0f));
            Map<String, Vec3> locators = rig.render(false, false, arm, kind.center, pose, animation, hardware, light, overlay, kind.draw, 0.0f);
            if (kind == Kind.MONOWIRE && glow != null && locators.containsKey("wire_outlet")) {
                PoseStack local = new PoseStack();
                local.mulPose(Axis.ZP.rotationDegrees(180.0f));
                Vec3 outlet = rig.render(false, false, arm, kind.center, local, animation, null, light, overlay, kind.draw, 0.0f).get("wire_outlet");
                if (outlet != null) {
                    pose.popPose();
                    pose.pushPose();
                    HardwareItemGeometry.wire(pose.last(), glow, new Vector3f((float)outlet.x, (float)outlet.y, (float)outlet.z));
                }
            }
        }
        finally {
            pose.popPose();
        }
    }

    static void wire(PoseStack.Pose pose, VertexConsumer glow, Vector3f outlet) {
        int n = 56;
        Vector3f[] points = new Vector3f[n + 1];
        float px = 0.0625f;
        float radius = 1.9f * px;
        float drop = 3.4f * px;
        float turns = 1.6f;
        Vector3f axis = new Vector3f((Vector3fc)outlet).add(radius, 0.0f, 0.0f);
        for (int i = 0; i <= n; ++i) {
            float t = (float)i / (float)n;
            float a = (float)(Math.PI + (double)(t * turns * 2.0f) * Math.PI);
            float r = radius * (0.55f + 0.45f * Math.min(1.0f, t * 4.0f));
            points[i] = new Vector3f(axis.x + r * (float)Math.cos(a), axis.y - drop * t, axis.z + r * (float)Math.sin(a));
        }
        points[0].set((Vector3fc)outlet);
        HardwareItemGeometry.tube(pose, glow, points, 0.34f * px, -36808);
    }

    static void tube(PoseStack.Pose pose, VertexConsumer buffer, Vector3f[] points, float width, int color) {
        for (int i = 0; i < points.length - 1; ++i) {
            Vector3f b = points[i + 1];
            Vector3f a = points[i];
            Vector3f along = new Vector3f((Vector3fc)b).sub((Vector3fc)a);
            if ((double)along.lengthSquared() < 1.0E-12) continue;
            along.normalize();
            Vector3f side = new Vector3f((Vector3fc)along).cross((Vector3fc)(Math.abs(along.y) > 0.9f ? new Vector3f(1.0f, 0.0f, 0.0f) : new Vector3f(0.0f, 1.0f, 0.0f))).normalize().mul(width / 2.0f);
            Vector3f up = new Vector3f((Vector3fc)along).cross((Vector3fc)side).normalize().mul(width / 2.0f);
            Vector3f[] corners = new Vector3f[]{new Vector3f((Vector3fc)side).add((Vector3fc)up), new Vector3f((Vector3fc)side).sub((Vector3fc)up), new Vector3f((Vector3fc)side).negate().sub((Vector3fc)up), new Vector3f((Vector3fc)side).negate().add((Vector3fc)up)};
            for (int k = 0; k < 4; ++k) {
                Vector3f c0 = corners[k];
                Vector3f c1 = corners[(k + 1) % 4];
                HardwareItemGeometry.vertex(pose, buffer, new Vector3f((Vector3fc)a).add((Vector3fc)c0), color);
                HardwareItemGeometry.vertex(pose, buffer, new Vector3f((Vector3fc)b).add((Vector3fc)c0), color);
                HardwareItemGeometry.vertex(pose, buffer, new Vector3f((Vector3fc)b).add((Vector3fc)c1), color);
                HardwareItemGeometry.vertex(pose, buffer, new Vector3f((Vector3fc)a).add((Vector3fc)c1), color);
            }
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vector3f p, int color) {
        buffer.addVertex(pose, p.x, p.y, p.z).setColor(color);
    }

    public static Function<String, CyberwareRig.Transform> pose(Animation clip, float seconds, Collection<String> bones) {
        Sampler controller = new Sampler(bones);
        controller.triggerAnimation(clip, seconds * 20.0f);
        controller.process(new AnimationData(0.0f, 0.0f));
        HashMap<String, CyberwareRig.Transform> result = new HashMap<String, CyberwareRig.Transform>();
        for (String name2 : bones) {
            PlayerAnimBone b = controller.get3DTransform(new PlayerAnimBone(name2));
            result.put(name2, new CyberwareRig.Transform(new Vector3f(b.positionX / 16.0f, -b.positionY / 16.0f, b.positionZ / 16.0f), new Vector3f(b.rotX, b.rotY, b.rotZ), new Vector3f(b.scaleX, b.scaleY, b.scaleZ)));
        }
        return name -> result.getOrDefault(name, CyberwareRig.Transform.IDENTITY);
    }

    static final class Bounds
    implements VertexConsumer {
        final Vector3f min = new Vector3f(Float.MAX_VALUE);
        final Vector3f max = new Vector3f(-3.4028235E38f);

        Bounds() {
        }

        public VertexConsumer addVertex(float x, float y, float z) {
            this.min.min((Vector3fc)new Vector3f(x, y, z));
            this.max.max((Vector3fc)new Vector3f(x, y, z));
            return this;
        }

        public VertexConsumer setColor(int r, int g, int b, int a) {
            return this;
        }

        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        public VertexConsumer setNormal(float x, float y, float z) {
            return this;
        }
    }

    public static enum Kind {
        MANTIS("mantis_blades", "models/cyberware/mantis_right_rig.json", "mantis_idle_right", -0.0625f, bone -> bone.endsWith("_strut") || bone.endsWith("_strut_slide") || bone.endsWith("_blade") || bone.endsWith("_tip") || bone.endsWith("_extension")),
        MONOWIRE("monowire", "models/cyberware/monowire_rig.json", "monowire_idle_alone", -0.0625f, bone -> true),
        LAUNCHER("projectile_launch_system", "models/cyberware/projectile_launcher_rig.json", "launcher_aim", 0.0625f, bone -> true);

        final String implant;
        final String rig;
        final String clip;
        final float center;
        final Predicate<String> draw;

        private Kind(String implant, String rig, String clip, float center, Predicate<String> draw) {
            this.implant = implant;
            this.rig = rig;
            this.clip = clip;
            this.center = center;
            this.draw = draw;
        }

        public String implant() {
            return this.implant;
        }

        public String rig() {
            return this.rig;
        }

        public String clip() {
            return this.clip;
        }
    }

    private static final class Sampler
    extends AnimationController {
        Sampler(Collection<String> bones) {
            super(Sampler.start(bones), MolangLoader::createNewEngine);
            REGISTERING.remove();
        }

        private static AnimationController.AnimationStateHandler start(Collection<String> bones) {
            REGISTERING.set(bones);
            return (controller, state, setter) -> PlayState.CONTINUE;
        }

        public void registerBones() {
            for (String bone : REGISTERING.get()) {
                this.registerPlayerAnimBone(bone);
            }
        }

        public Vec3f getBonePosition(String name) {
            return Vec3f.ZERO;
        }
    }
}

