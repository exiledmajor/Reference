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
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package com.chromeware.client.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.client.cyberware.GorillaArmRenderer;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.extendo.ExtendoRenderer;
import com.chromeware.client.launcher.LauncherRenderer;
import com.chromeware.client.mantis.MantisRenderer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class CyberLimbRenderer {
    public static final String IMPLANT = "cyber_limbs";
    private static final ResourceLocation MESH = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"models/cyberware/cyber_limbs_mesh.json");
    public static final float FOREARM_ALONG = 0.02f;
    private static volatile Map<String, List<Quad>> meshes = Map.of();

    public static Fitting fitting(final Map<String, Matrix4f> poses) {
        return new Fitting(){

            @Override
            public Matrix4f pose(String piece) {
                return (Matrix4f)poses.get(piece);
            }

            @Override
            public boolean hidden(String piece) {
                return false;
            }
        };
    }

    public static Matrix4f follow(Matrix4f rest, Matrix4f now, boolean mirrored, boolean leftArm) {
        Matrix4f delta = new Matrix4f((Matrix4fc)now).mul((Matrix4fc)new Matrix4f((Matrix4fc)rest).invert());
        float flip = (mirrored ? -1 : 1) * (leftArm ? -1 : 1);
        Matrix4f toRig = new Matrix4f().translate(0.0f, -0.02f, 0.0f).scale(flip, 1.0f, 1.0f);
        Matrix4f toFrame = new Matrix4f().scale(flip, 1.0f, 1.0f).translate(0.0f, 0.02f, 0.0f);
        return toFrame.mul((Matrix4fc)delta).mul((Matrix4fc)toRig);
    }

    public static void registerReloads(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            try (InputStreamReader reader = new InputStreamReader(manager.getResourceOrThrow(MESH).open(), StandardCharsets.UTF_8);){
                JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
                if (root.get("format").getAsInt() != 1) {
                    throw new IllegalArgumentException("Unsupported cyber limb mesh format");
                }
                HashMap result = new HashMap();
                for (Map.Entry limb : root.getAsJsonObject("limbs").entrySet()) {
                    ArrayList<Quad> quads = new ArrayList<Quad>();
                    for (JsonElement element : ((JsonElement)limb.getValue()).getAsJsonArray()) {
                        JsonObject q = element.getAsJsonObject();
                        JsonArray n = q.getAsJsonArray("normal");
                        JsonArray vertices = q.getAsJsonArray("vertices");
                        Vector3f[] positions = new Vector3f[vertices.size()];
                        float[] uvs = new float[vertices.size() * 2];
                        for (int i = 0; i < vertices.size(); ++i) {
                            JsonArray v = vertices.get(i).getAsJsonArray();
                            positions[i] = new Vector3f(v.get(0).getAsFloat(), v.get(1).getAsFloat(), v.get(2).getAsFloat());
                            uvs[i * 2] = v.get(3).getAsFloat();
                            uvs[i * 2 + 1] = v.get(4).getAsFloat();
                        }
                        quads.add(new Quad(q.has("group") && !q.get("group").isJsonNull() ? q.get("group").getAsString() : "", q.has("piece") ? q.get("piece").getAsString() : "", new Vector3f(n.get(0).getAsFloat(), n.get(1).getAsFloat(), n.get(2).getAsFloat()), positions, uvs));
                    }
                    result.put((String)limb.getKey(), List.copyOf(quads));
                }
                meshes = Map.copyOf(result);
            }
            catch (Exception exception) {
                throw new IllegalStateException("Could not load the cyber limb meshes", exception);
            }
        }));
    }

    private static String mesh(BodyPart part) {
        return switch (part) {
            case BodyPart.LEFT_ARM, BodyPart.RIGHT_ARM -> "arm";
            case BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG -> "leg";
            default -> "torso";
        };
    }

    private static boolean left(BodyPart part) {
        return part == BodyPart.LEFT_ARM || part == BodyPart.LEFT_LEG;
    }

    public static void render(BodyPart part, ModelPart bone, PoseStack stack, VertexConsumer buffer, int light, int overlay) {
        CyberLimbRenderer.render(part, bone, stack, buffer, light, overlay, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(BodyPart part, ModelPart bone, PoseStack stack, VertexConsumer buffer, int light, int overlay, Fitting fitting) {
        List<Quad> quads = meshes.get(CyberLimbRenderer.mesh(part));
        if (quads == null || !bone.visible) {
            return;
        }
        boolean arm = part == BodyPart.LEFT_ARM || part == BodyPart.RIGHT_ARM;
        float[] center = new float[]{0.0f};
        boolean[] slim = new boolean[]{false};
        if (part != BodyPart.BODY) {
            bone.visit(new PoseStack(), (pose, path, index, cube) -> {
                if (path.isEmpty() && index == 0) {
                    center[0] = (cube.minX + cube.maxX) / 32.0f;
                    slim[0] = arm && cube.maxX - cube.minX < 3.5f;
                }
            });
        }
        stack.pushPose();
        try {
            bone.translateAndRotate(stack);
            stack.translate(center[0], 0.0f, 0.0f);
            if (CyberLimbRenderer.left(part)) {
                stack.scale(-1.0f, 1.0f, 1.0f);
            }
            if (slim[0]) {
                stack.scale(0.75f, 1.0f, 1.0f);
            }
            PoseStack.Pose pose2 = stack.last();
            for (Quad quad : quads) {
                Matrix4f moved;
                Matrix4f matrix4f = moved = fitting == null ? null : fitting.pose(quad.piece());
                if (fitting != null && fitting.hidden(quad.piece())) continue;
                Matrix4f matrix = moved == null ? pose2.pose() : new Matrix4f((Matrix4fc)pose2.pose()).mul((Matrix4fc)moved);
                Vector3f normal = moved == null ? pose2.transformNormal(quad.normal(), new Vector3f()) : pose2.transformNormal(moved.transformDirection((Vector3fc)quad.normal(), new Vector3f()).normalize(), new Vector3f());
                for (int i = 0; i < quad.positions().length; ++i) {
                    Vector3f p = matrix.transformPosition((Vector3fc)quad.positions()[i], new Vector3f());
                    buffer.addVertex(p.x, p.y, p.z, -1, quad.uvs()[i * 2], quad.uvs()[i * 2 + 1], overlay, light, normal.x, normal.y, normal.z);
                }
            }
        }
        finally {
            stack.popPose();
        }
    }

    private static VertexConsumer buffer(AbstractClientPlayer player, MultiBufferSource buffers) {
        return buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.texture((Player)player, IMPLANT)));
    }

    private static boolean shown(AbstractClientPlayer player) {
        return !player.isInvisible() && !player.isSpectator();
    }

    public static void renderAll(AbstractClientPlayer player, HumanoidModel<?> model, PoseStack stack, MultiBufferSource buffers, int light) {
        CyberLimbs frames = CyberLimbs.get((Player)player);
        if (frames.installed() == 0 || !CyberLimbRenderer.shown(player)) {
            return;
        }
        int overlay = LivingEntityRenderer.getOverlayCoords((LivingEntity)player, (float)0.0f);
        for (BodyPart part : CyberLimbs.PARTS) {
            HumanoidArm side;
            if (!frames.has(part) || frames.skinned(part)) continue;
            Object object = part == BodyPart.LEFT_ARM ? HumanoidArm.LEFT : (side = part == BodyPart.RIGHT_ARM ? HumanoidArm.RIGHT : null);
            if (side != null) {
                CyberLimbRenderer.renderArm(player, side, CyberLimbRenderer.bone(model, part), stack, buffers, light);
                continue;
            }
            CyberLimbRenderer.render(part, CyberLimbRenderer.bone(model, part), stack, CyberLimbRenderer.buffer(player, buffers), light, overlay);
        }
    }

    public static void renderArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light) {
        BodyPart part = CyberLimbs.part(side);
        if (!(CyberLimbs.chrome((Player)player, part) && CyberLimbRenderer.shown(player) && arm.visible)) {
            return;
        }
        int overlay = LivingEntityRenderer.getOverlayCoords((LivingEntity)player, (float)0.0f);
        CyberLimbRenderer.render(part, arm, stack, CyberLimbRenderer.buffer(player, buffers), light, overlay, CyberLimbRenderer.fitting(player, side));
    }

    public static boolean chrome(AbstractClientPlayer player, HumanoidArm side) {
        return CyberLimbs.chrome((Player)player, CyberLimbs.part(side));
    }

    private static Fitting fitting(AbstractClientPlayer player, HumanoidArm side) {
        Fitting mantis = MantisRenderer.fitting(player, side);
        if (mantis != null) {
            return mantis;
        }
        Fitting launcher = LauncherRenderer.fitting(player, side);
        if (launcher != null) {
            return launcher;
        }
        Fitting extendo = ExtendoRenderer.fitting(player, side);
        if (extendo != null) {
            return extendo;
        }
        return GorillaArmRenderer.fitting(player, side);
    }

    public static void carried(AbstractClientPlayer player, BodyPart part, ModelPart bone, PoseStack stack, MultiBufferSource buffers, int light, int overlay) {
        CyberLimbRenderer.render(part, bone, stack, CyberLimbRenderer.buffer(player, buffers), light, overlay);
    }

    public static void preview(HumanoidModel<?> model, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, int light) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)texture));
        for (BodyPart part : CyberLimbs.PARTS) {
            CyberLimbRenderer.render(part, CyberLimbRenderer.bone(model, part), stack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
    }

    public static ModelPart bone(HumanoidModel<?> model, BodyPart part) {
        return switch (part) {
            case BodyPart.LEFT_ARM -> model.leftArm;
            case BodyPart.RIGHT_ARM -> model.rightArm;
            case BodyPart.LEFT_LEG -> model.leftLeg;
            case BodyPart.RIGHT_LEG -> model.rightLeg;
            default -> model.body;
        };
    }

    public static interface Fitting {
        public Matrix4f pose(String var1);

        public boolean hidden(String var1);
    }

    private record Quad(String group, String piece, Vector3f normal, Vector3f[] positions, float[] uvs) {
    }

    public static final class Layer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public Layer(PlayerRenderer renderer) {
            super((RenderLayerParent)renderer);
        }

        public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float swing, float amount, float tick, float age, float yaw, float pitch) {
            CyberLimbRenderer.renderAll(player, (HumanoidModel)this.getParentModel(), stack, buffers, light);
        }
    }
}

