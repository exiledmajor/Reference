/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.InCombat;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTunerState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class QuantumTuner {
    public static QuantumTunerState state(Player player) {
        return (QuantumTunerState)player.getData(PlayerData.QUANTUM_TUNER_STATE);
    }

    public static boolean active(Player player) {
        LimbState body = LimbAttachments.get(player);
        return InventoryImplants.hasQuantumTuner(player) && player.isAlive() && !player.isSpectator() && !body.isMissing(BodyPart.HEAD) && !body.isMissing(BodyPart.BODY);
    }

    public static int duration(Player player, int fullDuration) {
        return QuantumTuner.active(player) ? Math.max(1, (int)Math.round((double)fullDuration * (1.0 - ImplantSettings.stat("quantum_tuner", "cooldown_reduction_fraction", 0.15)))) : fullDuration;
    }

    static int refund(Player player, int remaining) {
        if (player.level().isClientSide() || !QuantumTuner.active(player)) {
            return 0;
        }
        int refund = Math.min(Math.max(0, remaining), QuantumTuner.state(player).reserve());
        if (refund > 0) {
            player.setData(PlayerData.QUANTUM_TUNER_STATE, (Object)QuantumTuner.state(player).spend(refund));
        }
        return refund;
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        boolean fighting;
        QuantumTunerState after;
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isAlive()) {
            return;
        }
        QuantumTunerState before = QuantumTuner.state(player);
        if (!before.equals(after = before.tickSharedCombat(fighting = InCombat.active(player)))) {
            player.setData(PlayerData.QUANTUM_TUNER_STATE, (Object)after);
        }
    }
}

