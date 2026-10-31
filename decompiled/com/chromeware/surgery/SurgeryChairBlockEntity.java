/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 */
package com.chromeware.surgery;

import com.chromeware.surgery.SurgeryBlocks;
import com.chromeware.surgery.SurgerySeat;
import com.chromeware.surgery.SurgerySeats;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SurgeryChairBlockEntity
extends BlockEntity {
    public SurgeryChairBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)SurgeryBlocks.CHAIR_ENTITY.get(), pos, state);
    }

    public SurgerySeat seat() {
        if (this.level == null) {
            return null;
        }
        List<SurgerySeat> seats = SurgerySeats.in(this.level, new AABB(this.worldPosition).inflate(0.5));
        return seats.isEmpty() ? null : (SurgerySeat)((Object)seats.getFirst());
    }
}

