/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.contraptions.ControlledContraptionEntity
 *  net.minecraft.core.BlockPos
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin;

import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ControlledContraptionEntity.class}, remap=false)
public interface SandevistanControlledContraptionAccessor {
    @Accessor(value="controllerPos")
    public BlockPos chrome$controllerPosition();
}

