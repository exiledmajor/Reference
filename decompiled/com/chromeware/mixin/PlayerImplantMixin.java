/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Player.class})
public abstract class PlayerImplantMixin {
    @Unique
    private boolean chrome$stepSound;

    @ModifyVariable(method={"causeFoodExhaustion"}, at=@At(value="HEAD"), argsOnly=true)
    private float chrome$universalExhaustion(float exhaustion) {
        return ImplantEffects.allExhaustion((Player)this, exhaustion);
    }

    @ModifyArg(method={"attack"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"), index=0)
    private float chrome$attackExhaustion(float exhaustion) {
        return ImplantEffects.attackExhaustion((Player)this, exhaustion);
    }

    @Inject(method={"playStepSound"}, at={@At(value="HEAD")})
    private void chrome$beginStep(BlockPos pos, BlockState state, CallbackInfo ci) {
        this.chrome$stepSound = true;
    }

    @Inject(method={"playStepSound"}, at={@At(value="RETURN")})
    private void chrome$endStep(BlockPos pos, BlockState state, CallbackInfo ci) {
        this.chrome$stepSound = false;
    }

    @ModifyVariable(method={"playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private float chrome$quietSteps(float volume) {
        return this.chrome$stepSound && Implant.LYNX_PAWS.active((Player)this) ? 0.0f : volume;
    }
}

