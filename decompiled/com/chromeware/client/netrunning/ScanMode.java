/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.simibubi.create.AllSoundEvents
 *  com.simibubi.create.foundation.gui.RemovedGuiUtils
 *  com.simibubi.create.infrastructure.config.AllConfigs
 *  com.simibubi.create.infrastructure.config.CClient
 *  net.createmod.catnip.gui.element.BoxElement
 *  net.createmod.catnip.gui.element.GuiGameElement
 *  net.createmod.catnip.theme.Color
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.SpawnEggItem
 *  net.minecraft.world.item.component.ResolvableProfile
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderGuiEvent$Post
 *  net.neoforged.neoforge.client.event.RenderGuiEvent$Pre
 *  net.neoforged.neoforge.client.event.RenderGuiLayerEvent$Pre
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.network.PacketDistributor
 */
package com.chromeware.client.netrunning;

import com.chromeware.client.netrunning.Scanner;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.EffectiveHealth;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantAbility;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.cyberware.KiroshiOptics;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.humanity.Humanity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.createmod.catnip.gui.element.BoxElement;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ScanMode {
    private static final ResourceLocation VIGNETTE = ResourceLocation.withDefaultNamespace((String)"textures/misc/vignette.png");
    private static final float VIGNETTE_STRENGTH = 0.32f;
    private static final int STICKY = 10;
    private static Entity target;
    private static int sticky;
    private static int hoverTicks;
    private static boolean returnToScan;
    private static boolean scanForHacks;

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ScanMode::tick);
        NeoForge.EVENT_BUS.addListener(ScanMode::vignette);
        NeoForge.EVENT_BUS.addListener(ScanMode::overlay);
        NeoForge.EVENT_BUS.addListener(ScanMode::goggleLayer);
    }

    public static boolean scanning() {
        LocalPlayer p = Minecraft.getInstance().player;
        return p != null && ImplantEffects.scanning((Player)p) && !Scanner.active();
    }

    public static boolean eyesOn() {
        LocalPlayer p = Minecraft.getInstance().player;
        return p != null && (ImplantEffects.scanning((Player)p) || Scanner.active());
    }

    public static Entity target() {
        return ScanMode.scanning() && target != null && target.isAlive() && !target.isRemoved() ? target : null;
    }

    public static void cycle() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            return;
        }
        if (Scanner.active()) {
            Scanner.close();
            if (returnToScan) {
                ScanMode.click(1.2f);
            }
            returnToScan = false;
            return;
        }
        returnToScan = ImplantEffects.scanning((Player)p);
        if (!returnToScan && ImplantEffects.hasOptics((Player)p)) {
            ScanMode.toggleScan();
            scanForHacks = true;
        }
        Scanner.toggle();
    }

    private static void toggleScan() {
        PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.Activate(ImplantAbility.OPTICAL_SCAN.ordinal()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static void click(float pitch) {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)AllSoundEvents.CONTROLLER_CLICK.getMainEvent(), (float)pitch, (float)0.6f));
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (scanForHacks && !Scanner.active()) {
            scanForHacks = false;
            if (mc.player != null && ImplantEffects.scanning((Player)mc.player)) {
                ScanMode.toggleScan();
            }
        }
        if (mc.player == null || mc.level == null || !ScanMode.scanning()) {
            target = null;
            hoverTicks = 0;
            return;
        }
        Entity aimed = ScanMode.aim((Player)mc.player);
        if (aimed != null) {
            if (aimed != target) {
                hoverTicks = 0;
            }
            target = aimed;
            sticky = 10;
        } else if (target != null && --sticky <= 0) {
            target = null;
            hoverTicks = 0;
        }
        if (target != null) {
            ++hoverTicks;
        }
    }

    private static Entity aim(Player p) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 end = eye.add(look.scale(32.0));
        Entity best = null;
        double bestScore = Double.MAX_VALUE;
        double currentScore = Double.MAX_VALUE;
        for (Entity e2 : mc.level.getEntities((Entity)p, new AABB(eye, end).inflate(2.0), e -> {
            Player other;
            LivingEntity l;
            return e instanceof LivingEntity && (l = (LivingEntity)e).isAlive() && (!(e instanceof Player) || !(other = (Player)e).isSpectator()) && !e.isInvisibleTo(p) && p.hasLineOfSight(e);
        })) {
            AABB box;
            Vec3 center = e2.getBoundingBox().getCenter();
            double along = center.subtract(eye).dot(look);
            if (along <= 0.0 || !(box = e2.getBoundingBox().inflate(0.25 + along * 0.02)).contains(eye) && box.clip(eye, end).isEmpty()) continue;
            double score = center.subtract(eye.add(look.scale(along))).length() / Math.max(0.5, (double)e2.getBbWidth()) * 4.0 + along * 0.05;
            if (e2 == target) {
                currentScore = score;
            }
            if (!(score < bestScore)) continue;
            best = e2;
            bestScore = score;
        }
        return currentScore < Double.MAX_VALUE && bestScore > currentScore * 0.6 ? target : best;
    }

    private static void vignette(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null || !ScanMode.eyesOn()) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.ZERO, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
        g.setColor(0.32f, 0.32f, 0.32f, 1.0f);
        g.blit(VIGNETTE, 0, 0, -90, 0.0f, 0.0f, g.guiWidth(), g.guiHeight(), g.guiWidth(), g.guiHeight());
        g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
    }

    private static void goggleLayer(RenderGuiLayerEvent.Pre event) {
        if (ScanMode.target() != null && event.getName().getNamespace().equals("create") && event.getName().getPath().equals("goggle_info")) {
            event.setCanceled(true);
        }
    }

    private static void overlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Entity t = ScanMode.target();
        if (mc.options.hideGui || mc.screen != null || !(t instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)t;
        GuiGraphics g = event.getGuiGraphics();
        Font font = mc.font;
        List<Component> tooltip = ScanMode.lines(living);
        int textWidth = 0;
        for (Component line : tooltip) {
            textWidth = Math.max(textWidth, font.width((FormattedText)line));
        }
        int tooltipHeight = 8 + (tooltip.size() > 1 ? 2 + (tooltip.size() - 1) * 10 : 0);
        int width = g.guiWidth();
        int height = g.guiHeight();
        CClient cfg = AllConfigs.client();
        int posX = Math.min(width / 2 + (Integer)cfg.overlayOffsetX.get(), width - textWidth - 20);
        int posY = Math.min(height / 2 + (Integer)cfg.overlayOffsetY.get(), height - tooltipHeight - 20);
        float fade = Mth.clamp((float)(((float)hoverTicks + event.getPartialTick().getGameTimeDeltaPartialTick(false)) / 24.0f), (float)0.0f, (float)1.0f);
        boolean custom = (Boolean)cfg.overlayCustomColor.get();
        Color background = custom ? new Color(((Integer)cfg.overlayBackgroundColor.get()).intValue()) : BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(0.75f);
        Color top = custom ? new Color(((Integer)cfg.overlayBorderColorTop.get()).intValue()) : ((Color)BoxElement.COLOR_VANILLA_BORDER.getFirst()).copy();
        Color bottom = custom ? new Color(((Integer)cfg.overlayBorderColorBot.get()).intValue()) : ((Color)BoxElement.COLOR_VANILLA_BORDER.getSecond()).copy();
        PoseStack pose = g.pose();
        pose.pushPose();
        if (fade < 1.0f) {
            pose.translate(Math.pow(1.0f - fade, 3.0) * (double)Math.signum((float)((Integer)cfg.overlayOffsetX.get()).intValue() + 0.5f) * 8.0, 0.0, 0.0);
            background.scaleAlpha(fade);
            top.scaleAlpha(fade);
            bottom.scaleAlpha(fade);
        }
        GuiGameElement.of((ItemStack)ScanMode.icon(living)).at((float)(posX + 10), (float)(posY - 16), 450.0f).render(g);
        RemovedGuiUtils.drawHoveringText((GuiGraphics)g, tooltip, (int)posX, (int)posY, (int)width, (int)height, (int)-1, (int)background.getRGB(), (int)top.getRGB(), (int)bottom.getRGB(), (Font)font);
        pose.popPose();
    }

    public static List<Component> lines(LivingEntity e) {
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)ScanMode.indent(0).append((Component)e.getDisplayName().copy().withStyle(ChatFormatting.WHITE)));
        lines.add((Component)ScanMode.row("health", (Component)Component.literal((String)(ScanMode.number(e.getHealth()) + " / " + ScanMode.number(e.getMaxHealth())))));
        float ehp = EffectiveHealth.of(e);
        lines.add((Component)ScanMode.row("ehp", (Component)Component.literal((String)(Float.isInfinite(ehp) ? "\u221e" : ScanMode.number(ehp)))));
        if (e instanceof Player) {
            Player p = (Player)e;
            double humanity = Humanity.view(p).humanity();
            lines.add((Component)ScanMode.row("humanity", (Component)Component.literal((String)String.format(Locale.ROOT, "%.0f%%", Math.floor(humanity + 1.0E-6)))));
            lines.add((Component)ScanMode.row("os", ScanMode.implant(SandevistanImplant.get(p).item())));
            lines.add((Component)ScanMode.indent(0).append((Component)Component.translatable((String)"screen.chromeware.scan.arms").withStyle(ChatFormatting.GRAY)));
            for (HumanoidArm side : new HumanoidArm[]{HumanoidArm.RIGHT, HumanoidArm.LEFT}) {
                lines.add((Component)ScanMode.indent(1).append((Component)Component.translatable((String)(side == HumanoidArm.RIGHT ? "screen.chromeware.arm_right" : "screen.chromeware.arm_left")).withStyle(ChatFormatting.DARK_GRAY)).append((Component)Component.literal((String)": ").withStyle(ChatFormatting.DARK_GRAY)).append(ScanMode.implant(ArmImplants.get(p, side).item())));
            }
        }
        return lines;
    }

    private static MutableComponent row(String key, Component value) {
        return ScanMode.indent(0).append((Component)Component.translatable((String)("screen.chromeware.scan." + key)).withStyle(ChatFormatting.GRAY)).append((Component)Component.literal((String)" ")).append((Component)value.copy().withStyle(ChatFormatting.WHITE));
    }

    private static Component implant(ItemStack item) {
        return item.isEmpty() ? Component.translatable((String)"screen.chromeware.scan.none").withStyle(ChatFormatting.DARK_GRAY) : item.getHoverName().copy().withStyle(ChatFormatting.WHITE);
    }

    private static MutableComponent indent(int level) {
        Font font = Minecraft.getInstance().font;
        int spaces = 4 + level;
        int width = font.width(" ");
        return Component.literal((String)" ".repeat(width == 4 ? spaces : Mth.ceil((float)(4.0f * (float)spaces / (float)width))));
    }

    private static String number(float value) {
        return (double)Math.abs(value - (float)Math.round(value)) < 0.05 ? Integer.toString(Math.round(value)) : String.format(Locale.ROOT, "%.1f", Float.valueOf(value));
    }

    private static ItemStack icon(LivingEntity e) {
        if (e instanceof Player) {
            Player p = (Player)e;
            ItemStack head = new ItemStack((ItemLike)Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, (Object)new ResolvableProfile(p.getGameProfile()));
            return head;
        }
        SpawnEggItem egg = SpawnEggItem.byId((EntityType)e.getType());
        if (egg != null) {
            return new ItemStack((ItemLike)egg);
        }
        LocalPlayer me = Minecraft.getInstance().player;
        Implant optic = me == null ? null : KiroshiOptics.installed((Player)me);
        return (optic == null ? Implant.KIROSHI_OPTICS : optic).stack();
    }
}

