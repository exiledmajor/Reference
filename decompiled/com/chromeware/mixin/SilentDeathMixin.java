/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.GameRules$BooleanValue
 *  net.minecraft.world.level.GameRules$Key
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.netrunning.Netrunning;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={GameRules.class})
public abstract class SilentDeathMixin {
    @ModifyReturnValue(method={"getBoolean"}, at={@At(value="RETURN")})
    private boolean chrome$silentDeath(boolean original, @Local(argsOnly=true) GameRules.Key<GameRules.BooleanValue> key) {
        return original && (key != GameRules.RULE_SHOWDEATHMESSAGES || !Netrunning.silentDeath());
    }
}

