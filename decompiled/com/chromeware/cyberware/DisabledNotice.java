/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.cyberware;

import com.chromeware.netrunning.Netrunning;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class DisabledNotice {
    private static final Map<Player, Long> LAST = new WeakHashMap<Player, Long>();

    public static boolean blocked(Player player) {
        if ((Netrunning.flags((Entity)player) & 4) == 0) {
            return false;
        }
        if (!player.level().isClientSide()) {
            long now = player.level().getGameTime();
            Long previous = LAST.get(player);
            if (previous == null || now < previous || now - previous >= 20L) {
                LAST.put(player, now);
                int ticks = Netrunning.disabledTicks(player);
                MutableComponent message = ticks < 0 ? Component.translatable((String)"message.chromeware.cyberware_disabled_until_death") : Component.translatable((String)"message.chromeware.cyberware_disabled", (Object[])new Object[]{Math.max(1, (int)(((long)ticks + 19L) / 20L))});
                player.displayClientMessage((Component)message.copy().withStyle(ChatFormatting.RED), false);
            }
        }
        return true;
    }
}

