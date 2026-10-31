/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity
 *  dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity$BodyPart
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbState;
import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.body.LimbRenderContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"dev.leo.sableplayerragdoll.neoforge.client.RagdollPartBlockEntityRenderer"}, remap=false)
public abstract class RagdollRendererMixin {
    @Shadow
    private PlayerModel<?> model;

    @WrapMethod(method={"renderLayers"})
    private void chrome$anatomy(RagdollPartBlockEntity block, RagdollPartBlockEntity.BodyPart part, LivingEntity entity, PoseStack stack, MultiBufferSource buffers, int light, float tick, Operation<Void> original) {
        try (LimbRenderContext.Scope ignored = LimbRenderContext.anatomy(new LimbState(block.getPersistentData().getInt("ChromeMissing")), this.model);){
            original.call(new Object[]{block, part, entity, stack, buffers, light, Float.valueOf(tick)});
        }
    }

    @Inject(method={"render(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$missing(RagdollPartBlockEntity block, float partialTick, PoseStack stack, MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        BodyPart part = RagdollRendererMixin.chrome$part(block.bodyPart());
        if ((block.getPersistentData().getInt("ChromeMissing") & part.bit()) != 0) {
            ci.cancel();
        }
    }

    @Inject(method={"renderLayers"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$frame(RagdollPartBlockEntity block, RagdollPartBlockEntity.BodyPart nativePart, LivingEntity entity, PoseStack stack, MultiBufferSource buffers, int light, float partialTick, CallbackInfo ci) {
        if (!block.getPersistentData().getBoolean("ChromeFrame")) {
            return;
        }
        BodyPart part = RagdollRendererMixin.chrome$part(nativePart);
        if (part == BodyPart.HEAD) {
            return;
        }
        ModelPart bone = switch (part) {
            case BodyPart.LEFT_ARM -> this.model.leftArm;
            case BodyPart.RIGHT_ARM -> this.model.rightArm;
            case BodyPart.LEFT_LEG -> this.model.leftLeg;
            case BodyPart.RIGHT_LEG -> this.model.rightLeg;
            default -> this.model.body;
        };
        CyberLimbRenderer.render(part, bone, stack, buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"textures/entity/cyber_limbs.png"))), light, OverlayTexture.NO_OVERLAY);
        ci.cancel();
    }

    private static BodyPart chrome$part(RagdollPartBlockEntity.BodyPart part) {
        return part == RagdollPartBlockEntity.BodyPart.TORSO ? BodyPart.BODY : BodyPart.valueOf(part.name());
    }
}

