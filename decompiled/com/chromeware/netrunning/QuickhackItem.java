/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 */
package com.chromeware.netrunning;

import com.chromeware.netrunning.Quickhack;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class QuickhackItem
extends Item {
    public final Quickhack hack;

    public QuickhackItem(Quickhack hack) {
        super(new Item.Properties().stacksTo(1));
        this.hack = hack;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> lines, TooltipFlag flag) {
        for (String line : this.hack.details().getString().split("\n")) {
            lines.add((Component)Component.literal((String)line).withStyle(ChatFormatting.GRAY));
        }
        lines.add((Component)Component.translatable((String)"tooltip.chromeware.quickhack_category", (Object[])new Object[]{Component.translatable((String)("screen.chromeware.scanner.kind." + this.hack.kind.name().toLowerCase(Locale.ROOT)))}).withStyle(ChatFormatting.AQUA));
        lines.add((Component)Component.translatable((String)"tooltip.chromeware.quickhack_stats", (Object[])new Object[]{this.hack.ram, (double)this.hack.uploadTicks / 20.0}).withStyle(ChatFormatting.GOLD));
        lines.add((Component)Component.translatable((String)"tooltip.chromeware.quickhack_install").withStyle(ChatFormatting.DARK_GRAY));
    }
}

