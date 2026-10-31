/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.client.cyberware.KiroshiHighlights;
import com.chromeware.client.netrunning.NetrunningClient;
import com.chromeware.client.netrunning.ScanMode;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Entity.class})
public abstract class EntityGlowMixin {
    @ModifyReturnValue(method={"isCurrentlyGlowing"}, at={@At(value="RETURN")})
    private boolean chrome$scanOutline(boolean original) {
        return original || KiroshiHighlights.highlighted((Entity)this) || NetrunningClient.highlighted((Entity)this) || ScanMode.target() == this;
    }
}

