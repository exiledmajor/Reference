/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.util.Mth
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={LevelRenderer.class})
public abstract class SandevistanWeatherMixin {
    @WrapWithCondition(method={"tickRain"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")})
    private boolean chrome$rainParticles(ClientLevel level, ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz) {
        return SandevistanClient.shouldAdvanceAt(x, z);
    }

    @ModifyExpressionValue(method={"renderSnowAndRain"}, at={@At(value="FIELD", target="Lnet/minecraft/client/renderer/LevelRenderer;ticks:I", ordinal=2), @At(value="FIELD", target="Lnet/minecraft/client/renderer/LevelRenderer;ticks:I", ordinal=3)})
    private int chrome$columnTicks(int ticks, @Local(argsOnly=true) float partialTick, @Local(ordinal=5) int z, @Local(ordinal=6) int x) {
        return SandevistanWeatherMixin.chrome$dilated(x, z) ? Mth.floor((double)SandevistanWeatherMixin.chrome$time(x, z, partialTick)) : ticks;
    }

    @ModifyVariable(method={"renderSnowAndRain"}, at=@At(value="LOAD", ordinal=3), argsOnly=true)
    private float chrome$rainFraction(float partialTick, @Local(ordinal=5) int z, @Local(ordinal=6) int x) {
        return SandevistanWeatherMixin.chrome$fraction(x, z, partialTick);
    }

    @ModifyVariable(method={"renderSnowAndRain"}, at=@At(value="LOAD", ordinal=4), argsOnly=true)
    private float chrome$snowFraction(float partialTick, @Local(ordinal=5) int z, @Local(ordinal=6) int x) {
        return SandevistanWeatherMixin.chrome$fraction(x, z, partialTick);
    }

    @ModifyVariable(method={"renderSnowAndRain"}, at=@At(value="LOAD"), index=18)
    private float chrome$snowTime(float time, @Local(argsOnly=true) float partialTick, @Local(ordinal=5) int z, @Local(ordinal=6) int x) {
        return SandevistanWeatherMixin.chrome$dilated(x, z) ? (float)SandevistanWeatherMixin.chrome$time(x, z, partialTick) : time;
    }

    @Unique
    private static boolean chrome$dilated(int x, int z) {
        return SandevistanClient.hasTimeFields() && SandevistanClient.isDilatedAt((double)x + 0.5, (double)z + 0.5);
    }

    @Unique
    private static double chrome$time(int x, int z, float partialTick) {
        return SandevistanClient.visualTimeAt((double)x + 0.5, (double)z + 0.5, partialTick);
    }

    @Unique
    private static float chrome$fraction(int x, int z, float partialTick) {
        if (!SandevistanWeatherMixin.chrome$dilated(x, z)) {
            return partialTick;
        }
        double time = SandevistanWeatherMixin.chrome$time(x, z, partialTick);
        return (float)(time - (double)Mth.floor((double)time));
    }
}

