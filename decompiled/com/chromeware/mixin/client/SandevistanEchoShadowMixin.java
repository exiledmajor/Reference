/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRenderDispatcher
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.AfterimageRenderContext;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={EntityRenderDispatcher.class})
public abstract class SandevistanEchoShadowMixin {
    @Inject(method={"renderShadow"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$noEchoShadow(CallbackInfo ci) {
        if (AfterimageRenderContext.isRendering()) {
            ci.cancel();
        }
    }
}

