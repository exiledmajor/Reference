/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.ItemTooltipEvent
 */
package com.chromeware.cyberware;

import com.chromeware.body.CyberLimbs;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.config.ConfiguredStats;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.ImplantCategory;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.InternalImplantItem;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.surgery.Surgery;
import java.math.BigDecimal;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid="chromeware")
public final class CyberwareTooltips {
    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        String[] stringArray;
        CyberwareStats.Stats stats = CyberwareStats.get(event.getItemStack());
        if (!ChromewareConfig.DEFINITIONS.containsKey(BuiltInRegistries.ITEM.getKey((Object)event.getItemStack().getItem()).getPath() + ".capacity")) {
            return;
        }
        if (event.getItemStack().is(CyberwareItems.GORILLA_ARMS)) {
            String[] stringArray2 = new String[4];
            stringArray2[0] = "gorilla_arms";
            stringArray2[1] = "gorilla_arms_bleed";
            stringArray2[2] = "gorilla_arms_damage";
            stringArray = stringArray2;
            stringArray2[3] = "gorilla_arms_charge";
        } else if (event.getItemStack().is(CyberwareItems.MANTIS_BLADES)) {
            String[] stringArray3 = new String[3];
            stringArray3[0] = "mantis_blades";
            stringArray3[1] = "mantis_blades_damage";
            stringArray = stringArray3;
            stringArray3[2] = "mantis_blades_charge";
        } else if (event.getItemStack().is(CyberwareItems.MONOWIRE)) {
            String[] stringArray4 = new String[2];
            stringArray4[0] = "monowire";
            stringArray = stringArray4;
            stringArray4[1] = "monowire_damage";
        } else if (event.getItemStack().is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM)) {
            String[] stringArray5 = new String[3];
            stringArray5[0] = "projectile_launch_system";
            stringArray5[1] = "projectile_launch_system_ammo";
            stringArray = stringArray5;
            stringArray5[2] = "projectile_launch_system_charge";
        } else if (event.getItemStack().is(CyberwareItems.CYBER_ARM)) {
            String[] stringArray6 = new String[1];
            stringArray = stringArray6;
            stringArray6[0] = "cyber_arm_damage";
        } else if (event.getItemStack().is(CyberwareItems.CYBER_LEG)) {
            String[] stringArray7 = new String[1];
            stringArray = stringArray7;
            stringArray7[0] = "cyber_leg_fall";
        } else {
            stringArray = new String[]{};
        }
        String[] description = stringArray;
        boolean limb = CyberLimbs.frame(event.getItemStack());
        if (!(event.getItemStack().getItem() instanceof InternalImplantItem) && ConfiguredStats.appendIfCustomized(event.getItemStack(), event.getToolTip())) {
            description = new String[]{};
        }
        for (String key : description) {
            event.getToolTip().add(Component.translatable((String)("tooltip.chromeware." + key)).withStyle(ChatFormatting.GRAY));
        }
        Component slot = CyberwareTooltips.slot(event.getItemStack());
        if (slot != null) {
            event.getToolTip().add(Component.translatable((String)"tooltip.chromeware.slot", (Object[])new Object[]{slot}).withStyle(ChatFormatting.AQUA));
        }
        if (limb) {
            event.getToolTip().add(Component.translatable((String)"tooltip.chromeware.capacity_bonus", (Object[])new Object[]{CyberwareCapacity.bonus(event.getItemStack())}).withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.translatable((String)"tooltip.chromeware.humanity_loss", (Object[])new Object[]{BigDecimal.valueOf(ImplantSettings.stat(BuiltInRegistries.ITEM.getKey((Object)event.getItemStack().getItem()).getPath(), "humanity_loss_percent", 5.0)).stripTrailingZeros().toPlainString()}).withStyle(ChatFormatting.RED));
        } else {
            event.getToolTip().add(Component.translatable((String)"tooltip.chromeware.capacity", (Object[])new Object[]{stats.capacity()}).withStyle(ChatFormatting.AQUA));
        }
        int ticks = ImplantCooldowns.baseDuration(event.getItemStack());
        if (ticks > 0) {
            event.getToolTip().add(Component.translatable((String)"tooltip.chromeware.cooldown", (Object[])new Object[]{CyberwareTooltips.cooldown(event.getItemStack(), ticks)}).withStyle(ChatFormatting.AQUA));
        }
        if (stats.armor() > 0.0) {
            event.getToolTip().add(Component.translatable((String)"tooltip.chromeware.armor", (Object[])new Object[]{CyberwareStats.formatArmor(stats.armor())}).withStyle(ChatFormatting.BLUE));
        } else if (event.getItemStack().is(CyberwareItems.RARA_AVIS)) {
            event.getToolTip().add(Component.translatable((String)"tooltip.chromeware.armor_percent", (Object[])new Object[]{Math.round(ImplantSettings.stat("rara_avis", "armor_bonus_fraction", 0.4) * 100.0)}).withStyle(ChatFormatting.BLUE));
        }
    }

    private static Component slot(ItemStack item) {
        if (CyberLimbs.frame(item)) {
            return Component.translatable((String)"screen.chromeware.page.body");
        }
        if (Surgery.armHardware(item)) {
            return Component.translatable((String)"screen.chromeware.category.arms");
        }
        if (Surgery.operatingSystem(item)) {
            return Component.translatable((String)"screen.chromeware.category.operating_system");
        }
        if (!InventoryImplants.supported(item)) {
            return null;
        }
        ImplantCategory group = ImplantCategory.byIndex(InventoryImplants.category(item));
        return group == null ? null : Component.translatable((String)("screen.chromeware.category." + group.key));
    }

    private static Component cooldown(ItemStack item, int ticks) {
        String seconds = BigDecimal.valueOf(ticks).divide(BigDecimal.valueOf(20L)).stripTrailingZeros().toPlainString();
        MutableComponent value = Component.translatable((String)"tooltip.chromeware.cooldown_seconds", (Object[])new Object[]{seconds});
        return item.is(CyberwareItems.CYBERDECK) ? Component.translatable((String)"tooltip.chromeware.cooldown_overclock", (Object[])new Object[]{value}) : value;
    }
}

