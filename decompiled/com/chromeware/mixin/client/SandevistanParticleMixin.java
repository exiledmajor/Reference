/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleEngine
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.mixin.client.SandevistanParticleAccessor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ParticleEngine.class})
public abstract class SandevistanParticleMixin {
    @Inject(method={"tickParticle"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$particle(Particle p, CallbackInfo ci) {
        SandevistanParticleAccessor a = (SandevistanParticleAccessor)p;
        if (!SandevistanClient.shouldAdvanceAt(a.chrome$x(), a.chrome$z())) {
            ci.cancel();
        }
    }
}

