/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.WalkAnimationState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin.client;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={WalkAnimationState.class})
public interface LimbAnimatorAccessor {
    @Accessor(value="speedOld")
    public float chrome$getPreviousSpeed();

    @Accessor(value="speedOld")
    public void chrome$setPreviousSpeed(float var1);

    @Accessor(value="speed")
    public float chrome$speed();

    @Accessor(value="speed")
    public void chrome$setSpeed(float var1);

    @Accessor(value="position")
    public float chrome$getPosition();

    @Accessor(value="position")
    public void chrome$setPos(float var1);
}

