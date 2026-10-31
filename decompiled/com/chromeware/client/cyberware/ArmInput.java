/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.InputEvent$InteractionKeyMappingTriggered
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.mantis.MantisClient;
import com.chromeware.client.monowire.MonowireClient;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.launcher.ProjectileLauncher;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ArmInput {
    public static final int HOLD_TICKS = 5;
    private static final Hold[] HOLDS = new Hold[]{new Hold(), new Hold()};

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, false, ArmInput::click);
        NeoForge.EVENT_BUS.addListener(ArmInput::tick);
    }

    public static void toggle() {
        PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.Arm(0, 0), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static boolean chargeable(Player player, HumanoidArm side) {
        return GorillaCombat.deployed(player, side) || ProjectileLauncher.deployed(player, side);
    }

    private static boolean charged(Player player, HumanoidArm side) {
        return GorillaCombat.state(player).charging(side) || ProjectileLauncher.chargeShown(player, side);
    }

    private static void click(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }
        if (!event.isAttack() && !event.isUseItem()) {
            return;
        }
        HumanoidArm side = ArmControls.armFor((Player)mc.player, event.isAttack());
        if (!ArmControls.deployedOn((Player)mc.player, side)) {
            return;
        }
        event.setCanceled(true);
        event.setSwingHand(false);
        if (event.isAttack() && MantisClient.pairDeployed((Player)mc.player)) {
            MantisClient.press();
            return;
        }
        if (event.isAttack() && MonowireClient.pairDeployed((Player)mc.player)) {
            MonowireClient.press();
            return;
        }
        if (ArmInput.chargeable((Player)mc.player, side)) {
            Hold hold = HOLDS[side.ordinal()];
            if (!hold.holding) {
                hold.holding = true;
                hold.charging = false;
                hold.held = 0;
                hold.attack = event.isAttack();
            }
            return;
        }
        PacketDistributor.sendToServer((CustomPacketPayload)ImplantPackets.Arm.of(1, side), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) {
            return;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            Hold hold = HOLDS[side.ordinal()];
            if (!hold.holding) continue;
            if (mc.player == null || mc.screen != null || !ArmInput.chargeable((Player)mc.player, side)) {
                if (hold.charging && mc.player != null) {
                    ArmInput.send(6, side);
                }
                hold.charging = false;
                hold.holding = false;
                continue;
            }
            if ((hold.attack ? mc.options.keyAttack : mc.options.keyUse).isDown()) {
                if (++hold.held < 5 || ArmInput.charged((Player)mc.player, side) || hold.charging && hold.held % 2 != 0) continue;
                hold.charging = true;
                ArmInput.send(4, side);
                continue;
            }
            ArmInput.send(hold.charging ? 5 : 1, side);
            hold.charging = false;
            hold.holding = false;
        }
    }

    private static void send(int action, HumanoidArm side) {
        PacketDistributor.sendToServer((CustomPacketPayload)ImplantPackets.Arm.of(action, side), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static final class Hold {
        boolean attack;
        boolean holding;
        boolean charging;
        int held;

        private Hold() {
        }
    }
}

