/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
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
 *  net.minecraft.world.entity.player.PlayerModelPart
 *  net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
 *  org.joml.Matrix4f
 */
package com.chromeware.client.mantis;

import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.mantis.MantisForearmCutout;
import com.chromeware.compat.HemodynamicsCompatibility;
import com.chromeware.mantis.MantisImplant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.joml.Matrix4f;

public final class MantisRenderer {
    private static Meshes left;
    private static Meshes right;
    static final Map<String, String> FRAME_BONES;

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)path);
    }

    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            BiFunction<String, String, CyberwareRig> read = (side, mode) -> {
                CyberwareRig cyberwareRig;
                InputStreamReader reader = new InputStreamReader(manager.getResourceOrThrow(MantisRenderer.id("models/cyberware/mantis_" + side + "_" + mode + ".json")).open(), StandardCharsets.UTF_8);
                try {
                    cyberwareRig = CyberwareRig.load(reader);
                }
                catch (Throwable throwable) {
                    try {
                        try {
                            reader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                        throw throwable;
                    }
                    catch (Exception e) {
                        throw new IllegalStateException("Could not load Mantis " + side + " " + mode, e);
                    }
                }
                reader.close();
                return cyberwareRig;
            };
            left = new Meshes(read.apply("left", "rig"), read.apply("left", "skin"), read.apply("left", "skin_slim"), read.apply("left", "sleeve"), read.apply("left", "sleeve_slim"));
            right = new Meshes(read.apply("right", "rig"), read.apply("right", "skin"), read.apply("right", "skin_slim"), read.apply("right", "sleeve"), read.apply("right", "sleeve_slim"));
        }));
    }

    public static void render(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light) {
        HemodynamicsCompatibility.arm((LivingEntity)player, side, true, () -> MantisRenderer.drawArm(player, side, arm, stack, buffers, light));
    }

    private static void drawArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light) {
        Meshes meshes;
        Meshes meshes2 = meshes = side == HumanoidArm.LEFT ? left : right;
        if (meshes == null || !arm.visible || !MantisImplant.hasHardware((Player)player, side) || player.isInvisible() || player.isSpectator()) {
            return;
        }
        float[] center = new float[]{side == HumanoidArm.LEFT ? 0.0625f : -0.0625f};
        boolean[] slim = new boolean[]{false};
        arm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 32.0f;
                slim[0] = cube.maxX - cube.minX < 3.5f;
            }
        });
        Function<String, CyberwareRig.Transform> animation = name -> MantisAnimation.transform(player, name);
        VertexConsumer hardware = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.texture((Player)player, "mantis_blades")));
        if (CyberLimbRenderer.chrome(player, side)) {
            meshes.hardware.render(false, slim[0], arm, center[0], stack, animation, hardware, light, OverlayTexture.NO_OVERLAY, MantisRenderer::mechanism, 0.02f);
            return;
        }
        meshes.hardware.render(false, slim[0], arm, center[0], stack, animation, hardware, light, OverlayTexture.NO_OVERLAY, bone -> !bone.endsWith("_plate_guide_head"), 0.0f);
        VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)player.getSkin().texture()));
        (slim[0] ? meshes.slimSkin : meshes.skin).render(false, slim[0], arm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
        if (player.isModelPartShown(side == HumanoidArm.LEFT ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE)) {
            (slim[0] ? meshes.slimSleeve : meshes.sleeve).render(false, slim[0], arm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
        }
    }

    static boolean mechanism(String bone) {
        if (bone.contains("_plate_guide")) {
            return false;
        }
        return bone.endsWith("_strut") || bone.endsWith("_strut_slide") || bone.endsWith("_blade") || bone.endsWith("_tip") || bone.endsWith("_extension");
    }

    public static CyberLimbRenderer.Fitting fitting(AbstractClientPlayer player, HumanoidArm side) {
        Meshes meshes;
        Meshes meshes2 = meshes = side == HumanoidArm.LEFT ? left : right;
        if (meshes == null || !MantisImplant.hasHardware((Player)player, side) || !CyberLimbRenderer.chrome(player, side)) {
            return null;
        }
        String prefix = side == HumanoidArm.LEFT ? "mantis_left_" : "mantis_right_";
        Map<String, Matrix4f> rest = meshes.hardware.poses(name -> CyberwareRig.Transform.IDENTITY);
        Map<String, Matrix4f> now = meshes.hardware.poses(name -> MantisAnimation.transform(player, name));
        boolean leftArm = side == HumanoidArm.LEFT;
        Function<String, Matrix4f> f = bone -> CyberLimbRenderer.follow((Matrix4f)rest.get(prefix + bone), (Matrix4f)now.get(prefix + bone), false, leftArm);
        HashMap<String, Matrix4f> poses = new HashMap<String, Matrix4f>();
        FRAME_BONES.forEach((piece, bone) -> poses.put((String)piece, (Matrix4f)f.apply((String)bone)));
        return CyberLimbRenderer.fitting(poses);
    }

    public static void previewArm(ModelPart arm, ModelPart sleeve, boolean slim, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, ResourceLocation skinTexture, Function<String, CyberwareRig.Transform> animation, int light) {
        if (right == null) {
            return;
        }
        VertexConsumer cut = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)skinTexture));
        float[] center = new float[]{-0.0625f};
        arm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 32.0f;
            }
        });
        for (ModelPart part : new ModelPart[]{arm, sleeve}) {
            part.visit(stack, (pose, path, index, cube) -> MantisForearmCutout.render(cube, pose, cut, light, OverlayTexture.NO_OVERLAY, -1));
        }
        MantisRenderer.right.hardware.render(false, slim, arm, center[0], stack, animation, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)texture)), light, OverlayTexture.NO_OVERLAY, bone -> !bone.endsWith("_plate_guide_head"), 0.0f);
        VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)skinTexture));
        (slim ? MantisRenderer.right.slimSkin : MantisRenderer.right.skin).render(false, slim, arm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
        (slim ? MantisRenderer.right.slimSleeve : MantisRenderer.right.sleeve).render(false, slim, arm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
    }

    public static void renderPair(AbstractClientPlayer player, HumanoidModel<?> model, PoseStack stack, MultiBufferSource buffers, int light) {
        MantisRenderer.render(player, HumanoidArm.LEFT, model.leftArm, stack, buffers, light);
        MantisRenderer.render(player, HumanoidArm.RIGHT, model.rightArm, stack, buffers, light);
    }

    static {
        FRAME_BONES = Map.of("forearm_rear_shell_front", "frame_cover_front", "forearm_rear_shell_back", "frame_cover_back", "actuator_cylinder", "strut", "actuator_rod", "strut", "forearm_front_shell", "plate_under_front");
    }

    private record Meshes(CyberwareRig hardware, CyberwareRig skin, CyberwareRig slimSkin, CyberwareRig sleeve, CyberwareRig slimSleeve) {
    }

    public static final class Layer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public Layer(PlayerRenderer renderer) {
            super((RenderLayerParent)renderer);
        }

        public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer p, float swing, float amount, float tick, float age, float yaw, float pitch) {
            MantisRenderer.renderPair(p, (HumanoidModel)this.getParentModel(), stack, buffers, light);
        }
    }
}

