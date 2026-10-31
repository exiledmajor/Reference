/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.cyberware.FirstPersonOnTop;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LevelRenderer.class})
public abstract class FirstPersonOnTopMixin {
    @Inject(method={"renderEntity"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$deferFirstPersonBody(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack stack, MultiBufferSource buffers, CallbackInfo ci) {
        if (FirstPersonOnTop.defer(entity, partialTick)) {
            ci.cancel();
        }
    }
}

