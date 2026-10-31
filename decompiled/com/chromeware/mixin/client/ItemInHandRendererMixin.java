/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.client.renderer.ItemInHandRenderer$HandRenderSelection
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.client.event.RenderHandEvent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbRegrowth;
import com.chromeware.body.OneHanded;
import com.chromeware.client.cyberware.FreeHands;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.GorillaCombat;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ItemInHandRenderer.class})
public abstract class ItemInHandRendererMixin
implements FreeHands.ArmView,
FreeHands.Renderer {
    @Shadow
    protected abstract void renderArmWithItem(AbstractClientPlayer var1, float var2, float var3, InteractionHand var4, float var5, ItemStack var6, float var7, PoseStack var8, MultiBufferSource var9, int var10);

    @Override
    public void chrome$renderFreeHand(LocalPlayer player, float tick, float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack stack, MultiBufferSource buffers, int light) {
        this.renderArmWithItem((AbstractClientPlayer)player, tick, pitch, hand, swing, item, equip, stack, buffers, light);
    }

    @Shadow
    protected abstract void renderPlayerArm(PoseStack var1, MultiBufferSource var2, int var3, float var4, float var5, HumanoidArm var6);

    @ModifyReturnValue(method={"evaluateWhichHandsToRender"}, at={@At(value="RETURN")})
    private static ItemInHandRenderer.HandRenderSelection chrome$deployedFists(ItemInHandRenderer.HandRenderSelection original, LocalPlayer player) {
        return GorillaCombat.deployed((Player)player) ? ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS : original;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void chrome$renderImplantArm(RenderHandEvent event, HumanoidArm arm) {
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        try {
            this.renderPlayerArm(stack, event.getMultiBufferSource(), event.getPackedLight(), event.getEquipProgress(), event.getSwingProgress(), arm);
        }
        finally {
            stack.popPose();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"renderArmWithItem"})
    private void chrome$firstPersonItems(AbstractClientPlayer player, float tick, float pitch, InteractionHand hand, float swing, ItemStack item, float equip, PoseStack stack, MultiBufferSource buffer, int light, Operation<Void> original) {
        HumanoidArm arm = LimbAttachments.armForHand((Player)player, hand);
        if (GorillaCombat.deployed((Player)player, arm) && !player.isInvisible() && !player.isScoping()) {
            stack.pushPose();
            try {
                this.renderPlayerArm(stack, buffer, light, equip, swing, arm);
            }
            finally {
                stack.popPose();
            }
        }
        if (OneHanded.resolve((Player)player, hand) != null) {
            original.call(new Object[]{player, Float.valueOf(tick), Float.valueOf(pitch), hand, Float.valueOf(swing), item, Float.valueOf(equip), stack, buffer, light});
        } else if ((LimbAttachments.hasArm((Player)player, arm) || LimbRegrowth.growing((Player)player, ArmImplants.part(arm))) && OneHanded.resolve((Player)player, hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND) != arm && !player.isInvisible() && !player.isScoping()) {
            stack.pushPose();
            try {
                this.renderPlayerArm(stack, buffer, light, equip, swing, arm);
            }
            finally {
                stack.popPose();
            }
        }
    }

    @WrapOperation(method={"renderArmWithItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;getMainArm()Lnet/minecraft/world/entity/HumanoidArm;", ordinal=0)})
    private HumanoidArm chrome$mainPhysicalArm(AbstractClientPlayer player, Operation<HumanoidArm> original) {
        return OneHanded.effectiveMainArm((Player)player, InteractionHand.MAIN_HAND);
    }

    @WrapOperation(method={"renderArmWithItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;getMainArm()Lnet/minecraft/world/entity/HumanoidArm;", ordinal=1)})
    private HumanoidArm chrome$offPhysicalArm(AbstractClientPlayer player, Operation<HumanoidArm> original) {
        return OneHanded.effectiveMainArm((Player)player, InteractionHand.OFF_HAND);
    }
}

