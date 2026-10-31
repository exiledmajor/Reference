/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlock
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.client.surgery;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.mixin.ArmBlockEntityAccessor;
import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryClaw;
import com.chromeware.surgery.SurgerySeat;
import com.chromeware.surgery.SurgerySeats;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlock;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record SurgeryCarry(ItemStack held, BodyPart part, Vec3 offset) {
    public boolean severed() {
        return Surgery.severed(this.held);
    }

    public boolean sandevistan() {
        return this.held.is(CyberwareItems.SANDEVISTAN);
    }

    public static SurgeryCarry of(SurgerySeat seat, float partialTick) {
        BlockEntity blockEntity;
        BlockPos pos = seat.arm();
        if (pos == null || !((blockEntity = seat.level().getBlockEntity(pos)) instanceof ArmBlockEntity)) {
            return null;
        }
        ArmBlockEntity arm = (ArmBlockEntity)blockEntity;
        ArmBlockEntityAccessor pose = (ArmBlockEntityAccessor)arm;
        ItemStack held = pose.chrome$heldItem();
        if (!SurgeryCarry.lifeSize(seat, held)) {
            return null;
        }
        boolean ceiling = arm.getBlockState().getOptionalValue((Property)ArmBlock.CEILING).orElse(false);
        Vec3 claw = SurgeryClaw.position(pos, ceiling, pose.chrome$baseAngle().getValue(partialTick), pose.chrome$lowerArmAngle().getValue(partialTick), pose.chrome$upperArmAngle().getValue(partialTick), pose.chrome$headAngle().getValue(partialTick));
        Vec3 grip = SurgeryClaw.position(pos, ceiling, Surgery.approach(seat, pos, ceiling, Surgery.site(seat, held, partialTick), partialTick));
        return new SurgeryCarry(held, seat.part(), claw.subtract(grip));
    }

    private static boolean lifeSize(SurgerySeat seat, ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }
        if (seat.patient() != null && LimbAttachments.get(seat.patient()).isMissing(BodyPart.BODY)) {
            return false;
        }
        if (Surgery.severed(held)) {
            return seat.ripped() && seat.part() != null;
        }
        if (CyberLimbs.frame(held)) {
            return seat.kind() == SurgerySeat.Kind.LIMB && seat.part() != null;
        }
        return held.is(CyberwareItems.SANDEVISTAN) && seat.kind() == SurgerySeat.Kind.SPINE;
    }

    public static boolean drawnOnPatient(ArmBlockEntity arm) {
        ItemStack held = ((ArmBlockEntityAccessor)arm).chrome$heldItem();
        if (held.isEmpty() || arm.getLevel() == null) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        for (SurgerySeat seat : SurgerySeats.in(arm.getLevel(), new AABB(arm.getBlockPos()).inflate(6.0))) {
            if (!arm.getBlockPos().equals((Object)seat.arm()) || !SurgeryCarry.lifeSize(seat, held)) continue;
            Player patient = seat.patient();
            if (patient == null) {
                return false;
            }
            return patient != mc.getCameraEntity() || !mc.options.getCameraType().isFirstPerson();
        }
        return false;
    }
}

