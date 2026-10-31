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
 *  org.joml.Matrix4fc
 */
package com.chromeware.client.launcher;

import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.launcher.LauncherAnimation;
import com.chromeware.client.launcher.LauncherForearmCutout;
import com.chromeware.compat.HemodynamicsCompatibility;
import com.chromeware.launcher.ProjectileLauncher;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.joml.Matrix4fc;

public final class LauncherRenderer {
    private static Meshes meshes;
    private static Meshes rightMeshes;
    static final Map<String, String> FRAME_BONES;

    static CyberwareRig rightSkin(CyberwareRig source, boolean slim, boolean sleeve) {
        int width = slim ? 3 : 4;
        List<CyberwareRig.Bone> bones = source.bones().stream().map(b -> new CyberwareRig.Bone(b.name(), b.parent(), b.pivot(), b.rotation(), b.quads().stream().map(q -> {
            float sum = (q.normal().z > 0.9f ? 88 + 3 * width : (q.normal().y > 0.9f ? 80 + 3 * width : 80 + width)) + (sleeve ? 16 : 0);
            return new CyberwareRig.Quad(q.piece(), q.normal(), q.vertices().stream().map(v -> new CyberwareRig.Vertex(v.point(), sum / 64.0f - v.u(), v.v() - (sleeve ? 16.0f : 32.0f) / 64.0f)).toList());
        }).toList())).toList();
        return new CyberwareRig(bones, source.locators());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)path);
    }

    public static void reload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            Function<String, CyberwareRig> read = name -> {
                CyberwareRig cyberwareRig;
                InputStreamReader reader = new InputStreamReader(manager.getResourceOrThrow(LauncherRenderer.id("models/cyberware/projectile_launcher_" + name + ".json")).open(), StandardCharsets.UTF_8);
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
                        throw new IllegalStateException("Could not load launcher " + name, e);
                    }
                }
                reader.close();
                return cyberwareRig;
            };
            meshes = new Meshes(read.apply("rig"), read.apply("skin"), read.apply("skin_slim"), read.apply("sleeve"), read.apply("sleeve_slim"));
            rightMeshes = meshes.forRightArm();
        }));
    }

    public static void render(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light) {
        HemodynamicsCompatibility.arm((LivingEntity)player, side, false, () -> LauncherRenderer.drawArm(player, side, arm, stack, buffers, light));
    }

    private static void drawArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light) {
        if (meshes == null || !arm.visible || !ProjectileLauncher.active((Player)player, side) || player.isInvisible() || player.isSpectator()) {
            return;
        }
        boolean right = side == HumanoidArm.RIGHT;
        Meshes set = right ? rightMeshes : meshes;
        float[] center = new float[]{right ? -0.0625f : 0.0625f};
        boolean[] slim = new boolean[]{false};
        arm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 32.0f;
                slim[0] = cube.maxX - cube.minX < 3.5f;
            }
        });
        Function<String, CyberwareRig.Transform> animation = name -> LauncherAnimation.transform(player, side, name);
        VertexConsumer hardware = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.texture((Player)player, "projectile_launch_system")));
        if (CyberLimbRenderer.chrome(player, side)) {
            set.hardware.render(right, slim[0], arm, center[0], stack, animation, hardware, light, OverlayTexture.NO_OVERLAY, LauncherRenderer::mechanism, 0.02f);
            return;
        }
        set.hardware.render(right, slim[0], arm, center[0], stack, animation, hardware, light, OverlayTexture.NO_OVERLAY);
        VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)player.getSkin().texture()));
        (slim[0] ? set.slimSkin : set.skin).render(right, slim[0], arm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
        if (player.isModelPartShown(right ? PlayerModelPart.RIGHT_SLEEVE : PlayerModelPart.LEFT_SLEEVE)) {
            (slim[0] ? set.slimSleeve : set.sleeve).render(right, slim[0], arm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
        }
    }

    static boolean mechanism(String bone) {
        return bone.equals("launcher_carriage") || bone.equals("launcher_barrel") || bone.startsWith("launcher_link_") || bone.startsWith("launcher_muscle_") || bone.startsWith("launcher_guide_");
    }

    public static CyberLimbRenderer.Fitting fitting(AbstractClientPlayer player, HumanoidArm side) {
        if (meshes == null || !ProjectileLauncher.active((Player)player, side) || !CyberLimbRenderer.chrome(player, side)) {
            return null;
        }
        Map<String, Matrix4f> rest = LauncherRenderer.meshes.hardware.poses(name -> CyberwareRig.Transform.IDENTITY);
        Map<String, Matrix4f> now = LauncherRenderer.meshes.hardware.poses(name -> LauncherAnimation.transform(player, side, name));
        boolean right = side == HumanoidArm.RIGHT;
        HashMap<String, Matrix4f> poses = new HashMap<String, Matrix4f>();
        FRAME_BONES.forEach((piece, bone) -> {
            if (bone.equals("launcher_hand")) {
                Matrix4f flip = new Matrix4f().scale(-1.0f, 1.0f, 1.0f);
                poses.put((String)piece, new Matrix4f((Matrix4fc)flip).mul((Matrix4fc)now.get(bone)).mul((Matrix4fc)new Matrix4f((Matrix4fc)rest.get(bone)).invert()).mul((Matrix4fc)flip));
            } else {
                poses.put((String)piece, CyberLimbRenderer.follow((Matrix4f)rest.get(bone), (Matrix4f)now.get(bone), right, !right));
            }
        });
        return CyberLimbRenderer.fitting(poses);
    }

    public static void renderPair(AbstractClientPlayer player, HumanoidModel<?> model, PoseStack stack, MultiBufferSource buffers, int light) {
        LauncherRenderer.render(player, HumanoidArm.LEFT, model.leftArm, stack, buffers, light);
        LauncherRenderer.render(player, HumanoidArm.RIGHT, model.rightArm, stack, buffers, light);
    }

    public static void previewArm(ModelPart leftArm, ModelPart sleeve, boolean slim, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, ResourceLocation skinTexture, Function<String, CyberwareRig.Transform> animation, int light) {
        if (meshes == null) {
            return;
        }
        VertexConsumer cut = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)skinTexture));
        float[] center = new float[]{0.0625f};
        leftArm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 32.0f;
            }
        });
        for (ModelPart part : new ModelPart[]{leftArm, sleeve}) {
            part.visit(stack, (pose, path, index, cube) -> LauncherForearmCutout.render(cube, true, pose, cut, light, OverlayTexture.NO_OVERLAY, -1));
        }
        LauncherRenderer.meshes.hardware.render(false, slim, leftArm, center[0], stack, animation, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)texture)), light, OverlayTexture.NO_OVERLAY);
        VertexConsumer skin = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)skinTexture));
        (slim ? LauncherRenderer.meshes.slimSkin : LauncherRenderer.meshes.skin).render(false, slim, leftArm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
        (slim ? LauncherRenderer.meshes.slimSleeve : LauncherRenderer.meshes.sleeve).render(false, slim, leftArm, center[0], stack, animation, skin, light, OverlayTexture.NO_OVERLAY);
    }

    static {
        FRAME_BONES = Map.of("inner_forearm_cheek", "launcher_panel_prox_front", "outer_forearm_cheek", "launcher_panel_prox_back", "forearm_rear_shell_front", "launcher_panel_dist_front", "forearm_rear_shell_back", "launcher_panel_dist_back", "forearm_front_shell", "launcher_under_dist", "wrist_band", "launcher_hand", "palm", "launcher_hand", "backhand_plate", "launcher_hand");
    }

    private record Meshes(CyberwareRig hardware, CyberwareRig skin, CyberwareRig slimSkin, CyberwareRig sleeve, CyberwareRig slimSleeve) {
        Meshes forRightArm() {
            return new Meshes(this.hardware, LauncherRenderer.rightSkin(this.skin, false, false), LauncherRenderer.rightSkin(this.slimSkin, true, false), LauncherRenderer.rightSkin(this.sleeve, false, true), LauncherRenderer.rightSkin(this.slimSleeve, true, true));
        }
    }

    public static final class Layer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public Layer(PlayerRenderer renderer) {
            super((RenderLayerParent)renderer);
        }

        public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float swing, float amount, float tick, float age, float yaw, float pitch) {
            LauncherRenderer.renderPair(player, (HumanoidModel)this.getParentModel(), stack, buffers, light);
        }
    }
}

