/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin;

import com.chromeware.netrunning.DevicePower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={BlockBehaviour.BlockStateBase.class})
public abstract class DevicePowerMixin {
    @Inject(method={"getSignal"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$hackedSignal(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> result) {
        if (DevicePower.powers(level, pos.relative(direction.getOpposite()))) {
            result.setReturnValue((Object)15);
        }
    }

    @Inject(method={"getDirectSignal"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$hackedDirectSignal(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> result) {
        if (DevicePower.powers(level, pos.relative(direction.getOpposite()))) {
            result.setReturnValue((Object)15);
        }
    }
}

