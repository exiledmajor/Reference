/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.attachment.AttachmentType
 */
package com.chromeware.monowire;

import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.monowire.MonowireState;
import java.util.function.Supplier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;

public final class MonowireImplant {
    public static boolean installed(Player player) {
        return ArmImplants.has(player, s -> s.is(CyberwareItems.MONOWIRE));
    }

    public static boolean installed(Player player, HumanoidArm side) {
        return ArmImplants.has(player, side, s -> s.is(CyberwareItems.MONOWIRE));
    }

    public static HumanoidArm side(Player player) {
        HumanoidArm side = ArmImplants.sideOf(player, s -> s.is(CyberwareItems.MONOWIRE));
        return side != null ? side : player.getMainArm();
    }

    public static boolean active(Player player) {
        return MonowireImplant.hasHardware(player, HumanoidArm.RIGHT) || MonowireImplant.hasHardware(player, HumanoidArm.LEFT);
    }

    public static boolean hasHardware(Player player, HumanoidArm side) {
        return ArmHardware.fitted(player, side, CyberwareItems.MONOWIRE);
    }

    public static Supplier<AttachmentType<MonowireState>> stateKey(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? PlayerData.MONOWIRE_STATE_LEFT : PlayerData.MONOWIRE_STATE;
    }

    public static MonowireState state(Player player, HumanoidArm side) {
        return (MonowireState)player.getData(MonowireImplant.stateKey(side));
    }

    public static MonowireState state(Player player) {
        return MonowireImplant.state(player, MonowireImplant.side(player));
    }

    public static boolean deployed(Player player, HumanoidArm side) {
        return MonowireImplant.hasHardware(player, side) && MonowireImplant.state(player, side).deployed();
    }

    public static boolean deployed(Player player) {
        return MonowireImplant.deployed(player, HumanoidArm.RIGHT) || MonowireImplant.deployed(player, HumanoidArm.LEFT);
    }

    public static boolean installHeld(Player player) {
        return ArmHardware.installHeld(player, s -> s.is(CyberwareItems.MONOWIRE), true);
    }
}

