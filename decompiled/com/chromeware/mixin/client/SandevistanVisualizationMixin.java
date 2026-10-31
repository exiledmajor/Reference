/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.engine_room.flywheel.api.visualization.VisualizationManager
 *  net.minecraft.world.level.LevelAccessor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanRigEffects;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={VisualizationManager.class}, remap=false)
public interface SandevistanVisualizationMixin {
    @Inject(method={"supportsVisualization"}, at={@At(value="HEAD")}, cancellable=true)
    private static void chrome$capture(LevelAccessor level, CallbackInfoReturnable<Boolean> ci) {
        if (SandevistanRigEffects.capturing()) {
            ci.setReturnValue((Object)false);
        }
    }
}

