/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.resources.ResourceLocation
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package com.chromeware.mixin.client;

import java.util.Optional;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={RenderStateShard.EmptyTextureStateShard.class})
public interface SandevistanTextureAccessor {
    @Invoker(value="cutoutTexture")
    public Optional<ResourceLocation> chrome$id();
}

