/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin.client;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderType.CompositeState.class})
public interface SandevistanCompositeAccessor {
    @Accessor(value="textureState")
    public RenderStateShard.EmptyTextureStateShard chrome$texture();
}

