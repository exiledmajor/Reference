/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.BloodPump;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.cyberware.SandevistanState;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.netrunning.Netrunning;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class ActiveImplantTimers {
    private static final Map<Player, Sent> LAST = new WeakHashMap<Player, Sent>();

    public static List<Timer> active(Player player) {
        ArrayList<Timer> timers = new ArrayList<Timer>();
        if (!Installing.canOperate(player)) {
            return List.of();
        }
        if (SandevistanImplant.active(player)) {
            ActiveImplantTimers.add(timers, CyberwareItems.SANDEVISTAN.toStack().getHoverName(), ((SandevistanState)player.getData(PlayerData.SANDEVISTAN_STATE)).activeTicks());
        }
        if (ZetatechBerserk.active(player)) {
            ActiveImplantTimers.add(timers, Implant.ZETATECH_BERSERK.stack().getHoverName(), ZetatechBerserk.state(player).activeTicks());
        }
        if (ImplantEffects.camouflaged(player)) {
            ActiveImplantTimers.add(timers, Implant.OPTICAL_CAMO.stack().getHoverName(), ImplantEffects.state(player).clock("optical_camo"));
        }
        if (Netrunning.usable(player)) {
            ActiveImplantTimers.add(timers, (Component)Component.translatable((String)"ability.chromeware.overclock"), Netrunning.state(player).overclock());
        }
        if (BloodPump.available(player) && !ZetatechBerserk.active(player)) {
            ActiveImplantTimers.add(timers, CyberwareItems.BLOOD_PUMP.toStack().getHoverName(), BloodPump.state(player).healingTicks());
        }
        return List.copyOf(timers);
    }

    private static void add(List<Timer> timers, Component name, int ticks) {
        if (ticks > 0) {
            timers.add(new Timer(name, (int)(((long)ticks + 19L) / 20L)));
        }
    }

    public static Component message(List<Timer> timers) {
        MutableComponent message = Component.empty();
        for (Timer timer : timers) {
            if (!message.getSiblings().isEmpty()) {
                message.append(" | ");
            }
            message.append((Component)Component.translatable((String)"hud.chromeware.active_implant", (Object[])new Object[]{timer.name(), timer.seconds()}));
        }
        return message;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        List<Timer> timers = ActiveImplantTimers.active(player);
        Sent previous = LAST.get(player);
        if (timers.isEmpty()) {
            if (LAST.remove(player) != null) {
                player.displayClientMessage((Component)Component.empty(), true);
            }
            return;
        }
        Component message = ActiveImplantTimers.message(timers);
        long now = player.level().getGameTime();
        if (previous == null || !previous.message().equals((Object)message) || now < previous.tick() || now - previous.tick() >= 20L) {
            player.displayClientMessage(message, true);
            LAST.put(player, new Sent(message, now));
        }
    }

    public record Timer(Component name, int seconds) {
    }

    private record Sent(Component message, long tick) {
    }
}

