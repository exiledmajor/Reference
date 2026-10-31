/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.ImplantEffects;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Entity.class})
public abstract class InvisibilityMixin {
    @ModifyReturnValue(method={"isInvisible"}, at={@At(value="RETURN")})
    private boolean chrome$opticalCamo(boolean original) {
        Player player;
        InvisibilityMixin invisibilityMixin;
        return original || (invisibilityMixin = this) instanceof Player && ImplantEffects.camouflaged(player = (Player)invisibilityMixin);
    }
}

