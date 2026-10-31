/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.client.cyberware;

import com.chromeware.cyberware.PainducerPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class PainducerClient {
    public static void register() {
        PainducerPackets.clientReceiver = PainducerClient::receive;
    }

    private static void receive(PainducerPackets.Payment payment) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean damaged = PainducerClient.applyHealth((Player)player, payment.health());
        if (damaged) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.15f, 1.0f, false);
        }
    }

    static boolean applyHealth(Player player, float health) {
        boolean damaged = health < player.getHealth();
        player.setHealth(health);
        return damaged;
    }
}

