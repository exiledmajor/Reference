/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CooldownNotice {
    private static final Map<Player, Map<ResourceLocation, Long>> LAST_NOTICE = new WeakHashMap<Player, Map<ResourceLocation, Long>>();

    public static Component message(Component implant, int ticks) {
        int seconds = Math.max(1, (ticks + 19) / 20);
        return Component.translatable((String)(seconds == 1 ? "message.chromeware.implant_cooldown_second" : "message.chromeware.implant_cooldown_seconds"), (Object[])new Object[]{implant, seconds}).withStyle(ChatFormatting.RED);
    }

    public static void show(Player player, ItemStack implant, int ticks) {
        if (player.level().isClientSide() || ticks <= 0) {
            return;
        }
        Map notices = LAST_NOTICE.computeIfAbsent(player, ignored -> new HashMap());
        ResourceLocation key = BuiltInRegistries.ITEM.getKey((Object)implant.getItem());
        long now = player.level().getGameTime();
        Long previous = (Long)notices.get(key);
        if (previous != null && now >= previous && now - previous < 20L) {
            return;
        }
        notices.put(key, now);
        player.displayClientMessage(CooldownNotice.message(implant.getHoverName(), ticks), false);
    }
}

