/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  de.maxhenkel.corpse.entities.CorpseEntity
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.client.body.LimbRenderContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets={"de.maxhenkel.corpse.entities.CorpseRenderer"}, remap=false)
public abstract class CorpseRendererMixin {
    @WrapOperation(method={"render(Lde/maxhenkel/corpse/entities/CorpseEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal=1), @At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", ordinal=2)})
    private void chrome$render(EntityRenderer renderer, Entity entity, float yaw, float tick, PoseStack stack, MultiBufferSource buffers, int light, Operation<Void> original, CorpseEntity corpse, float corpseYaw, float partialTick, PoseStack outer, MultiBufferSource source, int packedLight) {
        LivingEntityRenderer living;
        EntityModel entityModel;
        if (renderer instanceof LivingEntityRenderer && (entityModel = (living = (LivingEntityRenderer)renderer).getModel()) instanceof HumanoidModel) {
            HumanoidModel model = (HumanoidModel)entityModel;
            try (LimbRenderContext.Scope ignored = LimbRenderContext.anatomy((LimbState)corpse.getData(LimbAttachments.LIMBS), model);){
                original.call(new Object[]{renderer, entity, Float.valueOf(yaw), Float.valueOf(tick), stack, buffers, light});
            }
        } else {
            original.call(new Object[]{renderer, entity, Float.valueOf(yaw), Float.valueOf(tick), stack, buffers, light});
        }
    }

    @ModifyExpressionValue(method={"render(Lde/maxhenkel/corpse/entities/CorpseEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"}, at={@At(value="INVOKE", target="Lde/maxhenkel/corpse/corelib/CachedMap;get(Ljava/lang/Object;Ljava/util/function/Supplier;)Ljava/lang/Object;", ordinal=1)})
    private Object chrome$anatomy(Object original, CorpseEntity corpse, float yaw, float partialTick, PoseStack stack, MultiBufferSource buffer, int light) {
        if (original instanceof Player) {
            Player player = (Player)original;
            player.setData(LimbAttachments.LIMBS, (Object)((LimbState)corpse.getData(LimbAttachments.LIMBS)));
            player.setData(CyberLimbs.FRAMES, (Object)((CyberLimbs)corpse.getData(CyberLimbs.FRAMES)));
            player.getPersistentData().putBoolean("ChromeCorpse", true);
        }
        return original;
    }
}

