/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  dev.engine_room.flywheel.lib.model.baked.PartialModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.ModelPart$Cube
 *  net.minecraft.client.model.geom.ModelPart$Polygon
 *  net.minecraft.client.model.geom.ModelPart$Vertex
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.player.PlayerModelPart
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.chromeware.client.extendo;

import com.chromeware.client.body.ClippedLimbRenderer;
import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.extendo.ExtendoClient;
import com.chromeware.compat.HemodynamicsCompatibility;
import com.chromeware.extendo.ExtendoGrip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ExtendoRenderer {
    public static final float CUT_Y = 3.0f;
    public static final float OPEN = 38.0f;
    public static final float SCALE = 0.5f;
    private static final float WRIST = 7.0f;
    private static final float CHROME_WRIST = 8.07f;
    private static final float FOLDED = 4.68f;
    private static final PartialModel THIN_SHORT = ExtendoRenderer.part("thin_short");
    private static final PartialModel WIDE_SHORT = ExtendoRenderer.part("wide_short");
    private static final PartialModel THIN_LONG = ExtendoRenderer.part("thin_long");
    private static final PartialModel WIDE_LONG = ExtendoRenderer.part("wide_long");
    private static final float[] FRONT_LO = new float[]{-2.0f, 3.0f, -2.0f};
    private static final float[] FRONT_HI = new float[]{2.0f, 7.0f, -1.5f};
    private static final float[] BACK_LO = new float[]{-2.0f, 3.0f, 1.5f};
    private static final float[] BACK_HI = new float[]{2.0f, 7.0f, 2.0f};
    private static final float[] OUTER_LO = new float[]{-2.0f, 3.0f, -1.5f};
    private static final float[] OUTER_HI = new float[]{-1.5f, 7.0f, 1.5f};
    private static final float[] INNER_LO = new float[]{1.5f, 3.0f, -1.5f};
    private static final float[] INNER_HI = new float[]{2.0f, 7.0f, 1.5f};
    private static final float[] HAND_LO = new float[]{-2.0f, 7.0f, -2.0f};
    private static final float[] HAND_HI = new float[]{2.0f, 10.0f, 2.0f};
    private static final float[] MOUNT_LO = new float[]{-1.5f, 3.0f, -1.5f};
    private static final float[] MOUNT_HI = new float[]{1.5f, 4.4f, 1.5f};
    private static final List<Face> SKIN = List.of(new Face("front", FRONT_LO, FRONT_HI, 2, false), new Face("front", FRONT_LO, FRONT_HI, 0, false), new Face("front", FRONT_LO, FRONT_HI, 0, true), new Face("back", BACK_LO, BACK_HI, 2, true), new Face("back", BACK_LO, BACK_HI, 0, false), new Face("back", BACK_LO, BACK_HI, 0, true), new Face("outer", OUTER_LO, OUTER_HI, 0, false), new Face("inner", INNER_LO, INNER_HI, 0, true), new Face("hand", HAND_LO, HAND_HI, 2, false), new Face("hand", HAND_LO, HAND_HI, 2, true), new Face("hand", HAND_LO, HAND_HI, 0, false), new Face("hand", HAND_LO, HAND_HI, 0, true), new Face("hand", HAND_LO, HAND_HI, 1, true));
    private static final List<Face> LINING = List.of(new Face("front", FRONT_LO, FRONT_HI, 2, true), new Face("front", FRONT_LO, FRONT_HI, 1, false), new Face("front", FRONT_LO, FRONT_HI, 1, true), new Face("back", BACK_LO, BACK_HI, 2, false), new Face("back", BACK_LO, BACK_HI, 1, false), new Face("back", BACK_LO, BACK_HI, 1, true), new Face("outer", OUTER_LO, OUTER_HI, 0, true), new Face("outer", OUTER_LO, OUTER_HI, 1, false), new Face("outer", OUTER_LO, OUTER_HI, 1, true), new Face("outer", OUTER_LO, OUTER_HI, 2, false), new Face("outer", OUTER_LO, OUTER_HI, 2, true), new Face("inner", INNER_LO, INNER_HI, 0, false), new Face("inner", INNER_LO, INNER_HI, 1, false), new Face("inner", INNER_LO, INNER_HI, 1, true), new Face("inner", INNER_LO, INNER_HI, 2, false), new Face("inner", INNER_LO, INNER_HI, 2, true), new Face("mount", MOUNT_LO, MOUNT_HI, 0, false), new Face("mount", MOUNT_LO, MOUNT_HI, 0, true), new Face("mount", MOUNT_LO, MOUNT_HI, 2, false), new Face("mount", MOUNT_LO, MOUNT_HI, 2, true), new Face("mount", MOUNT_LO, MOUNT_HI, 1, true), new Face("hand", HAND_LO, HAND_HI, 1, false));
    static final Map<String, String> FRAME_BONES = Map.of("forearm_front_shell", "inner", "forearm_rear_shell_front", "outer", "forearm_rear_shell_back", "outer", "actuator_cylinder", "outer", "actuator_rod", "outer", "inner_forearm_cheek", "front", "outer_forearm_cheek", "back", "palm", "hand", "backhand_plate", "hand");
    private static final Map<String, float[]> FRAME_HINGES = Map.of("inner", new float[]{2.0f, 4.85f, 0.0f}, "outer", new float[]{-2.0f, 4.85f, 0.0f}, "front", new float[]{0.0f, 5.16f, -2.25f}, "back", new float[]{0.0f, 5.16f, 2.25f});

    private static PartialModel part(String name) {
        return PartialModel.of((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"create", (String)("item/extendo_grip/" + name)));
    }

    static float ease(float t) {
        t = Math.clamp((float)t, (float)0.0f, (float)1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private static float rad(float degrees) {
        return (float)Math.toRadians(degrees);
    }

    public static float shift(float extension) {
        float ext = Mth.lerp((float)extension, (float)24.0f, (float)156.0f);
        float half = ext / 2.0f;
        float opp = 180.0f - ext;
        Matrix4f m = new Matrix4f().scale(1.0f, 1.0f, 1.0f + extension);
        m.rotateX(ExtendoRenderer.rad(-180.0f + half)).translate(0.0f, 0.34375f, 0.0f);
        m.rotateX(ExtendoRenderer.rad(opp)).translate(0.0f, 0.6875f, 0.0f);
        m.rotateX(ExtendoRenderer.rad(-opp)).translate(0.0f, 0.375f, 0.0f);
        return 0.5f * (-m.m32() * 16.0f - 4.68f);
    }

    private static Matrix4f motion(String part, Pose pose) {
        Vector3f hinge;
        if (part.equals("hand")) {
            return new Matrix4f().translate(0.0f, ExtendoRenderer.shift(pose.extension()), 0.0f);
        }
        if (part.equals("mount")) {
            return new Matrix4f();
        }
        float a = ExtendoRenderer.rad(38.0f) * ExtendoRenderer.ease(pose.open());
        Vector3f vector3f = part.equals("front") ? new Vector3f(0.0f, 3.0f, -2.0f) : (part.equals("back") ? new Vector3f(0.0f, 3.0f, 2.0f) : (hinge = part.equals("outer") ? new Vector3f(-2.0f, 3.0f, 0.0f) : new Vector3f(2.0f, 3.0f, 0.0f)));
        Quaternionf turn = part.equals("front") ? new Quaternionf().rotationX(-a) : (part.equals("back") ? new Quaternionf().rotationX(a) : (part.equals("outer") ? new Quaternionf().rotationZ(a) : new Quaternionf().rotationZ(-a)));
        return new Matrix4f().translate((Vector3fc)hinge).rotate((Quaternionfc)turn).translate(-hinge.x, -hinge.y, -hinge.z);
    }

    public static void render(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, ModelPart sleeve, PoseStack stack, MultiBufferSource buffers, int light) {
        HemodynamicsCompatibility.arm((LivingEntity)player, side, false, () -> ExtendoRenderer.drawArm(player, side, arm, sleeve, stack, buffers, light));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void drawArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, ModelPart sleeve, PoseStack stack, MultiBufferSource buffers, int light) {
        if (!ExtendoGrip.active((Player)player, side) || player.isInvisible() || player.isSpectator() || !arm.visible) {
            return;
        }
        Pose pose = ExtendoClient.pose(player, side);
        boolean chrome = CyberLimbRenderer.chrome(player, side);
        stack.pushPose();
        try {
            arm.translateAndRotate(stack);
            if (!chrome) {
                VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)player.getSkin().texture()));
                ExtendoRenderer.skin(arm, null, side, pose, stack.last(), skin, light);
                if (sleeve != null && player.isModelPartShown(side == HumanoidArm.LEFT ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE)) {
                    ExtendoRenderer.skin(arm, sleeve, side, pose, stack.last(), skin, light);
                }
            }
            ExtendoRenderer.canonical(arm, side, stack);
            ExtendoRenderer.hardware(stack, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.texture((Player)player, "extendo_grip"))), pose, chrome, light, OverlayTexture.NO_OVERLAY);
        }
        finally {
            stack.popPose();
        }
    }

    private static void canonical(ModelPart arm, HumanoidArm side, PoseStack stack) {
        ModelPart.Cube cube = ExtendoRenderer.cube(arm);
        float center = cube == null ? (float)(side == HumanoidArm.LEFT ? 1 : -1) : (cube.minX + cube.maxX) / 2.0f;
        boolean slim = cube != null && cube.maxX - cube.minX < 3.5f;
        stack.translate(center / 16.0f, 0.0f, 0.0f);
        stack.scale((float)(side == HumanoidArm.LEFT ? -1 : 1) * (slim ? 0.75f : 1.0f), 1.0f, 1.0f);
    }

    private static ModelPart.Cube cube(ModelPart part) {
        ModelPart.Cube[] found = new ModelPart.Cube[]{null};
        part.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                found[0] = cube;
            }
        });
        return found[0];
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void hardware(PoseStack stack, VertexConsumer buffer, Pose pose, boolean chrome, int light, int overlay) {
        if (!chrome) {
            PoseStack.Pose frame = stack.last();
            for (Face face : LINING) {
                Matrix4f motion = ExtendoRenderer.motion(face.part(), pose);
                int u = (face.axis() + 1) % 3;
                int v = (face.axis() + 2) % 3;
                float u0 = 8.0f - (face.hi()[u] - face.lo()[u]) / 2.0f;
                float v0 = 8.0f - (face.hi()[v] - face.lo()[v]) / 2.0f;
                Vector3f n = frame.transformNormal(motion.transformDirection(ExtendoRenderer.normal(face)).normalize(), new Vector3f());
                for (Vector3f corner : ExtendoRenderer.corners(face.lo(), face.hi(), face.axis(), face.positive())) {
                    float cu = u0 + (u == 0 ? corner.x : (u == 1 ? corner.y : corner.z)) - face.lo()[u];
                    float cv = v0 + (v == 0 ? corner.x : (v == 1 ? corner.y : corner.z)) - face.lo()[v];
                    Vector3f p = frame.pose().transformPosition((Vector3fc)motion.transformPosition(corner).div(16.0f), new Vector3f());
                    buffer.addVertex(p.x, p.y, p.z, -1, (16.0f + cu) / 32.0f, cv / 16.0f, overlay, light, n.x, n.y, n.z);
                }
            }
        }
        if (pose.open() <= 0.0f) {
            return;
        }
        stack.pushPose();
        try {
            stack.translate(0.0f, ((chrome ? 8.07f : 7.0f) - 2.34f) / 16.0f, 0.0f);
            stack.mulPose(Axis.XP.rotationDegrees(90.0f));
            stack.scale(0.5f, 0.5f, 0.5f);
            ExtendoRenderer.scissor(stack, buffer, pose.extension(), light, overlay);
        }
        finally {
            stack.popPose();
        }
    }

    static void scissor(PoseStack stack, VertexConsumer buffer, float extension, int light, int overlay) {
        float ext = Mth.lerp((float)extension, (float)24.0f, (float)156.0f);
        float half = ext / 2.0f;
        float opp = 180.0f - ext;
        stack.pushPose();
        stack.scale(1.0f, 1.0f, 1.0f + extension);
        stack.pushPose();
        ExtendoRenderer.link(stack, buffer, THIN_SHORT, -half, 0.0f, light, overlay);
        stack.translate(0.0f, 0.34375f, 0.0f);
        ExtendoRenderer.link(stack, buffer, WIDE_LONG, -opp, 0.0f, light, overlay);
        stack.translate(0.0f, 0.6875f, 0.0f);
        ExtendoRenderer.link(stack, buffer, THIN_SHORT, opp, 0.03125f, light, overlay);
        stack.popPose();
        stack.pushPose();
        ExtendoRenderer.link(stack, buffer, WIDE_SHORT, -180.0f + half, 0.0f, light, overlay);
        stack.translate(0.0f, 0.34375f, 0.0f);
        ExtendoRenderer.link(stack, buffer, THIN_LONG, opp, 0.0f, light, overlay);
        stack.translate(0.0f, 0.6875f, 0.0f);
        ExtendoRenderer.link(stack, buffer, WIDE_SHORT, -opp, 0.03125f, light, overlay);
        stack.popPose();
        stack.popPose();
    }

    private static void link(PoseStack stack, VertexConsumer buffer, PartialModel part, float degrees, float lift, int light, int overlay) {
        stack.mulPose(Axis.XP.rotationDegrees(degrees));
        BakedModel model = part.get();
        if (model == null) {
            return;
        }
        stack.pushPose();
        stack.translate(-0.5f, lift, 0.0f);
        RandomSource random = RandomSource.create();
        for (Direction direction : new Direction[]{null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            for (BakedQuad quad : model.getQuads(null, direction, random, ModelData.EMPTY, null)) {
                TextureAtlasSprite sprite = quad.getSprite();
                int[] data = quad.getVertices();
                PoseStack.Pose pose = stack.last();
                Vector3f n = pose.transformNormal(new Vector3f((Vector3fc)quad.getDirection().step()), new Vector3f());
                for (int i = 0; i < 4; ++i) {
                    int k = i * 8;
                    Vector3f p = pose.pose().transformPosition(Float.intBitsToFloat(data[k]), Float.intBitsToFloat(data[k + 1]), Float.intBitsToFloat(data[k + 2]), new Vector3f());
                    float u = (Float.intBitsToFloat(data[k + 4]) - sprite.getU0()) / (sprite.getU1() - sprite.getU0()) / 2.0f;
                    float v = (Float.intBitsToFloat(data[k + 5]) - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
                    buffer.addVertex(p.x, p.y, p.z, -1, u, v, overlay, light, n.x, n.y, n.z);
                }
            }
        }
        stack.popPose();
    }

    private static Vector3f normal(Face face) {
        return new Vector3f(face.axis() == 0 ? 1.0f : 0.0f, face.axis() == 1 ? 1.0f : 0.0f, face.axis() == 2 ? 1.0f : 0.0f).mul(face.positive() ? 1.0f : -1.0f);
    }

    private static void skin(ModelPart arm, ModelPart sleeve, HumanoidArm side, Pose pose, PoseStack.Pose frame, VertexConsumer buffer, int light) {
        ModelPart.Cube cube;
        ModelPart.Cube armCube = ExtendoRenderer.cube(arm);
        ModelPart.Cube cube2 = cube = sleeve == null ? armCube : ExtendoRenderer.cube(sleeve);
        if (armCube == null || cube == null) {
            return;
        }
        float mirror = side == HumanoidArm.LEFT ? -1.0f : 1.0f;
        float scale = armCube.maxX - armCube.minX < 3.5f ? 0.75f : 1.0f;
        float center = (armCube.minX + armCube.maxX) / 2.0f;
        float grow = sleeve == null ? 0.0f : ExtendoRenderer.bottom(cube) - ExtendoRenderer.bottom(armCube);
        for (Face face : SKIN) {
            Matrix4f motion = ExtendoRenderer.motion(face.part(), pose);
            float[] lo = (float[])face.lo().clone();
            float[] hi = (float[])face.hi().clone();
            for (int axis : new int[]{0, 2}) {
                float out;
                float f = out = axis == 0 ? grow / scale : grow;
                if (lo[axis] <= -2.0f) {
                    int n = axis;
                    lo[n] = lo[n] - out;
                }
                if (!(hi[axis] >= 2.0f)) continue;
                int n = axis;
                hi[n] = hi[n] + out;
            }
            if (hi[1] >= 10.0f) {
                hi[1] = hi[1] + grow;
            }
            Vector3f normal = ExtendoRenderer.normal(face);
            ModelPart.Polygon polygon = ExtendoRenderer.polygon(cube, new Vector3f(normal.x * mirror, normal.y, normal.z));
            if (polygon == null) continue;
            Vector3f moved = motion.transformDirection(new Vector3f((Vector3fc)normal));
            Vector3f n = frame.transformNormal(new Vector3f(moved.x * mirror, moved.y, moved.z).normalize(), new Vector3f());
            for (Vector3f corner : ExtendoRenderer.corners(lo, hi, face.axis(), face.positive())) {
                float[] uv = ExtendoRenderer.uv(polygon, new Vector3f(center + mirror * scale * corner.x, corner.y, corner.z));
                Vector3f p = motion.transformPosition(new Vector3f((Vector3fc)corner));
                Vector3f position = frame.pose().transformPosition((center + mirror * scale * p.x) / 16.0f, p.y / 16.0f, p.z / 16.0f, new Vector3f());
                buffer.addVertex(position.x, position.y, position.z, -1, uv[0], uv[1], OverlayTexture.NO_OVERLAY, light, n.x, n.y, n.z);
            }
        }
    }

    private static float bottom(ModelPart.Cube cube) {
        float y = -3.4028235E38f;
        for (ModelPart.Polygon polygon : cube.polygons) {
            for (ModelPart.Vertex vertex : polygon.vertices) {
                y = Math.max(y, vertex.pos.y());
            }
        }
        return y;
    }

    private static Vector3f[] corners(float[] lo, float[] hi, int axis, boolean positive) {
        float[][] fArrayArray;
        float c;
        int u = (axis + 1) % 3;
        int v = (axis + 2) % 3;
        float f = c = positive ? hi[axis] : lo[axis];
        if (positive) {
            float[][] fArrayArray2 = new float[4][];
            fArrayArray2[0] = new float[]{lo[u], lo[v]};
            fArrayArray2[1] = new float[]{hi[u], lo[v]};
            fArrayArray2[2] = new float[]{hi[u], hi[v]};
            fArrayArray = fArrayArray2;
            fArrayArray2[3] = new float[]{lo[u], hi[v]};
        } else {
            float[][] fArrayArray3 = new float[4][];
            fArrayArray3[0] = new float[]{lo[u], lo[v]};
            fArrayArray3[1] = new float[]{lo[u], hi[v]};
            fArrayArray3[2] = new float[]{hi[u], hi[v]};
            fArrayArray = fArrayArray3;
            fArrayArray3[3] = new float[]{hi[u], lo[v]};
        }
        float[][] uv = fArrayArray;
        Vector3f[] result = new Vector3f[4];
        for (int i = 0; i < 4; ++i) {
            float[] p = new float[3];
            p[axis] = c;
            p[u] = uv[i][0];
            p[v] = uv[i][1];
            result[i] = new Vector3f(p[0], p[1], p[2]);
        }
        return result;
    }

    private static ModelPart.Polygon polygon(ModelPart.Cube cube, Vector3f normal) {
        for (ModelPart.Polygon polygon : cube.polygons) {
            if (!(polygon.normal.dot((Vector3fc)normal) > 0.9f)) continue;
            return polygon;
        }
        return null;
    }

    private static float[] uv(ModelPart.Polygon polygon, Vector3f point) {
        ModelPart.Vertex[] v = polygon.vertices;
        Vector3f origin = v[0].pos;
        Vector3f a = new Vector3f((Vector3fc)v[1].pos).sub((Vector3fc)origin);
        Vector3f b = new Vector3f((Vector3fc)v[3].pos).sub((Vector3fc)origin);
        Vector3f d = new Vector3f((Vector3fc)point).sub((Vector3fc)origin);
        float s = a.lengthSquared() == 0.0f ? 0.0f : d.dot((Vector3fc)a) / a.lengthSquared();
        float t = b.lengthSquared() == 0.0f ? 0.0f : d.dot((Vector3fc)b) / b.lengthSquared();
        return new float[]{v[0].u + s * (v[1].u - v[0].u) + t * (v[3].u - v[0].u), v[0].v + s * (v[1].v - v[0].v) + t * (v[3].v - v[0].v)};
    }

    public static CyberLimbRenderer.Fitting fitting(AbstractClientPlayer player, HumanoidArm side) {
        if (!ExtendoGrip.active((Player)player, side) || !CyberLimbRenderer.chrome(player, side)) {
            return null;
        }
        Pose pose = ExtendoClient.pose(player, side);
        float a = ExtendoRenderer.rad(38.0f) * ExtendoRenderer.ease(pose.open());
        HashMap<String, Matrix4f> poses = new HashMap<String, Matrix4f>();
        FRAME_BONES.forEach((piece, part) -> {
            if (part.equals("hand")) {
                poses.put((String)piece, new Matrix4f().translate(0.0f, ExtendoRenderer.shift(pose.extension()) / 16.0f, 0.0f));
                return;
            }
            float[] h = FRAME_HINGES.get(part);
            Quaternionf turn = part.equals("front") ? new Quaternionf().rotationX(-a) : (part.equals("back") ? new Quaternionf().rotationX(a) : (part.equals("outer") ? new Quaternionf().rotationZ(a) : new Quaternionf().rotationZ(-a)));
            poses.put((String)piece, new Matrix4f().translate(h[0] / 16.0f, h[1] / 16.0f, h[2] / 16.0f).rotate((Quaternionfc)turn).translate(-h[0] / 16.0f, -h[1] / 16.0f, -h[2] / 16.0f));
        });
        return CyberLimbRenderer.fitting(poses);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void previewArm(ModelPart arm, ModelPart sleeve, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, ResourceLocation skinTexture, float seconds, int light) {
        Pose pose = ExtendoClient.preview(seconds);
        VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)skinTexture));
        for (ModelPart part : new ModelPart[]{arm, sleeve}) {
            part.visit(stack, (p, path, index, cube) -> ClippedLimbRenderer.render(cube, 3.0f - cube.minY, p, skin, light, OverlayTexture.NO_OVERLAY, -1));
        }
        stack.pushPose();
        try {
            arm.translateAndRotate(stack);
            ExtendoRenderer.skin(arm, null, HumanoidArm.RIGHT, pose, stack.last(), skin, light);
            ExtendoRenderer.skin(arm, sleeve, HumanoidArm.RIGHT, pose, stack.last(), skin, light);
            ExtendoRenderer.canonical(arm, HumanoidArm.RIGHT, stack);
            ExtendoRenderer.hardware(stack, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)texture)), pose, false, light, OverlayTexture.NO_OVERLAY);
        }
        finally {
            stack.popPose();
        }
    }

    public record Pose(float open, float extension) {
        public static final Pose REST = new Pose(0.0f, 0.0f);
    }

    private record Face(String part, float[] lo, float[] hi, int axis, boolean positive) {
    }

    public static final class Layer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public Layer(PlayerRenderer renderer) {
            super((RenderLayerParent)renderer);
        }

        public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float swing, float amount, float tick, float age, float yaw, float pitch) {
            PlayerModel model = (PlayerModel)this.getParentModel();
            ExtendoRenderer.render(player, HumanoidArm.LEFT, model.leftArm, model.leftSleeve, stack, buffers, light);
            ExtendoRenderer.render(player, HumanoidArm.RIGHT, model.rightArm, model.rightSleeve, stack, buffers, light);
        }
    }
}

