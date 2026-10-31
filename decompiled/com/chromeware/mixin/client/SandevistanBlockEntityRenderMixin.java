/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={BlockEntityRenderDispatcher.class})
public abstract class SandevistanBlockEntityRenderMixin {
    @ModifyVariable(method={"render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private float chrome$blockEntityDelta(float tick, BlockEntity e) {
        return SandevistanClient.tickDelta(e.getLevel(), e.getBlockPos(), tick);
    }
}

