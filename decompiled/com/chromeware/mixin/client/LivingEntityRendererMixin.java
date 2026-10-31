/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.body.hitbox.BodyHitboxes;
import com.chromeware.client.body.LimbRenderContext;
import com.chromeware.client.body.RenderedBodyHitboxes;
import com.chromeware.client.cyberware.ArmFirstPerson;
import com.chromeware.client.launcher.LauncherAnimation;
import com.chromeware.client.launcher.LauncherFirstPerson;
import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.mantis.MantisFirstPerson;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {
    @Shadow
    protected EntityModel<T> model;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @WrapMethod(method={"render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"})
    private void chrome$renderWithLimbs(T entity, float yaw, float tick, PoseStack stack, MultiBufferSource buffer, int light, Operation<Void> original) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            EntityModel<T> entityModel = this.model;
            if (entityModel instanceof HumanoidModel) {
                HumanoidModel humanoid = (HumanoidModel)entityModel;
                try (LimbRenderContext scope = LimbRenderContext.open(player, humanoid, false);){
                    original.call(new Object[]{entity, Float.valueOf(yaw), Float.valueOf(tick), stack, buffer, light});
                    return;
                }
            }
        }
        original.call(new Object[]{entity, Float.valueOf(yaw), Float.valueOf(tick), stack, buffer, light});
    }

    @Inject(method={"render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/EntityModel;prepareMobModel(Lnet/minecraft/world/entity/Entity;FFF)V")})
    private void chrome$lowerBodyToHitbox(T entity, float yaw, float tick, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (entity instanceof Player) {
            AbstractClientPlayer clientPlayer;
            Player player = (Player)entity;
            if (player instanceof AbstractClientPlayer && ArmFirstPerson.applies(clientPlayer = (AbstractClientPlayer)player)) {
                return;
            }
            stack.translate(0.0, BodyHitboxes.modelDrop(player) / ((double)player.getScale() * 0.9375), 0.0);
        }
    }

    @Inject(method={"render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V", shift=At.Shift.AFTER)})
    private void chrome$captureBodyPose(T entity, float yaw, float tick, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        PlayerModel playerModel;
        EntityModel<T> entityModel;
        AbstractClientPlayer player;
        if (entity instanceof AbstractClientPlayer) {
            player = (AbstractClientPlayer)entity;
            entityModel = this.model;
            if (entityModel instanceof PlayerModel) {
                playerModel = (PlayerModel)entityModel;
                if (ArmFirstPerson.applies(player)) {
                    ArmFirstPerson.pose(playerModel, player);
                    return;
                }
            }
        }
        if (entity instanceof AbstractClientPlayer) {
            player = (AbstractClientPlayer)entity;
            entityModel = this.model;
            if (entityModel instanceof PlayerModel) {
                playerModel = (PlayerModel)entityModel;
                boolean rightBlade = MantisAnimation.rendering(player, HumanoidArm.RIGHT);
                boolean leftBlade = MantisAnimation.rendering(player, HumanoidArm.LEFT);
                if (rightBlade || leftBlade) {
                    MantisFirstPerson.followHead(playerModel, MantisAnimation.headFollowWeight(player, tick), rightBlade, leftBlade);
                }
                for (HumanoidArm side : HumanoidArm.values()) {
                    if (!LauncherAnimation.rendering(player, side)) continue;
                    LauncherFirstPerson.followHead(playerModel, side);
                }
            }
        }
        if (entity instanceof Player) {
            player = (Player)entity;
            EntityModel<T> entityModel2 = this.model;
            if (entityModel2 instanceof HumanoidModel) {
                HumanoidModel humanoid = (HumanoidModel)entityModel2;
                RenderedBodyHitboxes.capture((Player)player, humanoid, stack);
            }
        }
    }

    @Inject(method={"render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", shift=At.Shift.AFTER)})
    private void chrome$monowireFirstPersonHardware(T entity, float yaw, float tick, PoseStack stack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer)entity;
            EntityModel<T> entityModel = this.model;
            if (entityModel instanceof HumanoidModel) {
                HumanoidModel humanoid = (HumanoidModel)entityModel;
                if (ArmFirstPerson.applies(player)) {
                    ArmFirstPerson.renderHardware(player, humanoid, stack, buffer, light);
                }
            }
        }
    }
}

