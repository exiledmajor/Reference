/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package com.chromeware.surgery;

import com.chromeware.mixin.ArmBlockEntityAccessor;
import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryArmPoint;
import com.chromeware.surgery.SurgerySeat;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class SurgeryStock {
    public static volatile List<ItemStack> clientView = List.of();
    public static volatile int clientArms;

    public static List<Entry> scan(SurgerySeat seat) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        HashSet<BlockPos> counted = new HashSet<BlockPos>();
        for (ArmBlockEntity arm : SurgeryStock.arms(seat)) {
            for (ArmInteractionPoint point : ((ArmBlockEntityAccessor)arm).chrome$inputs()) {
                if (point instanceof SurgeryArmPoint || !point.isValid()) continue;
                boolean first = counted.add(point.getPos().immutable());
                for (int slot = 0; slot < point.getSlotCount(arm); ++slot) {
                    ItemStack stack = point.extract(arm, slot, 64, true);
                    if (stack.isEmpty() || !Surgery.installable(stack)) continue;
                    SurgeryStock.add(entries, stack, arm.getBlockPos(), seat.level(), first);
                }
            }
        }
        return entries;
    }

    private static boolean busy(Level level, BlockPos arm) {
        ArmBlockEntityAccessor held;
        BlockEntity blockEntity = level.getBlockEntity(arm);
        return blockEntity instanceof ArmBlockEntityAccessor && !(held = (ArmBlockEntityAccessor)blockEntity).chrome$heldItem().isEmpty();
    }

    private static void add(List<Entry> entries, ItemStack stack, BlockPos arm, Level level, boolean count) {
        for (int i = 0; i < entries.size(); ++i) {
            Entry e = entries.get(i);
            if (!ItemStack.isSameItemSameComponents((ItemStack)e.item(), (ItemStack)stack)) continue;
            BlockPos surgeon = SurgeryStock.busy(level, e.arm()) && !SurgeryStock.busy(level, arm) ? arm : e.arm();
            entries.set(i, new Entry(e.item().copyWithCount(e.item().getCount() + (count ? stack.getCount() : 0)), surgeon));
            return;
        }
        entries.add(new Entry(stack.copy(), arm));
    }

    public static BlockPos supplier(SurgerySeat seat, ItemStack item, BlockPos exclude) {
        BlockPos found = null;
        for (ArmBlockEntity arm : SurgeryStock.arms(seat)) {
            if (arm.getBlockPos().equals((Object)exclude)) continue;
            for (ArmInteractionPoint point : ((ArmBlockEntityAccessor)arm).chrome$inputs()) {
                if (point instanceof SurgeryArmPoint || !point.isValid()) continue;
                for (int slot = 0; slot < point.getSlotCount(arm); ++slot) {
                    if (!ItemStack.isSameItemSameComponents((ItemStack)point.extract(arm, slot, 1, true), (ItemStack)item)) continue;
                    if (!SurgeryStock.busy(seat.level(), arm.getBlockPos())) {
                        return arm.getBlockPos();
                    }
                    if (found != null) continue;
                    found = arm.getBlockPos();
                }
            }
        }
        return found;
    }

    public static List<ArmBlockEntity> arms(SurgerySeat seat) {
        Level level = seat.level();
        BlockPos foot = seat.chair();
        BlockPos head = foot.relative(seat.facing());
        int range = ArmBlockEntity.getRange() + 1;
        ArrayList<ArmBlockEntity> arms = new ArrayList<ArmBlockEntity>();
        for (BlockPos pos : BlockPos.betweenClosed((BlockPos)foot.offset(-range, -range, -range), (BlockPos)foot.offset(range, range, range))) {
            ArmBlockEntity arm;
            ArmBlockEntityAccessor points;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof ArmBlockEntity) || !SurgeryStock.serves((points = (ArmBlockEntityAccessor)(arm = (ArmBlockEntity)blockEntity)).chrome$inputs(), foot, head) && !SurgeryStock.serves(points.chrome$outputs(), foot, head)) continue;
            arms.add(arm);
        }
        return arms;
    }

    private static boolean serves(List<ArmInteractionPoint> points, BlockPos foot, BlockPos head) {
        for (ArmInteractionPoint point : points) {
            if (!(point instanceof SurgeryArmPoint) || !point.getPos().equals((Object)foot) && !point.getPos().equals((Object)head)) continue;
            return true;
        }
        return false;
    }

    public record Entry(ItemStack item, BlockPos arm) {
    }
}

