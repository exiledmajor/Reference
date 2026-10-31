/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MouseHandler
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.humanity.SyringeClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MouseHandler.class})
public abstract class SyringeMouseMixin {
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;

    @Inject(method={"turnPlayer"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$lockSyringeCamera(double time, CallbackInfo ci) {
        if (!SyringeClient.interactionLocked()) {
            return;
        }
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
        ci.cancel();
    }
}

