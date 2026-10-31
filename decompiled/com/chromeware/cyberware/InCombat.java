/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantData;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.PlayerData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class InCombat {
    public static final int DURATION = 200;

    public static int remaining(Player p) {
        return (Integer)p.getData(PlayerData.COMBAT_TICKS);
    }

    public static boolean active(Player p) {
        return p.isAlive() && !p.isSpectator() && InCombat.remaining(p) > 0;
    }

    public static void hit(Player p) {
        if (p.level().isClientSide() || !p.isAlive() || p.isSpectator()) {
            return;
        }
        if (!InCombat.active(p)) {
            ImplantEffects.enterCombat(p);
        }
        p.setData(PlayerData.COMBAT_TICKS, (Object)200);
        ImplantEffects.updateAttributes(p);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void damaged(LivingDamageEvent.Post event) {
        Player p;
        if (event.getEntity().level().isClientSide() || event.getOriginalDamage() <= 0.0f) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || attacker == event.getEntity()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            p = (Player)livingEntity;
            InCombat.hit(p);
        }
        if (attacker instanceof Player) {
            p = (Player)attacker;
            InCombat.hit(p);
        }
    }

    public static void tick(Player p) {
        int before;
        int boost;
        ImplantData nervous = ImplantEffects.state(p);
        boolean expires = false;
        for (String key : ImplantData.WORLD_CLOCKS) {
            int left = nervous.clock(key);
            if (left <= 0) continue;
            nervous = nervous.clock(key, left - 1);
            expires |= left == 1;
        }
        ImplantEffects.state(p, nervous);
        if (expires) {
            ImplantEffects.updateAttributes(p);
        }
        if ((boost = ((Integer)p.getData(PlayerData.TYROSINE_TICKS)).intValue()) > 0) {
            p.setData(PlayerData.TYROSINE_TICKS, (Object)(Implant.TYROSINE_INJECTOR.active(p) ? boost - 1 : 0));
            if (boost == 1) {
                p.setData(PlayerData.TYROSINE_STACKS, (Object)0);
                ImplantEffects.updateAttributes(p);
            }
        }
        if ((before = InCombat.remaining(p)) <= 0) {
            return;
        }
        p.setData(PlayerData.COMBAT_TICKS, (Object)(!p.isAlive() || p.isSpectator() ? 0 : before - 1));
        if (!InCombat.active(p)) {
            ImplantEffects.updateAttributes(p);
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer p : level.players()) {
                InCombat.tick((Player)p);
            }
        }
    }
}

