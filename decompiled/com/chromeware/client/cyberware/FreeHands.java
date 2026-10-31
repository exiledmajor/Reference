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
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.fml.ModList
 *  net.neoforged.neoforge.client.ClientHooks
 *  net.neoforged.neoforge.client.event.RenderHandEvent
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.compat.EpicFightClient;
import com.chromeware.client.cyberware.ArmFirstPerson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderHandEvent;

public final class FreeHands {
    private static ItemStack mainItem = ItemStack.EMPTY;
    private static ItemStack offItem = ItemStack.EMPTY;
    private static float mainHeight;
    private static float oMainHeight;
    private static float offHeight;
    private static float oOffHeight;

    public static void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        oMainHeight = mainHeight;
        oOffHeight = offHeight;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (ItemStack.matches((ItemStack)mainItem, (ItemStack)main)) {
            mainItem = main;
        }
        if (ItemStack.matches((ItemStack)offItem, (ItemStack)off)) {
            offItem = off;
        }
        if (player.isHandsBusy()) {
            mainHeight = Mth.clamp((float)(mainHeight - 0.4f), (float)0.0f, (float)1.0f);
            offHeight = Mth.clamp((float)(offHeight - 0.4f), (float)0.0f, (float)1.0f);
        } else {
            float strength = player.getAttackStrengthScale(1.0f);
            boolean reequipMain = ClientHooks.shouldCauseReequipAnimation((ItemStack)mainItem, (ItemStack)main, (int)player.getInventory().selected);
            boolean reequipOff = ClientHooks.shouldCauseReequipAnimation((ItemStack)offItem, (ItemStack)off, (int)-1);
            if (!reequipMain && mainItem != main) {
                mainItem = main;
            }
            if (!reequipOff && offItem != off) {
                offItem = off;
            }
            mainHeight += Mth.clamp((float)((reequipMain ? 0.0f : strength * strength * strength) - mainHeight), (float)-0.4f, (float)0.4f);
            offHeight += Mth.clamp((float)((float)(!reequipOff ? 1 : 0) - offHeight), (float)-0.4f, (float)0.4f);
        }
        if (mainHeight < 0.1f) {
            mainItem = main;
        }
        if (offHeight < 0.1f) {
            offItem = off;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(ItemInHandRenderer renderer, float tick, PoseStack stack, MultiBufferSource.BufferSource buffers, LocalPlayer player, int light) {
        boolean off;
        Renderer hands;
        block12: {
            block11: {
                if (!(renderer instanceof Renderer)) break block11;
                hands = (Renderer)renderer;
                if (ArmFirstPerson.applies((AbstractClientPlayer)player) && ArmFirstPerson.vanillaHandsCancelled((AbstractClientPlayer)player)) break block12;
            }
            return;
        }
        HumanoidArm mainArm = player.getMainArm();
        boolean main = !ArmFirstPerson.ownsArm((AbstractClientPlayer)player, mainArm);
        boolean bl = off = !ArmFirstPerson.ownsArm((AbstractClientPlayer)player, mainArm.getOpposite());
        if (!main && !off) {
            return;
        }
        float attack = player.getAttackAnim(tick);
        InteractionHand swinging = player.swingingArm == null ? InteractionHand.MAIN_HAND : player.swingingArm;
        float pitch = Mth.lerp((float)tick, (float)player.xRotO, (float)player.getXRot());
        float xBob = Mth.lerp((float)tick, (float)player.xBobO, (float)player.xBob);
        float yBob = Mth.lerp((float)tick, (float)player.yBobO, (float)player.yBob);
        stack.pushPose();
        try {
            stack.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(tick) - xBob) * 0.1f));
            stack.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(tick) - yBob) * 0.1f));
            if (main) {
                FreeHands.hand(hands, player, tick, pitch, InteractionHand.MAIN_HAND, swinging == InteractionHand.MAIN_HAND ? attack : 0.0f, mainItem, 1.0f - Mth.lerp((float)tick, (float)oMainHeight, (float)mainHeight), stack, buffers, light);
            }
            if (off) {
                FreeHands.hand(hands, player, tick, pitch, InteractionHand.OFF_HAND, swinging == InteractionHand.OFF_HAND ? attack : 0.0f, offItem, 1.0f - Mth.lerp((float)tick, (float)oOffHeight, (float)offHeight), stack, buffers, light);
            }
        }
        finally {
            stack.popPose();
        }
        buffers.endBatch();
    }

    private static void hand(Renderer hands, LocalPlayer player, float tick, float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack stack, MultiBufferSource.BufferSource buffers, int light) {
        boolean epicFight;
        boolean bl = epicFight = ModList.get().isLoaded("epicfight") && EpicFightClient.ownsFirstPersonHands();
        if (!epicFight && ClientHooks.renderSpecificFirstPersonHand((InteractionHand)hand, (PoseStack)stack, (MultiBufferSource)buffers, (int)light, (float)tick, (float)pitch, (float)swing, (float)equip, (ItemStack)item)) {
            return;
        }
        hands.chrome$renderFreeHand(player, tick, pitch, hand, swing, item, equip, stack, (MultiBufferSource)buffers, light);
    }

    public static interface Renderer {
        public void chrome$renderFreeHand(LocalPlayer var1, float var2, float var3, InteractionHand var4, float var5, ItemStack var6, float var7, PoseStack var8, MultiBufferSource var9, int var10);
    }

    public static interface ArmView {
        public void chrome$renderImplantArm(RenderHandEvent var1, HumanoidArm var2);
    }
}

