/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 */
package com.chromeware.client.cyberware;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class ImplantCategoryBanner {
    public static final int WIDTH = 162;
    public static final int HEIGHT = 18;
    public static final int FRAMES = 48;
    public static final int FRAME_MS = 50;
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"textures/gui/implant_category_banner.png");

    public static void render(GuiGraphics graphics, Font font, int x, int y, String category) {
        int frame = (int)(Util.getMillis() / 50L % 48L);
        graphics.blit(TEXTURE, x, y, 0.0f, (float)(frame * 18), 162, 18, 162, 864);
        MutableComponent title = Component.translatable((String)("creative.chromeware.category." + category));
        int width = font.width((FormattedText)title);
        graphics.fill(x + 2, y + 2, x + Math.min(160, width + 8), y + 16, -432527076);
        graphics.drawString(font, (Component)title, x + 5, y + 5, -1586814, true);
    }
}

