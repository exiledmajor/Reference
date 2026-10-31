/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.chromeware.client.mantis;

import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.mantis.MantisRenderer;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.mantis.MantisPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class MantisClient {
    public static final int HOLD_TICKS = 5;
    private static boolean holding;
    private static boolean charging;
    private static int held;

    public static void register(IEventBus bus) {
        bus.addListener(MantisRenderer::reload);
        MantisAnimation.register();
        NeoForge.EVENT_BUS.addListener(MantisClient::tick);
    }

    public static boolean pairDeployed(Player p) {
        return MantisImplant.deployed(p, HumanoidArm.RIGHT) && MantisImplant.deployed(p, HumanoidArm.LEFT);
    }

    public static void press() {
        if (!holding) {
            holding = true;
            charging = false;
            held = 0;
        }
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        for (AbstractClientPlayer player : mc.level.players()) {
            MantisAnimation.update(player);
        }
        if (!holding) {
            return;
        }
        if (mc.player == null || mc.screen != null || !MantisClient.pairDeployed((Player)mc.player)) {
            if (charging && mc.player != null) {
                MantisClient.send(4);
            }
            charging = false;
            holding = false;
        } else if (mc.options.keyAttack.isDown()) {
            if (++held >= 5 && (!charging || !MantisClient.charged((Player)mc.player) && held % 2 == 0)) {
                charging = true;
                MantisClient.send(2);
            }
        } else {
            if (charging && MantisClient.charged((Player)mc.player)) {
                MantisClient.send(3);
            } else {
                if (charging) {
                    MantisClient.send(4);
                }
                PacketDistributor.sendToServer((CustomPacketPayload)ImplantPackets.Arm.of(1, mc.player.getMainArm()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            charging = false;
            holding = false;
        }
    }

    private static boolean charged(Player p) {
        return MantisImplant.state(p, HumanoidArm.RIGHT).action() == 2;
    }

    private static void send(int action) {
        PacketDistributor.sendToServer((CustomPacketPayload)new MantisPackets.Action(action), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}

