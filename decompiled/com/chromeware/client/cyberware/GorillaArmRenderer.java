/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
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
 *  net.minecraft.world.entity.player.PlayerModelPart
 *  net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
 *  org.joml.Matrix4f
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.GorillaGeometry;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.compat.HemodynamicsCompatibility;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
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
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.joml.Matrix4f;

public final class GorillaArmRenderer {
    private static final ResourceLocation MESH = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"models/cyberware/gorilla_arms_mesh.json");
    private static volatile GorillaGeometry geometry;

    public static void registerReloads(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> {
            try (InputStreamReader reader = new InputStreamReader(manager.getResourceOrThrow(MESH).open(), StandardCharsets.UTF_8);){
                geometry = GorillaGeometry.load(reader);
            }
            catch (Exception exception) {
                throw new IllegalStateException("Could not load Gorilla Arms mesh", exception);
            }
        }));
    }

    public static void render(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light) {
        HemodynamicsCompatibility.arm((LivingEntity)player, side, false, () -> GorillaArmRenderer.drawArm(player, side, arm, stack, buffers, light));
    }

    private static void drawArm(AbstractClientPlayer player, HumanoidArm side, ModelPart arm, PoseStack stack, MultiBufferSource buffers, int light) {
        if (geometry == null || !arm.visible || !ArmImplants.has((Player)player, side, s -> s.is(CyberwareItems.GORILLA_ARMS)) || !ArmImplants.active((Player)player, side) || player.isInvisible() || player.isSpectator()) {
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
        int frameOverlay = LivingEntityRenderer.getOverlayCoords((LivingEntity)player, (float)0.0f);
        if (CyberLimbRenderer.chrome(player, side)) {
            geometry.renderFrameHardware(side == HumanoidArm.LEFT, slim[0], arm, center[0], stack, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ImplantTint.texture((Player)player, "gorilla_arms"))), light, frameOverlay);
            return;
        }
        ResourceLocation skin = player.getSkin().texture();
        boolean sleeve = player.isModelPartShown(side == HumanoidArm.LEFT ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE);
        int overlay = LivingEntityRenderer.getOverlayCoords((LivingEntity)player, (float)0.0f);
        Function<String, VertexConsumer> materials = material -> buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)(material.equals("hardware") ? ImplantTint.texture((Player)player, "gorilla_arms") : skin)));
        geometry.renderArm(side == HumanoidArm.LEFT, slim[0], arm, center[0], stack, materials, sleeve, light, overlay);
    }

    public static CyberLimbRenderer.Fitting fitting(AbstractClientPlayer player, HumanoidArm side) {
        if (!(geometry != null && ArmImplants.has((Player)player, side, s -> s.is(CyberwareItems.GORILLA_ARMS)) && ArmImplants.active((Player)player, side) && CyberLimbRenderer.chrome(player, side))) {
            return null;
        }
        return new CyberLimbRenderer.Fitting(){

            @Override
            public Matrix4f pose(String piece) {
                return null;
            }

            @Override
            public boolean hidden(String piece) {
                return piece.equals("actuator_cylinder") || piece.equals("actuator_rod");
            }
        };
    }

    public static void previewArm(ModelPart arm, boolean slim, PoseStack stack, MultiBufferSource buffers, ResourceLocation texture, ResourceLocation skinTexture, int light) {
        if (geometry == null) {
            return;
        }
        float[] center = new float[]{-0.0625f};
        arm.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty() && index == 0) {
                center[0] = (cube.minX + cube.maxX) / 32.0f;
            }
        });
        geometry.renderArm(false, slim, arm, center[0], stack, material -> buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)(material.equals("hardware") ? texture : skinTexture))), true, light, OverlayTexture.NO_OVERLAY);
    }

    public static final class Layer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        public Layer(PlayerRenderer renderer) {
            super((RenderLayerParent)renderer);
        }

        public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float swing, float amount, float tick, float age, float yaw, float pitch) {
            GorillaArmRenderer.render(player, HumanoidArm.LEFT, ((PlayerModel)this.getParentModel()).leftArm, stack, buffers, light);
            GorillaArmRenderer.render(player, HumanoidArm.RIGHT, ((PlayerModel)this.getParentModel()).rightArm, stack, buffers, light);
        }
    }
}

