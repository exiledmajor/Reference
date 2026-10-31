/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity$Phase
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin;

import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryArmPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ArmBlockEntity.class}, remap=false)
public abstract class SurgeryArmChoreographyMixin {
    @Shadow
    List<ArmInteractionPoint> inputs;
    @Shadow
    ItemStack heldItem;
    @Shadow
    ArmBlockEntity.Phase phase;
    @Shadow
    float chasedPointProgress;
    @Shadow
    int chasedPointIndex;
    @Unique
    private int chrome$tossTicks;

    @Inject(method={"getDistributableAmount"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$surgeryCall(ArmInteractionPoint point, int slot, CallbackInfoReturnable<Integer> cir) {
        if (point instanceof SurgeryArmPoint) {
            SurgeryArmPoint chair = (SurgeryArmPoint)point;
            cir.setReturnValue((Object)(chair.wantsVisit((ArmBlockEntity)this) ? 1 : 0));
        }
    }

    @Inject(method={"collectItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$surgeryVisit(CallbackInfo ci) {
        ArmInteractionPoint point;
        ArmInteractionPoint armInteractionPoint = point = this.phase == ArmBlockEntity.Phase.MOVE_TO_INPUT && this.chasedPointIndex >= 0 && this.chasedPointIndex < this.inputs.size() ? this.inputs.get(this.chasedPointIndex) : null;
        if (!(point instanceof SurgeryArmPoint)) {
            return;
        }
        SurgeryArmPoint chair = (SurgeryArmPoint)point;
        ArmBlockEntity arm = (ArmBlockEntity)this;
        this.heldItem = chair.visit(arm);
        this.phase = this.heldItem.isEmpty() ? ArmBlockEntity.Phase.SEARCH_INPUTS : ArmBlockEntity.Phase.SEARCH_OUTPUTS;
        this.chasedPointProgress = 0.0f;
        this.chasedPointIndex = -1;
        this.chrome$tossTicks = 0;
        arm.sendData();
        arm.setChanged();
        ci.cancel();
    }

    @Inject(method={"searchForDestination"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$keepSevered(CallbackInfo ci) {
        if (Surgery.severed(this.heldItem)) {
            ci.cancel();
        }
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void chrome$tossSevered(CallbackInfo ci) {
        ArmBlockEntity arm = (ArmBlockEntity)this;
        if (arm.getLevel() == null || arm.getLevel().isClientSide() || !Surgery.severed(this.heldItem)) {
            return;
        }
        if (this.phase != ArmBlockEntity.Phase.SEARCH_OUTPUTS || this.chasedPointProgress < 1.0f) {
            return;
        }
        if (++this.chrome$tossTicks < 8) {
            return;
        }
        SurgeryArmPoint.toss(arm);
        this.heldItem = ItemStack.EMPTY;
        this.phase = ArmBlockEntity.Phase.SEARCH_INPUTS;
        this.chrome$tossTicks = 0;
        arm.sendData();
        arm.setChanged();
    }
}

