/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.createmod.catnip.animation.LerpedFloat
 *  net.createmod.catnip.animation.LerpedFloat$Chaser
 *  net.createmod.catnip.gui.UIRenderHelper
 *  net.createmod.catnip.theme.Color
 *  net.minecraft.Util
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Mth
 */
package com.chromeware.client.update;

import com.chromeware.client.cyberware.PonderUi;
import com.chromeware.client.update.MarkdownText;
import com.chromeware.client.update.UpdateChecker;
import com.chromeware.update.MissedReleases;
import com.mojang.blaze3d.systems.RenderSystem;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.theme.Color;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public final class UpdateAvailableScreen
extends Screen {
    private static final URI CURSEFORGE = URI.create("https://www.curseforge.com/minecraft/mc-mods/cyberpunk2077-mod");
    private static final URI MODRINTH = URI.create("https://modrinth.com/project/cyberpunk2077mod");
    private static final int ROW = 24;
    private static final int TOGGLE = 58;
    private static final int WHITE = -1;
    private final TitleScreen parent;
    private final MissedReleases.Result update;
    private final PonderUi ui = new PonderUi();
    private final LerpedFloat fade = LerpedFloat.linear().startWithValue(0.0).chase(1.0, 0.45, LerpedFloat.Chaser.EXP);
    private final List<Action> actions = new ArrayList<Action>();
    private final List<ReleaseView> releases = new ArrayList<ReleaseView>();
    private boolean closing;
    private int expanded = -1;
    private double scroll;
    private int contentHeight;
    private int listX;
    private int listY;
    private int listWidth;
    private int listHeight;
    private int buttonY;
    private int buttonWidth;

    public UpdateAvailableScreen(TitleScreen parent, MissedReleases.Result update) {
        super((Component)Component.translatable((String)"screen.chromeware.update_available"));
        this.parent = parent;
        this.update = update;
    }

    protected void init() {
        if (this.closing) {
            this.parent.init(this.minecraft, this.width, this.height);
        }
        this.listWidth = Math.min(360, this.width - 48);
        this.listX = (this.width - this.listWidth) / 2;
        this.listY = 98;
        this.buttonY = this.height - 36;
        this.listHeight = Math.max(24, this.buttonY - 10 - this.listY);
        this.releases.clear();
        for (MissedReleases.Release release : this.update.releases()) {
            String changelog = MarkdownText.withoutTitleHeading(release.changelog());
            MutableComponent details = changelog.isBlank() ? Component.translatable((String)"screen.chromeware.update.changelog_unavailable") : MarkdownText.parse(changelog);
            this.releases.add(new ReleaseView(PonderUi.fit(this.font, release.title(), this.listWidth - 58 - 12), this.font.split((FormattedText)details, this.listWidth - 22)));
        }
        this.reflow();
        ArrayList<String> keys = new ArrayList<String>();
        ArrayList<Runnable> runs = new ArrayList<Runnable>();
        Set<MissedReleases.Source> hosts = ((MissedReleases.Release)this.update.releases().getLast()).sources();
        if (hosts.contains((Object)MissedReleases.Source.CURSEFORGE)) {
            keys.add("button.chromeware.update.curseforge");
            runs.add(() -> Util.getPlatform().openUri(CURSEFORGE));
        }
        if (hosts.contains((Object)MissedReleases.Source.MODRINTH)) {
            keys.add("button.chromeware.update.modrinth");
            runs.add(() -> Util.getPlatform().openUri(MODRINTH));
        }
        keys.add("button.chromeware.update.ignore");
        runs.add(() -> {
            UpdateChecker.ignore(this.update.newestVersion());
            this.onClose();
        });
        int gap = 10;
        int count = keys.size();
        this.buttonWidth = Math.min(110, (this.listWidth - gap * (count - 1)) / count);
        int x = (this.width - this.buttonWidth * count - gap * (count - 1)) / 2;
        this.actions.clear();
        for (int i = 0; i < count; ++i) {
            this.actions.add(new Action((String)keys.get(i), x + i * (this.buttonWidth + gap), (Runnable)runs.get(i)));
        }
    }

    public void tick() {
        this.ui.tick();
        this.fade.tickChaser();
        if (this.closing && this.fade.getValue() < 0.02f) {
            this.minecraft.setScreen((Screen)this.parent);
        }
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean live;
        this.ui.frame(partialTick);
        float a = this.fade.getValue(partialTick);
        boolean bl = live = !this.closing && a > 0.6f;
        if (this.closing) {
            this.parent.render(g, -1, -1, partialTick);
        } else {
            this.renderPanorama(g, partialTick);
        }
        this.blur(g, a, partialTick);
        g.fill(0, 0, this.width, this.height, new Color(-1609560048, true).scaleAlpha(a).getRGB());
        g.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.chromeware.update.heading"), this.width / 2, 14, UpdateAvailableScreen.alpha(-1, a));
        MutableComponent description = Component.translatable((String)"screen.chromeware.update.description");
        g.drawString(this.font, (Component)description, (this.width - this.font.width((FormattedText)description)) / 2, 28, UpdateAvailableScreen.alpha(PonderUi.DARKER, a), false);
        this.drawVersions(g, a);
        g.drawString(this.font, (Component)Component.translatable((String)"screen.chromeware.update.missed", (Object[])new Object[]{this.releases.size()}), this.listX + 10, 82, UpdateAvailableScreen.alpha(PonderUi.TEXT, a), false);
        this.drawReleases(g, mouseX, mouseY, a, live);
        for (Action action : this.actions) {
            boolean hovered = live && mouseX >= action.x() && mouseX < action.x() + this.buttonWidth && mouseY >= this.buttonY && mouseY < this.buttonY + 16;
            PonderUi.box(g, action.x(), this.buttonY, this.buttonWidth, 16, this.ui.edge(action.key(), hovered, false, true), PonderUi.BACKGROUND, 0, a);
            MutableComponent label = Component.translatable((String)action.key());
            g.drawString(this.font, (Component)label, action.x() + (this.buttonWidth - this.font.width((FormattedText)label)) / 2, this.buttonY + 5, UpdateAvailableScreen.alpha(hovered ? -1 : PonderUi.TEXT, a), false);
        }
    }

    private void blur(GuiGraphics g, float amount, float partialTick) {
        PostChain effect = this.minecraft.gameRenderer.blurEffect;
        float radius = (float)this.minecraft.options.getMenuBackgroundBlurriness() * amount;
        if (effect == null || radius <= 0.0f) {
            return;
        }
        g.flush();
        RenderSystem.disableDepthTest();
        effect.setUniform("Radius", radius);
        effect.process(partialTick);
        this.minecraft.getMainRenderTarget().bindWrite(false);
    }

    private void drawVersions(GuiGraphics g, float a) {
        String installed = this.update.installedVersion().getFriendlyString();
        String newest = this.update.newestVersion().getFriendlyString();
        int textWidth = this.font.width(installed) + 18 + this.font.width(newest);
        int boxWidth = Math.max(150, textWidth + 24);
        int x = (this.width - boxWidth) / 2;
        int y = 48;
        int tx = (this.width - textWidth) / 2;
        int arrow = tx + this.font.width(installed) + 4;
        int color = UpdateAvailableScreen.alpha(PonderUi.TEXT, a);
        PonderUi.box(g, x, y, boxWidth, 18, PonderUi.FRAME, PonderUi.BACKGROUND, 0, a);
        g.drawString(this.font, installed, tx, y + 5, UpdateAvailableScreen.alpha(PonderUi.DARKER, a), false);
        g.fill(arrow, y + 8, arrow + 9, y + 9, color);
        g.fill(arrow + 5, y + 5, arrow + 6, y + 12, color);
        g.fill(arrow + 6, y + 6, arrow + 7, y + 11, color);
        g.fill(arrow + 7, y + 7, arrow + 8, y + 10, color);
        g.drawString(this.font, newest, tx + this.font.width(installed) + 18, y + 5, UpdateAvailableScreen.alpha(PonderUi.ACCENT, a), false);
    }

    private void drawReleases(GuiGraphics g, int mouseX, int mouseY, float a, boolean live) {
        g.enableScissor(this.listX - 12, this.listY - 2, this.listX + this.listWidth + 8, this.listY + this.listHeight);
        boolean inside = live && this.contains(mouseX, mouseY);
        int y = this.listY - (int)Math.round(this.scroll);
        for (int i = 0; i < this.releases.size(); ++i) {
            ReleaseView release = this.releases.get(i);
            boolean open = i == this.expanded;
            UIRenderHelper.streak((GuiGraphics)g, (float)0.0f, (int)(this.listX - 10), (int)(y + 10), (int)20, (int)(this.listWidth / 8 * 7), (Color)new Color(-587202560, true).scaleAlpha(a));
            g.drawString(this.font, release.title(), this.listX, y + 6, UpdateAvailableScreen.alpha(open ? -1 : PonderUi.TEXT, a), false);
            int toggleX = this.listX + this.listWidth - 58;
            boolean hovered = inside && mouseX >= toggleX && mouseY >= y + 3 && mouseY < y + 17;
            String key = "release" + i;
            PonderUi.box(g, toggleX, y + 3, 58, 14, this.ui.edge(key, hovered || open, false, true), PonderUi.BACKGROUND, 0, a);
            MutableComponent label = Component.translatable((String)(open ? "button.chromeware.update.hide" : "button.chromeware.update.changelog"));
            g.drawString(this.font, (Component)label, toggleX + (58 - this.font.width((FormattedText)label)) / 2, y + 6, UpdateAvailableScreen.alpha(hovered || open ? -1 : PonderUi.TEXT, a), false);
            y += 24;
            if (!open) continue;
            PonderUi.box(g, this.listX + 8, y, this.listWidth - 8, release.details().size() * 10 + 8, PonderUi.FRAME, PonderUi.BACKGROUND, 0, a);
            int lineY = y + 5;
            for (FormattedCharSequence line : release.details()) {
                g.drawString(this.font, line, this.listX + 14, lineY, UpdateAvailableScreen.alpha(PonderUi.TEXT, a), false);
                lineY += 10;
            }
            y += release.detailsHeight();
        }
        if (this.contentHeight > this.listHeight) {
            int x = this.listX + this.listWidth + 4;
            int thumb = Math.max(8, this.listHeight * this.listHeight / this.contentHeight);
            int top = this.listY + (int)Math.round((double)(this.listHeight - thumb) * this.scroll / (double)this.maxScroll());
            g.fill(x, this.listY, x + 2, this.listY + this.listHeight, new Color(0x40FFFFFF, true).scaleAlpha(a).getRGB());
            g.fill(x, top, x + 2, top + thumb, new Color(-1056968995, true).scaleAlpha(a).getRGB());
        }
        g.disableScissor();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.closing || this.fade.getValue() < 0.6f || button != 0) {
            return false;
        }
        for (Action action : this.actions) {
            if (!(mouseX >= (double)action.x()) || !(mouseX < (double)(action.x() + this.buttonWidth)) || !(mouseY >= (double)this.buttonY) || !(mouseY < (double)(this.buttonY + 16))) continue;
            this.ui.press(action.key());
            this.click();
            action.run().run();
            return true;
        }
        if (!this.contains(mouseX, mouseY)) {
            return false;
        }
        double contentY = mouseY - (double)this.listY + (double)Math.round(this.scroll);
        int y = 0;
        for (int i = 0; i < this.releases.size(); ++i) {
            Style style;
            ReleaseView release = this.releases.get(i);
            if (contentY >= (double)y && contentY < (double)(y + 24)) {
                this.ui.press("release" + i);
                this.expanded = this.expanded == i ? -1 : i;
                this.reflow();
                this.click();
                return true;
            }
            y += 24;
            if (i != this.expanded) continue;
            int line = Mth.floor((double)((contentY - (double)y - 5.0) / 10.0));
            if (line >= 0 && line < release.details().size() && mouseX >= (double)(this.listX + 14) && (style = this.font.getSplitter().componentStyleAtWidth(release.details().get(line), (int)(mouseX - (double)this.listX - 14.0))) != null && style.getClickEvent() != null) {
                return this.handleComponentClicked(style);
            }
            y += release.detailsHeight();
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.contains(mouseX, mouseY)) {
            this.scroll = Mth.clamp((double)(this.scroll - scrollY * 18.0), (double)0.0, (double)this.maxScroll());
        }
        return true;
    }

    public void onClose() {
        if (this.closing) {
            return;
        }
        this.closing = true;
        this.fade.chase(0.0, 0.45, LerpedFloat.Chaser.EXP);
        this.parent.init(this.minecraft, this.width, this.height);
        this.parent.fading = true;
        this.parent.fadeInStart = Util.getMillis() - 1250L;
    }

    private void click() {
        this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((Holder)SoundEvents.UI_BUTTON_CLICK, (float)1.0f));
    }

    private void reflow() {
        this.contentHeight = this.releases.size() * 24 + (this.expanded >= 0 ? this.releases.get(this.expanded).detailsHeight() : 0);
        this.scroll = Mth.clamp((double)this.scroll, (double)0.0, (double)this.maxScroll());
    }

    private boolean contains(double mouseX, double mouseY) {
        return mouseX >= (double)this.listX && mouseX < (double)(this.listX + this.listWidth) && mouseY >= (double)this.listY && mouseY < (double)(this.listY + this.listHeight);
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - this.listHeight);
    }

    private static int alpha(int color, float a) {
        return new Color(color, true).scaleAlphaForText(a).getRGB();
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record ReleaseView(String title, List<FormattedCharSequence> details) {
        int detailsHeight() {
            return this.details.size() * 10 + 16;
        }
    }

    private record Action(String key, int x, Runnable run) {
    }
}

