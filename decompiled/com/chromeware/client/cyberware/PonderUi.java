/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.createmod.catnip.animation.LerpedFloat
 *  net.createmod.catnip.animation.LerpedFloat$Chaser
 *  net.createmod.catnip.data.Couple
 *  net.createmod.catnip.gui.UIRenderHelper
 *  net.createmod.catnip.gui.element.BoxElement
 *  net.createmod.catnip.gui.widget.AbstractSimiWidget
 *  net.createmod.catnip.theme.Color
 *  net.createmod.ponder.foundation.ui.PonderButton
 *  net.createmod.ponder.foundation.ui.PonderUI
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Mth
 */
package com.chromeware.client.cyberware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.foundation.ui.PonderButton;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class PonderUi {
    public static final Couple<Color> IDLE = PonderButton.COLOR_IDLE;
    public static final Couple<Color> HOVER = PonderButton.COLOR_HOVER;
    public static final Couple<Color> CLICK = PonderButton.COLOR_CLICK;
    public static final Couple<Color> DISABLED = PonderButton.COLOR_DISABLED;
    public static final Couple<Color> FRAME = PonderUI.COLOR_IDLE;
    public static final Couple<Color> FRAME_HIGHLIGHT = PonderUI.COLOR_HIGHLIGHT;
    public static final Couple<Color> FAIL = AbstractSimiWidget.COLOR_FAIL;
    public static final Color BACKGROUND = BoxElement.COLOR_BACKGROUND_TRANSPARENT;
    public static final Color FLAT = PonderUI.BACKGROUND_FLAT;
    public static final int TEXT = ((Color)UIRenderHelper.COLOR_TEXT.getFirst()).getRGB();
    public static final int DARKER = ((Color)UIRenderHelper.COLOR_TEXT_DARKER.getFirst()).getRGB();
    public static final int ACCENT = ((Color)UIRenderHelper.COLOR_TEXT_ACCENT.getFirst()).getRGB();
    public static final int FAINT = ((Color)UIRenderHelper.COLOR_TEXT_DARKER.getSecond()).getRGB();
    private final Map<String, LerpedFloat> hover = new HashMap<String, LerpedFloat>();
    private final Map<String, LerpedFloat> click = new HashMap<String, LerpedFloat>();
    private final UiFade fade = new UiFade();
    private long ticks;
    private long pulseStart;
    private float partial;
    public static final float RISE = 4.0f;

    public void tick() {
        ++this.ticks;
        this.fade.tick();
        this.hover.values().forEach(LerpedFloat::tickChaser);
        this.click.values().forEach(LerpedFloat::tickChaser);
    }

    public void frame(float partialTick) {
        this.partial = partialTick;
    }

    public void restart() {
        this.fade.restart();
    }

    public float fade() {
        return this.fade.get(this.partial);
    }

    public void rise(GuiGraphics g) {
        g.pose().pushPose();
        g.pose().translate(0.0f, (1.0f - this.fade()) * 4.0f, 0.0f);
    }

    public float hovered(String key, boolean hovered) {
        LerpedFloat value = this.hover.computeIfAbsent(key, k -> LerpedFloat.linear().startWithValue(hovered ? 1.0 : 0.0).chase(hovered ? 1.0 : 0.0, (double)0.6f, LerpedFloat.Chaser.EXP));
        value.updateChaseTarget(hovered ? 1.0f : 0.0f);
        return value.getValue(this.partial);
    }

    public void press(String key) {
        this.click.put(key, LerpedFloat.linear().startWithValue(1.0).chase(0.0, (double)0.15f, LerpedFloat.Chaser.EXP));
    }

    public float pulse() {
        return 0.5f + 0.5f * Mth.cos((float)(((float)(this.ticks - this.pulseStart) + this.partial) / 10.0f));
    }

    public void brighten() {
        this.pulseStart = this.ticks;
    }

    public Couple<Color> edge(String key, boolean hovered, boolean selected, boolean enabled) {
        return this.edge(key, hovered, selected, enabled, IDLE, HOVER);
    }

    public Couple<Color> edge(String key, boolean hovered, boolean selected, boolean enabled, Couple<Color> idle, Couple<Color> hot) {
        LerpedFloat c2;
        float k;
        float h = this.hovered(key, hovered && enabled);
        if (!enabled) {
            return DISABLED;
        }
        Couple edge = Couple.create((Object)Color.mixColors((Color)((Color)idle.getFirst()), (Color)((Color)hot.getFirst()), (float)h), (Object)Color.mixColors((Color)((Color)idle.getSecond()), (Color)((Color)hot.getSecond()), (float)h));
        if (selected) {
            float s = this.pulse();
            Couple white = Couple.create((Object)new Color(255, 255, 255, Mth.clamp((int)(((Color)edge.getFirst()).getAlpha() + 150), (int)0, (int)255)), (Object)new Color(155, 155, 155, Mth.clamp((int)(((Color)edge.getSecond()).getAlpha() + 150), (int)0, (int)255)));
            edge = edge.mapWithParams((c, w) -> c.mixWith(w, s), white);
        }
        float f = k = (c2 = this.click.get(key)) == null ? 0.0f : c2.getValue(this.partial);
        if (k > 0.01f) {
            edge = edge.mapWithParams((e, w) -> e.mixWith(w, k), CLICK);
        }
        return edge;
    }

    public void box(GuiGraphics g, float x, float y, int w, int h, Couple<Color> edge, Color background, int offset) {
        PonderUi.box(g, x, y, w, h, edge, background, offset, this.fade());
    }

    public static void box(GuiGraphics g, float x, float y, int w, int h, Couple<Color> edge, Color background, int offset, float alpha) {
        ((BoxElement)((BoxElement)((BoxElement)new BoxElement().withBackground(background).gradientBorder(edge).withBorderOffset(offset).at(x, y, 0.0f)).withBounds(w, h)).withAlpha(alpha)).render(g);
    }

    public void panel(GuiGraphics g, float x, float y, int w, int h) {
        this.box(g, x, y, w, h, FRAME, BACKGROUND, 2);
    }

    public void streak(GuiGraphics g, float angle, int x, int y, int breadth, int length) {
        UIRenderHelper.streak((GuiGraphics)g, (float)angle, (int)x, (int)y, (int)breadth, (int)((int)((float)length * this.fade())));
    }

    public int alpha(int argb) {
        return new Color(argb, true).scaleAlphaForText(this.fade()).getRGB();
    }

    public void text(GuiGraphics g, Font font, Component text, int x, int y, int color) {
        g.drawString(font, text, x, y, this.alpha(color), false);
    }

    public void text(GuiGraphics g, Font font, String text, int x, int y, int color) {
        g.drawString(font, text, x, y, this.alpha(color), false);
    }

    public void centered(GuiGraphics g, Font font, Component text, int cx, int y, int color) {
        this.text(g, font, text, cx - font.width((FormattedText)text) / 2, y, color);
    }

    public static String fit(Font font, String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        return font.plainSubstrByWidth(text, width - font.width("...")) + "...";
    }

    public static void tooltip(GuiGraphics g, Font font, List<Component> lines, int mouseX, int mouseY, int screenW, int screenH) {
        if (lines == null || lines.isEmpty()) {
            return;
        }
        ArrayList<FormattedCharSequence> wrapped = new ArrayList<FormattedCharSequence>();
        int max = Math.max(120, screenW / 2 - 20);
        for (Component component : lines) {
            if (component.getString().isEmpty()) {
                wrapped.add(FormattedCharSequence.EMPTY);
                continue;
            }
            wrapped.addAll(font.split((FormattedText)component, max));
        }
        int w = 0;
        for (FormattedCharSequence line : wrapped) {
            w = Math.max(w, font.width(line));
        }
        int n = wrapped.size() * 10 - 2 + (wrapped.size() > 1 ? 2 : 0);
        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + w + 4 > screenW) {
            x = Math.max(4, mouseX - 16 - w);
        }
        y = Mth.clamp((int)y, (int)4, (int)Math.max(4, screenH - n - 4));
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, 400.0f);
        ((BoxElement)((BoxElement)new BoxElement().withBackground(FLAT).gradientBorder(FRAME_HIGHLIGHT).at((float)x, (float)y, 0.0f)).withBounds(w, n)).render(g);
        int ty = y;
        for (int i = 0; i < wrapped.size(); ++i) {
            g.drawString(font, (FormattedCharSequence)wrapped.get(i), x, ty, i == 0 ? -1 : TEXT, false);
            ty += i == 0 ? 12 : 10;
        }
        g.pose().popPose();
    }

    public static final class UiFade {
        static final float SPEED = 0.45f;
        private final LerpedFloat value = LerpedFloat.linear().startWithValue(0.0).chase(1.0, (double)0.45f, LerpedFloat.Chaser.EXP);

        public void tick() {
            this.value.tickChaser();
        }

        public void restart() {
            this.value.startWithValue(0.0).chase(1.0, (double)0.45f, LerpedFloat.Chaser.EXP);
        }

        public float get(float partialTick) {
            return this.value.getValue(partialTick);
        }
    }
}

