/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin.client;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LivingEntity.class})
public interface LivingEntityAfterimageAccessor {
    @Accessor(value="swinging")
    public boolean chrome$isHandSwinging();

    @Accessor(value="swinging")
    public void chrome$setHandSwinging(boolean var1);

    @Accessor(value="attackAnim")
    public float chrome$getAttackAnim();

    @Accessor(value="attackAnim")
    public void chrome$setHandSwingProgress(float var1);

    @Accessor(value="oAttackAnim")
    public float chrome$getPreviousHandSwingProgress();

    @Accessor(value="oAttackAnim")
    public void chrome$setPreviousHandSwingProgress(float var1);

    @Accessor(value="swimAmount")
    public float chrome$getSwimAmount();

    @Accessor(value="swimAmount")
    public void chrome$setLeaningPitch(float var1);

    @Accessor(value="swimAmountO")
    public float chrome$getPreviousLeaningPitch();

    @Accessor(value="swimAmountO")
    public void chrome$setPreviousLeaningPitch(float var1);

    @Accessor(value="useItemRemaining")
    public int chrome$getUseItemRemainingTicks();

    @Accessor(value="useItemRemaining")
    public void chrome$setItemUseTimeLeft(int var1);
}

