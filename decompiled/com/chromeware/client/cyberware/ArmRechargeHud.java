/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager$DestFactor
 *  com.mojang.blaze3d.platform.GlStateManager$SourceFactor
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.AttackIndicatorStatus
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderGuiLayerEvent$Post
 *  net.neoforged.neoforge.client.gui.VanillaGuiLayers
 */
package com.chromeware.client.cyberware;

import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmRecharge;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireImplant;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid="chromeware", value={Dist.CLIENT})
public final class ArmRechargeHud {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace((String)"hud/crosshair_attack_indicator_background");
    private static final ResourceLocation PROGRESS = ResourceLocation.withDefaultNamespace((String)"hud/crosshair_attack_indicator_progress");
    private static final ResourceLocation FULL = ResourceLocation.withDefaultNamespace((String)"hud/crosshair_attack_indicator_full");
    private static ArmRecharge.State seen;
    private static int arrived;
    private static final int[] CHARGED;

    public static boolean replacesVanilla(Player player) {
        return player != null && ArmControls.anyDeployed(player);
    }

    public static float progress(Player player, HumanoidArm side, float partialTick) {
        if (!ArmControls.deployedOn(player, side)) {
            return 1.0f;
        }
        ArmRecharge.State state = (ArmRecharge.State)player.getData(PlayerData.ARM_RECHARGE);
        if (state != seen) {
            seen = state;
            arrived = player.tickCount;
        }
        float f = state.progress(side, (float)(player.tickCount - arrived) + partialTick);
        return f;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        for (HumanoidArm side : HumanoidArm.values()) {
            int i = side.ordinal();
            if (player == null || ArmRechargeHud.chargeTicks((Player)player, side) <= 0) {
                ArmRechargeHud.CHARGED[i] = -1;
                continue;
            }
            if (CHARGED[i] >= 0 && CHARGED[i] <= player.tickCount) continue;
            ArmRechargeHud.CHARGED[i] = player.tickCount;
        }
    }

    public static float charge(Player player, HumanoidArm side, float partialTick) {
        int full = ArmRechargeHud.chargeTicks(player, side);
        int i = side.ordinal();
        if (full <= 0 || CHARGED[i] < 0) {
            return -1.0f;
        }
        return Math.min(1.0f, ((float)(player.tickCount - CHARGED[i]) + partialTick) / (float)full);
    }

    private static int chargeTicks(Player player, HumanoidArm side) {
        if (GorillaCombat.deployed(player, side) && GorillaCombat.state(player).charging(side)) {
            return 10;
        }
        if (ProjectileLauncher.chargeShown(player, side)) {
            return 15;
        }
        if (MantisImplant.deployed(player, side) && MantisImplant.state(player, side).action() == 2) {
            return 6;
        }
        if (MonowireImplant.deployed(player, side) && MonowireImplant.state(player, side).action() == 2) {
            return 6;
        }
        return 0;
    }

    @SubscribeEvent
    public static void draw(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals((Object)VanillaGuiLayers.CROSSHAIR)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!ArmRechargeHud.replacesVanilla((Player)player) || mc.options.hideGui || !mc.options.getCameraType().isFirstPerson() || player.isSpectator() || mc.options.attackIndicator().get() == AttackIndicatorStatus.OFF || mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        int y = graphics.guiHeight() / 2 - 7 + 16;
        int centre = graphics.guiWidth() / 2;
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate((GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, (GlStateManager.DestFactor)GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, (GlStateManager.SourceFactor)GlStateManager.SourceFactor.ONE, (GlStateManager.DestFactor)GlStateManager.DestFactor.ZERO);
        for (HumanoidArm side : HumanoidArm.values()) {
            float f;
            int x = side == HumanoidArm.LEFT ? centre - 17 : centre + 1;
            boolean full = false;
            float charge = ArmRechargeHud.charge((Player)player, side, partialTick);
            if (charge >= 0.0f) {
                f = charge;
                full = charge >= 1.0f;
            } else if (ArmControls.deployedOn((Player)player, side)) {
                f = ArmRechargeHud.progress((Player)player, side, partialTick);
            } else {
                if (side != player.getMainArm()) continue;
                f = player.getAttackStrengthScale(0.0f);
                Entity entity = mc.crosshairPickEntity;
                if (entity instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity)entity;
                    if (f >= 1.0f) {
                        boolean bl = full = player.getCurrentItemAttackStrengthDelay() > 5.0f && target.isAlive();
                    }
                }
            }
            if (full) {
                graphics.blitSprite(FULL, x, y, 16, 16);
                continue;
            }
            if (!(f < 1.0f)) continue;
            graphics.blitSprite(BACKGROUND, x, y, 16, 4);
            graphics.blitSprite(PROGRESS, 16, 4, 0, 0, x, y, (int)(f * 17.0f), 4);
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    static {
        CHARGED = new int[]{-1, -1};
    }
}

