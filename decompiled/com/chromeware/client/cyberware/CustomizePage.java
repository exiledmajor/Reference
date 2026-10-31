/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.AllItems
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.chromeware.client.cyberware;

import com.chromeware.body.CyberLimbs;
import com.chromeware.client.cyberware.CyberwareScreen;
import com.chromeware.client.cyberware.ImplantPreview;
import com.chromeware.client.cyberware.ImplantTint;
import com.chromeware.client.cyberware.PonderUi;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanImplant;
import com.simibubi.create.AllItems;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

final class CustomizePage {
    private static final int[] PRESETS = new int[]{14856278, 12877914, 11843248, 3421752, 11022388, 4356276, 5933642, 15262420};
    private final CyberwareScreen screen;
    private final PonderUi ui;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Font font;
    private int selectedImplant;
    private int selectedPart;
    private int dragChannel;
    private boolean custom;
    private boolean rotating;
    private float implantYaw;
    private float implantPitch;
    private long implantShownAt;
    private float implantZoom;
    private float implantZoomShown;
    private double lastDragX;
    private double lastDragY;
    private ImplantColors colors;
    private static final int HW_X = 14;
    private static final int HW_W = 104;
    private static final int LIST_Y = 46;
    private static final int ROW_STEP = 24;
    private static final int PARTS_X = 250;
    private static final int CHIP_W = 80;
    private static final int CHIP_STEP = 88;
    private static final int SLIDER_W = 112;
    private static final String[] RESETS = new String[]{"screen.chromeware.reset_part", "screen.chromeware.reset_parts", "screen.chromeware.reset_all"};

    CustomizePage(CyberwareScreen screen, PonderUi ui) {
        this.font = this.minecraft.font;
        this.dragChannel = -1;
        this.custom = true;
        this.implantYaw = ImplantPreview.defaultYaw(ImplantColors.IMPLANTS.get(0));
        this.implantShownAt = System.currentTimeMillis();
        this.implantZoom = 1.0f;
        this.implantZoomShown = 1.0f;
        this.colors = ImplantColors.EMPTY;
        this.screen = screen;
        this.ui = ui;
    }

    void init() {
        if (this.minecraft.player != null) {
            this.colors = (ImplantColors)this.minecraft.player.getData(PlayerData.IMPLANT_COLORS);
        }
    }

    void restart() {
        this.implantShownAt = System.currentTimeMillis();
    }

    void renderPreview(GuiGraphics graphics, float rise) {
        this.implantZoomShown += (this.implantZoom - this.implantZoomShown) * 0.3f;
        String implant = ImplantColors.IMPLANTS.get(this.selectedImplant);
        graphics.enableScissor((int)this.screen.sx(126.0), (int)this.screen.sy(33.0), (int)this.screen.sx(235.0), (int)this.screen.sy(212.0));
        ImplantPreview.render(graphics, implant, this.colors, this.screen.sx(180.0), this.screen.sy(116.0) + rise, 150.0f * this.implantZoomShown * this.screen.scale, this.implantYaw, this.implantPitch, (float)(System.currentTimeMillis() - this.implantShownAt) / 1000.0f - 0.25f);
        graphics.disableScissor();
    }

    boolean click(double x, double y, double mx, double my) {
        this.lastDragX = x;
        this.lastDragY = y;
        return this.clickControls(mx, my);
    }

    boolean scroll(double mx, double my, double dy) {
        if (!CyberwareScreen.inside(mx, my, 126, 33, 235, 212)) {
            return false;
        }
        this.implantZoom = Math.clamp((float)(this.implantZoom * (float)Math.pow(1.15, dy)), (float)0.6f, (float)2.6f);
        return true;
    }

    boolean drag(double x, double y, double mx) {
        if (this.dragChannel >= 0) {
            this.dragSlider(mx, false);
            return true;
        }
        if (!this.rotating) {
            return false;
        }
        this.implantYaw -= (float)(x - this.lastDragX);
        this.implantPitch = Math.clamp((float)(this.implantPitch - (float)(y - this.lastDragY)), (float)-90.0f, (float)90.0f);
        this.lastDragX = x;
        this.lastDragY = y;
        return true;
    }

