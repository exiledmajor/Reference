/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CapacityProgress;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCategory;
import com.chromeware.cyberware.InstalledImplant;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanImplant;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CyberwareCapacity {
    public static final int DEFAULT_LIMIT = 38;
    public static final int OLD_DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT = 10000;

    public static List<ItemStack> installed(Player player) {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        result.addAll(ArmImplants.items(player));
        result.add(SandevistanImplant.get(player).item());
        for (int category = 0; category <= ImplantCategory.FACE.index; ++category) {
            for (InstalledImplant implant : InventoryImplants.get(player, category).slots()) {
                result.add(implant.item());
            }
        }
        for (BodyPart part : CyberLimbs.PARTS) {
            if (!CyberLimbs.has(player, part)) continue;
            result.add(CyberLimbs.item(part));
        }
        return List.copyOf(result);
    }

    public static int used(Player player) {
        return CyberwareCapacity.installed(player).stream().mapToInt(i -> CyberwareStats.get(i).capacity()).sum();
    }

    public static int limit(Player player) {
        int limbs = 0;
        for (BodyPart part : CyberLimbs.PARTS) {
            if (!CyberLimbs.has(player, part)) continue;
            limbs += CyberwareCapacity.bonus(CyberLimbs.item(part));
        }
        return CyberwareCapacity.baseLimit(player) + CapacityProgress.bonus(player) + limbs + (Implant.CHROME_COMPRESSOR.active(player) ? CyberwareCapacity.bonus(Implant.CHROME_COMPRESSOR.stack()) : 0);
    }

    public static int bonus(ItemStack item) {
        if (Implant.CHROME_COMPRESSOR.matches(item)) {
            return ImplantSettings.istat("chrome_compressor", "capacity_bonus", 70);
        }
        return CyberLimbs.frame(item) ? ImplantSettings.istat(BuiltInRegistries.ITEM.getKey((Object)item.getItem()).getPath(), "capacity_bonus", 10) : 0;
    }

    public static int baseLimit(Player player) {
        int saved = (Integer)player.getData(PlayerData.CAPACITY_LIMIT);
        return (Boolean)player.getData(PlayerData.CAPACITY_CUSTOM_LIMIT) != false || saved != 38 ? saved : ChromewareConfig.startingCapacity();
    }

    public static boolean enabled(Player player) {
        return ChromewareConfig.capacityEnabled() && (Boolean)player.getData(PlayerData.CAPACITY_ENFORCED) != false;
    }

    public static boolean canInstall(Player player, ItemStack next, ItemStack previous) {
        if (CyberwareCapacity.fits(player, next, previous)) {
            return true;
        }
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.capacity_full", (Object[])new Object[]{CyberwareCapacity.used(player) - CyberwareStats.get(previous).capacity() + CyberwareStats.get(next).capacity(), CyberwareCapacity.limit(player)}), true);
        return false;
    }

    public static boolean fits(Player player, ItemStack next, ItemStack previous) {
        int projectedLimit;
        if (!CyberwareCapacity.enabled(player)) {
            return true;
        }
        int used = CyberwareCapacity.used(player);
        int projected = used - CyberwareStats.get(previous).capacity() + CyberwareStats.get(next).capacity();
        return projected <= (projectedLimit = CyberwareCapacity.limit(player) - CyberwareCapacity.bonus(previous) + CyberwareCapacity.bonus(next)) || projected <= used && projectedLimit >= CyberwareCapacity.limit(player);
    }
}

