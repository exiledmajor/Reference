/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.client.compat;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.client.cyberware.ArmFirstPerson;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.HardwareHands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CompatHands {
    public static boolean hardwareOwnsButton(boolean attack) {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && ArmControls.deployedOn((Player)player, ArmControls.armFor((Player)player, attack));
    }

    public static boolean hardwareFirstPerson() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && ArmFirstPerson.hardwareRendering((AbstractClientPlayer)player);
    }

    public static boolean lockedStack(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (stack.isEmpty() || mc.level == null || !mc.isSameThread()) {
            return false;
        }
        for (Player player : mc.level.players()) {
            Inventory inventory = player.getInventory();
            if (inventory.getSelected() == stack && HardwareHands.locked(player, InteractionHand.MAIN_HAND)) {
                return true;
            }
            if (inventory.offhand.getFirst() != stack || !HardwareHands.locked(player, InteractionHand.OFF_HAND)) continue;
            return true;
        }
        return false;
    }

    public static boolean needsVanillaModel(Player player) {
        AbstractClientPlayer client;
        if (player instanceof AbstractClientPlayer && ArmFirstPerson.hardwareRendering(client = (AbstractClientPlayer)player)) {
            return true;
        }
        if (GorillaCombat.deployed(player) || ArmImplants.gorilla(player)) {
            return true;
        }
        for (BodyPart part : CyberLimbs.PARTS) {
            if (!CyberLimbs.chrome(player, part)) continue;
            return true;
        }
        return false;
    }
}

