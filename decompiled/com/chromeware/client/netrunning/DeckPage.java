/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.createmod.catnip.data.Couple
 *  net.createmod.catnip.theme.Color
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.chromeware.client.netrunning;

import com.chromeware.client.cyberware.PonderUi;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.NetrunningPackets;
import com.chromeware.netrunning.Quickhack;
import com.chromeware.netrunning.QuickhackItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DeckPage {
    private static final int ROW = 23;
    private static final int ROWS = 8;
    private static final int LEFT = 30;
    private static final int RIGHT = 227;
    private static final int COL_W = 170;
    private static final int TOP = 42;
    private static final int WHITE = -1;
    private static final int BRASS = -1982856;
    private static final Couple<Color> EMPTY = Couple.create((Object)new Color(0x50909090, true), (Object)new Color(412127376, true)).map(Color::setImmutable);
    private static final Color EMPTY_BACKGROUND = new Color(-2013265920, true).setImmutable();
    private static int scroll;

    public static List<Component> draw(GuiGraphics g, Font font, PonderUi ui, double mx, double my) {
        int y;
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            return null;
        }
        List<String> programs = Netrunning.state((Player)p).programs();
        List<Component> tooltip = null;
        DeckPage.heading(g, font, ui, (Component)Component.translatable((String)"screen.chromeware.deck.programs", (Object[])new Object[]{programs.size(), 8}), 30);
        for (int i = 0; i < 8; ++i) {
            boolean hovered;
            y = 42 + i * 23;
            Quickhack hack = i < programs.size() ? Quickhack.byId(programs.get(i)) : null;
            boolean bl = hovered = hack != null && DeckPage.in(mx, my, 30, y);
            if (hack == null) {
                ui.box(g, 30.0f, y, 170, 16, EMPTY, EMPTY_BACKGROUND, 0);
                ui.text(g, font, (Component)Component.translatable((String)"screen.chromeware.deck.empty"), 52, y + 4, PonderUi.FAINT);
                continue;
            }
            ui.box(g, 30.0f, y, 170, 16, ui.edge("program:" + i, hovered, false, true), PonderUi.BACKGROUND, 0);
            DeckPage.row(g, font, ui, hack, 30, y, hovered ? -1 : PonderUi.TEXT);
            if (!hovered) continue;
            tooltip = DeckPage.details((Player)p, hack, (Component)Component.translatable((String)"screen.chromeware.deck.remove").withStyle(ChatFormatting.YELLOW));
        }
        List<Shard> shards = DeckPage.shards((Player)p);
        DeckPage.heading(g, font, ui, (Component)Component.translatable((String)"screen.chromeware.deck.shards"), 227);
        scroll = Math.clamp((long)scroll, (int)0, (int)Math.max(0, shards.size() - 8));
        if (shards.isEmpty()) {
            y = 44;
            for (FormattedCharSequence line : font.split((FormattedText)Component.translatable((String)"screen.chromeware.deck.no_shards"), 162)) {
                g.drawString(font, line, 229, y, ui.alpha(PonderUi.FAINT), false);
                y += 10;
            }
        }
        for (int i = 0; i < Math.min(8, shards.size() - scroll); ++i) {
            Shard shard = shards.get(i + scroll);
            int y2 = 42 + i * 23;
            boolean owned = programs.contains(shard.hack.id);
            boolean full = programs.size() >= 8;
            boolean hovered = DeckPage.in(mx, my, 227, y2);
            ui.box(g, 227.0f, y2, 170, 16, ui.edge("shard:" + shard.slot, hovered, false, !owned && !full), PonderUi.BACKGROUND, 0);
            DeckPage.row(g, font, ui, shard.hack, 227, y2, owned || full ? PonderUi.FAINT : (hovered ? -1 : PonderUi.TEXT));
            if (shard.stack.getCount() > 1) {
                g.renderItemDecorations(font, shard.stack, 227, y2);
            }
            if (!hovered) continue;
            tooltip = DeckPage.details((Player)p, shard.hack, (Component)(owned ? Component.translatable((String)"screen.chromeware.deck.owned").withStyle(ChatFormatting.GRAY) : (full ? Component.translatable((String)"screen.chromeware.deck.full").withStyle(ChatFormatting.RED) : Component.translatable((String)"screen.chromeware.deck.install").withStyle(ChatFormatting.YELLOW))));
        }
        if (shards.size() > 8) {
            MutableComponent more = Component.literal((String)(scroll + 1 + "-" + Math.min(shards.size(), scroll + 8) + " / " + shards.size()));
            ui.text(g, font, (Component)more, 397 - font.width((FormattedText)more), 25, PonderUi.FAINT);
        }
        return tooltip;
    }

    public static boolean click(PonderUi ui, double mx, double my) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            return false;
        }
        List<String> programs = Netrunning.state((Player)p).programs();
        for (int i = 0; i < Math.min(8, programs.size()); ++i) {
            if (!DeckPage.in(mx, my, 30, 42 + i * 23)) continue;
            Quickhack hack = Quickhack.byId(programs.get(i));
            if (hack != null) {
                ui.press("program:" + i);
                PacketDistributor.sendToServer((CustomPacketPayload)new NetrunningPackets.Action(1, -1, hack.ordinal()), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return true;
        }
        List<Shard> shards = DeckPage.shards((Player)p);
        scroll = Math.clamp((long)scroll, (int)0, (int)Math.max(0, shards.size() - 8));
        for (int i = 0; i < Math.min(8, shards.size() - scroll); ++i) {
            if (!DeckPage.in(mx, my, 227, 42 + i * 23)) continue;
            Shard shard = shards.get(i + scroll);
            if (!programs.contains(shard.hack.id) && programs.size() < 8) {
                ui.press("shard:" + shard.slot);
                PacketDistributor.sendToServer((CustomPacketPayload)new NetrunningPackets.Action(4, -1, shard.slot), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
            return true;
        }
        return false;
    }

    public static boolean scroll(double mx, double dy) {
        if (mx < 227.0 || mx > 397.0) {
            return false;
        }
        LocalPlayer p = Minecraft.getInstance().player;
        scroll = Math.clamp((long)(scroll - (int)Math.signum(dy)), (int)0, (int)(p == null ? 0 : Math.max(0, DeckPage.shards((Player)p).size() - 8)));
        return true;
    }

    private static List<Shard> shards(Player p) {
        ArrayList<Shard> result = new ArrayList<Shard>();
        Inventory inventory = p.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); ++slot) {
            ItemStack stack = inventory.getItem(slot);
            Item item = stack.getItem();
            if (!(item instanceof QuickhackItem)) continue;
            QuickhackItem item2 = (QuickhackItem)item;
            result.add(new Shard(slot, stack, item2.hack));
        }
        return result;
    }

    private static void row(GuiGraphics g, Font font, PonderUi ui, Quickhack hack, int x, int y, int color) {
        g.renderItem(CyberwareItems.QUICKHACKS.get((Object)hack).toStack(), x, y);
        MutableComponent cost = Component.translatable((String)"screen.chromeware.deck.cost", (Object[])new Object[]{hack.ram});
        ui.text(g, font, PonderUi.fit(font, hack.title().getString(), 144 - font.width((FormattedText)cost) - 8), x + 22, y + 4, color);
        ui.text(g, font, (Component)cost, x + 170 - 4 - font.width((FormattedText)cost), y + 4, color == PonderUi.FAINT ? PonderUi.FAINT : -1982856);
    }

    private static List<Component> details(Player p, Quickhack hack, Component action) {
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)hack.title().copy().withStyle(ChatFormatting.GOLD));
        lines.add((Component)Component.translatable((String)("screen.chromeware.scanner.kind." + hack.kind.name().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.GRAY));
        MutableComponent facts = Component.translatable((String)"screen.chromeware.deck.cost", (Object[])new Object[]{hack.ram}).append("  ").append((Component)Component.translatable((String)"screen.chromeware.scanner.upload", (Object[])new Object[]{DeckPage.seconds(Netrunning.uploadTicks(p, hack))}));
        if (hack.duration > 0) {
            facts.append("  ").append((Component)Component.translatable((String)"screen.chromeware.scanner.duration", (Object[])new Object[]{DeckPage.seconds(hack.duration)}));
        }
        lines.add((Component)facts.withStyle(ChatFormatting.GRAY));
        lines.add((Component)hack.details().copy().withStyle(ChatFormatting.WHITE));
        lines.add(action);
        return lines;
    }

    private static String seconds(int ticks) {
        return String.format(Locale.ROOT, ticks % 20 == 0 ? "%.0fs" : "%.1fs", (double)ticks / 20.0);
    }

    private static boolean in(double mx, double my, int x, int y) {
        return mx >= (double)(x - 2) && mx <= (double)(x + 170 + 2) && my >= (double)(y - 2) && my <= (double)(y + 18);
    }

    private static void heading(GuiGraphics g, Font font, PonderUi ui, Component text, int x) {
        ui.streak(g, 0.0f, x - 4, 29, 12, font.width((FormattedText)text) + 30);
        ui.text(g, font, text, x + 2, 25, PonderUi.TEXT);
    }

    private record Shard(int slot, ItemStack stack, Quickhack hack) {
    }
}

