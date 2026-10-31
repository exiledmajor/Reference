/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.ticks.LevelTicks
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.TickingLevel;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerLevel.class})
public abstract class SandevistanServerLevelMixin {
    @WrapWithCondition(method={"tickNonPassenger", "tickPassenger"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;setOldPosAndRot()V")})
    private boolean chrome$oldPosition(Entity e) {
        return Sandevistan.shouldAdvance(e);
    }

    @WrapWithCondition(method={"tickNonPassenger", "tickPassenger"}, at={@At(value="FIELD", target="Lnet/minecraft/world/entity/Entity;tickCount:I", opcode=181)})
    private boolean chrome$age(Entity e, int next) {
        return Sandevistan.shouldAdvance(e);
    }

    @WrapWithCondition(method={"tickNonPassenger"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;tick()V")})
    private boolean chrome$entity(Entity e) {
        return Sandevistan.shouldAdvance(e);
    }

    @WrapWithCondition(method={"tickPassenger"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;rideTick()V")})
    private boolean chrome$passenger(Entity e) {
        return Sandevistan.shouldAdvance(e);
    }

    @Inject(method={"tickChunk"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$chunk(LevelChunk chunk, int speed, CallbackInfo ci) {
        if (!Sandevistan.shouldAdvanceChunk((Level)((ServerLevel)this), chunk.getPos().toLong())) {
            ci.cancel();
        }
    }

    @WrapOperation(method={"runBlockEvents"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerLevel;shouldTickBlocksAt(Lnet/minecraft/core/BlockPos;)Z")})
    private boolean chrome$blockEvent(ServerLevel level, BlockPos pos, Operation<Boolean> original) {
        return (Boolean)original.call(new Object[]{level, pos}) != false && Sandevistan.shouldAdvance((Level)level, pos);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapOperation(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/ticks/LevelTicks;tick(JILjava/util/function/BiConsumer;)V")})
    private <T> void chrome$schedule(LevelTicks<T> ticks, long time, int max, BiConsumer<BlockPos, T> action, Operation<Void> original) {
        TickingLevel.enter((ServerLevel)this);
        try {
            original.call(new Object[]{ticks, time, max, action});
        }
        finally {
            TickingLevel.exit();
        }
    }
}

