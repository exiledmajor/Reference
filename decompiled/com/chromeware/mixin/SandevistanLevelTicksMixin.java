/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.ticks.LevelTicks
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.TickingLevel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.LongPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.LevelTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={LevelTicks.class})
public abstract class SandevistanLevelTicksMixin {
    @WrapOperation(method={"sortContainersToTick"}, at={@At(value="INVOKE", target="Ljava/util/function/LongPredicate;test(J)Z")})
    private boolean chrome$scheduledChunk(LongPredicate predicate, long chunk, Operation<Boolean> original) {
        ServerLevel level = TickingLevel.level();
        return (Boolean)original.call(new Object[]{predicate, chunk}) != false && (level == null || Sandevistan.shouldAdvanceChunk((Level)level, chunk));
    }
}

