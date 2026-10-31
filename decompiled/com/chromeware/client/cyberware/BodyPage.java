/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.createmod.catnip.data.Couple
 *  net.createmod.catnip.theme.Color
 *  net.createmod.ponder.enums.PonderGuiTextures
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.chromeware.client.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.client.cyberware.CyberwareScreen;
import com.chromeware.client.cyberware.PonderUi;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.surgery.SurgeryPackets;
import com.chromeware.surgery.SurgeryStock;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.theme.Color;
import net.createmod.ponder.enums.PonderGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

final class BodyPage {
    private static final String[] FRAMES = new String[]{"flesh", "synthskin", "chrome"};
    private final CyberwareScreen screen;
    private final PonderUi ui;
    private final CyberwareScreen.CyberwarePlayerPreview preview;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Font font;
    private float bodyZoom;
    private float bodyZoomShown;
    private static final int CARD_H = 46;
    private static final int CARD_GAP = 8;
    private static final int BODY_GAP = 62;
    private static final int BODY_MID = 134;
    private static final int BODY_X0 = 168;
    private static final int BODY_X1 = 259;
    private static final int BODY_FOOT = 214;
    private static final float BODY_SIZE = 80.0f;
    private static final List<Limb> LIMBS = List.of(new Limb("right_arm", BodyPart.RIGHT_ARM, HumanoidArm.RIGHT, true, 0, -0.35f, 1.05f), new Limb("torso", BodyPart.BODY, null, true, 1, 0.0f, 1.1f), new Limb("right_leg", BodyPart.RIGHT_LEG, null, true, 2, -0.12f, 0.35f), new Limb("left_arm", BodyPart.LEFT_ARM, HumanoidArm.LEFT, false, 0, 0.35f, 1.05f), new Limb("left_leg", BodyPart.LEFT_LEG, null, false, 1, 0.12f, 0.35f));

    BodyPage(CyberwareScreen screen, PonderUi ui, CyberwareScreen.CyberwarePlayerPreview preview) {
        this.font = this.minecraft.font;
        this.bodyZoom = 1.0f;
        this.bodyZoomShown = 1.0f;
        this.screen = screen;
        this.ui = ui;
        this.preview = preview;
    }

    void renderPreview(GuiGraphics graphics, float rise) {
        this.bodyZoomShown += (this.bodyZoom - this.bodyZoomShown) * 0.3f;
        this.preview.render(graphics, (int)this.screen.sx(168.0), (int)this.screen.sy(26.0), (int)Math.ceil(this.screen.sx(259.0)), (int)Math.ceil(this.screen.sy(222.0)), this.screen.sx(213.5), this.screen.sy(this.bodyY(214.0f)) + rise, 80.0f * this.bodyZoomShown * this.screen.scale);
    }

    boolean click(double mx, double my, int button, double x) {
        return this.clickCards(mx, my) || this.preview.begin(CyberwareScreen.inside(mx, my, 168, 26, 259, 222), button, x);
    }

    boolean scroll(double mx, double my, double dy) {
        if (!CyberwareScreen.inside(mx, my, 168, 26, 259, 222)) {
            return false;
        }
        this.bodyZoom = Math.clamp((float)(this.bodyZoom * (float)Math.pow(1.15, dy)), (float)0.6f, (float)2.6f);
        return true;
    }

    private float bodyY(float y) {
        return 134.0f + (y - 134.0f) * this.bodyZoomShown;
    }

