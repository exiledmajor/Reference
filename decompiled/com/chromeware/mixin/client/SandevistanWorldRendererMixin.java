/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.client.sandevistan.SandevistanEffects;
import com.chromeware.client.sandevistan.SavedRenderState;
import com.chromeware.mixin.client.LimbAnimatorAccessor;
import com.chromeware.mixin.client.LivingEntityAfterimageAccessor;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public abstract class SandevistanWorldRendererMixin {
    @Unique
    private SavedRenderState chrome$renderRestoreState;
    @Unique
    private float chrome$spatialTickDelta;

    @Inject(method={"renderLevel"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;")})
    private void chrome$sceneDepth(CallbackInfo ci) {
        SandevistanEffects.captureSceneDepth();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"renderEntity"})
    private void chrome$renderWithLogicalState(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta, PoseStack matrices, MultiBufferSource consumers, Operation<Void> original) {
        SavedRenderState previous = this.chrome$renderRestoreState;
        float previousDelta = this.chrome$spatialTickDelta;
        SavedRenderState restore = null;
        try {
            this.chrome$renderRestoreState = null;
            if (SandevistanClient.isDilated(entity)) {
                this.chrome$renderRestoreState = restore = SavedRenderState.capture(entity);
                this.chrome$spatialTickDelta = SandevistanClient.tickDelta(entity, tickDelta);
                SandevistanWorldRendererMixin.chrome$applySample(entity, SandevistanClient.renderSample(entity, tickDelta));
            }
            original.call(new Object[]{entity, cameraX, cameraY, cameraZ, Float.valueOf(tickDelta), matrices, consumers});
        }
        finally {
            if (restore != null) {
                restore.restore(entity);
            }
            this.chrome$renderRestoreState = previous;
            this.chrome$spatialTickDelta = previousDelta;
        }
    }

    @ModifyArg(method={"renderEntity"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"), index=5)
    private float chrome$useDilatedTickDelta(float vanillaTickDelta) {
        return this.chrome$renderRestoreState == null ? vanillaTickDelta : this.chrome$spatialTickDelta;
    }

    @Unique
    private static void chrome$applySample(Entity entity, SandevistanClient.RenderSample sample) {
        Vec3 position = sample.position();
        entity.xOld = position.x;
        entity.yOld = position.y;
        entity.zOld = position.z;
        entity.xo = position.x;
        entity.yo = position.y;
        entity.zo = position.z;
        entity.setPos(position.x, position.y, position.z);
        entity.yRotO = sample.yaw();
        entity.setYRot(sample.yaw());
        entity.xRotO = sample.pitch();
        entity.setXRot(sample.pitch());
        entity.tickCount = Mth.floor((float)sample.animationAge());
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)entity;
        living.yBodyRotO = sample.yBodyRot();
        living.yBodyRot = sample.yBodyRot();
        living.yHeadRotO = sample.yHeadRot();
        living.yHeadRot = sample.yHeadRot();
        living.setPose(sample.pose());
        living.setShiftKeyDown(sample.sneaking());
        living.setSprinting(sample.sprinting());
        living.setSwimming(sample.swimming());
        LimbAnimatorAccessor limb = (LimbAnimatorAccessor)living.walkAnimation;
        limb.chrome$setPreviousSpeed(sample.previousLimbSpeed());
        limb.chrome$setSpeed(sample.limbSpeed());
        limb.chrome$setPos(sample.limbPosition());
        LivingEntityAfterimageAccessor accessor = (LivingEntityAfterimageAccessor)living;
        accessor.chrome$setPreviousLeaningPitch(sample.previousLeaningPitch());
        accessor.chrome$setLeaningPitch(sample.swimAmount());
        accessor.chrome$setPreviousHandSwingProgress(sample.previousHandSwingProgress());
        accessor.chrome$setHandSwingProgress(sample.attackAnim());
        accessor.chrome$setHandSwinging(sample.swinging());
        if (sample.usingItem()) {
            living.startUsingItem(sample.activeHand());
            accessor.chrome$setItemUseTimeLeft(sample.useItemRemaining());
        } else {
            living.stopUsingItem();
        }
    }
}

