/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.InstalledImplant;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.humanity.Humanity;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.sandevistan.Sandevistan;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SandevistanImplant {
    public static InstalledImplant get(Player player) {
        return (InstalledImplant)player.getData(PlayerData.OPERATING_SYSTEM);
    }

    public static boolean active(Player player) {
        return SandevistanImplant.get(player).item().is(CyberwareItems.SANDEVISTAN) && !LimbAttachments.get(player).isMissing(BodyPart.BODY);
    }

    public static boolean installHeld(Player player) {
        if (!Installing.canOperate(player)) {
            return false;
        }
        InteractionHand hand = SandevistanImplant.supported(player.getMainHandItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack held = player.getItemInHand(hand);
        if (!SandevistanImplant.supported(held)) {
            return false;
        }
        InstalledImplant previous = SandevistanImplant.get(player);
        if (!CyberwareCapacity.canInstall(player, held, previous.item())) {
            return false;
        }
        InstalledImplant installed = new InstalledImplant(held);
        Netrunning.disconnect(player);
        ZetatechBerserk.end(player, true);
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (!held.is(CyberwareItems.SANDEVISTAN) && Sandevistan.isActive((Entity)player)) {
                Sandevistan.deactivate(serverPlayer);
            }
        }
        held.shrink(1);
        player.setData(PlayerData.OPERATING_SYSTEM, (Object)installed);
        if (previous.installed()) {
            Humanity.removed(player);
        }
        Installing.returnItem(player, previous.item());
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.internal_installed", (Object[])new Object[]{installed.item().getHoverName()}), true);
        return true;
    }

    private static boolean supported(ItemStack item) {
        return item.is(CyberwareItems.SANDEVISTAN) || item.is(CyberwareItems.CYBERDECK) || Implant.CHROME_COMPRESSOR.matches(item) || Implant.ZETATECH_BERSERK.matches(item);
    }

    public static boolean remove(Player player) {
        if (!Installing.canOperate(player)) {
            return false;
        }
        InstalledImplant previous = SandevistanImplant.get(player);
        if (!previous.installed()) {
            return false;
        }
        Netrunning.disconnect(player);
        ZetatechBerserk.end(player, true);
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (Sandevistan.isActive((Entity)player)) {
                Sandevistan.deactivate(serverPlayer);
            }
        }
        player.setData(PlayerData.OPERATING_SYSTEM, (Object)InstalledImplant.EMPTY);
        if (previous.installed()) {
            Humanity.removed(player);
        }
        Installing.returnItem(player, previous.item());
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.internal_removed", (Object[])new Object[]{previous.item().getHoverName()}), true);
        return true;
    }
}