    boolean release(double mx) {
        if (this.dragChannel >= 0) {
            this.dragSlider(mx, true);
            this.dragChannel = -1;
            return true;
        }
        this.rotating = false;
        return false;
    }

    private static ItemStack implantItem(String implant) {
        return switch (implant) {
            case "sandevistan" -> CyberwareItems.SANDEVISTAN.toStack();
            case "mantis_blades" -> CyberwareItems.MANTIS_BLADES.toStack();
            case "gorilla_arms" -> CyberwareItems.GORILLA_ARMS.toStack();
            case "projectile_launch_system" -> CyberwareItems.PROJECTILE_LAUNCH_SYSTEM.toStack();
            case "extendo_grip" -> AllItems.EXTENDO_GRIP.asStack();
            case "cyber_limbs" -> CyberwareItems.CYBER_ARM.toStack();
            default -> CyberwareItems.MONOWIRE.toStack();
        };
    }

    private boolean installedImplant(String implant) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return false;
        }
        ItemStack item = CustomizePage.implantItem(implant);
        if (implant.equals("cyber_limbs")) {
            return CyberLimbs.get((Player)player).installed() != 0;
        }
        return implant.equals("sandevistan") ? SandevistanImplant.get((Player)player).item().is(CyberwareItems.SANDEVISTAN) : ArmImplants.has((Player)player, s -> ItemStack.isSameItem((ItemStack)s, (ItemStack)item));
    }

    private List<String> parts() {
        return ImplantTint.parts(ImplantColors.IMPLANTS.get(this.selectedImplant));
    }

    private String part() {
        List<String> parts = this.parts();
        return parts.isEmpty() ? null : parts.get(Math.min(this.selectedPart, parts.size() - 1));
    }

    private int currentColor(String implant, String part) {
        Integer c = this.colors.get(implant, part);
        return c != null ? c : ImplantTint.defaultColor(implant, part);
    }

    List<Component> draw(GuiGraphics g, double mx, double my) {
        String implant = ImplantColors.IMPLANTS.get(this.selectedImplant);
        List<MutableComponent> tooltip = null;
        this.screen.title(g, (Component)Component.translatable((String)"screen.chromeware.hardware"), 16, 30, false);
        for (int i = 0; i < ImplantColors.IMPLANTS.size(); ++i) {
            String id = ImplantColors.IMPLANTS.get(i);
            int y = 46 + i * 24;
            boolean on = i == this.selectedImplant;
            boolean hover = CyberwareScreen.inside(mx, my, 12, y - 2, 120, y + 22);
            this.ui.box(g, 14.0f, y, 104, 20, on ? PonderUi.FRAME_HIGHLIGHT : this.ui.edge("hw:" + i, hover, false, true), PonderUi.BACKGROUND, 0);
            ItemStack item = CustomizePage.implantItem(id);
            g.renderItem(item, 16, y + 2);
            MutableComponent name = id.equals("cyber_limbs") ? Component.translatable((String)"screen.chromeware.cyber_limbs") : item.getHoverName();
            this.ui.text(g, this.font, PonderUi.fit(this.font, name.getString(), 80), 35, y + 2, on || hover ? -1 : PonderUi.TEXT);
            boolean in = this.installedImplant(id);
            this.ui.text(g, this.font, (Component)Component.translatable((String)(in ? "screen.chromeware.installed" : "screen.chromeware.not_installed")), 35, y + 11, in ? -7355782 : PonderUi.FAINT);
            if (!hover || this.font.width((FormattedText)name) <= 80) continue;
            tooltip = List.of(name);
        }
        this.ui.box(g, 126.0f, 33.0f, 109, 179, PonderUi.FRAME, CyberwareScreen.VIEW_BACKGROUND, 0);
        this.ui.centered(g, this.font, (Component)Component.translatable((String)"screen.chromeware.drag_rotate"), 180, 200, PonderUi.FAINT);
        this.screen.title(g, (Component)Component.translatable((String)"screen.chromeware.parts"), 252, 30, false);
        List<String> parts = this.parts();
        for (int i = 0; i < parts.size(); ++i) {
            int x = 250 + i % 2 * 88;
            int y = 46 + i / 2 * 22;
            boolean on = i == this.selectedPart;
            boolean hover = CyberwareScreen.inside(mx, my, x - 2, y - 2, x + 80 + 2, y + 18);
            this.ui.box(g, x, y, 80, 16, on ? PonderUi.FRAME_HIGHLIGHT : this.ui.edge("part:" + i, hover, false, true), PonderUi.BACKGROUND, 0);
            int color = this.currentColor(implant, parts.get(i));
            g.fill(x + 3, y + 3, x + 13, y + 13, this.ui.alpha(0xFF000000 | color));
            g.renderOutline(x + 2, y + 2, 12, 12, this.ui.alpha(1627385565));
            this.ui.text(g, this.font, (Component)Component.translatable((String)("screen.chromeware.part." + implant + "." + parts.get(i))), x + 18, y + 4, on || hover ? -1 : PonderUi.TEXT);
            if (this.colors.get(implant, parts.get(i)) == null) continue;
            g.fill(x + 80 - 4, y + 2, x + 80 - 2, y + 4, this.ui.alpha(-1982856));
        }
        int ey = this.editorY();
        this.ui.panel(g, 250.0f, ey, 165, 92);
        for (int k = 0; k < 2; ++k) {
            int[] b = this.modeButton(k);
            this.screen.textButton(g, "mode:" + k, (Component)Component.translatable((String)(k == 0 ? "screen.chromeware.preset" : "screen.chromeware.custom")), b[0], b[1], mx, my, k == 1 == this.custom, false);
        }
        String part = this.part();
        if (part != null) {
            int color = this.currentColor(implant, part);
            if (this.custom) {
                int[] channel = new int[]{color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF};
                int[] tint = new int[]{-2341316, -10827686, -11505946};
                String[] names = new String[]{"R", "G", "B"};
                for (int k = 0; k < 3; ++k) {
                    int y = ey + 26 + k * 16;
                    int x0 = 264;
                    this.ui.text(g, this.font, names[k], 253, y - 3, tint[k]);
                    g.fill(x0, y, x0 + 112, y + 2, this.ui.alpha(1191177949));
                    int knob = x0 + channel[k] * 112 / 255;
                    g.fill(x0, y, knob, y + 2, this.ui.alpha(tint[k]));
                    boolean hover = this.dragChannel == k || CyberwareScreen.inside(mx, my, x0 - 4, y - 5, x0 + 112 + 4, y + 7);
                    this.ui.box(g, knob - 1, y - 3, 3, 8, this.ui.edge("knob:" + k, hover, false, true), PonderUi.FLAT, 0);
                    this.ui.text(g, this.font, String.valueOf(channel[k]), x0 + 112 + 8, y - 3, PonderUi.TEXT);
                }
            } else {
                for (int k = 0; k < PRESETS.length; ++k) {
                    int x = 253 + k % 4 * 30;
                    int y = ey + 24 + k / 4 * 22;
                    boolean on = PRESETS[k] == color;
                    boolean hover = CyberwareScreen.inside(mx, my, x, y, x + 24, y + 16);
                    g.fill(x, y, x + 24, y + 16, this.ui.alpha(0xFF000000 | PRESETS[k]));
                    g.renderOutline(x - 1, y - 1, 26, 18, this.ui.alpha(on ? -4387 : (hover ? -1593839907 : 1191177949)));
                }
            }
            MutableComponent original = Component.translatable((String)"screen.chromeware.part_original");
            this.ui.text(g, this.font, (Component)original, 253, ey + 78, PonderUi.FAINT);
            int sx0 = 253 + this.font.width((FormattedText)original) + 4;
            g.fill(sx0, ey + 77, sx0 + 10, ey + 87, this.ui.alpha(0xFF000000 | ImplantTint.defaultColor(implant, part)));
        }
        for (int k = 0; k < RESETS.length; ++k) {
            int[] b = this.resetButton(k);
            this.screen.textButton(g, "reset:" + k, (Component)Component.translatable((String)RESETS[k]), b[0], b[1], mx, my, false, false);
        }
        return tooltip;
    }

    private int editorY() {
        return 46 + (this.parts().size() + 1) / 2 * 22 + 6;
    }

    private int[] modeButton(int k) {
        int x = 253;
        if (k == 1) {
            x += CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)"screen.chromeware.preset")) + 6;
        }
        return new int[]{x, this.editorY() + 3};
    }

    private int[] resetButton(int k) {
        if (k == 2) {
            return new int[]{14, 216};
        }
        int x = 250;
        if (k == 1) {
            x += CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)RESETS[0])) + 8;
        }
        return new int[]{x, 216};
    }

    private void setColor(String implant, String part, int rgb, boolean send) {
        String key = ImplantColors.key(implant, part);
        this.colors = this.colors.with(key, rgb);
        if (send) {
            PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.SetColor(key, rgb), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    private boolean clickControls(double mx, double my) {
        int k;
        for (int i = 0; i < ImplantColors.IMPLANTS.size(); ++i) {
            int y = 46 + i * 24;
            if (!CyberwareScreen.inside(mx, my, 12, y - 2, 120, y + 22)) continue;
            this.ui.press("hw:" + i);
            if (i != this.selectedImplant) {
                this.implantShownAt = System.currentTimeMillis();
                this.implantZoom = 1.0f;
            }
            this.selectedImplant = i;
            this.selectedPart = 0;
            this.implantYaw = ImplantPreview.defaultYaw(ImplantColors.IMPLANTS.get(i));
            this.implantPitch = 0.0f;
            return true;
        }
        if (CyberwareScreen.inside(mx, my, 126, 33, 235, 212)) {
            this.rotating = true;
            return true;
        }
        List<String> parts = this.parts();
        for (int i = 0; i < parts.size(); ++i) {
            int x = 250 + i % 2 * 88;
            int y = 46 + i / 2 * 22;
            if (!CyberwareScreen.inside(mx, my, x - 2, y - 2, x + 80 + 2, y + 18)) continue;
            this.ui.press("part:" + i);
            this.selectedPart = i;
            return true;
        }
        for (int k2 = 0; k2 < 2; ++k2) {
            int w;
            int[] b = this.modeButton(k2);
            if (!CyberwareScreen.inside(mx, my, b[0] - 2, b[1] - 2, b[0] + (w = CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)(k2 == 0 ? "screen.chromeware.preset" : "screen.chromeware.custom")))) + 2, b[1] + 14)) continue;
            this.custom = k2 == 1;
            return true;
        }
        int ey = this.editorY();
        String implant = ImplantColors.IMPLANTS.get(this.selectedImplant);
        String part = this.part();
        if (part != null && this.custom) {
            for (k = 0; k < 3; ++k) {
                int x0 = 264;
                int y = ey + 26 + k * 16;
                if (!CyberwareScreen.inside(mx, my, x0 - 4, y - 5, x0 + 112 + 4, y + 7)) continue;
                this.dragChannel = k;
                this.dragSlider(mx, false);
                return true;
            }
        }
        if (part != null && !this.custom) {
            for (k = 0; k < PRESETS.length; ++k) {
                int x = 253 + k % 4 * 30;
                int y = ey + 24 + k / 4 * 22;
                if (!CyberwareScreen.inside(mx, my, x, y, x + 24, y + 16)) continue;
                this.setColor(implant, part, PRESETS[k], true);
                return true;
            }
        }
        for (k = 0; k < RESETS.length; ++k) {
            int w;
            int[] b = this.resetButton(k);
            if (!CyberwareScreen.inside(mx, my, b[0] - 2, b[1] - 2, b[0] + (w = CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)RESETS[k]))) + 2, b[1] + 14)) continue;
            this.ui.press("reset:" + k);
            if (k == 0) {
                if (part != null) {
                    this.setColor(implant, part, -1, true);
                }
            } else {
                for (String each : k == 1 ? List.of(implant) : ImplantColors.IMPLANTS) {
                    for (String p : ImplantColors.PARTS) {
                        if (this.colors.get(each, p) == null) continue;
                        this.setColor(each, p, -1, true);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void dragSlider(double mx, boolean send) {
        String implant = ImplantColors.IMPLANTS.get(this.selectedImplant);
        String part = this.part();
        if (part == null || this.dragChannel < 0) {
            return;
        }
        int value = (int)Math.round(Math.clamp((double)((mx - 264.0) / 112.0), (double)0.0, (double)1.0) * 255.0);
        int color = this.currentColor(implant, part);
        int shift = 16 - this.dragChannel * 8;
        color = color & ~(255 << shift) | value << shift;
        this.setColor(implant, part, color, send);
    }
}

