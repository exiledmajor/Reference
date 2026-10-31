/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelLayers
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.resources.PlayerSkin$Model
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 */
package com.chromeware.client.surgery;

import com.chromeware.body.BodyPart;
import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.cyberware.SandevistanGeometry;
import com.chromeware.client.cyberware.SandevistanLayer;
import com.chromeware.client.surgery.SurgeryCarry;
import com.chromeware.surgery.SurgerySeat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public class SurgeryPropLayer
extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static PlayerModel<AbstractClientPlayer> wide;
    private static PlayerModel<AbstractClientPlayer> slim;

    public SurgeryPropLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    public static boolean drawsSandevistan(AbstractClientPlayer player) {
        Entity entity = player.getVehicle();
        if (!(entity instanceof SurgerySeat)) {
            return false;
        }
        SurgerySeat seat = (SurgerySeat)entity;
        SurgeryCarry carry = SurgeryCarry.of(seat, 0.0f);
        return carry != null && carry.sandevistan();
    }

    public void render(PoseStack stack, MultiBufferSource buffers, int light, AbstractClientPlayer player, float limbSwing, float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        SurgerySeat seat;
        block12: {
            block11: {
                Entity entity = player.getVehicle();
                if (!(entity instanceof SurgerySeat)) break block11;
                seat = (SurgerySeat)entity;
                if (!player.isInvisible()) break block12;
            }
            return;
        }
        SurgeryCarry carry = SurgeryCarry.of(seat, partialTick);
        if (carry == null) {
            return;
        }
        PlayerModel model = (PlayerModel)this.getParentModel();
        int overlay = LivingEntityRenderer.getOverlayCoords((LivingEntity)player, (float)0.0f);
        stack.pushPose();
        Vector3f offset = new Matrix4f((Matrix4fc)stack.last().pose()).invert().transformDirection(new Vector3f((float)carry.offset().x, (float)carry.offset().y, (float)carry.offset().z));
        stack.translate(offset.x, offset.y, offset.z);
        if (carry.sandevistan()) {
            SandevistanGeometry geometry = SandevistanLayer.geometry();
            if (geometry != null) {
                geometry.render(stack, model.body, model.head, model.hat.visible ? 0.009375f : 0.0f, model.head.visible, buffers.getBuffer(SandevistanLayer.renderType((Player)player)), light, overlay);
            }
        } else {
            PlayerModel<AbstractClientPlayer> fresh = SurgeryPropLayer.fresh(player.getSkin().model() == PlayerSkin.Model.SLIM);
            if (carry.severed()) {
                VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent((ResourceLocation)player.getSkin().texture()));
                for (ModelPart[] pair : SurgeryPropLayer.parts(fresh, model, carry.part())) {
                    pair[0].copyFrom(pair[1]);
                    pair[0].visible = true;
                    pair[0].render(stack, buffer, light, overlay);
                }
            } else {
                ModelPart[] bone = SurgeryPropLayer.parts(fresh, model, carry.part())[0];
                bone[0].copyFrom(bone[1]);
                bone[0].visible = true;
                CyberLimbRenderer.carried(player, carry.part(), bone[0], stack, buffers, light, overlay);
            }
        }
        stack.popPose();
    }

    private static ModelPart[][] parts(PlayerModel<?> fresh, PlayerModel<?> model, BodyPart part) {
        ModelPart[][] modelPartArrayArray;
        switch (part) {
            case LEFT_ARM: {
                ModelPart[][] modelPartArrayArray2 = new ModelPart[2][];
                modelPartArrayArray2[0] = new ModelPart[]{fresh.leftArm, model.leftArm};
                modelPartArrayArray = modelPartArrayArray2;
                modelPartArrayArray2[1] = new ModelPart[]{fresh.leftSleeve, model.leftSleeve};
                break;
            }
            case RIGHT_ARM: {
                ModelPart[][] modelPartArrayArray3 = new ModelPart[2][];
                modelPartArrayArray3[0] = new ModelPart[]{fresh.rightArm, model.rightArm};
                modelPartArrayArray = modelPartArrayArray3;
                modelPartArrayArray3[1] = new ModelPart[]{fresh.rightSleeve, model.rightSleeve};
                break;
            }
            case LEFT_LEG: {
                ModelPart[][] modelPartArrayArray4 = new ModelPart[2][];
                modelPartArrayArray4[0] = new ModelPart[]{fresh.leftLeg, model.leftLeg};
                modelPartArrayArray = modelPartArrayArray4;
                modelPartArrayArray4[1] = new ModelPart[]{fresh.leftPants, model.leftPants};
                break;
            }
            case RIGHT_LEG: {
                ModelPart[][] modelPartArrayArray5 = new ModelPart[2][];
                modelPartArrayArray5[0] = new ModelPart[]{fresh.rightLeg, model.rightLeg};
                modelPartArrayArray = modelPartArrayArray5;
                modelPartArrayArray5[1] = new ModelPart[]{fresh.rightPants, model.rightPants};
                break;
            }
            default: {
                ModelPart[][] modelPartArrayArray6 = new ModelPart[2][];
                modelPartArrayArray6[0] = new ModelPart[]{fresh.body, model.body};
                modelPartArrayArray = modelPartArrayArray6;
                modelPartArrayArray6[1] = new ModelPart[]{fresh.jacket, model.jacket};
            }
        }
        return modelPartArrayArray;
    }

    private static PlayerModel<AbstractClientPlayer> fresh(boolean slimArms) {
        if (slimArms && slim == null) {
            slim = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
        }
        if (!slimArms && wide == null) {
            wide = new PlayerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        }
        return slimArms ? slim : wide;
    }
}

