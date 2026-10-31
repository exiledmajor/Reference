/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.createmod.catnip.data.Couple
 *  net.createmod.catnip.theme.Color
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FastColor$ARGB32
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.client.netrunning;

import com.chromeware.client.cyberware.CyberwareMenuClient;
import com.chromeware.client.cyberware.PonderUi;
import com.chromeware.client.netrunning.NetrunningClient;
import com.chromeware.client.netrunning.Scanner;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.netrunning.DeckState;
import com.chromeware.netrunning.DeviceHacks;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.Quickhack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ScannerHud {
    private static final int GOLD = -271235;
    private static final int WARN = -1996170;
    private static final int BRASS = -1982856;
    private static final int CELL = -3235750;
    private static final int CELL_LIGHT = -537748;
    private static final int CELL_DARK = -7446990;
    private static final int CELL_EMPTY = -14018032;
    private static final int TRACK = -15859712;
    private static final int ROW = 24;
    private static final int EDGE = 18;
    private static final PonderUi UI = new PonderUi();

    public static void tick() {
        UI.tick();
    }

    public static void opened() {
        UI.restart();
    }

    public static void brighten() {
        UI.brighten();
    }

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !Netrunning.installed((Player)mc.player)) {
            return;
        }
        float pt = delta.getGameTimeDeltaPartialTick(false);
        UI.frame(pt);
        RenderSystem.enableBlend();
        if (Scanner.active() && mc.screen == null) {
            ScannerHud.scanner(g, mc, (Player)mc.player, pt);
        }
    }

    private static void scanner(GuiGraphics g, Minecraft mc, Player p, float pt) {
        Font font = mc.font;
        int w = g.guiWidth();
        int h = g.guiHeight();
        int cx = w / 2;
        int cy = h / 2;
        Quickhack hack = Scanner.selectedHack();
        boolean denied = Scanner.denyTicks() > 0;
        UI.rise(g);
        ScannerHud.ram(g, font, p, hack, cx, denied, pt);
        ScannerHud.programs(g, font, p, hack, cy, cx, denied);
        ScannerHud.target(g, font, p, Scanner.target(), Scanner.device(), hack, w, cx, cy, h, pt);
        MutableComponent hint = Component.translatable((String)"screen.chromeware.scanner.hint", (Object[])new Object[]{mc.options.keyAttack.getTranslatedKeyMessage(), CyberwareMenuClient.SANDEVISTAN.getTranslatedKeyMessage()});
        if (Scanner.openTicks() < 100) {
            UI.centered(g, font, (Component)hint, cx, h - 54, PonderUi.DARKER);
        }
        g.pose().popPose();
    }

    private static void ram(GuiGraphics g, Font font, Player p, Quickhack hack, int cx, boolean denied, float pt) {
        DeckState s = Netrunning.state(p);
        int max = Math.max(1, Netrunning.maximum(p));
        double ram = Netrunning.available(p);
        int cell = Math.clamp((long)(180 / max - 1), (int)2, (int)6);
        int barW = max * (cell + 1) - 1;
        MutableComponent title = Component.translatable((String)"screen.chromeware.ram", (Object[])new Object[]{String.format(Locale.ROOT, "%.0f", Math.floor(ram)), max});
        if (s.overclock() > 0) {
            title = title.copy().append("   ").append((Component)Component.translatable((String)"screen.chromeware.overclock_active", (Object[])new Object[]{(s.overclock() + 19) / 20}));
        }
        int panelW = Math.max(barW, font.width((FormattedText)title));
        int top = 10;
        UI.panel(g, cx - panelW / 2, top, panelW, 19);
        UI.centered(g, font, (Component)title, cx, top + 1, denied || s.overclock() > 0 ? -1996170 : PonderUi.TEXT);
        int bx = cx - barW / 2;
        int y = top + 13;
        g.fill(bx - 1, y - 1, bx + barW + 1, y + 7, UI.alpha(-15859712));
        int full = (int)Math.floor(ram);
        int cost = hack == null ? 0 : Scanner.cost(hack);
        float blink = 0.5f + 0.5f * Mth.sin((float)(((float)Minecraft.getInstance().level.getGameTime() + pt) * 0.45f));
        for (int i = 0; i < max; ++i) {
            int x = bx + i * (cell + 1);
            if (i < full) {
                ScannerHud.cellAt(g, x, y, cell, (Scanner.target() != null || Scanner.device() != null) && i >= full - cost ? FastColor.ARGB32.lerp((float)blink, (int)-3235750, (int)-7446990) : -3235750);
                continue;
            }
            if (i == full && ram > (double)full) {
                g.fill(x, y, x + cell, y + 6, UI.alpha(-14018032));
                int part = (int)Math.round((double)cell * (ram - (double)full));
                if (part <= 0) continue;
                ScannerHud.cellAt(g, x, y, part, -7446990);
                continue;
            }
            g.fill(x, y, x + cell, y + 6, UI.alpha(-14018032));
        }
    }

    private static void programs(GuiGraphics g, Font font, Player p, Quickhack selected, int cy, int cx, boolean denied) {
        List<Quickhack> programs = Scanner.programs();
        int maxW = Math.max(90, cx - 90 - 18);
        int nameW = 0;
        int costW = 0;
        for (Quickhack hack : programs) {
            nameW = Math.max(nameW, font.width((FormattedText)hack.title()));
            costW = Math.max(costW, font.width((FormattedText)Component.translatable((String)"screen.chromeware.deck.cost", (Object[])new Object[]{Scanner.cost(hack)})));
        }
        int rowW = Math.min(maxW, Math.max(110, 22 + nameW + 10 + costW + 4));
        int rows = Math.max(1, programs.size());
        int y0 = cy - (rows * 24 - 8) / 2;
        MutableComponent title = Component.translatable((String)"screen.chromeware.scanner.quickhacks");
        UI.streak(g, 0.0f, 14, y0 - 12, 12, font.width((FormattedText)title) + 30);
        UI.text(g, font, (Component)title, 20, y0 - 16, PonderUi.TEXT);
        if (programs.isEmpty()) {
            UI.box(g, 18.0f, y0, rowW, 16, PonderUi.DISABLED, PonderUi.BACKGROUND, 0);
            UI.text(g, font, (Component)Component.translatable((String)"screen.chromeware.scanner.no_programs"), 22, y0 + 4, PonderUi.FAINT);
            return;
        }
        boolean noTarget = Scanner.target() == null && Scanner.device() == null;
        for (int i = 0; i < programs.size(); ++i) {
            boolean affordable;
            Quickhack hack = programs.get(i);
            int y = y0 + i * 24;
            boolean sel = hack == selected;
            boolean bl = affordable = Scanner.affordable(p, hack) && (noTarget || Scanner.applicable(hack));
            Couple<Color> edge = sel && denied ? PonderUi.FAIL : (affordable ? UI.edge("hack:" + hack.id, false, sel, true) : UI.edge("hack:" + hack.id, false, sel, true, PonderUi.DISABLED, PonderUi.DISABLED));
            UI.box(g, 18.0f, y, rowW, 16, edge, PonderUi.BACKGROUND, 0);
            g.renderItem(CyberwareItems.QUICKHACKS.get((Object)hack).toStack(), 18, y);
            MutableComponent cost = Component.translatable((String)"screen.chromeware.deck.cost", (Object[])new Object[]{Scanner.cost(hack)});
            int color = !affordable ? PonderUi.FAINT : (sel ? -1 : PonderUi.TEXT);
            UI.text(g, font, PonderUi.fit(font, hack.title().getString(), rowW - 22 - font.width((FormattedText)cost) - 10), 39, y + 4, color);
            UI.text(g, font, (Component)cost, 18 + rowW - 4 - font.width((FormattedText)cost), y + 4, !affordable ? PonderUi.FAINT : (sel && denied ? -1996170 : -1982856));
        }
    }

    private static void target(GuiGraphics g, Font font, Player p, Entity target, BlockPos device, Quickhack hack, int w, int cx, int cy, int screenH, float pt) {
        boolean deviceStatus;
        int wrap = Math.clamp((long)(cx - 90 - 18), (int)90, (int)160);
        record Line(FormattedCharSequence text, int color) {
        }
        ArrayList<Line> lines = new ArrayList<Line>();
        CompoundTag status = NetrunningClient.status;
        boolean bl = deviceStatus = device != null && status.contains("device") && status.getLong("device") == device.asLong();
        if (device != null) {
            String name = p.level().getBlockState(device).getBlock().getName().getString();
            lines.add(new Line(Component.literal((String)PonderUi.fit(font, name, wrap)).getVisualOrderText(), -1));
            lines.add(new Line(Component.translatable((String)"screen.chromeware.scanner.device").getVisualOrderText(), PonderUi.DARKER));
            lines.add(new Line(ScannerHud.stat("screen.chromeware.scanner.distance", String.format(Locale.ROOT, "%.1f m", Math.sqrt(p.getEyePosition().distanceToSqr(DeviceHacks.center(p.level(), device))))), PonderUi.DARKER));
            if (deviceStatus && status.getBoolean("powered")) {
                lines.add(new Line(Component.translatable((String)"screen.chromeware.scanner.powered").getVisualOrderText(), -271235));
            }
            if (deviceStatus && !status.getBoolean("reachable")) {
                lines.add(new Line(Component.translatable((String)"screen.chromeware.link_lost").getVisualOrderText(), -1996170));
            }
        } else if (target == null) {
            lines.add(new Line(Component.translatable((String)"screen.chromeware.scanner.no_target").getVisualOrderText(), PonderUi.DARKER));
        } else {
            lines.add(new Line(Component.literal((String)PonderUi.fit(font, target.getDisplayName().getString(), wrap)).getVisualOrderText(), -1));
            if (target instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)target;
                lines.add(new Line(ScannerHud.stat("screen.chromeware.scanner.health", String.format(Locale.ROOT, "%.1f / %.0f", Float.valueOf(living.getHealth()), Float.valueOf(living.getMaxHealth()))), PonderUi.DARKER));
            }
            lines.add(new Line(ScannerHud.stat("screen.chromeware.scanner.distance", String.format(Locale.ROOT, "%.1f m", Float.valueOf(p.distanceTo(target)))), PonderUi.DARKER));
            if (!Netrunning.targetAllowed(p, target)) {
                lines.add(new Line(Component.translatable((String)"screen.chromeware.link_lost").getVisualOrderText(), -1996170));
            }
        }
        int gap = lines.size();
        if (hack != null) {
            lines.add(new Line(hack.title().getVisualOrderText(), -271235));
            MutableComponent facts = Component.translatable((String)("screen.chromeware.scanner.kind." + hack.kind.name().toLowerCase(Locale.ROOT))).copy().append("  ").append((Component)Component.translatable((String)"screen.chromeware.scanner.upload", (Object[])new Object[]{ScannerHud.seconds(Netrunning.uploadTicks(p, hack))}));
            if (hack.duration > 0) {
                facts.append("  ").append((Component)Component.translatable((String)"screen.chromeware.scanner.duration", (Object[])new Object[]{ScannerHud.seconds(hack.duration)}));
            }
            for (FormattedCharSequence l : font.split((FormattedText)facts, wrap)) {
                lines.add(new Line(l, PonderUi.DARKER));
            }
            double missing = (double)Scanner.cost(hack) - Netrunning.available(p);
            if (!(target == null && device == null || Scanner.applicable(hack))) {
                for (Line l : font.split((FormattedText)Component.translatable((String)(hack.device() ? "screen.chromeware.scanner.needs_device" : "screen.chromeware.scanner.not_applicable")), wrap)) {
                    lines.add(new Line((FormattedCharSequence)l, -1996170));
                }
            }
            if (missing > 0.0 && Netrunning.state(p).overclock() > 0) {
                for (Line l : font.split((FormattedText)Component.translatable((String)"screen.chromeware.scanner.health_cost", (Object[])new Object[]{(int)Math.ceil(missing)}), wrap)) {
                    lines.add(new Line((FormattedCharSequence)l, -1996170));
                }
            }
            for (Line l : font.split((FormattedText)hack.details(), wrap)) {
                lines.add(new Line((FormattedCharSequence)l, PonderUi.TEXT));
            }
        }
        ListTag queue = target != null && status.getInt("target") == target.getId() || deviceStatus ? status.getList("queue", 10) : null;
        int queued = queue == null ? 0 : Math.min(4, queue.size());
        int width = 0;
        for (Line l : lines) {
            width = Math.max(width, font.width(l.text));
        }
        width = Math.max(width, Math.min(110, wrap));
        int height = lines.size() * 10 - 2 + (hack != null && gap > 0 ? 6 : 0) + (queued > 0 ? 6 + queued * 14 : 0);
        int x = w - 18 - width;
        int y = Math.clamp((long)(cy - height / 2), (int)40, (int)Math.max(40, screenH - 64 - height));
        UI.box(g, x, y, width, height, PonderUi.FRAME, PonderUi.FLAT, 2);
        int ty = y;
        for (int i = 0; i < lines.size(); ++i) {
            if (i == gap && gap > 0 && hack != null) {
                g.fill(x, ty + 1, x + width, ty + 2, UI.alpha(822079197));
                ty += 6;
            }
            g.drawString(font, ((Line)lines.get((int)i)).text, x, ty, UI.alpha(((Line)lines.get((int)i)).color), false);
            ty += 10;
        }
        if (queued > 0) {
            ty += 4;
            float since = NetrunningClient.statusAge() + pt;
            for (int i = 0; i < queued; ++i) {
                CompoundTag row = queue.getCompound(i);
                Quickhack h = Quickhack.byId(row.getString("hack"));
                if (h == null) continue;
                int total = Math.max(1, row.getInt("total"));
                float remaining = Math.max(0.0f, (float)row.getInt("remaining") - (i == 0 ? since : 0.0f));
                g.drawString(font, h.title(), x, ty, UI.alpha(i == 0 ? -1 : PonderUi.DARKER), false);
                int fill = Math.round((float)width * (1.0f - remaining / (float)total));
                g.fill(x, ty + 9, x + width, ty + 11, UI.alpha(-14018032));
                g.fill(x, ty + 9, x + fill, ty + 11, UI.alpha(i == 0 ? -537748 : -7446990));
                ty += 14;
            }
        }
    }

    private static void cellAt(GuiGraphics g, int x, int y, int w, int color) {
        g.fill(x, y, x + w, y + 6, UI.alpha(color));
        g.fill(x, y, x + w, y + 1, UI.alpha(FastColor.ARGB32.lerp((float)0.55f, (int)color, (int)-537748)));
        g.fill(x, y + 5, x + w, y + 6, UI.alpha(FastColor.ARGB32.lerp((float)0.5f, (int)color, (int)-10865640)));
    }

    private static FormattedCharSequence stat(String key, String value) {
        return Component.translatable((String)key).append((Component)Component.literal((String)(" " + value)).withColor(0xFFFFFF)).getVisualOrderText();
    }

    private static String seconds(int ticks) {
        return String.format(Locale.ROOT, ticks % 20 == 0 ? "%.0fs" : "%.1fs", (double)ticks / 20.0);
    }
}

