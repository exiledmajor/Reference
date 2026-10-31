/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.humanity;

import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireImplant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class BerserkReach {
    public static final double LAUNCHER = 48.0;
    public static final double GUN = 96.0;
    private static final Set<String> GUNS = Set.of("com.vicmatskiv.pointblank.item.GunItem", "com.tacz.guns.api.item.IGun", "com.atsuishio.superbwarfare.item.gun.GunItem", "top.ribs.scguns.item.GunItem");
    private static final Map<Item, Boolean> GUN_ITEMS = new ConcurrentHashMap<Item, Boolean>();

    public static double reach(Player p, boolean attack) {
        HumanoidArm arm = ArmControls.armFor(p, attack);
        if (ArmControls.deployedOn(p, arm)) {
            return BerserkReach.hardware(p, arm);
        }
        if (!attack) {
            return 0.0;
        }
        return BerserkReach.gun(p.getMainHandItem()) ? 96.0 : p.entityInteractionRange();
    }

    static double hardware(Player p, HumanoidArm arm) {
        if (MonowireImplant.deployed(p, arm)) {
            return 8.0;
        }
        if (MantisImplant.deployed(p, arm)) {
            return 3.5;
        }
        if (ProjectileLauncher.deployed(p, arm)) {
            return 48.0;
        }
        return GorillaCombat.deployed(p, arm) ? p.entityInteractionRange() : 0.0;
    }

    public static boolean gun(ItemStack stack) {
        return !stack.isEmpty() && GUN_ITEMS.computeIfAbsent(stack.getItem(), item -> {
            for (Class<?> type = item.getClass(); type != null; type = type.getSuperclass()) {
                if (GUNS.contains(type.getName())) {
                    return true;
                }
                for (Class<?> face : type.getInterfaces()) {
                    if (!GUNS.contains(face.getName())) continue;
                    return true;
                }
            }
            return false;
        }) != false;
    }
}

