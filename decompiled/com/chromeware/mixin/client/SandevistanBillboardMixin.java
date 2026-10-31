/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.SingleQuadParticle
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.mixin.client.SandevistanParticleAccessor;
import net.minecraft.client.particle.SingleQuadParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={SingleQuadParticle.class}, priority=900)
public abstract class SandevistanBillboardMixin {
    @ModifyVariable(method={"render"}, at=@At(value="HEAD"), argsOnly=true, ordinal=0)
    private float chrome$particleDelta(float tick) {
        SandevistanParticleAccessor p = (SandevistanParticleAccessor)((Object)this);
        return SandevistanClient.tickDeltaAt(p.chrome$x(), p.chrome$z(), tick);
    }
}

