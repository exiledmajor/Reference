/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.gameevent.GameEvent
 *  net.minecraft.world.level.gameevent.GameEvent$Context
 *  net.minecraft.world.level.gameevent.vibrations.VibrationSystem$Listener
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.LynxPaws;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={VibrationSystem.Listener.class})
public abstract class LynxVibrationListenerMixin {
    @Inject(method={"handleGameEvent"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$quietVibration(ServerLevel level, Holder<GameEvent> event, GameEvent.Context context, Vec3 pos, CallbackInfoReturnable<Boolean> ci) {
        if (LynxPaws.silences(context.sourceEntity())) {
            ci.setReturnValue((Object)false);
        }
    }

    @Inject(method={"forceScheduleVibration"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$quietContact(ServerLevel level, Holder<GameEvent> event, GameEvent.Context context, Vec3 pos, CallbackInfo ci) {
        if (LynxPaws.silences(context.sourceEntity())) {
            ci.cancel();
        }
    }
}

