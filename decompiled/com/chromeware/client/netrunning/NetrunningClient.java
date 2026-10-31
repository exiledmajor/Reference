/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.netrunning;

import com.chromeware.client.cyberware.KiroshiHighlights;
import com.chromeware.client.netrunning.HackFxClient;
import com.chromeware.client.netrunning.PingMarkers;
import com.chromeware.client.netrunning.ScanMode;
import com.chromeware.client.netrunning.Scanner;
import com.chromeware.client.netrunning.ScannerHud;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.NetrunningPackets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class NetrunningClient {
    public static CompoundTag status = new CompoundTag();
    private static final Map<Integer, Long> VEILED = new HashMap<Integer, Long>();
    private static Object lastLevel;
    private static long statusTime;

    public static void register() {
        NetrunningPackets.clientReceiver = data -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            status = data.copy();
            statusTime = mc.level.getGameTime();
        };
        NetrunningPackets.markerReceiver = PingMarkers::receive;
        NetrunningPackets.veilReceiver = veil -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                VEILED.put(veil.entity(), level.getGameTime() + (long)veil.ticks());
            }
        };
        Netrunning.veiled = NetrunningClient::veiled;
        NeoForge.EVENT_BUS.addListener(event -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != lastLevel) {
                VEILED.clear();
                PingMarkers.clear();
                status = new CompoundTag();
                lastLevel = level;
                Scanner.close();
            }
            if (level != null) {
                VEILED.values().removeIf(expiry -> expiry <= level.getGameTime());
            }
            ScannerHud.tick();
        });
        NeoForge.EVENT_BUS.addListener(event -> {
            PingMarkers.render(event.getGuiGraphics());
            ScannerHud.render(event.getGuiGraphics(), event.getPartialTick());
        });
        NeoForge.EVENT_BUS.addListener(NetrunningClient::outlines);
        NeoForge.EVENT_BUS.addListener(PingMarkers::project);
        Scanner.register();
        HackFxClient.register();
        ScanMode.register();
    }

    public static boolean veiled(Entity entity) {
        return !VEILED.isEmpty() && VEILED.containsKey(entity.getId());
    }

    public static float statusAge() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? 0.0f : (float)Math.max(0L, level.getGameTime() - statusTime);
    }

    public static boolean highlighted(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && Netrunning.usable((Player)mc.player) && Scanner.target() == entity;
    }

    private static void outlines(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !ScanMode.eyesOn()) {
            return;
        }
        BlockPos aimed = Scanner.device();
        if (aimed == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        KiroshiHighlights.box(poses, buffer.getBuffer(RenderType.lines()), (Level)mc.level, aimed, 0.006, 1.0f, 1.0f, 1.0f, 1.0f, event.getPartialTick().getGameTimeDeltaPartialTick(false));
        buffer.endBatch(RenderType.lines());
        poses.popPose();
    }
}

