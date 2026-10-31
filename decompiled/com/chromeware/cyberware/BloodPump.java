/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$Clone
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.BloodPumpState;
import com.chromeware.cyberware.CooldownNotice;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.ZetatechBerserk;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class BloodPump {
    public static BloodPumpState state(Player player) {
        return (BloodPumpState)player.getData(PlayerData.BLOOD_PUMP_STATE);
    }

    public static boolean available(Player player) {
        return InventoryImplants.hasBloodPump(player) && player.isAlive() && !player.isSpectator() && !LimbAttachments.get(player).isMissing(BodyPart.BODY);
    }

    public static boolean activate(Player player) {
        return BloodPump.activate(player, true);
    }

    public static boolean activate(Player player, boolean notifyCooldown) {
        if (player.level().isClientSide() || !BloodPump.available(player) || ZetatechBerserk.active(player)) {
            return false;
        }
        if (BloodPump.state(player).charges() == 0) {
            if (notifyCooldown) {
                CooldownNotice.show(player, CyberwareItems.BLOOD_PUMP.toStack(), BloodPump.state(player).rechargeTicks());
            }
            return false;
        }
        if (player.getHealth() >= player.getMaxHealth()) {
            return false;
        }
        player.setData(PlayerData.BLOOD_PUMP_STATE, (Object)BloodPump.state(player).activate(QuantumTuner.duration(player, ImplantSettings.cooldown("blood_pump", 600))));
        if (BloodPump.state(player).charges() == 0) {
            ImplantCooldowns.fullyUsed(player, ImplantCooldowns.BLOOD_PUMP);
        }
        player.heal(ImplantSettings.fstat("blood_pump", "instant_healing", 6.0) + (Implant.BIOMONITOR.active(player) ? ImplantSettings.fstat("biomonitor", "extra_pump_healing", 2.0) : 0.0f));
        ImplantEffects.healthItem(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.35f, 1.5f);
        return true;
    }

    public static void stopHealing(Player player) {
        if (!player.level().isClientSide()) {
            player.setData(PlayerData.BLOOD_PUMP_STATE, (Object)BloodPump.state(player).stopHealing());
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        BloodPumpState next;
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        BloodPumpState previous = BloodPump.state(player);
        boolean healing = BloodPump.available(player);
        if (healing && previous.healOnNextTick()) {
            player.heal(ImplantSettings.fstat("blood_pump", "healing_per_second", 1.0));
        }
        if (!(next = previous.tick(healing, QuantumTuner.duration(player, ImplantSettings.cooldown("blood_pump", 600)))).equals(previous)) {
            player.setData(PlayerData.BLOOD_PUMP_STATE, (Object)next);
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        BloodPump.stopHealing(event.getEntity());
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        BloodPump.stopHealing(event.getEntity());
    }
}

