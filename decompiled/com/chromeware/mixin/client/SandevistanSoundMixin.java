/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.SoundEngine
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.mixin.client.SandevistanSoundAccessor;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={SoundEngine.class})
public abstract class SandevistanSoundMixin {
    @Inject(method={"calculatePitch"}, at={@At(value="RETURN")}, cancellable=true)
    private void chrome$worldPitch(SoundInstance sound, CallbackInfoReturnable<Float> ci) {
        SandevistanSoundAccessor tracking;
        if (sound.isRelative() || SandevistanClient.isTransitionSound(sound) || !SandevistanClient.isDilatedAt(sound.getX(), sound.getZ())) {
            return;
        }
        if (sound instanceof SandevistanSoundAccessor && SandevistanClient.isImmune((tracking = (SandevistanSoundAccessor)sound).chrome$entity())) {
            return;
        }
        ci.setReturnValue((Object)Float.valueOf(((Float)ci.getReturnValue()).floatValue() * (0.35f + 0.65f * SandevistanClient.timeScaleAt(sound.getX(), sound.getZ()))));
    }
}

