/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.Implant;
import com.chromeware.extendo.ExtendoGrip;
import java.math.BigDecimal;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class CyberwareStats {
    private static final Stats NONE = new Stats(0, 0);
    private static final Map<ResourceLocation, Stats> STATS = Map.ofEntries(CyberwareStats.entry("sandevistan", 44, 0), CyberwareStats.entry("cyberdeck", 16, 0), CyberwareStats.entry("gorilla_arms", 4, 0), CyberwareStats.entry("monowire", 4, 0), CyberwareStats.entry("mantis_blades", 4, 0), CyberwareStats.entry("projectile_launch_system", 4, 0), CyberwareStats.entry("cyber_arm", 0, 50), CyberwareStats.entry("cyber_leg", 0, 175), CyberwareStats.entry("cyber_torso", 0, 300), CyberwareStats.entry("blood_pump", 15, 0), CyberwareStats.entry("axolotl", 48, 0), CyberwareStats.entry("quantum_tuner", 45, 0), CyberwareStats.entry("dense_marrow", 16, 54), CyberwareStats.entry("rara_avis", 45, 0), CyberwareStats.entry("scarab", 14, 46));

    private static Map.Entry<ResourceLocation, Stats> entry(String name, int capacity, int armor) {
        return Map.entry(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name), new Stats(capacity, armor));
    }

    public static Stats get(ItemStack item) {
        Stats base;
        Implant implant = Implant.of(item);
        Stats stats = implant != null ? new Stats(implant.capacity, implant.cyberpunkArmor, implant == Implant.COGITO_LATTICE ? 2.5 : CyberwareStats.armorPoints(implant.cyberpunkArmor)) : (item.isEmpty() ? NONE : (base = ExtendoGrip.is(item) ? new Stats(4, 0) : STATS.getOrDefault(BuiltInRegistries.ITEM.getKey((Object)item.getItem()), NONE)));
        if (item.isEmpty() || base == NONE) {
            return base;
        }
        String id = BuiltInRegistries.ITEM.getKey((Object)item.getItem()).getPath();
        return new Stats(ImplantSettings.istat(id, "capacity", base.capacity()), base.cyberpunkArmor(), ImplantSettings.stat(id, "armor", base.armor()));
    }

    public static double armorPoints(int cyberpunkArmor) {
        return Math.ceil((double)Math.max(0, cyberpunkArmor) / 25.0) * 0.5;
    }

    public static String formatArmor(double armor) {
        return BigDecimal.valueOf(armor).stripTrailingZeros().toPlainString();
    }

    public record Stats(int capacity, int cyberpunkArmor, double armor) {
        public Stats(int capacity, int cyberpunkArmor) {
            this(capacity, cyberpunkArmor, CyberwareStats.armorPoints(cyberpunkArmor));
        }
    }
}

