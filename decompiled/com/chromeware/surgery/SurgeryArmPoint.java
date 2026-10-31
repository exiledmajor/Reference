/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmAngleTarget
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlock
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.surgery;

import com.chromeware.mixin.ArmBlockEntityAccessor;
import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryChairBlock;
import com.chromeware.surgery.SurgeryChairBlockEntity;
import com.chromeware.surgery.SurgerySeat;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmAngleTarget;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

public class SurgeryArmPoint
extends ArmInteractionPoint {
    public static final int TOSS_TICKS = 8;

    public SurgeryArmPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    private SurgerySeat seat() {
        SurgerySeat surgerySeat;
        BlockState state = this.level.getBlockState(this.pos);
        if (!(state.getBlock() instanceof SurgeryChairBlock)) {
            return null;
        }
        BlockEntity blockEntity = this.level.getBlockEntity(SurgeryChairBlock.foot(this.pos, state));
        if (blockEntity instanceof SurgeryChairBlockEntity) {
            SurgeryChairBlockEntity chair = (SurgeryChairBlockEntity)blockEntity;
            surgerySeat = chair.seat();
        } else {
            surgerySeat = null;
        }
        return surgerySeat;
    }

    protected Vec3 getInteractionPositionVector() {
        return Vec3.atLowerCornerOf((Vec3i)this.pos).add(0.5, 0.9, 0.5);
    }

    public ArmAngleTarget getTargetAngles(BlockPos armPos, boolean ceiling) {
        ItemStack itemStack;
        SurgerySeat seat = this.seat();
        if (seat == null) {
            return new ArmAngleTarget(armPos, this.getInteractionPositionVector(), Direction.UP, ceiling);
        }
        BlockEntity blockEntity = this.level.getBlockEntity(armPos);
        if (blockEntity instanceof ArmBlockEntityAccessor) {
            ArmBlockEntityAccessor arm = (ArmBlockEntityAccessor)blockEntity;
            itemStack = arm.chrome$heldItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        ItemStack carried = itemStack;
        return Surgery.approach(seat, armPos, ceiling, SurgeryArmPoint.target(seat, carried, armPos, 0.0f), 0.0f);
    }

    public static Vec3 target(SurgerySeat seat, ItemStack carried, BlockPos arm, float partialTick) {
        boolean assisting = seat.assisting(arm);
        if (carried.isEmpty()) {
            return Surgery.site(seat, assisting ? seat.assistImplant() : seat.implant(), partialTick);
        }
        if (!assisting && (seat.handBack(carried) || seat.removing())) {
            return Surgery.handOver(seat, partialTick);
        }
        Vec3 site = Surgery.site(seat, carried, partialTick);
        return Surgery.kind(carried) == SurgerySeat.Kind.STANDARD ? site.add(Surgery.inward(seat, partialTick)) : site;
    }

    public int getSlotCount(ArmBlockEntity arm) {
        return 1;
    }

    public boolean wantsVisit(ArmBlockEntity arm) {
        SurgerySeat seat = this.seat();
        return seat != null && seat.wantsVisit(arm.getBlockPos(), !arm.getLevel().isClientSide());
    }

    public ItemStack visit(ArmBlockEntity arm) {
        SurgerySeat seat = this.seat();
        return seat == null ? ItemStack.EMPTY : seat.visit(arm.getBlockPos());
    }

    public ItemStack insert(ArmBlockEntity arm, ItemStack stack, boolean simulate) {
        SurgerySeat seat = this.seat();
        if (seat == null || stack.isEmpty() || Surgery.severed(stack)) {
            return stack;
        }
        if (seat.handBack(stack)) {
            if (!simulate) {
                seat.receiveHandBack(stack.copy());
            }
            return ItemStack.EMPTY;
        }
        if (seat.accepts(stack, arm.getBlockPos())) {
            if (!simulate) {
                seat.deliver(stack, arm.getBlockPos());
            }
            return stack.copyWithCount(stack.getCount() - 1);
        }
        return stack;
    }

    public ItemStack extract(ArmBlockEntity arm, int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    public static void toss(ArmBlockEntity arm) {
        Level level = arm.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        boolean ceiling = arm.getBlockState().getOptionalValue((Property)ArmBlock.CEILING).orElse(false);
        Vec3 at = Vec3.atCenterOf((Vec3i)arm.getBlockPos()).add(0.0, ceiling ? -1.1 : 1.1, 0.0);
        level2.sendParticles((ParticleOptions)ParticleTypes.POOF, at.x, at.y, at.z, 14, 0.2, 0.2, 0.2, 0.02);
        level2.playSound(null, at.x, at.y, at.z, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.8f, 0.6f);
    }

    public static class Type
    extends ArmInteractionPointType {
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof SurgeryChairBlock;
        }

        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new SurgeryArmPoint(this, level, pos, state);
        }
    }
}

