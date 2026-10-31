/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.zigythebird.playeranim.accessors.IAnimatedPlayer
 *  com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.world.entity.HumanoidArm
 *  org.joml.Quaternionf
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.GorillaAnimation;
import com.chromeware.client.cyberware.GorillaArmRenderer;
import com.chromeware.client.extendo.ExtendoRenderer;
import com.chromeware.client.launcher.LauncherAnimation;
import com.chromeware.client.launcher.LauncherFirstPerson;
import com.chromeware.client.launcher.LauncherRenderer;
import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.mantis.MantisFirstPerson;
import com.chromeware.client.mantis.MantisRenderer;
import com.chromeware.client.monowire.MonowireAnimation;
import com.chromeware.client.monowire.MonowireClient;
import com.chromeware.client.monowire.MonowireFirstPerson;
import com.chromeware.client.monowire.MonowireWristRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;

public final class ArmFirstPerson {
    public static boolean applies(AbstractClientPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        return FirstPersonMode.isFirstPersonPass() && mc.getCameraEntity() == player && mc.options.getCameraType().isFirstPerson() && ArmFirstPerson.hardwareRendering(player);
    }

    public static boolean hardwareRendering(AbstractClientPlayer player) {
        return MantisAnimation.rendering(player) || LauncherAnimation.rendering(player) || MonowireAnimation.rendering(player) || GorillaAnimation.rendering(player);
    }

    public static void place(PoseStack stack, Quaternionf cameraRotation, float scale, double projectionRatio) {
        MonowireFirstPerson.place(stack, cameraRotation, scale, projectionRatio);
    }

    public static boolean ownsArm(AbstractClientPlayer player, HumanoidArm side) {
        return MantisAnimation.rendering(player, side) || LauncherAnimation.rendering(player, side) || MonowireAnimation.rendering(player, side) || MonowireAnimation.holdsOther(player, side.getOpposite()) || GorillaAnimation.rendering(player, side);
    }

    public static boolean vanillaHandsCancelled(AbstractClientPlayer player) {
        IAnimatedPlayer animated;
        return player instanceof IAnimatedPlayer && (animated = (IAnimatedPlayer)player).playerAnimLib$getAnimManager().getFirstPersonTransitionProgress() == 1.0f;
    }

    public static void pose(PlayerModel<?> model, AbstractClientPlayer player) {
        MonowireFirstPerson.prepareCameraModel(model);
        for (HumanoidArm side : HumanoidArm.values()) {
            boolean left;
            ModelPart arm = side == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
            ModelPart sleeve = side == HumanoidArm.LEFT ? model.leftSleeve : model.rightSleeve;
            boolean bl = left = side == HumanoidArm.LEFT;
            if (MantisAnimation.rendering(player, side)) {
                MantisFirstPerson.cameraArm(arm, left, MantisAnimation.drawWeight(player, side, Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true)), MantisAnimation.transform(player, "mantis_" + (left ? "left" : "right") + "_view"));
            } else if (MonowireAnimation.rendering(player, side)) {
                MonowireFirstPerson.cameraArm(arm, MonowireAnimation.view(player, side, "wire_view"));
            } else if (MonowireAnimation.holdsOther(player, side.getOpposite())) {
                MonowireFirstPerson.cameraArm(arm, MonowireAnimation.view(player, side.getOpposite(), "offhand_view"));
            } else if (LauncherAnimation.rendering(player, side)) {
                LauncherFirstPerson.cameraArm(arm, LauncherAnimation.view(player, side));
            } else if (GorillaAnimation.rendering(player, side)) {
                GorillaAnimation.cameraArm(player, side, arm);
            } else {
                arm.visible = false;
                sleeve.visible = false;
                continue;
            }
            sleeve.copyFrom(arm);
        }
    }

    public static void renderHardware(AbstractClientPlayer player, HumanoidModel<?> model, PoseStack stack, MultiBufferSource buffers, int light) {
        for (HumanoidArm side : HumanoidArm.values()) {
            if (!ArmFirstPerson.ownsArm(player, side)) continue;
            ModelPart arm = side == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
            MonowireClient.captureArm(player, side, arm, stack);
            CyberLimbRenderer.renderArm(player, side, arm, stack, buffers, light);
            MonowireWristRenderer.render(player, side, arm, stack, buffers, light, false);
            LauncherRenderer.render(player, side, arm, stack, buffers, light);
            MantisRenderer.render(player, side, arm, stack, buffers, light);
            if (model instanceof PlayerModel) {
                PlayerModel skin = (PlayerModel)model;
                ExtendoRenderer.render(player, side, arm, side == HumanoidArm.LEFT ? skin.leftSleeve : skin.rightSleeve, stack, buffers, light);
            }
            GorillaArmRenderer.render(player, side, arm, stack, buffers, light);
        }
        MonowireWristRenderer.captureGrips(player, model, stack);
    }
}

