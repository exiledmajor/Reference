/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.launcher;

import com.chromeware.client.launcher.LauncherAnimation;
import com.chromeware.client.launcher.LauncherRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class LauncherClient {
    public static void register(IEventBus bus) {
        bus.addListener(LauncherRenderer::reload);
        LauncherAnimation.register();
        NeoForge.EVENT_BUS.addListener(LauncherClient::tick);
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) {
            return;
        }
        for (AbstractClientPlayer player : mc.level.players()) {
            LauncherAnimation.update(player);
        }
    }
}

