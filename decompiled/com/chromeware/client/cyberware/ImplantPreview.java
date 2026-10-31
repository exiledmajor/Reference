/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.Lighting
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.EntityModelSet
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.resources.DefaultPlayerSkin
 *  net.minecraft.client.resources.PlayerSkin
 *  net.minecraft.client.resources.PlayerSkin$Model
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.GorillaArmRenderer;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.cyberware.PreviewDeploy;
import com.chromeware.client.cyberware.SandevistanLayer;
import com.chromeware.client.extendo.ExtendoRenderer;
import com.chromeware.client.launcher.LauncherRenderer;
import com.chromeware.client.mantis.MantisRenderer;
import com.chromeware.client.monowire.MonowireWristRenderer;
import com.chromeware.cyberware.ImplantColors;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;

final class ImplantPreview {
    private static ModelPart player;
    private static HumanoidModel<?> mannequin;
    private static PlayerModel<?> wide;
    private static PlayerModel<?> slim;

    ImplantPreview() {
    }

    private static ModelPart part(String name) {
        if (player == null) {
            player = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER);
        }
        ModelPart part = player.getChild(name);
        part.resetPose();
        return part;
    }

    private static HumanoidModel<?> mannequin() {
        if (mannequin == null) {
            mannequin = new HumanoidModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
        }
        for (String name : new String[]{"head", "body", "right_arm", "left_arm", "right_leg", "left_leg"}) {
            ImplantPreview.part(name);
        }
        ImplantPreview.mannequin.head.resetPose();
        ImplantPreview.mannequin.body.resetPose();
        ImplantPreview.mannequin.rightArm.resetPose();
        ImplantPreview.mannequin.leftArm.resetPose();
        ImplantPreview.mannequin.rightLeg.resetPose();
        ImplantPreview.mannequin.leftLeg.resetPose();
        return mannequin;
    }

    private static PlayerSkin skin() {
        LocalPlayer viewer = Minecraft.getInstance().player;
        return viewer != null ? viewer.getSkin() : DefaultPlayerSkin.get((UUID)new UUID(0L, 0L));
    }

    private static PlayerModel<?> arms(boolean slimArms) {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        if (slimArms && slim == null) {
            slim = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        }
        if (!slimArms && wide == null) {
            wide = new PlayerModel(models.bakeLayer(ModelLayers.PLAYER), false);
        }
        PlayerModel<?> model = slimArms ? slim : wide;
        for (ModelPart p : new ModelPart[]{model.rightArm, model.leftArm, model.rightSleeve, model.leftSleeve}) {
            p.resetPose();
        }
        model.leftSleeve.y = 2.0f;
        model.rightSleeve.y = 2.0f;
        model.leftArm.y = 2.0f;
        model.rightArm.y = 2.0f;
        return model;
    }

    static float defaultYaw(String implant) {
        return implant.equals("sandevistan") ? 180.0f : (implant.equals("projectile_launch_system") ? -35.0f : 35.0f);
    }

    private static int wire(ImplantColors colors) {
        Integer color = colors.get("monowire", "wire");
        return color == null ? -1 : color;
    }

    static void render(GuiGraphics graphics, String implant, ImplantColors colors, float cx, float cy, float size, float yaw, float pitch, float deploySeconds) {
        ResourceLocation texture = ImplantTint.texture(colors, implant);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(cx, cy, 150.0f);
        pose.scale(size, size, -size);
        pose.mulPose(Axis.XP.rotationDegrees(-pitch));
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        switch (implant) {
            case "sandevistan": {
                pose.translate(0.0f, -0.4375f, 0.0f);
                break;
            }
            case "projectile_launch_system": {
                pose.translate(-0.375f, -0.5625f, 0.0f);
                break;
            }
            case "cyber_limbs": {
                pose.scale(0.55f, 0.55f, 0.55f);
                pose.translate(0.0f, -0.75f, 0.0f);
                break;
            }
            default: {
                pose.translate(0.375f, -0.5625f, 0.0f);
            }
        }
        if (PreviewDeploy.clip(implant, HumanoidArm.RIGHT) != null || implant.equals("gorilla_arms") || implant.equals("extendo_grip")) {
            pose.translate(implant.equals("projectile_launch_system") ? 0.375f : -0.375f, 0.5625f, 0.0f);
            pose.scale(0.66f, 0.66f, 0.66f);
            pose.translate(implant.equals("projectile_launch_system") ? -0.375f : 0.375f, -0.5625f, 0.0f);
        }
        Lighting.setupForEntityInInventory();
        MultiBufferSource.BufferSource buffers = graphics.bufferSource();
        int light = 0xF000F0;
        PlayerSkin skin = ImplantPreview.skin();
        boolean slimArms = skin.model() == PlayerSkin.Model.SLIM;
        ResourceLocation skinTexture = skin.texture();
        HumanoidArm side = implant.equals("projectile_launch_system") ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        String clip = PreviewDeploy.clip(implant, side);
        Function<String, CyberwareRig.Transform> deployed = clip == null ? null : PreviewDeploy.pose(clip, deploySeconds);
        Function<String, CyberwareRig.Transform> animation = deployed != null ? deployed : name -> CyberwareRig.Transform.IDENTITY;
        PlayerModel<?> arms = ImplantPreview.arms(slimArms);
        switch (implant) {
            case "sandevistan": {
                SandevistanLayer.preview(ImplantPreview.part("body"), ImplantPreview.part("head"), pose, (MultiBufferSource)buffers, texture, light);
                break;
            }
            case "mantis_blades": {
                MantisRenderer.previewArm(arms.rightArm, arms.rightSleeve, slimArms, pose, (MultiBufferSource)buffers, texture, skinTexture, animation, light);
                break;
            }
            case "gorilla_arms": {
                GorillaArmRenderer.previewArm(arms.rightArm, slimArms, pose, (MultiBufferSource)buffers, texture, skinTexture, light);
                break;
            }
            case "projectile_launch_system": {
                LauncherRenderer.previewArm(arms.leftArm, arms.leftSleeve, slimArms, pose, (MultiBufferSource)buffers, texture, skinTexture, animation, light);
                break;
            }
            case "extendo_grip": {
                ExtendoRenderer.previewArm(arms.rightArm, arms.rightSleeve, pose, (MultiBufferSource)buffers, texture, skinTexture, deploySeconds, light);
                break;
            }
            case "monowire": {
                MonowireWristRenderer.previewArm(arms.rightArm, arms.rightSleeve, slimArms, pose, (MultiBufferSource)buffers, texture, skinTexture, animation, light, new Vec3((double)cx, (double)cy, 150.0), ImplantPreview.wire(colors));
                break;
            }
            case "cyber_limbs": {
                CyberLimbRenderer.preview(ImplantPreview.mannequin(), pose, (MultiBufferSource)buffers, texture, light);
                break;
            }
        }
        graphics.flush();
        Lighting.setupFor3DItems();
        pose.popPose();
    }
}

