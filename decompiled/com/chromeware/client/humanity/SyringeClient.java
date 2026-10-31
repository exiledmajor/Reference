/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.Util
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.Input
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.humanity;

import com.chromeware.client.humanity.SyringeAnimation;
import com.chromeware.client.humanity.SyringeFirstPerson;
import com.chromeware.humanity.HumanityPackets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

public final class SyringeClient {
    private static final Map<Integer, Link> LINKS = new HashMap<Integer, Link>();
    private static int lockedHotbarSlot = -1;
    private static Component notice;
    private static long noticeUntil;

    public static void register() {
        HumanityPackets.syringeReceiver = SyringeClient::receive;
        SyringeAnimation.register();
        SyringeFirstPerson.register();
        NeoForge.EVENT_BUS.addListener(event -> SyringeClient.consumeLockedKeys());
        NeoForge.EVENT_BUS.addListener(event -> {
            SyringeClient.tickLinks();
            SyringeClient.tickItemLock();
        });
        NeoForge.EVENT_BUS.addListener(event -> {
            if (!SyringeClient.interactionLocked()) {
                return;
            }
            Input input = event.getInput();
            input.forwardImpulse = 0.0f;
            input.leftImpulse = 0.0f;
            input.shiftKeyDown = false;
            input.jumping = false;
            input.right = false;
            input.left = false;
            input.down = false;
            input.up = false;
        });
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, event -> {
            if (SyringeClient.itemLocked() && (event.isAttack() || event.isUseItem())) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        });
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, event -> {
            if (SyringeClient.itemLocked()) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener(event -> SyringeClient.renderNotice(event.getGuiGraphics()));
    }

    private static void receive(CustomPacketPayload payload) {
        HumanityPackets.SyringeStart start;
        Entity entity;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        if (payload instanceof HumanityPackets.SyringeStart && (entity = mc.level.getEntity((start = (HumanityPackets.SyringeStart)payload).actor())) instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer)entity;
            if (start.other() && mc.level.getEntity(start.target()) != null) {
                LINKS.put(start.actor(), new Link(start.target(), start.actorX(), start.actorZ(), start.targetX(), start.targetZ(), new int[]{start.accelerated() ? 53 : 105}));
            }
            SyringeAnimation.play(player, start.other(), start.accelerated());
        } else if (payload instanceof HumanityPackets.SyringeStop) {
            HumanityPackets.SyringeStop stop = (HumanityPackets.SyringeStop)payload;
            LINKS.remove(stop.actor());
        } else if (payload instanceof HumanityPackets.Notice) {
            HumanityPackets.Notice n = (HumanityPackets.Notice)payload;
            boolean expired = n.kind() == 1;
            notice = expired ? Component.translatable((String)"hud.chromeware.immunosuppressant.expired") : Component.translatable((String)"hud.chromeware.immunosuppressant.applied", (Object[])new Object[]{String.format(Locale.ROOT, "%.1f", Math.max(0.0, n.amount()))});
            noticeUntil = Util.getMillis() + (expired ? 10000L : 5000L);
        }
    }

    private static void tickLinks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            LINKS.clear();
            return;
        }
        Iterator<Map.Entry<Integer, Link>> iterator = LINKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Link> entry = iterator.next();
            Link link = entry.getValue();
            link.remaining[0] = link.remaining[0] - 1;
            if (link.remaining[0] <= 0) {
                iterator.remove();
                continue;
            }
            int local = mc.player.getId();
            if (local == entry.getKey()) {
                SyringeClient.lock((AbstractClientPlayer)mc.player, mc.level.getEntity(link.target), link.actorX, link.actorZ);
                continue;
            }
            if (local != link.target) continue;
            SyringeClient.lock((AbstractClientPlayer)mc.player, mc.level.getEntity(entry.getKey().intValue()), link.targetX, link.targetZ);
        }
    }

    private static void lock(AbstractClientPlayer local, Entity counterpart, double x, double z) {
        local.setPos(x, local.getY(), z);
        local.setDeltaMovement(0.0, Math.min(0.0, local.getDeltaMovement().y), 0.0);
        if (counterpart == null) {
            return;
        }
        Vec3 delta = counterpart.getBoundingBox().getCenter().subtract(local.getEyePosition());
        float yaw = (float)(Mth.atan2((double)delta.z, (double)delta.x) * 57.2957763671875) - 90.0f;
        float horizontal = Mth.sqrt((float)((float)(delta.x * delta.x + delta.z * delta.z)));
        float pitch = (float)(-(Mth.atan2((double)delta.y, (double)horizontal) * 57.2957763671875));
        local.setYRot(yaw);
        local.yRotO = yaw;
        local.setXRot(pitch);
        local.xRotO = pitch;
        local.setYHeadRot(yaw);
        local.yHeadRotO = yaw;
        local.yBodyRot = yaw;
        local.yBodyRotO = yaw;
    }

    public static boolean interactionLocked() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        int local = mc.player.getId();
        if (LINKS.containsKey(local)) {
            return true;
        }
        for (Link link : LINKS.values()) {
            if (link.target != local) continue;
            return true;
        }
        return false;
    }

    static boolean itemLocked() {
        LocalPlayer p = Minecraft.getInstance().player;
        return p != null && SyringeAnimation.active((AbstractClientPlayer)p);
    }

    private static void tickItemLock() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !SyringeClient.itemLocked()) {
            lockedHotbarSlot = -1;
            return;
        }
        if (lockedHotbarSlot < 0) {
            lockedHotbarSlot = mc.player.getInventory().selected;
        }
        mc.player.getInventory().selected = lockedHotbarSlot;
    }

    private static void consumeLockedKeys() {
        if (!SyringeClient.itemLocked()) {
            return;
        }
        Options options = Minecraft.getInstance().options;
        for (KeyMapping hotbar : options.keyHotbarSlots) {
            while (hotbar.consumeClick()) {
            }
        }
        while (options.keySwapOffhand.consumeClick()) {
        }
    }

    private static void renderNotice(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || notice == null) {
            return;
        }
        if (Util.getMillis() >= noticeUntil) {
            notice = null;
            return;
        }
        float w = graphics.guiWidth();
        float h = graphics.guiHeight();
        float bx = w * 170.0f / 640.0f;
        float by = h * 92.0f / 360.0f;
        float bw = w * 300.0f / 640.0f;
        float bh = h * 24.0f / 360.0f;
        int textWidth = Math.max(1, mc.font.width((FormattedText)notice));
        float f = Math.max(1.0f, bw - 12.0f) / (float)textWidth;
        float f2 = Math.max(1.0f, bh - 8.0f);
        Objects.requireNonNull(mc.font);
        float scale = Math.max(0.25f, Math.min(f, f2 / 9.0f));
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(bx + bw * 0.5f, by + bh * 0.5f, 0.0f);
        pose.scale(scale, scale, 1.0f);
        int x = Math.round((float)(-textWidth) * 0.5f);
        Objects.requireNonNull(mc.font);
        int y = Math.round((float)(-9) * 0.5f);
        for (int[] offset : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            graphics.drawString(mc.font, notice, x + offset[0], y + offset[1], -16777216, false);
        }
        graphics.drawString(mc.font, notice, x, y, -1, false);
        pose.popPose();
    }

    private record Link(int target, double actorX, double actorZ, double targetX, double targetZ, int[] remaining) {
    }
}

