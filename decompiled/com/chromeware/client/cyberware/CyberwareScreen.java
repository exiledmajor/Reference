/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  java.lang.MatchException
 *  net.createmod.catnip.animation.LerpedFloat
 *  net.createmod.catnip.animation.LerpedFloat$Chaser
 *  net.createmod.catnip.data.Couple
 *  net.createmod.catnip.theme.Color
 *  net.createmod.ponder.enums.PonderGuiTextures
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package com.chromeware.client.cyberware;

import com.chromeware.body.CyberLimbs;
import com.chromeware.client.cyberware.BodyPage;
import com.chromeware.client.cyberware.CustomizePage;
import com.chromeware.client.cyberware.CyberwareMenuClient;
import com.chromeware.client.cyberware.CyberwareMenuLayout;
import com.chromeware.client.cyberware.PonderUi;
import com.chromeware.client.cyberware.PreviewDeploy;
import com.chromeware.client.netrunning.DeckPage;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CapacityProgress;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.ImplantCategory;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.QuantumTunerState;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.extendo.ExtendoGrip;
import com.chromeware.humanity.Humanity;
import com.chromeware.humanity.HumanityRules;
import com.chromeware.humanity.HumanityState;
import com.chromeware.humanity.ImmunosuppressantItem;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryPackets;
import com.chromeware.surgery.SurgerySeat;
import com.chromeware.surgery.SurgeryStock;
import com.mojang.blaze3d.vertex.PoseStack;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class CyberwareScreen
extends Screen {
    static final int W = 427;
    static final int H = 240;
    static final int WHITE = -1;
    static final int BRASS = -1982856;
    private static final int SLOT_O = 28;
    private static final int GAP_O = 10;
    private static final List<CyberwareMenuLayout.Group> GROUPS = CyberwareMenuLayout.GROUPS;
    private static final List<String> ORDER = List.of("frontal_cortex", "operating_system", "arms", "face", "skeleton", "hands", "nervous_system", "circulatory_system", "integumentary_system", "legs");
    static final Couple<Color> EMPTY = Couple.create((Object)new Color(0x50909090, true), (Object)new Color(412127376, true)).map(Color::setImmutable);
    static final Color EMPTY_BACKGROUND = new Color(-2013265920, true).setImmutable();
    static final Color VIEW_BACKGROUND = new Color(0x48000000, true).setImmutable();
    private final Screen parent;
    private final PonderUi ui = new PonderUi();
    private final CyberwarePlayerPreview preview = new CyberwarePlayerPreview();
    private Page page = Page.CYBERWARE;
    private String focus;
    private int focusIndex;
    private int stockScroll;
    private int stockTimer;
    private float focusYaw;
    private final CyberwarePlayerPreview focusPreview = new CyberwarePlayerPreview();
    private final LerpedFloat slide = CyberwareScreen.chaser(0.0f);
    private final LerpedFloat zoom = CyberwareScreen.chaser(1.0f);
    private final LerpedFloat lookHeight = CyberwareScreen.chaser(OVERVIEW_HEIGHT);
    private final LerpedFloat lookSide = CyberwareScreen.chaser(0.0f);
    float scale;
    private int ox;
    private int oy;
    static final float ZOOM_MIN = 0.6f;
    static final float ZOOM_MAX = 2.6f;
    private final BodyPage body = new BodyPage(this, this.ui, this.preview);
    private final CustomizePage customize = new CustomizePage(this, this.ui);
    private long armsOpenedAt;
    private static final float GLIDE = 0.4f;
    private static final int TAB_Y = 6;
    private static final int TAB_STEP = 8;
    private static final int VIAL_WIDTH = 12;
    private static final int VIAL_HEIGHT = 252;
    private static final int COL_X = 48;
    private static final int COL_W = 144;
    private static final int HEAD_Y = 32;
    private static final int SOCKET_Y = 70;
    private static final int SOCKET_STEP = 30;
    private static final int INFO_Y = 101;
    private static final int STOCK_Y = 122;
    private static final int STOCK_H = 84;
    private static final int GRID_Y = 138;
    private static final int CELL = 24;
    private static final int COLUMNS = 6;
    private static final int ROWS_SHOWN = 3;
    private static final int VIEW_X0 = 204;
    private static final int VIEW_Y0 = 32;
    private static final int VIEW_X1 = 374;
    private static final int VIEW_Y1 = 196;
    private static final int NAV_Y = 210;
    private static final float OVERVIEW_HEIGHT = (CyberwareScreen.o(322.0) - 114.0f) / CyberwareScreen.o(136.0);

    private static CyberwareMenuLayout.Group group(String id) {
        for (CyberwareMenuLayout.Group g : GROUPS) {
            if (!g.id().equals(id)) continue;
            return g;
        }
        return GROUPS.get(0);
    }

    public CyberwareScreen(Screen parent) {
        super((Component)Component.translatable((String)"screen.chromeware.cyberware"));
        this.parent = parent;
    }

    private static LerpedFloat chaser(float value) {
        return LerpedFloat.linear().startWithValue((double)value).chase((double)value, (double)0.4f, LerpedFloat.Chaser.EXP);
    }

    protected void init() {
        CyberwareMenuLayout layout = CyberwareMenuLayout.fit(this.width, this.height);
        this.scale = CyberwareMenuLayout.canvasScale(this.width, this.height);
        this.ox = (int)layout.offsetX();
        this.oy = (int)layout.offsetY();
        this.customize.init();
    }

    protected void setInitialFocus() {
    }

    public void tick() {
        super.tick();
        this.ui.tick();
        for (LerpedFloat f : List.of(this.slide, this.zoom, this.lookHeight, this.lookSide)) {
            f.tickChaser();
        }
        if (this.onChair() && this.stockTimer++ % 20 == 0) {
            PacketDistributor.sendToServer((CustomPacketPayload)new SurgeryPackets.StockRequest(), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private List<Page> pages() {
        boolean deck = this.minecraft.player != null && Netrunning.installed((Player)this.minecraft.player);
        return Arrays.stream(Page.values()).filter(p -> p != Page.DECK || deck).toList();
    }

    private void show(Page next) {
        if (next != this.page || this.focus != null) {
            this.ui.restart();
        }
        if (next == Page.CUSTOMIZE && next != this.page) {
            this.customize.restart();
        }
        this.page = next;
        this.focus = null;
    }

    private double vx(double x) {
        return (x - (double)this.ox) / (double)this.scale;
    }

    private double vy(double y) {
        return (y - (double)this.oy) / (double)this.scale;
    }

    float sx(double x) {
        return (float)this.ox + (float)x * this.scale;
    }

    float sy(double y) {
        return (float)this.oy + (float)y * this.scale;
    }

    boolean onChair() {
        return this.minecraft.player != null && this.minecraft.player.getVehicle() instanceof SurgerySeat;
    }

    static boolean inside(double mx, double my, int x0, int y0, int x1, int y1) {
        return mx >= (double)x0 && mx <= (double)x1 && my >= (double)y0 && my <= (double)y1;
    }

    private static float o(double v) {
        return (float)(v * 427.0 / 640.0);
    }

    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.minecraft.level != null) {
            this.renderBlurredBackground(partialTick);
        }
        graphics.fill(0, 0, this.width, this.height, this.minecraft.level == null ? -14342366 : 2014253069);
    }

    void button(GuiGraphics g, String key, int x, int y, int w, int h, boolean hovered, boolean selected, boolean enabled, int offset) {
        this.ui.box(g, x, y, w, h, this.ui.edge(key, hovered, selected, enabled), PonderUi.BACKGROUND, offset);
    }

    int[] textButton(GuiGraphics g, String key, Component text, int x, int y, double mx, double my, boolean selected, boolean danger) {
        int w = this.font.width((FormattedText)text) + 8;
        boolean hover = CyberwareScreen.inside(mx, my, x - 2, y - 2, x + w + 2, y + 14);
        if (danger) {
            this.ui.box(g, x, y, w, 12, this.ui.edge(key, hover, false, true, PonderUi.IDLE, PonderUi.FAIL), PonderUi.BACKGROUND, 0);
        } else if (selected) {
            this.ui.box(g, x, y, w, 12, PonderUi.FRAME_HIGHLIGHT, PonderUi.BACKGROUND, 0);
        } else {
            this.button(g, key, x, y, w, 12, hover, false, true, 0);
        }
        this.ui.text(g, this.font, text, x + 4, y + 2, selected || hover ? -1 : PonderUi.DARKER);
        return new int[]{x - 2, y - 2, x + w + 2, y + 14};
    }

    static int textButtonWidth(Font font, Component text) {
        return font.width((FormattedText)text) + 8;
    }

    void title(GuiGraphics g, Component text, int x, int y, boolean rightAligned) {
        int w = this.font.width((FormattedText)text);
        if (rightAligned) {
            this.ui.streak(g, 180.0f, x + 6, y + 4, 14, w + 30);
            this.ui.text(g, this.font, text, x - w, y, PonderUi.TEXT);
        } else {
            this.ui.streak(g, 0.0f, x - 6, y + 4, 14, w + 30);
            this.ui.text(g, this.font, text, x, y, PonderUi.TEXT);
        }
    }

    private void cornerLetter(GuiGraphics g, String letter, int x, int y) {
        g.pose().pushPose();
        g.pose().translate(0.0f, 0.0f, 200.0f);
        g.drawString(this.font, letter, x - this.font.width(letter) / 2, y, this.ui.alpha(PonderUi.DARKER), true);
        g.pose().popPose();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        List<Component> content;
        super.render(graphics, mouseX, mouseY, partialTick);
        this.ui.frame(partialTick);
        double mx = this.vx(mouseX);
        double my = this.vy(mouseY);
        if (!this.pages().contains((Object)this.page)) {
            this.show(Page.CYBERWARE);
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate((float)this.ox, (float)this.oy, 0.0f);
        pose.scale(this.scale, this.scale, 1.0f);
        this.drawTabs(graphics, mx, my);
        this.ui.rise(graphics);
        List<Component> tooltip = this.page == Page.CYBERWARE ? this.drawGauges(graphics, mx, my) : null;
        switch (this.page.ordinal()) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                List<Component> list;
                if (this.focus != null) {
                    list = this.drawFocus(graphics, mx, my);
                    break;
                }
                list = this.drawCyberware(graphics, mx, my);
                break;
            }
            case 1: {
                List<Component> list = this.body.draw(graphics, mx, my);
                break;
            }
            case 2: {
                List<Component> list = this.customize.draw(graphics, mx, my);
                break;
            }
            case 3: {
                List<Component> list = content = DeckPage.draw(graphics, this.font, this.ui, mx, my);
            }
        }
        if (content != null) {
            tooltip = content;
        }
        pose.popPose();
        pose.popPose();
        float rise = (1.0f - this.ui.fade()) * 4.0f * this.scale;
        if (this.page == Page.CYBERWARE) {
            this.renderBody(graphics, partialTick, rise);
        }
        if (this.page == Page.BODY) {
            this.body.renderPreview(graphics, rise);
        }
        if (this.page == Page.CUSTOMIZE) {
            this.customize.renderPreview(graphics, rise);
        }
        PonderUi.tooltip(graphics, this.font, tooltip, mouseX, mouseY, this.width, this.height);
    }

    private void drawTabs(GuiGraphics g, double mx, double my) {
        int x = 213 - this.tabsWidth() / 2;
        for (Page p : this.pages()) {
            Component text = this.tabName(p);
            int w = CyberwareScreen.textButtonWidth(this.font, text);
            boolean on = p == this.page;
            boolean hover = CyberwareScreen.inside(mx, my, x - 2, 4, x + w + 2, 20);
            String key = "tab:" + String.valueOf((Object)p);
            PonderUi.box(g, x, 6.0f, w, 12, on ? PonderUi.FRAME_HIGHLIGHT : this.ui.edge(key, hover, false, true), PonderUi.BACKGROUND, 0, 1.0f);
            g.drawString(this.font, text, x + 4, 8, on || hover ? -1 : PonderUi.DARKER, false);
            x += w + 8;
        }
    }

    private Component tabName(Page p) {
        return Component.translatable((String)("screen.chromeware.page." + p.name().toLowerCase(Locale.ROOT)));
    }

    private int tabsWidth() {
        int w = -8;
        for (Page p : this.pages()) {
            w += CyberwareScreen.textButtonWidth(this.font, this.tabName(p)) + 8;
        }
        return w;
    }

    private Page tabAt(double mx, double my) {
        int x = 213 - this.tabsWidth() / 2;
        for (Page p : this.pages()) {
            int w = CyberwareScreen.textButtonWidth(this.font, this.tabName(p));
            if (CyberwareScreen.inside(mx, my, x - 2, 4, x + w + 2, 20)) {
                return p;
            }
            x += w + 8;
        }
        return null;
    }

    private List<Component> drawGauges(GuiGraphics g, double mx, double my) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return null;
        }
        double px = mx * 640.0 / 427.0;
        double py = my * 640.0 / 427.0;
        CyberwareMenuLayout.Rect amount = CyberwareMenuLayout.AMOUNT;
        CyberwareMenuLayout.Rect toxicity = CyberwareMenuLayout.TOXICITY;
        boolean overChrome = amount.contains(px, py);
        boolean overHumanity = toxicity.contains(px, py);
        g.pose().pushPose();
        g.pose().scale(0.6671875f, 0.6671875f, 1.0f);
        int used = CyberwareCapacity.used((Player)player);
        int limit = Math.max(1, CyberwareCapacity.limit((Player)player));
        boolean over = CyberwareCapacity.enabled((Player)player) && used > limit;
        double chrome = 100.0 * (double)used / (double)limit;
        CyberwareScreen.vial(g, amount, chrome, 0.0, chrome, over ? -3386027 : -1982856, -1982856, overChrome);
        HumanityState.View view = Humanity.view((Player)player);
        double left = HumanityRules.humanity(view.loss());
        boolean episodes = HumanityRules.episodeInterval(view.loss()) > 0;
        CyberwareScreen.vial(g, toxicity, left, view.temporary(), left + (double)view.temporary(), episodes ? -53700 : -3649466, 1623740486, overHumanity);
        g.pose().popPose();
        if (overChrome) {
            return this.capacityTooltip();
        }
        if (overHumanity) {
            return this.humanityTooltip(view);
        }
        return null;
    }

    private static void vial(GuiGraphics g, CyberwareMenuLayout.Rect bounds, double permanent, double temporary, double total, int baseColor, int temporaryColor, boolean hovered) {
        int x = bounds.x();
        g.fill(x - 2, bounds.y() - 2, x + 12 + 2, bounds.bottom() + 2, -1342177280);
        g.fill(x, bounds.y(), x + 12, bounds.bottom(), 1930366735);
        double visibleTotal = Mth.clamp((double)total, (double)0.0, (double)100.0);
        double visiblePermanent = Mth.clamp((double)permanent, (double)0.0, (double)visibleTotal);
        double visibleTemporary = Mth.clamp((double)temporary, (double)0.0, (double)(visibleTotal - visiblePermanent));
        visibleTemporary += Math.max(0.0, visibleTotal - visiblePermanent - visibleTemporary);
        int permanentHeight = (int)Math.round(252.0 * visiblePermanent / 100.0);
        int temporaryHeight = (int)Math.round(252.0 * visibleTemporary / 100.0);
        int permanentTop = bounds.bottom() - permanentHeight;
        if (permanentHeight > 0) {
            g.fill(x, permanentTop, x + 12, bounds.bottom(), baseColor);
        }
        if (temporaryHeight > 0) {
            g.fill(x, permanentTop - temporaryHeight, x + 12, permanentTop, temporaryColor);
        }
        g.renderOutline(x - 2, bounds.y() - 2, 16, 256, hovered ? -1 : -1432705377);
    }

    private List<Component> capacityTooltip() {
        LocalPlayer player = this.minecraft.player;
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)Component.translatable((String)"screen.chromeware.capacity").withStyle(ChatFormatting.GOLD));
        lines.add((Component)Component.translatable((String)(CyberwareCapacity.enabled((Player)player) ? "screen.chromeware.capacity_usage" : "screen.chromeware.capacity_disabled"), (Object[])new Object[]{CyberwareCapacity.used((Player)player), CyberwareCapacity.limit((Player)player)}));
        lines.add((Component)Component.translatable((String)"screen.chromeware.capacity_progress", (Object[])new Object[]{CapacityProgress.bonus((Player)player), CapacityProgress.MAX_BONUS}).withStyle(ChatFormatting.YELLOW));
        for (ItemStack item : CyberwareCapacity.installed((Player)player)) {
            if (item.isEmpty()) continue;
            lines.add((Component)Component.translatable((String)"screen.chromeware.capacity_item", (Object[])new Object[]{item.getHoverName(), CyberwareStats.get(item).capacity()}).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private List<Component> humanityTooltip(HumanityState.View view) {
        int interval;
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)Component.translatable((String)"screen.chromeware.humanity").withStyle(ChatFormatting.RED));
        lines.add((Component)Component.translatable((String)"screen.chromeware.humanity_value", (Object[])new Object[]{CyberwareScreen.tenth(view.humanity())}));
        lines.add((Component)Component.translatable((String)"screen.chromeware.humanity_permanent", (Object[])new Object[]{CyberwareScreen.tenth(view.permanent()), CyberwareScreen.tenth(view.chrome())}).withStyle(ChatFormatting.GRAY));
        double recovery = Humanity.strainDecayPerTick((Player)this.minecraft.player);
        if ((double)view.temporary() >= 0.05) {
            lines.add((Component)Component.translatable((String)"screen.chromeware.humanity_strain", (Object[])new Object[]{CyberwareScreen.tenth(view.temporary()), CyberwareScreen.tenth(recovery * 20.0), ImmunosuppressantItem.clock(HumanityRules.strainTicksRemaining(view.temporary(), recovery))}).withStyle(ChatFormatting.GRAY));
        }
        if ((double)view.suppressed() >= 0.05) {
            lines.add((Component)(view.returning() ? Component.translatable((String)"screen.chromeware.humanity_held_returning", (Object[])new Object[]{CyberwareScreen.tenth(view.suppressed())}) : Component.translatable((String)"screen.chromeware.humanity_held_active", (Object[])new Object[]{CyberwareScreen.tenth(view.suppressed()), ImmunosuppressantItem.clock(view.protectionTicks())})).withStyle(ChatFormatting.AQUA));
        }
        lines.add((Component)((interval = HumanityRules.episodeInterval(view.loss())) > 0 ? Component.translatable((String)"screen.chromeware.humanity_psychosis", (Object[])new Object[]{CyberwareScreen.tenth((double)interval / 20.0)}).withStyle(ChatFormatting.RED) : Component.translatable((String)"screen.chromeware.humanity_safe", (Object[])new Object[]{CyberwareScreen.tenth(HumanityRules.humanity(60.0))}).withStyle(ChatFormatting.DARK_GRAY)));
        return lines;
    }

    private static String tenth(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private List<Socket> sockets() {
        ArrayList<Socket> sockets = new ArrayList<Socket>();
        int size = Math.round(CyberwareScreen.o(28.0));
        for (CyberwareMenuLayout.Group group : GROUPS) {
            for (int k = 0; k < group.count(); ++k) {
                sockets.add(new Socket(group.id(), k, Math.round(CyberwareScreen.o(group.x() + k * 38)), Math.round(CyberwareScreen.o(group.y())), size));
            }
        }
        return sockets;
    }

    private List<Component> drawCyberware(GuiGraphics g, double mx, double my) {
        for (CyberwareMenuLayout.Group group : GROUPS) {
            this.title(g, CyberwareScreen.category(group.id()), Math.round(CyberwareScreen.o(group.labelAnchor())), Math.round(CyberwareScreen.o(group.y())) - 13, group.left());
        }
        List<Component> tooltip = null;
        for (Socket socket : this.sockets()) {
            ItemStack item = this.installed(socket.category, socket.index);
            boolean hovered = socket.contains(mx, my);
            int x = socket.x + (socket.size - 16) / 2;
            int y = socket.y + (socket.size - 16) / 2;
            String key = "socket:" + socket.category + socket.index;
            this.ui.box(g, x, y, 16, 16, this.ui.edge(key, hovered, false, true, item.isEmpty() ? EMPTY : PonderUi.IDLE, PonderUi.HOVER), item.isEmpty() ? EMPTY_BACKGROUND : PonderUi.BACKGROUND, 0);
            if (!item.isEmpty()) {
                g.renderItem(item, x, y);
            }
            if (socket.category.equals("arms")) {
                this.cornerLetter(g, this.side(socket.index), x + 15, y + 9);
            }
            if (!hovered) continue;
            tooltip = this.socketTooltip(socket, item);
        }
        return tooltip;
    }

    private String side(int index) {
        return Component.translatable((String)(index == 0 ? "screen.chromeware.arm_r" : "screen.chromeware.arm_l")).getString();
    }

    private static HumanoidArm arm(int index) {
        return index == 1 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    private List<Component> socketTooltip(Socket socket, ItemStack item) {
        ArrayList<Component> lines = new ArrayList<Component>();
        boolean arms = socket.category.equals("arms");
        if (item.isEmpty()) {
            lines.add(CyberwareScreen.category(socket.category));
            lines.add((Component)(arms ? Component.translatable((String)(socket.index == 0 ? "screen.chromeware.arm_right" : "screen.chromeware.arm_left")).withStyle(ChatFormatting.GRAY) : Component.translatable((String)"screen.chromeware.empty_slot", (Object[])new Object[]{socket.index + 1}).withStyle(ChatFormatting.GRAY)));
            lines.add((Component)Component.translatable((String)"screen.chromeware.open_socket").withStyle(ChatFormatting.YELLOW));
            return lines;
        }
        lines.addAll(Screen.getTooltipFromItem((Minecraft)this.minecraft, (ItemStack)item));
        if (arms) {
            KeyMapping button;
            HumanoidArm side = CyberwareScreen.arm(socket.index);
            lines.add((Component)Component.translatable((String)(side == HumanoidArm.RIGHT ? "screen.chromeware.arm_right" : "screen.chromeware.arm_left")).withStyle(ChatFormatting.GRAY));
            KeyMapping keyMapping = button = this.minecraft.player != null && side == this.minecraft.player.getMainArm() ? this.minecraft.options.keyAttack : this.minecraft.options.keyUse;
            if (!item.is(CyberwareItems.GORILLA_ARMS)) {
                lines.add((Component)Component.translatable((String)"screen.chromeware.arm_controls", (Object[])new Object[]{CyberwareMenuClient.armKey(), button.getTranslatedKeyMessage()}).withStyle(ChatFormatting.GRAY));
            }
        }
        if (item.is(CyberwareItems.QUANTUM_TUNER) && this.minecraft.player != null) {
            QuantumTunerState state = QuantumTuner.state((Player)this.minecraft.player);
            lines.add((Component)Component.translatable((String)"screen.chromeware.tuner_status", (Object[])new Object[]{String.format(Locale.ROOT, "%.1f", (double)state.reserve() / 20.0), (state.recovery() + 19) / 20}));
        }
        lines.add((Component)Component.translatable((String)"screen.chromeware.open_socket").withStyle(ChatFormatting.YELLOW));
        return lines;
    }

    private ItemStack installed(String category, int index) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return ItemStack.EMPTY;
        }
        return switch (category) {
            case "arms" -> ArmImplants.get((Player)player, CyberwareScreen.arm(index)).item();
            case "operating_system" -> {
                if (index == 0) {
                    yield SandevistanImplant.get((Player)player).item();
                }
                yield ItemStack.EMPTY;
            }
            default -> {
                ImplantCategory group = ImplantCategory.byKey(category);
                if (group == null || index >= group.sockets) {
                    yield ItemStack.EMPTY;
                }
                yield InventoryImplants.get((Player)player, group.index).get(index).item();
            }
        };
    }

    private void openFocus(String category, int index) {
        if (this.focus == null) {
            this.slide.startWithValue(0.0);
            this.zoom.startWithValue(1.0);
            this.lookHeight.startWithValue((double)OVERVIEW_HEIGHT);
            this.lookSide.startWithValue(0.0);
        }
        this.ui.restart();
        if (category.equals("arms") && !category.equals(this.focus)) {
            this.armsOpenedAt = System.currentTimeMillis() + 350L;
        }
        this.focus = category;
        this.focusIndex = Math.clamp((long)index, (int)0, (int)(CyberwareScreen.group(category).count() - 1));
        this.stockScroll = 0;
        this.focusPreview.rotate(category.equals("operating_system") ? 180.0f - this.focusYaw : -this.focusYaw);
        this.focusYaw = category.equals("operating_system") ? 180.0f : 0.0f;
        this.aim();
    }

    private void select(int index) {
        this.focusIndex = index;
        this.aim();
    }

    private void aim() {
        View v = this.view();
        this.slide.chase(1.0, (double)0.4f, LerpedFloat.Chaser.EXP);
        this.zoom.chase((double)v.zoom(), (double)0.4f, LerpedFloat.Chaser.EXP);
        this.lookHeight.chase((double)v.height(), (double)0.4f, LerpedFloat.Chaser.EXP);
        this.lookSide.chase((double)v.side(), (double)0.4f, LerpedFloat.Chaser.EXP);
    }

    private void removeSelected() {
        ItemStack item = this.installed(this.focus, this.focusIndex);
        if (item.isEmpty() || !this.onChair()) {
            return;
        }
        if (this.focus.equals("arms")) {
            PacketDistributor.sendToServer((CustomPacketPayload)ImplantPackets.Arm.of(2, CyberwareScreen.arm(this.focusIndex)), (CustomPacketPayload[])new CustomPacketPayload[0]);
        } else if (this.focus.equals("operating_system")) {
            PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.RemoveOperatingSystem(), (CustomPacketPayload[])new CustomPacketPayload[0]);
        } else {
            ImplantCategory group = ImplantCategory.byKey(this.focus);
            if (group != null) {
                PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.RemoveInternal(group.index, this.focusIndex), (CustomPacketPayload[])new CustomPacketPayload[0]);
            }
        }
    }

    private List<ItemStack> stockFor(String category) {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack item : SurgeryStock.clientView) {
            if (Surgery.belongs(item, category) && (!ExtendoGrip.is(item) || CyberwareScreen.arm(this.focusIndex) != HumanoidArm.LEFT)) {
                list.add(item);
                continue;
            }
            if (!category.equals("arms") || !CyberLimbs.fits(item, CyberLimbs.part(CyberwareScreen.arm(this.focusIndex)))) continue;
            list.add(item);
        }
        return list;
    }

    private Component unfit(ItemStack item) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return null;
        }
        if (CyberLimbs.frame(item)) {
            return CyberLimbs.refusal((Player)player, item, CyberLimbs.part(CyberwareScreen.arm(this.focusIndex)));
        }
        ItemStack previous = this.installed(this.focus, this.focusIndex);
        if (Surgery.duplicate((Player)player, item, this.focus.equals("arms") ? CyberwareScreen.arm(this.focusIndex) : null)) {
            return Component.translatable((String)"message.chromeware.already_installed", (Object[])new Object[]{item.getHoverName()});
        }
        if (!CyberwareCapacity.fits((Player)player, item, previous)) {
            return Component.translatable((String)"message.chromeware.capacity_full", (Object[])new Object[]{CyberwareCapacity.used((Player)player) - CyberwareStats.get(previous).capacity() + CyberwareStats.get(item).capacity(), CyberwareCapacity.limit((Player)player)});
        }
        return null;
    }

    private int[] focusSocket(int k) {
        int x = 50 + k * 30;
        return new int[]{x, 70, x + 20, 90};
    }

    private int[] cell(int i) {
        return new int[]{52 + i % 6 * 24, 138 + i / 6 * 24};
    }

    private int[] navButton(int k) {
        int x = 279 + (k - 1) * 30;
        return new int[]{x, 210, x + 20, 230};
    }

    private int[] removeButton() {
        MutableComponent text = Component.translatable((String)"screen.chromeware.remove");
        int x = 192 - CyberwareScreen.textButtonWidth(this.font, (Component)text);
        return new int[]{x, 99};
    }

    private String neighbour(int step) {
        return ORDER.get(Math.floorMod(ORDER.indexOf(this.focus) + step, ORDER.size()));
    }

    private List<Component> drawFocus(GuiGraphics g, double mx, double my) {
        List stock;
        int[] b;
        List<MutableComponent> tooltip = null;
        ItemStack current = this.installed(this.focus, this.focusIndex);
        this.ui.streak(g, 0.0f, 76, 44, 30, this.font.width((FormattedText)CyberwareScreen.category(this.focus)) + 60);
        this.ui.streak(g, 180.0f, 76, 44, 30, 24);
        this.ui.box(g, 50.0f, 32.0f, 24, 24, PonderUi.FRAME, PonderUi.FLAT, 2);
        if (!current.isEmpty()) {
            g.pose().pushPose();
            g.pose().translate(50.0f, 32.0f, 0.0f);
            g.pose().scale(1.5f, 1.5f, 1.0f);
            g.renderItem(current, 0, 0);
            g.pose().popPose();
        }
        this.ui.text(g, this.font, (Component)Component.translatable((String)"screen.chromeware.page.cyberware"), 82, 34, PonderUi.DARKER);
        this.ui.text(g, this.font, CyberwareScreen.category(this.focus), 82, 46, PonderUi.TEXT);
        for (int k = 0; k < CyberwareScreen.group(this.focus).count(); ++k) {
            b = this.focusSocket(k);
            ItemStack item = this.installed(this.focus, k);
            boolean hovered = CyberwareScreen.inside(mx, my, b[0] - 4, b[1] - 4, b[2] + 4, b[3] + 4);
            boolean selected = k == this.focusIndex;
            this.ui.box(g, b[0], b[1], 20, 20, this.ui.edge("focus:" + k, hovered, selected, true, item.isEmpty() ? EMPTY : PonderUi.IDLE, PonderUi.HOVER), item.isEmpty() ? EMPTY_BACKGROUND : PonderUi.BACKGROUND, 2);
            if (!item.isEmpty()) {
                g.renderItem(item, b[0] + 2, b[1] + 2);
            }
            if (this.focus.equals("arms")) {
                this.cornerLetter(g, this.side(k), b[0] + 18, b[1] + 14);
            }
            if (!hovered) continue;
            ArrayList lines = new ArrayList();
            if (item.isEmpty()) {
                lines.add(Component.translatable((String)(this.focus.equals("arms") ? (k == 0 ? "screen.chromeware.arm_right" : "screen.chromeware.arm_left") : "screen.chromeware.empty_slot"), (Object[])new Object[]{k + 1}));
            } else {
                lines.addAll(Screen.getTooltipFromItem((Minecraft)this.minecraft, (ItemStack)item));
            }
            if (!selected) {
                lines.add(Component.translatable((String)"screen.chromeware.select_socket").withStyle(ChatFormatting.GRAY));
            }
            tooltip = lines;
        }
        int nameWidth = 140;
        if (!current.isEmpty() && this.onChair()) {
            b = this.removeButton();
            int[] remove = this.textButton(g, "remove", (Component)Component.translatable((String)"screen.chromeware.remove"), b[0], b[1], mx, my, false, true);
            if (CyberwareScreen.inside(mx, my, remove[0], remove[1], remove[2], remove[3])) {
                tooltip = List.of(Component.translatable((String)"screen.chromeware.remove_socket", (Object[])new Object[]{current.getHoverName()}));
            }
            nameWidth = b[0] - 48 - 6;
        }
        Component name = current.isEmpty() ? (this.focus.equals("arms") ? Component.translatable((String)(this.focusIndex == 0 ? "screen.chromeware.arm_right" : "screen.chromeware.arm_left")) : Component.translatable((String)"screen.chromeware.empty_slot", (Object[])new Object[]{this.focusIndex + 1})) : current.getHoverName();
        this.ui.text(g, this.font, PonderUi.fit(this.font, name.getString(), nameWidth), 50, 101, current.isEmpty() ? PonderUi.DARKER : PonderUi.TEXT);
        this.ui.panel(g, 48.0f, 122.0f, 144, 84);
        this.ui.text(g, this.font, (Component)Component.translatable((String)"screen.chromeware.stock"), 51, 124, PonderUi.DARKER);
        MutableComponent empty = null;
        List<Object> list = stock = this.onChair() ? this.stockFor(this.focus) : List.of();
        if (!this.onChair()) {
            empty = Component.translatable((String)"screen.chromeware.stock_chair");
        } else if (stock.isEmpty()) {
            int others = SurgeryStock.clientView.size();
            MutableComponent mutableComponent = SurgeryStock.clientArms == 0 ? Component.translatable((String)"screen.chromeware.stock_no_arm") : (empty = others > 0 ? Component.translatable((String)"screen.chromeware.stock_elsewhere", (Object[])new Object[]{others}) : Component.translatable((String)"screen.chromeware.stock_none"));
        }
        if (empty != null) {
            int y = 138;
            for (FormattedCharSequence line : this.font.split((FormattedText)empty, 138)) {
                g.drawString(this.font, line, 51, y, this.ui.alpha(PonderUi.FAINT), false);
                y += 10;
            }
        } else {
            int k;
            int rows = (stock.size() + 6 - 1) / 6;
            this.stockScroll = Math.clamp((long)this.stockScroll, (int)0, (int)Math.max(0, rows - 3));
            if (rows > 3) {
                MutableComponent pageText = Component.literal((String)(this.stockScroll + 1 + " / " + (rows - 3 + 1)));
                this.ui.text(g, this.font, (Component)pageText, 189 - this.font.width((FormattedText)pageText), 124, PonderUi.FAINT);
            }
            for (int i = 0; i < 18 && (k = i + this.stockScroll * 6) < stock.size(); ++i) {
                ItemStack item = (ItemStack)stock.get(k);
                int[] c = this.cell(i);
                Component reason = this.unfit(item);
                boolean hovered = CyberwareScreen.inside(mx, my, c[0] - 3, c[1] - 3, c[0] + 19, c[1] + 19);
                this.ui.box(g, c[0], c[1], 16, 16, this.ui.edge("stock:" + k, hovered, false, reason == null), PonderUi.BACKGROUND, 1);
                g.renderItem(item.copyWithCount(1), c[0], c[1]);
                if (reason != null) {
                    g.pose().pushPose();
                    g.pose().translate(0.0f, 0.0f, 190.0f);
                    g.fill(c[0], c[1], c[0] + 16, c[1] + 16, -1879048192);
                    g.pose().popPose();
                }
                g.renderItemDecorations(this.font, item, c[0], c[1]);
                if (!hovered) continue;
                ArrayList<MutableComponent> lines = new ArrayList<MutableComponent>(Screen.getTooltipFromItem((Minecraft)this.minecraft, (ItemStack)item.copyWithCount(1)));
                lines.add(Component.translatable((String)"screen.chromeware.stock_count", (Object[])new Object[]{item.getCount()}).withStyle(ChatFormatting.GRAY));
                if (reason != null) {
                    lines.add(reason.copy().withStyle(ChatFormatting.RED));
                } else if (CyberLimbs.frame(item)) {
                    lines.add(Component.translatable((String)"screen.chromeware.frame_fit").withStyle(ChatFormatting.YELLOW));
                } else {
                    lines.add(Component.translatable((String)(current.isEmpty() ? "screen.chromeware.stock_install" : "screen.chromeware.stock_swap"), (Object[])new Object[]{current.getHoverName()}).withStyle(ChatFormatting.YELLOW));
                }
                tooltip = lines;
            }
        }
        this.ui.box(g, 204.0f, 32.0f, 170, 164, PonderUi.FRAME, VIEW_BACKGROUND, 0);
        PonderGuiTextures[] icons = new PonderGuiTextures[]{PonderGuiTextures.ICON_PONDER_LEFT, PonderGuiTextures.ICON_PONDER_CLOSE, PonderGuiTextures.ICON_PONDER_RIGHT};
        String[] letters = new String[]{"a", null, "d"};
        for (int k = 0; k < 3; ++k) {
            int[] b2 = this.navButton(k);
            boolean hovered = CyberwareScreen.inside(mx, my, b2[0] - 4, b2[1] - 4, b2[2] + 4, b2[3] + 4);
            this.button(g, "nav:" + k, b2[0], b2[1], 20, 20, hovered, false, true, 2);
            icons[k].render(g, b2[0] + 2, b2[1] + 2);
            if (letters[k] != null) {
                this.cornerLetter(g, letters[k], b2[0] + 18, b2[1] + 14);
            }
            if (!hovered) continue;
            tooltip = List.of(k == 1 ? Component.translatable((String)"screen.chromeware.page.cyberware") : CyberwareScreen.category(this.neighbour(k == 0 ? -1 : 1)));
        }
        return tooltip;
    }

    private View view() {
        float arm = this.focusIndex == 0 ? 1.0f : -1.0f;
        return switch (this.focus) {
            case "frontal_cortex" -> new View(3.2f, 1.62f, 0.0f);
            case "face" -> new View(3.2f, 1.52f, 0.0f);
            case "operating_system" -> new View(2.0f, 1.2f, 0.0f);
            case "arms" -> new View(2.1f, 1.08f, 0.31f * arm);
            case "hands" -> new View(2.8f, 0.72f, 0.33f * arm);
            case "nervous_system" -> new View(1.8f, 1.15f, 0.0f);
            case "circulatory_system" -> new View(2.1f, 1.22f, 0.0f);
            case "legs" -> new View(1.7f, 0.45f, 0.0f);
            default -> new View(1.6f, 1.0f, 0.0f);
        };
    }

    private void renderBody(GuiGraphics graphics, float pt, float rise) {
        if (this.focus == null) {
            this.preview.render(graphics, (int)this.sx(CyberwareScreen.o(248.0)), (int)this.sy(CyberwareScreen.o(40.0)), (int)Math.ceil(this.sx(CyberwareScreen.o(392.0))), (int)Math.ceil(this.sy(CyberwareScreen.o(326.0))), this.sx(213.5), this.sy(CyberwareScreen.o(322.0)) + rise, CyberwareScreen.o(136.0) * this.scale);
            return;
        }
        float t = this.slide.getValue(pt);
        float size = CyberwareScreen.o(136.0) * this.scale * this.zoom.getValue(pt);
        float centreX = Mth.lerp((float)t, (float)213.5f, (float)289.0f);
        float lookY = 114.0f;
        float facing = this.focus.equals("operating_system") ? -1.0f : 1.0f;
        Runnable body = () -> this.focusPreview.render(graphics, (int)this.sx(Mth.lerp((float)t, (float)CyberwareScreen.o(248.0), (float)205.0f)), (int)this.sy(Mth.lerp((float)t, (float)CyberwareScreen.o(40.0), (float)33.0f)), (int)Math.ceil(this.sx(Mth.lerp((float)t, (float)CyberwareScreen.o(392.0), (float)373.0f))), (int)Math.ceil(this.sy(Mth.lerp((float)t, (float)CyberwareScreen.o(326.0), (float)195.0f))), this.sx(centreX) + this.lookSide.getValue(pt) * size * facing, this.sy(lookY) + this.lookHeight.getValue(pt) * size + rise, size);
        if (this.focus.equals("arms") && this.minecraft.player != null) {
            PreviewDeploy.during((AbstractClientPlayer)this.minecraft.player, this.armsOpenedAt, body);
        } else {
            body.run();
        }
    }

    private boolean clickFocus(double mx, double my) {
        int k;
        int w;
        int[] b;
        int[] b2;
        int k2;
        for (k2 = 0; k2 < 3; ++k2) {
            b2 = this.navButton(k2);
            if (!CyberwareScreen.inside(mx, my, b2[0] - 4, b2[1] - 4, b2[2] + 4, b2[3] + 4)) continue;
            this.ui.press("nav:" + k2);
            if (k2 == 1) {
                this.show(Page.CYBERWARE);
            } else {
                this.cycle(k2 == 0 ? -1 : 1);
            }
            return true;
        }
        for (k2 = 0; k2 < CyberwareScreen.group(this.focus).count(); ++k2) {
            b2 = this.focusSocket(k2);
            if (!CyberwareScreen.inside(mx, my, b2[0] - 4, b2[1] - 4, b2[2] + 4, b2[3] + 4)) continue;
            this.ui.press("focus:" + k2);
            this.select(k2);
            return true;
        }
        if (!this.installed(this.focus, this.focusIndex).isEmpty() && this.onChair() && CyberwareScreen.inside(mx, my, (b = this.removeButton())[0] - 2, b[1] - 2, b[0] + (w = CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)"screen.chromeware.remove"))) + 2, b[1] + 14)) {
            this.removeSelected();
            return true;
        }
        if (!this.onChair()) {
            return false;
        }
        List<ItemStack> stock = this.stockFor(this.focus);
        for (int i = 0; i < 18 && (k = i + this.stockScroll * 6) < stock.size(); ++i) {
            int[] c = this.cell(i);
            if (!CyberwareScreen.inside(mx, my, c[0] - 3, c[1] - 3, c[0] + 19, c[1] + 19)) continue;
            ItemStack item = stock.get(k);
            if (this.unfit(item) != null) {
                return true;
            }
            this.ui.press("stock:" + k);
            PacketDistributor.sendToServer((CustomPacketPayload)(CyberLimbs.frame(item) ? new SurgeryPackets.Operate(item.copyWithCount(1), "limb", CyberLimbs.part(CyberwareScreen.arm(this.focusIndex)).ordinal()) : new SurgeryPackets.Operate(item.copyWithCount(1), this.focus, this.focusIndex)), (CustomPacketPayload[])new CustomPacketPayload[0]);
            return true;
        }
        return false;
    }

    private void cycle(int step) {
        this.openFocus(this.neighbour(step), 0);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.page == Page.CYBERWARE && this.focus != null) {
            if (keyCode == 256) {
                this.ui.press("nav:1");
                this.show(Page.CYBERWARE);
                return true;
            }
            if (keyCode == 263 || keyCode == 65) {
                this.ui.press("nav:0");
                this.cycle(-1);
                return true;
            }
            if (keyCode == 262 || keyCode == 68) {
                this.ui.press("nav:2");
                this.cycle(1);
                return true;
            }
        }
        if (keyCode == 258) {
            List<Page> list = this.pages();
            this.show(list.get((list.indexOf((Object)this.page) + 1) % list.size()));
            return true;
        }
        if (this.page != Page.CUSTOMIZE && (keyCode == 263 || keyCode == 262 || keyCode == 65 || keyCode == 68)) {
            this.preview.rotate(keyCode == 263 || keyCode == 65 ? -15.0f : 15.0f);
            return true;
        }
        if (CyberwareMenuClient.OPEN.matches(keyCode, scanCode) || this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean mouseClicked(double x, double y, int button) {
        double mx = this.vx(x);
        double my = this.vy(y);
        if (button != 0) {
            return super.mouseClicked(x, y, button);
        }
        Page tab = this.tabAt(mx, my);
        if (tab != null) {
            this.ui.press("tab:" + String.valueOf((Object)tab));
            this.show(tab);
            return true;
        }
        switch (this.page.ordinal()) {
            case 0: {
                if (this.focus != null) {
                    if (this.clickFocus(mx, my)) {
                        return true;
                    }
                    if (!this.focusPreview.begin(CyberwareScreen.inside(mx, my, 204, 32, 374, 196), button, x)) break;
                    return true;
                }
                for (Socket socket : this.sockets()) {
                    if (!socket.contains(mx, my)) continue;
                    this.ui.press("socket:" + socket.category + socket.index);
                    this.openFocus(socket.category, socket.index);
                    return true;
                }
                if (!this.preview.begin(mx >= (double)CyberwareScreen.o(248.0) && mx <= (double)CyberwareScreen.o(392.0) && my >= (double)CyberwareScreen.o(40.0) && my <= (double)CyberwareScreen.o(326.0), button, x)) break;
                return true;
            }
            case 1: {
                if (!this.body.click(mx, my, button, x)) break;
                return true;
            }
            case 2: {
                if (!this.customize.click(x, y, mx, my)) break;
                return true;
            }
            case 3: {
                if (!DeckPage.click(this.ui, mx, my)) break;
                return true;
            }
        }
        return super.mouseClicked(x, y, button);
    }

    public boolean mouseScrolled(double x, double y, double dx, double dy) {
        if (this.page == Page.DECK && DeckPage.scroll(this.vx(x), dy)) {
            return true;
        }
        if (this.page == Page.CYBERWARE && this.focus != null) {
            this.stockScroll -= (int)Math.signum(dy);
            return true;
        }
        double mx = this.vx(x);
        double my = this.vy(y);
        if (this.page == Page.CUSTOMIZE && this.customize.scroll(mx, my, dy)) {
            return true;
        }
        if (this.page == Page.BODY && this.body.scroll(mx, my, dy)) {
            return true;
        }
        return super.mouseScrolled(x, y, dx, dy);
    }

    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (this.page == Page.CUSTOMIZE && button == 0 && this.customize.drag(x, y, this.vx(x))) {
            return true;
        }
        return button == 0 && (this.preview.drag(x) || this.focusPreview.drag(x)) || super.mouseDragged(x, y, button, dx, dy);
    }

    public boolean mouseReleased(double x, double y, int button) {
        if (this.customize.release(this.vx(x))) {
            return true;
        }
        return this.preview.end(button) | this.focusPreview.end(button) || super.mouseReleased(x, y, button);
    }

    private static Component category(String id) {
        return Component.translatable((String)("screen.chromeware.category." + id));
    }

    static final class CyberwarePlayerPreview {
        private float yaw = 180.0f;
        private boolean dragging;
        private double lastMouseX;

        CyberwarePlayerPreview() {
        }

        void rotate(float degrees) {
            this.yaw = Mth.wrapDegrees((float)(this.yaw + degrees));
        }

        float turn() {
            return 180.0f - this.yaw;
        }

        boolean begin(boolean inside, int button, double mouseX) {
            if (!inside || button != 0) {
                return false;
            }
            this.dragging = true;
            this.lastMouseX = mouseX;
            return true;
        }

        boolean drag(double mouseX) {
            if (!this.dragging) {
                return false;
            }
            this.yaw = Mth.wrapDegrees((float)(this.yaw - (float)(mouseX - this.lastMouseX)));
            this.lastMouseX = mouseX;
            return true;
        }

        boolean end(int button) {
            if (button != 0 || !this.dragging) {
                return false;
            }
            this.dragging = false;
            return true;
        }

        void render(GuiGraphics graphics, CyberwareMenuLayout layout) {
            CyberwareMenuLayout.Rect box = CyberwareMenuLayout.MODEL;
            this.render(graphics, (int)layout.screenX(box.x()), (int)layout.screenY(box.y()), (int)Math.ceil(layout.screenX(box.right())), (int)Math.ceil(layout.screenY(box.bottom())), layout.screenX(320.0), layout.screenY(322.0), 136.0f * layout.scale());
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        void render(GuiGraphics graphics, int x0, int y0, int x1, int y1, float centerX, float footY, float size) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            float body = player.yBodyRot;
            float oldBody = player.yBodyRotO;
            float rotation = player.getYRot();
            float oldRotation = player.yRotO;
            float pitch = player.getXRot();
            float oldPitch = player.xRotO;
            float head = player.yHeadRot;
            float oldHead = player.yHeadRotO;
            graphics.enableScissor(x0, y0, x1, y1);
            try {
                player.yBodyRotO = 180.0f;
                player.yBodyRot = 180.0f;
                player.setYRot(180.0f);
                player.yRotO = 180.0f;
                player.setXRot(0.0f);
                player.xRotO = 0.0f;
                player.yHeadRotO = 180.0f;
                player.yHeadRot = 180.0f;
                InventoryScreen.renderEntityInInventory((GuiGraphics)graphics, (float)centerX, (float)footY, (float)(size / player.getScale()), (Vector3f)new Vector3f(), (Quaternionf)new Quaternionf().rotateZ((float)Math.PI).rotateY((180.0f - this.yaw) * (float)Math.PI / 180.0f), (Quaternionf)new Quaternionf(), (LivingEntity)player);
            }
            finally {
                player.yBodyRot = body;
                player.yBodyRotO = oldBody;
                player.setYRot(rotation);
                player.yRotO = oldRotation;
                player.setXRot(pitch);
                player.xRotO = oldPitch;
                player.yHeadRot = head;
                player.yHeadRotO = oldHead;
                graphics.disableScissor();
            }
        }
    }

    private static enum Page {
        CYBERWARE,
        BODY,
        CUSTOMIZE,
        DECK;

    }

    private record Socket(String category, int index, int x, int y, int size) {
        boolean contains(double px, double py) {
            return px >= (double)this.x && px <= (double)(this.x + this.size) && py >= (double)this.y && py <= (double)(this.y + this.size);
        }
    }

    private record View(float zoom, float height, float side) {
    }
}

