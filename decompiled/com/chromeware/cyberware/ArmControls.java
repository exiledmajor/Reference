/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireCombat;
import com.chromeware.monowire.MonowireImplant;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;

public final class ArmControls {
    public static boolean deployable(Player p) {
        return MantisImplant.installed(p) || ProjectileLauncher.installed(p) || MonowireImplant.installed(p) || ArmImplants.gorilla(p);
    }

    public static boolean anyDeployed(Player p) {
        return MantisImplant.deployed(p) || ProjectileLauncher.deployed(p) || MonowireImplant.deployed(p) || GorillaCombat.deployed(p);
    }

    public static boolean deployedOn(Player p, HumanoidArm side) {
        if (MantisImplant.deployed(p, side)) {
            return true;
        }
        if (MonowireImplant.deployed(p, side)) {
            return true;
        }
        return ProjectileLauncher.deployed(p, side) || GorillaCombat.deployed(p, side);
    }

    public static HumanoidArm armFor(Player p, boolean attack) {
        return attack ? p.getMainArm() : p.getMainArm().getOpposite();
    }

    public static void toggle(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        if (DisabledNotice.blocked(p)) {
            return;
        }
        if (ArmControls.anyDeployed(p)) {
            ArmImplants.retractAll(p);
            return;
        }
        MantisImplant.toggle(p);
        if (ProjectileLauncher.installed(p)) {
            ProjectileLauncher.toggle(p);
        }
        if (MonowireImplant.installed(p)) {
            MonowireCombat.toggle(p);
        }
        GorillaCombat.deploy(p);
    }

    public static void act(Player p, HumanoidArm side) {
        if (p.level().isClientSide()) {
            return;
        }
        if (DisabledNotice.blocked(p)) {
            return;
        }
        if (MantisImplant.deployed(p, side)) {
            MantisImplant.slash(p, side);
        } else if (MonowireImplant.deployed(p, side)) {
            MonowireCombat.attack(p, side);
        } else if (ProjectileLauncher.deployed(p, side)) {
            ProjectileLauncher.fire(p, side);
        } else if (GorillaCombat.deployed(p, side)) {
            GorillaCombat.punch(p, side);
        }
    }

    public static void charge(Player p, HumanoidArm side) {
        if (p.level().isClientSide() || DisabledNotice.blocked(p)) {
            return;
        }
        if (MonowireImplant.deployed(p, side)) {
            MonowireCombat.charge(p);
        } else if (ProjectileLauncher.deployed(p, side)) {
            ProjectileLauncher.charge(p, side);
        } else if (GorillaCombat.deployed(p, side)) {
            GorillaCombat.charge(p, side);
        }
    }

    public static void release(Player p, HumanoidArm side) {
        if (p.level().isClientSide() || DisabledNotice.blocked(p)) {
            return;
        }
        if (MonowireImplant.deployed(p, side)) {
            MonowireCombat.release(p);
        } else if (ProjectileLauncher.deployed(p, side)) {
            ProjectileLauncher.release(p, side);
        } else if (GorillaCombat.deployed(p, side)) {
            GorillaCombat.release(p, side);
        }
    }

    public static void cancel(Player p, HumanoidArm side) {
        if (p.level().isClientSide()) {
            return;
        }
        MonowireCombat.cancel(p);
        ProjectileLauncher.cancel(p, side);
        GorillaCombat.cancel(p, side);
    }
}

