/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class OneHanded {
    public static HumanoidArm resolve(Player player, InteractionHand hand) {
        Inventory inventory = player.getInventory();
        return OneHanded.resolve(LimbAttachments.functional(player), player.getMainArm(), inventory.getSelected().isEmpty(), ((ItemStack)inventory.offhand.getFirst()).isEmpty(), hand);
    }

    public static HumanoidArm resolve(LimbState state, HumanoidArm mainArm, boolean mainEmpty, boolean offEmpty, InteractionHand hand) {
        HumanoidArm sole;
        boolean right;
        boolean left = !state.isMissing(BodyPart.LEFT_HAND);
        boolean bl = right = !state.isMissing(BodyPart.RIGHT_HAND);
        if (!left && !right) {
            return null;
        }
        if (left && right) {
            return hand == InteractionHand.MAIN_HAND ? mainArm : mainArm.getOpposite();
        }
        HumanoidArm humanoidArm = sole = left ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        if (hand == InteractionHand.OFF_HAND) {
            return mainEmpty ? sole : null;
        }
        return mainEmpty && !offEmpty ? null : sole;
    }

    public static HumanoidArm effectiveMainArm(Player player, InteractionHand hand) {
        HumanoidArm arm = OneHanded.resolve(player, hand);
        if (arm == null) {
            return player.getMainArm();
        }
        return hand == InteractionHand.MAIN_HAND ? arm : arm.getOpposite();
    }
}

