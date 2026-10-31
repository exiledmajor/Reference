/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.config;

import com.chromeware.config.ChromewareConfig;
import com.chromeware.config.ImplantSettings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ConfiguredStats {
    public static boolean appendIfCustomized(ItemStack item, List<Component> lines) {
        String id = BuiltInRegistries.ITEM.getKey((Object)item.getItem()).getPath();
        boolean changed = ChromewareConfig.DEFINITIONS.values().stream().anyMatch(stat -> stat.implant().equals(id) && !stat.key().equals("capacity") && !stat.key().equals("armor") && Math.abs(ImplantSettings.stat(id, stat.key(), stat.defaultValue()) - stat.defaultValue()) > 1.0E-8);
        if (!changed) {
            return false;
        }
        lines.add((Component)Component.translatable((String)"tooltip.chromeware.configured_effects").withStyle(ChatFormatting.GOLD));
        for (ChromewareConfig.Stat stat2 : ChromewareConfig.DEFINITIONS.values()) {
            if (!stat2.implant().equals(id) || List.of("capacity", "armor", "cooldown_seconds").contains(stat2.key())) continue;
            double value = ImplantSettings.stat(id, stat2.key(), stat2.defaultValue());
            String suffix = "";
            if (stat2.unit().startsWith("fraction")) {
                value *= 100.0;
                suffix = "%";
            } else if (stat2.unit().startsWith("seconds")) {
                suffix = "s";
            } else if (stat2.unit().equals("blocks")) {
                suffix = " blocks";
            } else if (stat2.unit().equals("multiplier")) {
                suffix = "x";
            }
            String number = BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
            lines.add((Component)Component.translatable((String)"tooltip.chromeware.configured_stat", (Object[])new Object[]{Component.translatable((String)("config.chromeware.stat." + id + "." + stat2.key())), number + suffix}).withStyle(ChatFormatting.GRAY));
        }
        return true;
    }
}

