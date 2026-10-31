/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.renderer.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package com.chromeware.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={GameRenderer.class})
public interface MonowireFovAccessor {
    @Invoker(value="getFov")
    public double chrome$wireFov(Camera var1, float var2, boolean var3);
}

