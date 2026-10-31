/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.createmod.ponder.api.registration.PonderPlugin
 *  net.createmod.ponder.foundation.PonderIndex
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.NoopRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.ThrownItemRenderer
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.client.renderer.entity.player.PlayerRenderer
 *  net.minecraft.client.resources.PlayerSkin$Model
 *  net.minecraft.world.entity.EntityType
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.neoforge.client.event.EntityRenderersEvent$AddLayers
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware;

import com.chromeware.Chromeware;
import com.chromeware.body.DetachedLimb;
import com.chromeware.body.hitbox.BodyHitboxes;
import com.chromeware.client.berserk.BerserkClient;
import com.chromeware.client.body.CyberLimbRenderer;
import com.chromeware.client.body.RenderedBodyHitboxes;
import com.chromeware.client.compat.EpicFightClient;
import com.chromeware.client.config.ConfigScreen;
import com.chromeware.client.cyberware.ArmInput;
import com.chromeware.client.cyberware.CyberwareMenuClient;
import com.chromeware.client.cyberware.FreeHands;
import com.chromeware.client.cyberware.GorillaAnimation;
import com.chromeware.client.cyberware.GorillaArmRenderer;
import com.chromeware.client.cyberware.HardwareItems;
import com.chromeware.client.cyberware.PainducerClient;
import com.chromeware.client.cyberware.SandevistanLayer;
import com.chromeware.client.extendo.ExtendoClient;
import com.chromeware.client.extendo.ExtendoRenderer;
import com.chromeware.client.humanity.ImmunosuppressantFluidsClient;
import com.chromeware.client.humanity.PsychosisClient;
import com.chromeware.client.humanity.SyringeClient;
import com.chromeware.client.launcher.LauncherClient;
import com.chromeware.client.launcher.LauncherRenderer;
import com.chromeware.client.mantis.MantisClient;
import com.chromeware.client.mantis.MantisRenderer;
import com.chromeware.client.monowire.MonowireClient;
import com.chromeware.client.monowire.MonowireWristRenderer;
import com.chromeware.client.netrunning.NetrunningClient;
import com.chromeware.client.netrunning.Scanner;
import com.chromeware.client.ponder.PonderSetup;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.client.surgery.SurgeryClient;
import com.chromeware.client.surgery.SurgeryPropLayer;
import com.chromeware.client.update.UpdateChecker;
import com.chromeware.launcher.LauncherEntities;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value="chromeware", dist={Dist.CLIENT})
public final class ChromewareClient {
    public ChromewareClient(IEventBus modEventBus, ModContainer container) {
        ConfigScreen.register(container);
        PonderIndex.addPlugin((PonderPlugin)new PonderSetup());
        UpdateChecker.register();
        NetrunningClient.register();
        modEventBus.addListener(ChromewareClient::addLayers);
        modEventBus.addListener(event -> event.registerEntityRenderer((EntityType)DetachedLimb.TYPE.get(), NoopRenderer::new));
        CyberwareMenuClient.register(modEventBus);
        SandevistanClient.register(modEventBus);
        BerserkClient.register(modEventBus);
        PainducerClient.register();
        MonowireClient.register(modEventBus);
        LauncherClient.register(modEventBus);
        ExtendoClient.register(modEventBus);
        GorillaAnimation.register();
        modEventBus.addListener(event -> event.registerEntityRenderer((EntityType)LauncherEntities.NUGGET.get(), ThrownItemRenderer::new));
        MantisClient.register(modEventBus);
        SurgeryClient.register(modEventBus);
        ArmInput.register();
        PsychosisClient.register(modEventBus);
        SyringeClient.register();
        ImmunosuppressantFluidsClient.register(modEventBus);
        if (ModList.get().isLoaded("epicfight")) {
            EpicFightClient.register();
        }
        NeoForge.EVENT_BUS.addListener(event -> {
            if (!Minecraft.getInstance().isPaused()) {
                FreeHands.tick();
            }
        });
        modEventBus.addListener(SandevistanLayer::registerReloads);
        SandevistanLayer.register();
        modEventBus.addListener(GorillaArmRenderer::registerReloads);
        modEventBus.addListener(HardwareItems::register);
        modEventBus.addListener(HardwareItems::registerReloads);
        modEventBus.addListener(CyberLimbRenderer::registerReloads);
        BodyHitboxes.clientBoxes = RenderedBodyHitboxes::get;
        Chromeware.LOGGER.info("Create: Chromeware client initialized");
    }

    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer entityRenderer = event.getSkin(skin);
            if (!(entityRenderer instanceof PlayerRenderer)) continue;
            PlayerRenderer renderer = (PlayerRenderer)entityRenderer;
            renderer.addLayer((RenderLayer)new SurgeryPropLayer((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>)renderer));
            renderer.addLayer((RenderLayer)new SandevistanLayer(renderer));
            renderer.addLayer((RenderLayer)new CyberLimbRenderer.Layer(renderer));
            renderer.addLayer((RenderLayer)new GorillaArmRenderer.Layer(renderer));
            renderer.addLayer((RenderLayer)new MonowireWristRenderer.Layer(renderer));
            renderer.addLayer((RenderLayer)new LauncherRenderer.Layer(renderer));
            renderer.addLayer((RenderLayer)new MantisRenderer.Layer(renderer));
            renderer.addLayer((RenderLayer)new ExtendoRenderer.Layer(renderer));
            renderer.addLayer((RenderLayer)new Scanner.EyeLayer((RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>)renderer));
        }
    }
}

