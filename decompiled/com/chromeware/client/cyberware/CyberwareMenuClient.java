/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.ScreenEvent$Init$Post
 *  net.neoforged.neoforge.client.settings.IKeyConflictContext
 *  net.neoforged.neoforge.client.settings.KeyConflictContext
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.lwjgl.glfw.GLFW
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.cyberware.CyberwareScreen;
import com.chromeware.client.cyberware.CyberwareWheelScreen;
import com.chromeware.client.monowire.MonowireClient;
import com.chromeware.client.netrunning.ScanMode;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.NetrunningPackets;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class CyberwareMenuClient {
    private static final CyberwareWheelInput WHEEL_INPUT = new CyberwareWheelInput();
    public static final KeyMapping OPEN = new KeyMapping("key.chromeware.cyberware_menu", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 66, "key.categories.chromeware");
    public static final KeyMapping WHEEL = new KeyMapping("key.chromeware.cyberware", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 86, "key.categories.chromeware");
    public static final KeyMapping SANDEVISTAN = new KeyMapping("key.chromeware.sandevistan", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 88, "key.categories.chromeware");

    public static void register(IEventBus modBus) {
        modBus.addListener(event -> event.register(OPEN));
        modBus.addListener(event -> event.register(WHEEL));
        modBus.addListener(event -> event.register(SANDEVISTAN));
        NeoForge.EVENT_BUS.addListener(CyberwareMenuClient::tick);
        NeoForge.EVENT_BUS.addListener(CyberwareMenuClient::inventoryButton);
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN.consumeClick()) {
            if (mc.player == null || mc.screen != null || !mc.isWindowActive()) continue;
            mc.setScreen((Screen)new CyberwareScreen(null));
        }
        while (WHEEL.consumeClick()) {
        }
        if (WHEEL_INPUT.update(mc.player != null && mc.player.isAlive() && !mc.player.isSpectator() && mc.screen == null && mc.isWindowActive(), CyberwareMenuClient.physicallyPressed())) {
            mc.setScreen((Screen)new CyberwareWheelScreen());
        }
        while (SANDEVISTAN.consumeClick()) {
            if (mc.player == null || mc.screen != null) continue;
            if ((Netrunning.flags((Entity)mc.player) & 4) != 0) {
                PacketDistributor.sendToServer((CustomPacketPayload)new NetrunningPackets.OperatingSystem(), (CustomPacketPayload[])new CustomPacketPayload[0]);
                continue;
            }
            if (Netrunning.installed((Player)mc.player)) {
                ScanMode.cycle();
                continue;
            }
            PacketDistributor.sendToServer((CustomPacketPayload)new NetrunningPackets.OperatingSystem(), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public static Component armKey() {
        return MonowireClient.TOGGLE.getTranslatedKeyMessage();
    }

    public static boolean physicallyPressed() {
        Minecraft mc = Minecraft.getInstance();
        InputConstants.Key key = WHEEL.getKey();
        if (key.getValue() < 0) {
            return false;
        }
        long window = mc.getWindow().getWindow();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton((long)window, (int)key.getValue()) == 1;
        }
        if (key.getType() == InputConstants.Type.SCANCODE) {
            for (int code = 32; code <= 348; ++code) {
                if (GLFW.glfwGetKeyScancode((int)code) != key.getValue() || GLFW.glfwGetKey((long)window, (int)code) != 1) continue;
                return true;
            }
            return false;
        }
        return InputConstants.isKeyDown((long)window, (int)key.getValue());
    }

    private static void inventoryButton(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof InventoryScreen) && !(screen instanceof CreativeModeInventoryScreen)) {
            return;
        }
        AbstractContainerScreen container = (AbstractContainerScreen)screen;
        CyberwareInventoryButtonLayout.Bounds bounds = CyberwareInventoryButtonLayout.fit(screen.width, screen.height, container.getGuiLeft(), container.getGuiTop(), container.getXSize(), container.getYSize(), screen instanceof CreativeModeInventoryScreen);
        event.addListener((GuiEventListener)Button.builder((Component)Component.translatable((String)"button.chromeware.cyberware"), button -> Minecraft.getInstance().setScreen((Screen)new CyberwareScreen(screen))).bounds(bounds.x(), bounds.y(), bounds.width(), bounds.height()).tooltip(Tooltip.create((Component)Component.translatable((String)"button.chromeware.cyberware.tooltip"))).build());
    }

    static final class CyberwareWheelInput {
        private boolean wasDown;

        CyberwareWheelInput() {
        }

        boolean update(boolean eligible, boolean down) {
            boolean open = eligible && down && !this.wasDown;
            this.wasDown = down;
            return open;
        }
    }

    static final class CyberwareInventoryButtonLayout {
        private static final int WIDTH = 142;
        private static final int HEIGHT = 20;
        private static final int GAP = 4;
        private static final int CREATIVE_TAB_CLICK_HEIGHT = 32;

        CyberwareInventoryButtonLayout() {
        }

        static Bounds fit(int screenWidth, int screenHeight, int left, int top, int inventoryWidth, int inventoryHeight, boolean creative) {
            int x = Math.max(0, Math.min(left + (inventoryWidth - 142) / 2, screenWidth - 142));
            int bottomMargin = creative ? 0 : 4;
            int belowInventory = top + inventoryHeight + 4 + (creative ? 32 : 0);
            int y = Math.max(0, Math.min(belowInventory, screenHeight - 20 - bottomMargin));
            return new Bounds(x, y, 142, 20);
        }

        record Bounds(int x, int y, int width, int height) {
        }
    }
}

