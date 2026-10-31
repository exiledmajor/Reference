/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.HumanoidModel$ArmPose
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.body.BodyFunctions;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.OneHanded;
import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.body.LimbRenderContext;
import com.chromeware.client.cyberware.ArmFirstPerson;
import com.chromeware.client.cyberware.GorillaArmRenderer;
import com.chromeware.client.cyberware.GorillaFirstPerson;
import com.chromeware.client.extendo.ExtendoClient;
import com.chromeware.client.extendo.ExtendoRenderer;
import com.chromeware.client.launcher.LauncherRenderer;
import com.chromeware.client.mantis.MantisRenderer;
import com.chromeware.client.monowire.MonowireFirstPerson;
import com.chromeware.client.monowire.MonowireProjection;
import com.chromeware.client.monowire.MonowireWristRenderer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PlayerRenderer.class})
public abstract class PlayerRendererMixin {
    @WrapMethod(method={"setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFFF)V"})
    private void chrome$monowireCamera(AbstractClientPlayer player, PoseStack stack, float age, float bodyYaw, float tick, float scale, Operation<Void> original) {
        original.call(new Object[]{player, stack, Float.valueOf(age), Float.valueOf(bodyYaw), Float.valueOf(tick), Float.valueOf(scale)});
        if (ArmFirstPerson.applies(player)) {
            ArmFirstPerson.place(stack, Minecraft.getInstance().gameRenderer.getMainCamera().rotation(), scale, MonowireProjection.ratio(tick));
        }
    }

    @Inject(method={"setModelProperties"}, at={@At(value="TAIL")})
    private void chrome$remainingArmPoses(AbstractClientPlayer player, CallbackInfo ci) {
        if (ArmFirstPerson.applies(player)) {
            MonowireFirstPerson.prepareCameraModel((HumanoidModel)((PlayerRenderer)this).getModel());
        }
        if (BodyFunctions.usableHands(LimbAttachments.functional((Player)player)) == 2) {
            return;
        }
        PlayerModel model = (PlayerModel)((PlayerRenderer)this).getModel();
        HumanoidModel.ArmPose main = player.getMainArm() == HumanoidArm.RIGHT ? model.rightArmPose : model.leftArmPose;
        HumanoidModel.ArmPose off = player.getMainArm() == HumanoidArm.RIGHT ? model.leftArmPose : model.rightArmPose;
        model.rightArmPose = model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        HumanoidArm arm = OneHanded.resolve((Player)player, InteractionHand.MAIN_HAND);
        if (arm == HumanoidArm.RIGHT) {
            model.rightArmPose = main;
        } else if (arm == HumanoidArm.LEFT) {
            model.leftArmPose = main;
        }
        arm = OneHanded.resolve((Player)player, InteractionHand.OFF_HAND);
        if (arm == HumanoidArm.RIGHT) {
            model.rightArmPose = off;
        } else if (arm == HumanoidArm.LEFT) {
            model.leftArmPose = off;
        }
    }

    @WrapMethod(method={"renderHand"})
    private void chrome$firstPersonLimb(PoseStack stack, MultiBufferSource buffer, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, Operation<Void> original) {
        PlayerModel model = (PlayerModel)((PlayerRenderer)this).getModel();
        try (LimbRenderContext scope = LimbRenderContext.open((Player)player, model, false);){
            HumanoidArm side;
            original.call(new Object[]{stack, buffer, light, player, arm, sleeve});
            HumanoidArm humanoidArm = side = arm == model.leftArm ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            if (GorillaFirstPerson.showsArm((Player)player, side)) {
                GorillaArmRenderer.render(player, side, arm, stack, buffer, light);
            }
            CyberLimbRenderer.renderArm(player, side, arm, stack, buffer, light);
            MonowireWristRenderer.render(player, side, arm, stack, buffer, light, true);
            LauncherRenderer.render(player, side, arm, stack, buffer, light);
            MantisRenderer.render(player, side, arm, stack, buffer, light);
            ExtendoRenderer.render(player, side, arm, sleeve, stack, buffer, light);
        }
    }

    @Inject(method={"renderHand"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V", ordinal=0)})
    private void chrome$extendoArm(PoseStack stack, MultiBufferSource buffer, int light, AbstractClientPlayer player, ModelPart arm, ModelPart sleeve, CallbackInfo ci) {
        if (!ExtendoClient.straightArm()) {
            return;
        }
        for (ModelPart part : new ModelPart[]{arm, sleeve}) {
            part.z = 0.0f;
            part.y = 0.0f;
            part.x = 0.0f;
            part.zRot = 0.0f;
            part.yRot = 0.0f;
            part.xRot = 0.0f;
        }
    }
}

