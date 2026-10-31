/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package com.chromeware.client.ponder;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.client.ponder.SurgeryScenes;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.surgery.SurgeryBlocks;
import com.chromeware.surgery.SurgerySeat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class SurgeryPonderSeat
extends SurgerySeat {
    private SurgerySeat.Stage sceneStage = SurgerySeat.Stage.IDLE;
    private int stageStarted;
    private boolean limbOff;

    SurgeryPonderSeat(Level level) {
        super((EntityType)SurgeryBlocks.SEAT.get(), level);
        Direction facing = SurgeryScenes.CHAIR_FACING;
        this.setPos((double)SurgeryScenes.CHAIR.getX() + 0.5 + (double)facing.getStepX() * 0.3, (double)SurgeryScenes.CHAIR.getY() + 0.01, (double)SurgeryScenes.CHAIR.getZ() + 0.5 + (double)facing.getStepZ() * 0.3);
        this.setNoGravity(true);
    }

    @Override
    public SurgerySeat.Stage stage() {
        return this.sceneStage;
    }

    @Override
    public SurgerySeat.Kind kind() {
        return SurgerySeat.Kind.LIMB;
    }

    @Override
    public BodyPart part() {
        return BodyPart.RIGHT_ARM;
    }

    @Override
    public BlockPos arm() {
        return SurgeryScenes.ARM;
    }

    @Override
    public ItemStack implant() {
        return CyberwareItems.CYBER_ARM.toStack();
    }

    @Override
    public boolean ripped() {
        return this.limbOff;
    }

    @Override
    public float stageTime(float partialTick) {
        return (float)(this.tickCount - this.stageStarted) + partialTick;
    }

    void showStage(SurgerySeat.Stage next) {
        this.sceneStage = next;
        this.stageStarted = this.tickCount;
    }

    void removeFlesh() {
        this.limbOff = true;
        if (this.patient() != null) {
            this.patient().setData(LimbAttachments.LIMBS, (Object)LimbState.INTACT.remove(this.part()));
        }
    }

    void fitFrame() {
        this.limbOff = false;
        if (this.patient() == null) {
            return;
        }
        this.patient().setData(LimbAttachments.LIMBS, (Object)LimbState.INTACT);
        this.patient().setData(CyberLimbs.FRAMES, (Object)CyberLimbs.NONE.with(this.part(), true));
    }
}