    private int cardW() {
        return 5 + CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)"screen.chromeware.frame.synthskin")) + 4 + CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)"screen.chromeware.frame.chrome")) + 5;
    }

    private int cardX(Limb limb) {
        return limb.left ? 151 - this.cardW() : 275;
    }

    private int cardY(Limb limb) {
        int count = limb.left ? 3 : 2;
        return 134 - (count * 46 + (count - 1) * 8) / 2 + limb.row * 54;
    }

    private Component limbState(Limb limb) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return Component.translatable((String)"screen.chromeware.frame.flesh");
        }
        if (CyberLimbs.has((Player)player, limb.part)) {
            return CyberLimbs.item(limb.part).getHoverName();
        }
        if (LimbAttachments.get((Player)player).isMissing(limb.part)) {
            return Component.translatable((String)"screen.chromeware.limb_missing");
        }
        return Component.translatable((String)"screen.chromeware.frame.flesh");
    }

    private ItemStack limbItem(Limb limb) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return ItemStack.EMPTY;
        }
        if (CyberLimbs.has((Player)player, limb.part)) {
            return CyberLimbs.item(limb.part);
        }
        return ItemStack.EMPTY;
    }

    private int[] finishButton(Limb limb, int k) {
        int x = this.cardX(limb) + 5;
        if (k == 2) {
            x += CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)"screen.chromeware.frame.synthskin")) + 4;
        }
        return new int[]{x, this.cardY(limb) + 29};
    }

    private int[] limbRemove(Limb limb) {
        return new int[]{this.cardX(limb) + this.cardW() - 21, this.cardY(limb) + 5};
    }

    private int[] frameTile(Limb limb, int k) {
        return new int[]{this.cardX(limb) + 6 + k * 22, this.cardY(limb) + 27};
    }

    List<Component> draw(GuiGraphics g, double mx, double my) {
        List<Object> tooltip = null;
        LocalPlayer player = this.minecraft.player;
        int cardW = this.cardW();
        float turn = (float)Math.cos(Math.toRadians(this.preview.turn()));
        for (Limb limb : LIMBS) {
            int k;
            int x = this.cardX(limb);
            int y = this.cardY(limb);
            boolean framed = player != null && CyberLimbs.has((Player)player, limb.part);
            boolean on = CyberwareScreen.inside(mx, my, x - 2, y - 2, x + cardW + 2, y + 46 + 2);
            float h = this.ui.hovered("card:" + limb.id, on);
            Couple edge = Couple.create((Object)Color.mixColors((Color)((Color)PonderUi.FRAME.getFirst()), (Color)((Color)PonderUi.FRAME_HIGHLIGHT.getFirst()), (float)h), (Object)Color.mixColors((Color)((Color)PonderUi.FRAME.getSecond()), (Color)((Color)PonderUi.FRAME_HIGHLIGHT.getSecond()), (float)h));
            int px = Math.round(213.5f + limb.side * 80.0f * this.bodyZoomShown * turn);
            int py = Math.clamp((long)Math.round(this.bodyY(214.0f - limb.height * 80.0f)), (int)28, (int)220);
            int ex = limb.left ? x + cardW + 2 : x - 2;
            int ey = y + 23;
            int line = this.ui.alpha(((Color)edge.getFirst()).copy().setAlpha(Math.max(80, ((Color)edge.getFirst()).getAlpha())).getRGB());
            g.fill(Math.min(ex, px), ey, Math.max(ex, px) + 1, ey + 1, line);
            g.fill(px, Math.min(ey, py), px + 1, Math.max(ey, py) + 1, line);
            this.ui.box(g, x, y, cardW, 46, (Couple<Color>)edge, PonderUi.BACKGROUND, 0);
            ItemStack item = this.limbItem(limb);
            this.ui.box(g, x + 5, y + 5, 16, 16, item.isEmpty() ? CyberwareScreen.EMPTY : PonderUi.IDLE, item.isEmpty() ? CyberwareScreen.EMPTY_BACKGROUND : PonderUi.BACKGROUND, 0);
            if (!item.isEmpty()) {
                g.renderItem(item, x + 5, y + 5);
            }
            int stateWidth = framed && this.screen.onChair() ? this.limbRemove(limb)[0] - x - 30 : cardW - 30;
            this.ui.text(g, this.font, PonderUi.fit(this.font, Component.translatable((String)("screen.chromeware.limb." + limb.id)).getString(), stateWidth), x + 26, y + 4, PonderUi.TEXT);
            String state = this.limbState(limb).getString();
            this.ui.text(g, this.font, PonderUi.fit(this.font, state, stateWidth), x + 26, y + 14, PonderUi.DARKER);
            if (CyberwareScreen.inside(mx, my, x + 3, y + 3, x + 23, y + 23) && !item.isEmpty()) {
                tooltip = new ArrayList(Screen.getTooltipFromItem((Minecraft)this.minecraft, (ItemStack)item));
            } else if (CyberwareScreen.inside(mx, my, x + 24, y + 12, x + 26 + stateWidth, y + 24) && this.font.width(state) > stateWidth) {
                tooltip = List.of(Component.literal((String)state));
            }
            if (framed) {
                boolean skinned = CyberLimbs.get((Player)player).skinned(limb.part);
                for (k = 1; k < FRAMES.length; ++k) {
                    int[] b = this.finishButton(limb, k);
                    int[] r = this.screen.textButton(g, "finish:" + limb.id + k, (Component)Component.translatable((String)("screen.chromeware.frame." + FRAMES[k])), b[0], b[1], mx, my, skinned == (k == 1), false);
                    if (!CyberwareScreen.inside(mx, my, r[0], r[1], r[2], r[3])) continue;
                    tooltip = List.of(Component.translatable((String)"screen.chromeware.frame_finish"));
                }
                if (!this.screen.onChair()) continue;
                int[] b = this.limbRemove(limb);
                boolean hover = CyberwareScreen.inside(mx, my, b[0] - 2, b[1] - 2, b[0] + 18, b[1] + 18);
                this.ui.box(g, b[0], b[1], 16, 16, this.ui.edge("remove:" + limb.id, hover, false, true, PonderUi.IDLE, PonderUi.FAIL), PonderUi.BACKGROUND, 0);
                PonderGuiTextures.ICON_CONFIG_DISCARD.render(g, b[0], b[1]);
                if (!hover) continue;
                tooltip = limb.part == BodyPart.BODY ? List.of(Component.translatable((String)"screen.chromeware.remove_socket", (Object[])new Object[]{item.getHoverName()}), Component.translatable((String)"screen.chromeware.remove_torso_warning").withStyle(ChatFormatting.RED)) : List.of(Component.translatable((String)"screen.chromeware.remove_socket", (Object[])new Object[]{item.getHoverName()}));
                continue;
            }
            if (!this.screen.onChair()) continue;
            List<ItemStack> frames = this.frameStock(limb);
            for (k = 0; k < frames.size(); ++k) {
                int[] t = this.frameTile(limb, k);
                boolean hover = CyberwareScreen.inside(mx, my, t[0] - 2, t[1] - 2, t[0] + 18, t[1] + 18);
                this.screen.button(g, "frame:" + limb.id + k, t[0], t[1], 16, 16, hover, false, true, 0);
                g.renderItem(frames.get(k).copyWithCount(1), t[0], t[1]);
                g.renderItemDecorations(this.font, frames.get(k), t[0], t[1]);
                if (!hover) continue;
                ArrayList<MutableComponent> lines = new ArrayList<MutableComponent>(Screen.getTooltipFromItem((Minecraft)this.minecraft, (ItemStack)frames.get(k).copyWithCount(1)));
                lines.add(Component.translatable((String)"screen.chromeware.stock_count", (Object[])new Object[]{frames.get(k).getCount()}).withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable((String)"screen.chromeware.frame_fit").withStyle(ChatFormatting.YELLOW));
                tooltip = lines;
            }
            if (!frames.isEmpty()) continue;
            this.ui.text(g, this.font, (Component)Component.translatable((String)"screen.chromeware.frame_none"), x + 6, y + 31, PonderUi.FAINT);
        }
        return tooltip;
    }

    private List<ItemStack> frameStock(Limb limb) {
        ArrayList<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack item : SurgeryStock.clientView) {
            if (!CyberLimbs.fits(item, limb.part)) continue;
            list.add(item);
        }
        return list.size() > 4 ? list.subList(0, 4) : list;
    }

    private boolean clickCards(double mx, double my) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return false;
        }
        int cardW = this.cardW();
        for (Limb limb : LIMBS) {
            int y;
            int x = this.cardX(limb);
            if (!CyberwareScreen.inside(mx, my, x - 2, (y = this.cardY(limb)) - 2, x + cardW + 2, y + 46 + 2)) continue;
            boolean framed = CyberLimbs.has((Player)player, limb.part);
            if (framed) {
                int[] b;
                for (int k = 1; k < FRAMES.length; ++k) {
                    int w;
                    int[] b2 = this.finishButton(limb, k);
                    if (!CyberwareScreen.inside(mx, my, b2[0] - 2, b2[1] - 2, b2[0] + (w = CyberwareScreen.textButtonWidth(this.font, (Component)Component.translatable((String)("screen.chromeware.frame." + FRAMES[k])))) + 2, b2[1] + 14)) continue;
                    this.ui.press("finish:" + limb.id + k);
                    PacketDistributor.sendToServer((CustomPacketPayload)ImplantPackets.Limb.of(k == 1 ? 2 : 3, limb.part), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    return true;
                }
                if (this.screen.onChair() && CyberwareScreen.inside(mx, my, (b = this.limbRemove(limb))[0] - 2, b[1] - 2, b[0] + 18, b[1] + 18)) {
                    PacketDistributor.sendToServer((CustomPacketPayload)ImplantPackets.Limb.of(1, limb.part), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    return true;
                }
            } else if (this.screen.onChair()) {
                List<ItemStack> frames = this.frameStock(limb);
                for (int k = 0; k < frames.size(); ++k) {
                    int[] t = this.frameTile(limb, k);
                    if (!CyberwareScreen.inside(mx, my, t[0] - 2, t[1] - 2, t[0] + 18, t[1] + 18)) continue;
                    PacketDistributor.sendToServer((CustomPacketPayload)new SurgeryPackets.Operate(frames.get(k).copyWithCount(1), "limb", limb.part.ordinal()), (CustomPacketPayload[])new CustomPacketPayload[0]);
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    private record Limb(String id, BodyPart part, HumanoidArm arm, boolean left, int row, float side, float height) {
    }
}

