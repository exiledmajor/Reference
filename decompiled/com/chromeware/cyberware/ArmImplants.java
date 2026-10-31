/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.BodyRestrictions;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.cyberware.ArmImplant;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.extendo.ExtendoGrip;
import com.chromeware.humanity.Humanity;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireCombat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ArmImplants {
    private static final Map<Player, HumanoidArm> REQUESTED = new WeakHashMap<Player, HumanoidArm>();

    public static ArmImplant get(Player player, HumanoidArm side) {
        return (ArmImplant)player.getData(side == HumanoidArm.LEFT ? PlayerData.ARM_IMPLANT_LEFT : PlayerData.ARM_IMPLANT);
    }

    public static void set(Player player, HumanoidArm side, ArmImplant implant) {
        ArmImplant placed = implant.installed() ? new ArmImplant(implant.item(), side) : ArmImplant.EMPTY;
        player.setData(side == HumanoidArm.LEFT ? PlayerData.ARM_IMPLANT_LEFT : PlayerData.ARM_IMPLANT, (Object)placed);
    }

    public static HumanoidArm sideOf(Player player, Predicate<ItemStack> test) {
        for (HumanoidArm side : new HumanoidArm[]{HumanoidArm.RIGHT, HumanoidArm.LEFT}) {
            if (!ArmImplants.get(player, side).is(test)) continue;
            return side;
        }
        return null;
    }

    public static boolean has(Player player, Predicate<ItemStack> test) {
        return ArmImplants.sideOf(player, test) != null;
    }

    public static boolean has(Player player, HumanoidArm side, Predicate<ItemStack> test) {
        return ArmImplants.get(player, side).is(test);
    }

    public static List<ItemStack> items(Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (HumanoidArm side : HumanoidArm.values()) {
            items.add(ArmImplants.get(player, side).item());
        }
        return items;
    }

    public static boolean gorilla(Player player) {
        return ArmImplants.has(player, s -> s.is(CyberwareItems.GORILLA_ARMS));
    }

    public static InteractionHand hand(Player player, HumanoidArm side) {
        return player.getMainArm() == side ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public static BodyPart part(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
    }

    public static BodyPart handPart(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? BodyPart.LEFT_HAND : BodyPart.RIGHT_HAND;
    }

    public static boolean active(Player player, HumanoidArm side) {
        return ArmImplants.get(player, side).replacesLimb() && !LimbAttachments.get(player).isMissing(BodyPart.BODY);
    }

    public static LimbState functionalState(Player player, LimbState state) {
        if (state.isMissing(BodyPart.BODY)) {
            return state;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            if (!ArmImplants.get(player, side).replacesLimb()) continue;
            state = state.restore(ArmImplants.part(side));
        }
        return state;
    }

    public static void request(Player player, HumanoidArm side) {
        if (side == null) {
            REQUESTED.remove(player);
        } else {
            REQUESTED.put(player, side);
        }
    }

    public static HumanoidArm installSide(Player player, ItemStack item) {
        return ArmImplants.installSide(player, item, InteractionHand.MAIN_HAND);
    }

    public static HumanoidArm installSide(Player player, ItemStack item, InteractionHand holding) {
        HumanoidArm main;
        if (ExtendoGrip.is(item)) {
            return HumanoidArm.RIGHT;
        }
        HumanoidArm requested = REQUESTED.get(player);
        if (requested != null) {
            return requested;
        }
        HumanoidArm humanoidArm = main = holding == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
        if (!ArmImplants.get(player, main).installed()) {
            return main;
        }
        if (!ArmImplants.get(player, main.getOpposite()).installed()) {
            return main.getOpposite();
        }
        return main;
    }

    public static boolean canPlace(Player player, ItemStack item, HumanoidArm side) {
        return (!ExtendoGrip.is(item) || side != HumanoidArm.LEFT) && CyberwareCapacity.canInstall(player, item, ArmImplants.get(player, side).item());
    }

    public static void place(Player player, HumanoidArm side, ItemStack held) {
        ArmImplants.retractAll(player);
        ArmImplant previous = ArmImplants.get(player, side);
        ArmImplants.set(player, side, new ArmImplant(held, side));
        held.shrink(1);
        if (previous.installed()) {
            Humanity.removed(player);
        }
        Installing.returnItem(player, previous.item());
        BodyRestrictions.apply(player);
    }

    public static void retractAll(Player player) {
        GorillaCombat.retract(player);
        MonowireCombat.retract(player);
        ProjectileLauncher.retract(player);
        MantisImplant.retract(player);
    }

    public static boolean remove(Player player, HumanoidArm side) {
        if (!Installing.canOperate(player)) {
            return false;
        }
        ArmImplant old = ArmImplants.get(player, side);
        if (!old.installed()) {
            return false;
        }
        ArmImplants.retractAll(player);
        ArmImplants.set(player, side, ArmImplant.EMPTY);
        Humanity.removed(player);
        Installing.returnItem(player, old.item());
        BodyRestrictions.apply(player);
        return true;
    }
}

