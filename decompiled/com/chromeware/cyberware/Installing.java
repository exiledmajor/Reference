/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.extendo.ExtendoGrip;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireImplant;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.QuickhackItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class Installing {
    private static final ThreadLocal<List<ItemStack>> CAPTURE = new ThreadLocal();

    public static void installHeld(Player player) {
        ItemStack selected;
        ItemStack itemStack = selected = Installing.supported(player.getMainHandItem()) ? player.getMainHandItem() : player.getOffhandItem();
        if (selected.getItem() instanceof QuickhackItem) {
            Netrunning.installProgram(player, selected);
            return;
        }
        if (CyberLimbs.frame(selected)) {
            CyberLimbs.installHeld(player);
        } else if (InventoryImplants.supported(selected)) {
            InventoryImplants.installHeld(player);
        } else if (selected.is(CyberwareItems.GORILLA_ARMS)) {
            GorillaImplant.installHeld(player);
        } else if (selected.is(CyberwareItems.MONOWIRE)) {
            MonowireImplant.installHeld(player);
        } else if (selected.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM)) {
            ProjectileLauncher.installHeld(player);
        } else if (selected.is(CyberwareItems.MANTIS_BLADES)) {
            MantisImplant.installHeld(player);
        } else if (ExtendoGrip.is(selected)) {
            ExtendoGrip.installHeld(player);
        } else if (selected.is(CyberwareItems.SANDEVISTAN) || selected.is(CyberwareItems.CYBERDECK) || Implant.CHROME_COMPRESSOR.matches(selected) || Implant.ZETATECH_BERSERK.matches(selected)) {
            SandevistanImplant.installHeld(player);
        } else {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.hold_implant"), true);
        }
    }

    public static boolean supported(ItemStack item) {
        if (item.is(CyberwareItems.CYBERDECK) || item.getItem() instanceof QuickhackItem) {
            return true;
        }
        if (Implant.CHROME_COMPRESSOR.matches(item) || Implant.ZETATECH_BERSERK.matches(item)) {
            return true;
        }
        return CyberLimbs.frame(item) || InventoryImplants.supported(item) || item.is(CyberwareItems.MANTIS_BLADES) || ExtendoGrip.is(item) || item.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM) || item.is(CyberwareItems.MONOWIRE) || item.is(CyberwareItems.GORILLA_ARMS) || item.is(CyberwareItems.SANDEVISTAN);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean installStack(Player player, ItemStack stack) {
        InteractionHand hand = InteractionHand.MAIN_HAND;
        ItemStack original = player.getMainHandItem();
        ItemStack single = stack.copyWithCount(1);
        player.setItemInHand(hand, single);
        boolean consumed = false;
        try {
            Installing.installHeld(player);
        }
        catch (Throwable throwable) {
            ItemStack now = player.getMainHandItem();
            consumed = single.isEmpty() || now != single;
            player.setItemInHand(hand, original);
            if (now != single && !now.isEmpty()) {
                Installing.returnItem(player, now);
            }
            throw throwable;
        }
        ItemStack now = player.getMainHandItem();
        consumed = single.isEmpty() || now != single;
        player.setItemInHand(hand, original);
        if (now != single && !now.isEmpty()) {
            Installing.returnItem(player, now);
        }
        return consumed;
    }

    public static List<ItemStack> capture(Runnable removal) {
        ArrayList<ItemStack> captured = new ArrayList<ItemStack>();
        CAPTURE.set(captured);
        try {
            removal.run();
        }
        finally {
            CAPTURE.remove();
        }
        return captured;
    }

    public static boolean canOperate(Player player) {
        return !player.level().isClientSide() && player.isAlive() && !player.isSpectator() && !LimbAttachments.get(player).isMissing(BodyPart.BODY);
    }

    public static void returnItem(Player player, ItemStack item) {
        List<ItemStack> captured = CAPTURE.get();
        if (captured != null) {
            if (!item.isEmpty()) {
                captured.add(item);
            }
            return;
        }
        for (int i = 9; i < player.getInventory().items.size() && !item.isEmpty(); ++i) {
            if (!((ItemStack)player.getInventory().items.get(i)).isEmpty()) continue;
            player.getInventory().setItem(i, item);
            player.getInventory().setChanged();
            return;
        }
        if (!item.isEmpty() && !player.getInventory().add(item)) {
            player.drop(item, false);
        }
    }

    public static final class GorillaImplant {
        public static boolean installHeld(Player player) {
            return ArmHardware.installHeld(player, s -> s.is(CyberwareItems.GORILLA_ARMS), false);
        }
    }
}

