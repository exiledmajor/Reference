/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Style
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 */
package com.chromeware.humanity;

import com.chromeware.humanity.ImmunosuppressantItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class EmptySyringeItem
extends Item {
    public EmptySyringeItem(Item.Properties properties) {
        super(properties);
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        for (String line : ImmunosuppressantItem.wrap(Component.translatable((String)(this.getDescriptionId() + ".description")).getString())) {
            tooltip.add((Component)Component.literal((String)line).setStyle(Style.EMPTY.withItalic(Boolean.valueOf(false)).withColor(ChatFormatting.GRAY)));
        }
    }
}

