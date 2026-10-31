/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.client.compat.CompatHands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"yesman.epicfight.client.events.engine.ControlEngine"}, remap=false)
public abstract class EpicFightControlMixin {
    @Inject(method={"shouldDisableVanillaAttack()Z"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$hardwareAttack(CallbackInfoReturnable<Boolean> cir) {
        if (CompatHands.hardwareOwnsButton(true)) {
            cir.setReturnValue((Object)false);
        }
    }

    @Inject(method={"maybeAttack()V"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$noComboWithHardware(CallbackInfo ci) {
        if (CompatHands.hardwareOwnsButton(true)) {
            ci.cancel();
        }
    }

    @Inject(method={"maybeGuard()V"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$noGuardWithHardware(CallbackInfo ci) {
        if (CompatHands.hardwareOwnsButton(false)) {
            ci.cancel();
        }
    }
}

