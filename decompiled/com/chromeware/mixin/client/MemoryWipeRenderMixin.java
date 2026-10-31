/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderDispatcher
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.netrunning.NetrunningClient;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={EntityRenderDispatcher.class})
public abstract class MemoryWipeRenderMixin {
    @Inject(method={"shouldRender"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$memoryWipe(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (NetrunningClient.veiled(entity)) {
            cir.setReturnValue((Object)false);
        }
    }
}

