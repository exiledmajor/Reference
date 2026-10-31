/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin.client;

import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.client.renderer.RenderType$CompositeRenderType"})
public interface SandevistanRenderTypeAccessor {
    @Accessor(value="state")
    public RenderType.CompositeState chrome$state();
}

