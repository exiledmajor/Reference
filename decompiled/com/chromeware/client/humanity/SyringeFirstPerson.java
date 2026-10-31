/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.neoforge.client.event.RenderHandEvent
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.humanity;

import com.chromeware.client.cyberware.FreeHands;
import com.chromeware.client.humanity.SyringeAnimation;
import com.chromeware.humanity.EmptySyringeItem;
import com.chromeware.humanity.ImmunosuppressantItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class SyringeFirstPerson {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, SyringeFirstPerson::render);
    }

    private static void render(RenderHandEvent event) {
        ItemInHandRenderer itemInHandRenderer;
        InteractionHand syringeHand;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !SyringeAnimation.selfInjecting((AbstractClientPlayer)player)) {
            return;
        }
        Object object = SyringeFirstPerson.syringe(player.getMainHandItem()) ? InteractionHand.MAIN_HAND : (syringeHand = SyringeFirstPerson.syringe(player.getOffhandItem()) ? InteractionHand.OFF_HAND : null);
        if (syringeHand == null) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != syringeHand) {
            return;
        }
        HumanoidArm arm = syringeHand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        float f = arm == HumanoidArm.RIGHT ? 1.0f : -1.0f;
        float t = SyringeAnimation.ticks((AbstractClientPlayer)player, event.getPartialTick());
        float raise = SyringeFirstPerson.smooth(t / 20.0f) * (1.0f - SyringeFirstPerson.smooth((t - 80.0f) / 20.0f));
        float insert = SyringeFirstPerson.smooth((t - 25.0f) / 10.0f) * (1.0f - SyringeFirstPerson.smooth((t - 80.0f) / 8.0f));
        PoseStack pose = event.getPoseStack();
        if (mc.getEntityRenderDispatcher().getRenderer((Entity)player) != null && (itemInHandRenderer = mc.gameRenderer.itemInHandRenderer) instanceof FreeHands.ArmView) {
            FreeHands.ArmView view = (FreeHands.ArmView)itemInHandRenderer;
            pose.pushPose();
            pose.translate(f * 0.26f * raise, 0.22f * raise, 0.0f);
            view.chrome$renderImplantArm(new RenderHandEvent(syringeHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, pose, event.getMultiBufferSource(), event.getPackedLight(), event.getPartialTick(), event.getInterpolatedPitch(), 0.0f, 1.0f - raise, ItemStack.EMPTY), arm.getOpposite());
            pose.popPose();
        }
        pose.pushPose();
        pose.translate(f * (0.56f - 0.5f * raise - 0.06f * insert), -0.52f + 0.52f * raise - 0.05f * insert, -0.72f - 0.18f * raise);
        pose.mulPose(Axis.ZP.rotationDegrees(f * 120.0f * raise));
        pose.scale(1.0f - 0.25f * raise, 1.0f - 0.25f * raise, 1.0f - 0.25f * raise);
        ItemStack stack = syringeHand == InteractionHand.MAIN_HAND ? player.getMainHandItem() : player.getOffhandItem();
        mc.gameRenderer.itemInHandRenderer.renderItem((LivingEntity)player, stack, arm == HumanoidArm.RIGHT ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, arm == HumanoidArm.LEFT, pose, event.getMultiBufferSource(), event.getPackedLight());
        pose.popPose();
    }

    private static boolean syringe(ItemStack stack) {
        return stack.getItem() instanceof ImmunosuppressantItem || stack.getItem() instanceof EmptySyringeItem;
    }

    private static float smooth(float x) {
        x = Mth.clamp((float)x, (float)0.0f, (float)1.0f);
        return x * x * (3.0f - 2.0f * x);
    }
}

