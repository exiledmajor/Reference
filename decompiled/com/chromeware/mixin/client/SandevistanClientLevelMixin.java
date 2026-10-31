/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.client.sandevistan.SandevistanRigEffects;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientLevel.class})
public abstract class SandevistanClientLevelMixin {
    @Inject(method={"sendBlockUpdated"}, at={@At(value="TAIL")})
    private void chrome$echoGeometry(BlockPos pos, BlockState oldState, BlockState newState, int flags, CallbackInfo ci) {
        SandevistanRigEffects.invalidate(pos);
    }

    @WrapWithCondition(method={"tickNonPassenger", "tickPassenger"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;setOldPosAndRot()V")})
    private boolean chrome$oldPosition(Entity e) {
        return SandevistanClient.shouldAdvance(e);
    }

    @WrapWithCondition(method={"tickNonPassenger", "tickPassenger"}, at={@At(value="FIELD", target="Lnet/minecraft/world/entity/Entity;tickCount:I", opcode=181)})
    private boolean chrome$age(Entity e, int next) {
        return SandevistanClient.shouldAdvance(e);
    }

    @WrapWithCondition(method={"tickNonPassenger"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;tick()V")})
    private boolean chrome$entity(Entity e) {
        return SandevistanClient.shouldAdvance(e);
    }

    @WrapWithCondition(method={"tickPassenger"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;rideTick()V")})
    private boolean chrome$passenger(Entity e) {
        return SandevistanClient.shouldAdvance(e);
    }
}

