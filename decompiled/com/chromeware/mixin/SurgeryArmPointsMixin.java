/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity
 *  com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.surgery.SurgeryArmPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ArmBlockEntity.class}, remap=false)
public abstract class SurgeryArmPointsMixin {
    @Shadow
    List<ArmInteractionPoint> inputs;
    @Shadow
    List<ArmInteractionPoint> outputs;
    @Shadow
    ListTag interactionPointTag;

    @Inject(method={"read"}, at={@At(value="RETURN")})
    private void chrome$dropRepeatedPoints(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        if (this.interactionPointTag == null || this.interactionPointTag.size() < 2) {
            return;
        }
        LinkedHashSet unique = new LinkedHashSet(this.interactionPointTag);
        if (unique.size() == this.interactionPointTag.size()) {
            return;
        }
        ListTag repaired = new ListTag();
        repaired.addAll(unique);
        this.interactionPointTag = repaired;
    }

    @Inject(method={"initInteractionPoints"}, at={@At(value="TAIL")})
    private void chrome$surgeryBothWays(CallbackInfo ci) {
        SurgeryArmPointsMixin.dedupe(this.inputs);
        SurgeryArmPointsMixin.dedupe(this.outputs);
        for (ArmInteractionPoint point : List.copyOf(this.outputs)) {
            if (!(point instanceof SurgeryArmPoint) || SurgeryArmPointsMixin.hasSurgery(this.inputs, point)) continue;
            this.inputs.add(point);
        }
        for (ArmInteractionPoint point : List.copyOf(this.inputs)) {
            if (!(point instanceof SurgeryArmPoint) || SurgeryArmPointsMixin.hasSurgery(this.outputs, point)) continue;
            this.outputs.add(point);
        }
    }

    private static boolean hasSurgery(List<ArmInteractionPoint> points, ArmInteractionPoint point) {
        return points.stream().anyMatch(p -> p instanceof SurgeryArmPoint && p.getPos().equals((Object)point.getPos()));
    }

    private static void dedupe(List<ArmInteractionPoint> points) {
        HashSet seen = new HashSet();
        points.removeIf(p -> p instanceof SurgeryArmPoint && !seen.add(p.getPos()));
    }
}

