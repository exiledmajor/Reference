/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.client.cyberware;

import com.chromeware.body.OneHanded;
import com.chromeware.cyberware.GorillaCombat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class GorillaFirstPerson {
    public static boolean showsArm(Player player, HumanoidArm arm) {
        if (GorillaCombat.deployed(player, arm)) {
            return true;
        }
        InteractionHand hand = GorillaHandVisibility.shownHand(player.isUsingItem(), player.getUsedItemHand());
        return OneHanded.resolve(player, hand) == arm;
    }

    public static final class GorillaHandVisibility {
        public static InteractionHand shownHand(boolean usingItem, InteractionHand usedHand) {
            return usingItem ? usedHand : InteractionHand.MAIN_HAND;
        }
    }
}

