/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.TickingBlockEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.TimeDilation;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Level.class})
public abstract class SandevistanLevelMixin {
    @WrapWithCondition(method={"tickBlockEntities"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/TickingBlockEntity;tick()V")})
    private boolean chrome$blockTick(TickingBlockEntity ticker) {
        return TimeDilation.advance((Level)this, ticker.getPos());
    }
}

