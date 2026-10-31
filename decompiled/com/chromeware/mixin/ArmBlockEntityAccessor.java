/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
 *  net.createmod.catnip.animation.LerpedFloat
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import java.util.List;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ArmBlockEntity.class}, remap=false)
public interface ArmBlockEntityAccessor {
    @Accessor(value="heldItem")
    public ItemStack chrome$heldItem();

    @Accessor(value="baseAngle")
    public LerpedFloat chrome$baseAngle();

    @Accessor(value="lowerArmAngle")
    public LerpedFloat chrome$lowerArmAngle();

    @Accessor(value="upperArmAngle")
    public LerpedFloat chrome$upperArmAngle();

    @Accessor(value="headAngle")
    public LerpedFloat chrome$headAngle();

    @Accessor(value="inputs")
    public List<ArmInteractionPoint> chrome$inputs();

    @Accessor(value="outputs")
    public List<ArmInteractionPoint> chrome$outputs();
}

