/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.Util
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.lwjgl.glfw.GLFW
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.cyberware.CyberwareMenuClient;
import com.chromeware.client.cyberware.CyberwareWheelRenderer;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.BerserkState;
import com.chromeware.cyberware.BloodPump;
import com.chromeware.cyberware.BloodPumpState;
import com.chromeware.cyberware.ImplantAbility;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanState;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.launcher.ProjectileLauncher;
import java.util.Arrays;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class CyberwareWheelScreen
extends Screen {
    private final long openedAt = Util.getMillis();
    private List<ImplantAbility> entries = List.of();
    private boolean finished;
    private double scale = 1.0;

    public CyberwareWheelScreen() {
        super((Component)Component.translatable((String)"screen.chromeware.wheel"));
    }

    protected void init() {
        this.entries = this.minecraft.player == null ? List.of() : Arrays.stream(ImplantAbility.values()).filter(ability -> ability != ImplantAbility.ARM_IMPLANT).filter(ability -> ability.installed((Player)this.minecraft.player)).toList();
        double radius = CyberwareWheelRenderer.outerRadius(this.count());
        this.scale = Math.min(1.0, Math.min((double)this.width / (2.0 * (radius + 18.0)), (double)this.height / (2.0 * (radius + 48.0))));
        GLFW.glfwSetCursorPos((long)this.minecraft.getWindow().getWindow(), (double)((double)this.minecraft.getWindow().getScreenWidth() / 2.0), (double)((double)this.minecraft.getWindow().getScreenHeight() / 2.0));
    }

    private int count() {
        return Math.max(2, this.entries.size() + 1);
    }

    private int selection(double mouseX, double mouseY) {
        double x = (mouseX - (double)this.width / 2.0) / this.scale;
        double y = (mouseY - (double)this.height / 2.0) / this.scale;
        if (Math.hypot(x, y) < CyberwareWheelRenderer.innerRadius(this.count())) {
            return -1;
        }
        return CyberwareWheelRenderer.selectedIndex(x, y, 0.0, 0.0, this.count());
    }

    private boolean eligible() {
        return this.minecraft.player != null && this.minecraft.level != null && this.minecraft.player.isAlive() && !this.minecraft.player.isSpectator() && this.minecraft.isWindowActive();
    }

    public void tick() {
        if (!this.eligible()) {
            this.onClose();
            return;
        }
        if (!CyberwareMenuClient.physicallyPressed()) {
            this.finishSelection();
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float tick) {
        graphics.fill(0, 0, this.width, this.height, 2014253069);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float tick) {
        this.renderBackground(graphics, mouseX, mouseY, tick);
        int selected = this.selection(mouseX, mouseY);
        float progress = Math.clamp((float)((float)(Util.getMillis() - this.openedAt) / 160.0f), (float)0.0f, (float)1.0f);
        float animation = 0.72f + 0.28f * (1.0f - (1.0f - progress) * (1.0f - progress));
        graphics.pose().pushPose();
        graphics.pose().translate((double)this.width / 2.0, (double)this.height / 2.0, 0.0);
        graphics.pose().scale((float)this.scale * animation, (float)this.scale * animation, 1.0f);
        for (int i = 0; i < this.count(); ++i) {
            boolean dismiss;
            ImplantAbility ability = i < this.entries.size() ? this.entries.get(i) : null;
            boolean bl = dismiss = i == this.count() - 1;
            MutableComponent label = dismiss ? Component.translatable((String)"screen.chromeware.wheel.dismiss") : (ability == null ? Component.translatable((String)"screen.chromeware.wheel.empty") : (ability == ImplantAbility.ARM_IMPLANT && ProjectileLauncher.installed((Player)this.minecraft.player) ? Component.translatable((String)"screen.chromeware.wheel.launcher") : ability.label((Player)this.minecraft.player)));
            CyberwareWheelRenderer.drawWedge(graphics, this.font, 0, 0, CyberwareWheelRenderer.wedgeStartAngle(i, this.count()), CyberwareWheelRenderer.wedgeEndAngle(i, this.count()), this.count(), selected == i, (Component)label, ability == null ? null : this.detail(ability));
            if (ability == null) continue;
            double angle = Math.toRadians((CyberwareWheelRenderer.wedgeStartAngle(i, this.count()) + CyberwareWheelRenderer.wedgeEndAngle(i, this.count())) / 2.0);
            double radius = (CyberwareWheelRenderer.innerRadius(this.count()) + CyberwareWheelRenderer.outerRadius(this.count())) / 2.0;
            double offset = selected == i ? 6.0 : 0.0;
            int x = (int)Math.round(Math.cos(angle) * (radius + offset));
            int y = (int)Math.round(Math.sin(angle) * (radius + offset));
            graphics.renderItem(ability.icon((Player)this.minecraft.player), x - 8, y - 30);
        }
        CyberwareWheelRenderer.drawPointer(graphics, 0, 0, (int)(((double)mouseX - (double)this.width / 2.0) / this.scale), (int)(((double)mouseY - (double)this.height / 2.0) / this.scale), this.count());
        int caption = (int)Math.ceil(CyberwareWheelRenderer.outerRadius(this.count())) + 22;
        graphics.drawCenteredString(this.font, this.title, 0, -caption, -2762281);
        graphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.chromeware.wheel.release", (Object[])new Object[]{CyberwareMenuClient.WHEEL.getTranslatedKeyMessage()}), 0, caption, -1);
        graphics.drawCenteredString(this.font, (Component)Component.translatable((String)"screen.chromeware.wheel.cancel_hint"), 0, caption + 13, -6313818);
        graphics.pose().popPose();
    }

    private Component detail(ImplantAbility ability) {
        LocalPlayer player = this.minecraft.player;
        return switch (ability) {
            default -> throw new MatchException(null, null);
            case ImplantAbility.BLOOD_PUMP -> {
                BloodPumpState state = BloodPump.state((Player)player);
                String charges = state.charges() + "/" + BloodPumpState.maxCharges();
                if (state.charges() < BloodPumpState.maxCharges()) {
                    yield Component.translatable((String)"screen.chromeware.wheel.recharge", (Object[])new Object[]{charges, (state.rechargeTicks() + 19) / 20});
                }
                yield Component.translatable((String)(player.getHealth() >= player.getMaxHealth() ? "screen.chromeware.wheel.full_health" : "screen.chromeware.wheel.charges"), (Object[])new Object[]{charges});
            }
            case ImplantAbility.SANDEVISTAN -> {
                SandevistanState state = (SandevistanState)player.getData(PlayerData.SANDEVISTAN_STATE);
                if (state.activeTicks() > 0) {
                    yield Component.translatable((String)"screen.chromeware.wheel.sandevistan_remaining", (Object[])new Object[]{((long)state.activeTicks() + 19L) / 20L});
                }
                if (state.cooldown() > 0) {
                    yield Component.translatable((String)"screen.chromeware.wheel.berserk_cooldown", (Object[])new Object[]{(state.cooldown() + 19) / 20});
                }
                yield Component.translatable((String)"screen.chromeware.wheel.activate");
            }
            case ImplantAbility.ARM_IMPLANT -> Component.translatable((String)(ArmControls.anyDeployed((Player)player) ? "screen.chromeware.wheel.retract" : "screen.chromeware.wheel.deploy"));
            case ImplantAbility.OPTICAL_CAMO -> Component.translatable((String)"screen.chromeware.wheel.activate");
            case ImplantAbility.OPTICAL_SCAN -> Component.translatable((String)(ImplantEffects.scanning((Player)player) ? "screen.chromeware.wheel.deactivate" : "screen.chromeware.wheel.scan"));
            case ImplantAbility.BERSERK -> {
                BerserkState state = ZetatechBerserk.state((Player)player);
                if (state.activeTicks() > 0) {
                    yield Component.translatable((String)"screen.chromeware.wheel.berserk_remaining", (Object[])new Object[]{(state.activeTicks() + 19) / 20});
                }
                if (state.cooldown() > 0) {
                    yield Component.translatable((String)"screen.chromeware.wheel.berserk_cooldown", (Object[])new Object[]{(state.cooldown() + 19) / 20});
                }
                yield Component.translatable((String)"screen.chromeware.wheel.activate");
            }
            case ImplantAbility.SUPERHERO_LANDING -> Component.translatable((String)"screen.chromeware.wheel.landing_hint");
            case ImplantAbility.OVERCLOCK -> Component.translatable((String)"screen.chromeware.overclock_hint");
        };
    }

    private void finishSelection() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        int selected = this.selection(this.minecraft.mouseHandler.xpos() * (double)this.width / (double)this.minecraft.getWindow().getScreenWidth(), this.minecraft.mouseHandler.ypos() * (double)this.height / (double)this.minecraft.getWindow().getScreenHeight());
        if (this.eligible() && selected >= 0 && selected < this.entries.size()) {
            PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.Activate(this.entries.get(selected).ordinal()), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
        this.minecraft.setScreen(null);
    }

    public void onClose() {
        this.finished = true;
        this.minecraft.setScreen(null);
    }

    public boolean keyReleased(int key, int scan, int modifiers) {
        if (CyberwareMenuClient.WHEEL.matches(key, scan)) {
            this.finishSelection();
        }
        return true;
    }

    public boolean mouseReleased(double x, double y, int button) {
        if (CyberwareMenuClient.WHEEL.matchesMouse(button)) {
            this.finishSelection();
        }
        return true;
    }

    public boolean mouseClicked(double x, double y, int button) {
        return true;
    }

    public boolean keyPressed(int key, int scan, int modifiers) {
        if (key == 256) {
            this.onClose();
        }
        return true;
    }
}

