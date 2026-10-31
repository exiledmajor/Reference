/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.client.sandevistan.SandevistanClient;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Camera.class})
public abstract class SandevistanCameraMixin {
    @Shadow
    protected abstract void setPosition(double var1, double var3, double var5);

    @Inject(method={"setup"}, at={@At(value="TAIL")})
    private void chrome$logicalCamera(BlockGetter level, Entity entity, boolean thirdPerson, boolean inverse, float tick, CallbackInfo ci) {
        if (thirdPerson || !SandevistanClient.isDilated(entity)) {
            return;
        }
        float delta = SandevistanClient.tickDelta(entity, tick);
        Vec3 position = SandevistanClient.renderSample(entity, tick).position().add(entity.getEyePosition(delta).subtract(entity.getPosition(delta)));
        this.setPosition(position.x, position.y, position.z);
    }
}

