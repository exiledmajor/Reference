/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.netrunning;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.netrunning.DeckState;
import com.chromeware.netrunning.Netrunning;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class NetrunnerImplants {
    public static int maximum(Player p) {
        if (!Netrunning.installed(p)) {
            return 0;
        }
        return Math.max(1, ImplantSettings.istat("cyberdeck", "maximum_ram", 24) + (Implant.EX_DISK.active(p) ? ImplantSettings.istat("ex_disk", "ram_bonus", 6) : 0) + (Implant.RAM_UPGRADE.active(p) ? ImplantSettings.istat("ram_upgrade", "ram_bonus", 2) : 0) + (Implant.MECHATRONIC_CORE.active(p) ? ImplantSettings.istat("mechatronic_core", "ram_bonus", 2) : 0) + (Implant.MEMORY_BOOST.active(p) ? ImplantSettings.istat("memory_boost", "ram_bonus", 1) : 0) + (Implant.CAMILLO_RAM_MANAGER.active(p) ? ImplantSettings.istat("camillo_ram_manager", "ram_bonus", 2) : 0) + (Implant.RAM_REALLOCATOR.active(p) ? ImplantSettings.istat("ram_reallocator", "ram_bonus", 2) : 0) + (Implant.SELF_ICE.active(p) ? ImplantSettings.istat("self_ice", "ram_bonus", 2) : 0) + (Implant.RAM_RECOUP.active(p) ? ImplantSettings.istat("ram_recoup", "ram_bonus", 2) : 0) - (Implant.BIOCONDUCTOR.active(p) ? ImplantSettings.istat("bioconductor", "ram_penalty", 4) : 0) - (Implant.COX_2_CYBERSOMATIC_OPTIMIZER.active(p) ? ImplantSettings.istat("cox_2_cybersomatic_optimizer", "ram_penalty", 4) : 0));
    }

    public static double recovery(Player p) {
        double rate = ImplantSettings.stat("cyberdeck", "ram_recovery_per_second", 0.5) + (Implant.RAM_UPGRADE.active(p) ? ImplantSettings.stat("ram_upgrade", "ram_recovery_per_second", 0.2) : 0.0);
        if (Implant.FEEN_X.active(p) && Netrunning.available(p) < ImplantSettings.stat("feen_x", "ram_threshold", 7.0)) {
            rate *= ImplantSettings.stat("feen_x", "recovery_multiplier", 3.5);
        }
        return rate;
    }

    public static double uploadSpeed(Player p) {
        return Implant.EX_DISK.active(p) ? ImplantSettings.stat("ex_disk", "upload_speed_multiplier", 1.35) : 1.0;
    }

    public static boolean critical(Player p) {
        return Implant.COX_2_CYBERSOMATIC_OPTIMIZER.active(p) && (ImplantSettings.stat("cox_2_cybersomatic_optimizer", "quickhack_critical_chance", 1.0) >= 1.0 || p.getRandom().nextDouble() < ImplantSettings.stat("cox_2_cybersomatic_optimizer", "quickhack_critical_chance", 1.0)) || Implant.BIOCONDUCTOR.active(p) && p.getRandom().nextDouble() < ImplantSettings.stat("bioconductor", "quickhack_critical_chance", 0.35);
    }

    public static void recover(Player p, double amount) {
        if (Netrunning.usable(p)) {
            Netrunning.state(p, Netrunning.state(p).debt(Math.max(0.0, Netrunning.state(p).debt() - Math.max(0.0, amount))));
        }
    }

    public static void kill(Player p) {
        if (Implant.MEMORY_BOOST.active(p)) {
            NetrunnerImplants.recover(p, ImplantSettings.stat("memory_boost", "ram_per_kill", 1.0));
        }
    }

    public static void recoup(Player p, float damage) {
        if (Implant.RAM_RECOUP.active(p)) {
            NetrunnerImplants.recover(p, (double)damage * ImplantSettings.stat("ram_recoup", "damage_to_ram_fraction", 0.2));
        }
    }

    public static void thresholds(Player p) {
        if (!Netrunning.usable(p)) {
            return;
        }
        double observed = Netrunning.available(p);
        DeckState s = Netrunning.state(p);
        if (Implant.CAMILLO_RAM_MANAGER.active(p) && observed <= (double)NetrunnerImplants.maximum(p) * ImplantSettings.stat("camillo_ram_manager", "low_ram_threshold_fraction", 0.2) && s.camillo() == 0) {
            Netrunning.state(p, new DeckState(Math.max(0.0, s.debt() - (double)NetrunnerImplants.maximum(p) * ImplantSettings.stat("camillo_ram_manager", "ram_refill_fraction", 0.2)), s.programs(), s.overclock(), s.cooldown(), QuantumTuner.duration(p, ImplantSettings.cooldown("camillo_ram_manager", 1600)), s.reallocator(), s.ice()));
            ImplantCooldowns.fullyUsed(p, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"camillo_ram_manager"));
        }
        s = Netrunning.state(p);
        if (Implant.RAM_REALLOCATOR.active(p) && observed <= (double)NetrunnerImplants.maximum(p) * ImplantSettings.stat("ram_reallocator", "low_ram_threshold_fraction", 0.2) && s.reallocator() == 0) {
            Netrunning.state(p, new DeckState(Math.max(0.0, s.debt() - (double)NetrunnerImplants.maximum(p) * ImplantSettings.stat("ram_reallocator", "ram_refill_fraction", 0.4)), s.programs(), s.overclock(), s.cooldown(), s.camillo(), QuantumTuner.duration(p, ImplantSettings.cooldown("ram_reallocator", 1700)), s.ice()));
            ImplantCooldowns.fullyUsed(p, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"ram_reallocator"));
        }
    }

    public static boolean block(Player p) {
        if (!Implant.SELF_ICE.active(p) || Netrunning.state(p).ice() > 0) {
            return false;
        }
        DeckState s = Netrunning.state(p);
        Netrunning.state(p, new DeckState(s.debt(), s.programs(), s.overclock(), s.cooldown(), s.camillo(), s.reallocator(), QuantumTuner.duration(p, ImplantSettings.cooldown("self_ice", 400))));
        ImplantCooldowns.fullyUsed(p, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"self_ice"));
        Netrunning.message(p, "ice");
        return true;
    }

    public static int remaining(Player p, int kind) {
        DeckState s = Netrunning.state(p);
        return kind == 0 ? s.cooldown() : (kind == 1 ? s.camillo() : (kind == 2 ? s.reallocator() : s.ice()));
    }

    public static void reduce(Player p, int kind, int ticks) {
        DeckState s = Netrunning.state(p);
        Netrunning.state(p, new DeckState(s.debt(), s.programs(), s.overclock(), kind == 0 ? Math.max(0, s.cooldown() - ticks) : s.cooldown(), kind == 1 ? Math.max(0, s.camillo() - ticks) : s.camillo(), kind == 2 ? Math.max(0, s.reallocator() - ticks) : s.reallocator(), kind == 3 ? Math.max(0, s.ice() - ticks) : s.ice()));
    }
}

