/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.BloodPump;
import com.chromeware.cyberware.BloodPumpState;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.cyberware.SandevistanState;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireCombat;
import com.chromeware.monowire.MonowireImplant;
import com.chromeware.netrunning.NetrunnerImplants;
import com.chromeware.netrunning.Netrunning;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ImplantCooldowns {
    public static final ResourceLocation BLOOD_PUMP = ImplantCooldowns.id("blood_pump");
    public static final ResourceLocation LAUNCHER = ImplantCooldowns.id("launcher");
    public static final ResourceLocation MONOWIRE = ImplantCooldowns.id("monowire");
    public static final ResourceLocation MANTIS = ImplantCooldowns.id("mantis_blades");
    public static final ResourceLocation GORILLA = ImplantCooldowns.id("gorilla_arms");
    public static final ResourceLocation QUANTUM_TUNER = ImplantCooldowns.id("quantum_tuner");
    public static final ResourceLocation SANDEVISTAN = ImplantCooldowns.id("sandevistan");
    private static final Map<ResourceLocation, Entry> ENTRIES = new LinkedHashMap<ResourceLocation, Entry>();

    private static void netrunner(Implant implant, int duration, int kind) {
        ImplantCooldowns.register(ImplantCooldowns.id(implant.id), duration, implant::installed, p -> NetrunnerImplants.remaining(p, kind), (p, ticks) -> NetrunnerImplants.reduce(p, kind, ticks), true);
    }

    private static void catalogue(Implant implant, int duration) {
        String key = implant.id + "_cooldown";
        ImplantCooldowns.register(ImplantCooldowns.id(implant.id), duration, implant::installed, p -> ImplantEffects.state(p).clock(key), (p, ticks) -> ImplantEffects.clock(p, key, Math.max(0, ImplantEffects.state(p).clock(key) - ticks)), true);
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }

    public static int baseDuration(ItemStack item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey((Object)item.getItem());
        if (!key.getNamespace().equals("chromeware")) {
            return 0;
        }
        if (key.getPath().equals("mantis_blades")) {
            return ImplantSettings.cooldown("mantis_blades", MantisImplant.RECHARGE_TICKS);
        }
        ResourceLocation timer = switch (key.getPath()) {
            case "projectile_launch_system" -> LAUNCHER;
            case "cyberdeck" -> ImplantCooldowns.id("cyberdeck_overclock");
            default -> key;
        };
        Entry entry = ENTRIES.get(timer);
        return entry == null ? 0 : ImplantSettings.cooldown(timer.getPath(), entry.duration);
    }

    public static void register(ResourceLocation id, int duration, Predicate<Player> installed, ToIntFunction<Player> remaining, ObjIntConsumer<Player> reduce, boolean tunerEligible) {
        if (duration <= 0 || ENTRIES.containsKey(id)) {
            throw new IllegalArgumentException("Unique timer and positive duration required");
        }
        ENTRIES.put(id, new Entry(duration, installed, remaining, reduce, tunerEligible));
    }

    public static void forEachRemaining(Player player, ObjIntConsumer<ResourceLocation> action) {
        for (Map.Entry<ResourceLocation, Entry> entry : ENTRIES.entrySet()) {
            if (!entry.getValue().installed.test(player)) continue;
            action.accept(entry.getKey(), entry.getValue().remaining.applyAsInt(player));
        }
    }

    public static void reduce(Player player, double fraction) {
        if (player.level().isClientSide() || fraction <= 0.0) {
            return;
        }
        for (Map.Entry<ResourceLocation, Entry> named : ENTRIES.entrySet()) {
            Entry entry = named.getValue();
            if (!entry.installed.test(player)) continue;
            entry.reduce.accept(player, (int)Math.round((double)ImplantSettings.cooldown(named.getKey().getPath(), entry.duration) * Math.min(1.0, fraction)));
        }
    }

    public static void fullyUsed(Player player, ResourceLocation id) {
        Entry entry = ENTRIES.get(id);
        if (player.level().isClientSide() || entry == null || !entry.tunerEligible || !entry.installed.test(player)) {
            return;
        }
        int refund = QuantumTuner.refund(player, entry.remaining.applyAsInt(player));
        if (refund > 0) {
            entry.reduce.accept(player, refund);
        }
    }

    static {
        ImplantCooldowns.register(BLOOD_PUMP, 600, InventoryImplants::hasBloodPump, player -> BloodPump.state(player).charges() == BloodPumpState.maxCharges() ? 0 : BloodPump.state(player).rechargeTicks() + (BloodPumpState.maxCharges() - BloodPump.state(player).charges() - 1) * QuantumTuner.duration(player, ImplantSettings.cooldown("blood_pump", 600)), (player, ticks) -> player.setData(PlayerData.BLOOD_PUMP_STATE, (Object)BloodPump.state(player).reduceRecharge(ticks, QuantumTuner.duration(player, ImplantSettings.cooldown("blood_pump", 600)))), true);
        ImplantCooldowns.register(LAUNCHER, 10, ProjectileLauncher::installed, ProjectileLauncher::cooldown, ProjectileLauncher::reduceCooldown, true);
        ImplantCooldowns.register(MONOWIRE, 12, MonowireImplant::installed, MonowireCombat::cooldown, MonowireCombat::reduceCooldown, true);
        ImplantCooldowns.register(GORILLA, 5, ArmImplants::gorilla, GorillaCombat::cooldown, GorillaCombat::reduceCooldown, true);
        ImplantCooldowns.register(MANTIS, MantisImplant.RECHARGE_TICKS, MantisImplant::installed, MantisImplant::cooldown, MantisImplant::reduceCooldown, true);
        ImplantCooldowns.register(QUANTUM_TUNER, 1200, InventoryImplants::hasQuantumTuner, player -> QuantumTuner.state(player).recovery(), (player, ticks) -> player.setData(PlayerData.QUANTUM_TUNER_STATE, (Object)QuantumTuner.state(player).reduceRecovery(ticks)), false);
        ImplantCooldowns.register(SANDEVISTAN, 600, SandevistanImplant::active, p -> ((SandevistanState)p.getData(PlayerData.SANDEVISTAN_STATE)).cooldown(), (p, ticks) -> p.setData(PlayerData.SANDEVISTAN_STATE, (Object)((SandevistanState)p.getData(PlayerData.SANDEVISTAN_STATE)).reduce(ticks)), true);
        ImplantCooldowns.catalogue(Implant.SECOND_HEART, 4400);
        ImplantCooldowns.catalogue(Implant.OPTICAL_CAMO, 1000);
        ImplantCooldowns.catalogue(Implant.COUNTERSHELL, 120);
        ImplantCooldowns.catalogue(Implant.REFLEX_TUNER, 700);
        ImplantCooldowns.catalogue(Implant.REVULSOR, 700);
        ImplantCooldowns.register(ZetatechBerserk.TIMER, 500, Implant.ZETATECH_BERSERK::installed, p -> ZetatechBerserk.state(p).cooldown(), (p, ticks) -> p.setData(PlayerData.BERSERK_STATE, (Object)ZetatechBerserk.state(p).reduce(ticks)), true);
        ImplantCooldowns.register(ImplantCooldowns.id("cyberdeck_overclock"), 600, Netrunning::installed, p -> NetrunnerImplants.remaining(p, 0), (p, ticks) -> NetrunnerImplants.reduce(p, 0, ticks), true);
        ImplantCooldowns.netrunner(Implant.CAMILLO_RAM_MANAGER, 1600, 1);
        ImplantCooldowns.netrunner(Implant.RAM_REALLOCATOR, 1700, 2);
        ImplantCooldowns.netrunner(Implant.SELF_ICE, 400, 3);
    }

    private record Entry(int duration, Predicate<Player> installed, ToIntFunction<Player> remaining, ObjIntConsumer<Player> reduce, boolean tunerEligible) {
    }
}

