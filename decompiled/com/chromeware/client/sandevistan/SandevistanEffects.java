/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.EffectInstance
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.server.packs.resources.ResourceProvider
 *  net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 */
package com.chromeware.client.sandevistan;

import com.chromeware.Chromeware;
import com.chromeware.client.sandevistan.EchoCache;
import com.chromeware.client.sandevistan.SandevistanAfterimages;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.client.sandevistan.SandevistanFieldWalls;
import com.chromeware.client.sandevistan.SandevistanHistory;
import com.chromeware.client.sandevistan.SandevistanKinetics;
import com.chromeware.client.sandevistan.SandevistanRenderTypes;
import com.chromeware.client.sandevistan.SandevistanRigEffects;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.mixin.client.SandevistanPostChainAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

public final class SandevistanEffects {
    private static PostChain chain;
    private static int width;
    private static int height;
    private static boolean wasInside;
    private static Boolean lastPreset;
    private static double activatedAt;
    private static long retryAt;
    private static boolean sceneDepth;
    private static boolean entityDepth;

    public static void registerReloads(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> SandevistanEffects.clear()));
    }

    public static void clear() {
        if (chain != null) {
            chain.close();
        }
        chain = null;
        SandevistanRenderTypes.clear();
        SandevistanAfterimages.clear();
        SandevistanRigEffects.clear();
        SandevistanKinetics.clear();
        SandevistanHistory.clear();
        wasInside = false;
        lastPreset = null;
        retryAt = 0L;
    }

    public static boolean available() {
        return chain != null;
    }

    public static void captureSceneDepth() {
        sceneDepth = SandevistanEffects.captureDepth("scene_depth");
    }

    private static boolean captureDepth(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (chain == null || mc.level == null || !SandevistanEffects.visible()) {
            return false;
        }
        int bound = GlStateManager._getInteger((int)36006);
        chain.getTempTarget(name).copyDepthFrom(mc.getMainRenderTarget());
        GlStateManager._glBindFramebuffer((int)36160, (int)bound);
        return true;
    }

    private static boolean visible() {
        return SandevistanClient.isLocalInsideSandevistanField() || SandevistanClient.hasTimeFields() || SandevistanAfterimages.hasTrails() || SandevistanRigEffects.hasTrails();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            entityDepth = SandevistanEffects.captureDepth("entity_depth");
            return;
        }
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        if (!ChromewareConfig.timeSlowVfx()) {
            SandevistanEffects.clear();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        float tick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean inside = SandevistanClient.isLocalInsideSandevistanField();
        double now = (float)mc.level.getGameTime() + tick;
        boolean preset = SandevistanClient.localSandevistan();
        if (!(!inside || wasInside && Boolean.valueOf(preset).equals(lastPreset))) {
            activatedAt = now;
        }
        lastPreset = preset;
        wasInside = inside;
        if (!SandevistanEffects.visible()) {
            return;
        }
        try {
            SandevistanEffects.ensure(mc);
            if (chain == null) {
                SandevistanEffects.renderWorldGeometry(event, null, null);
                return;
            }
            RenderTarget echoes = SandevistanEffects.layer(mc, "afterimages");
            RenderTarget live = SandevistanEffects.layer(mc, "live");
            boolean drawn = SandevistanEffects.renderWorldGeometry(event, echoes, live);
            EffectInstance effect = ((PostPass)((SandevistanPostChainAccessor)chain).chrome$passes().getFirst()).getEffect();
            effect.safeGetUniform("WorldTime").set((float)now);
            effect.safeGetUniform("SandevistanStrength").set(inside ? 1.0f : 0.0f);
            effect.safeGetUniform("SandevistanMovement").set((float)Math.clamp((double)(mc.player.getDeltaMovement().horizontalDistance() * 5.0), (double)0.0, (double)1.0));
            effect.safeGetUniform("SandevistanPulse").set(inside ? (float)Math.clamp((double)(1.0 - (now - activatedAt) / 7.0), (double)0.0, (double)1.0) : 0.0f);
            effect.safeGetUniform("TimeDilationStyle").set(preset ? 1.0f : 0.0f);
            effect.safeGetUniform("AfterimageStrength").set(drawn ? 1.0f : 0.0f);
            effect.safeGetUniform("EntityMask").set(sceneDepth && entityDepth ? 1.0f : 0.0f);
            effect.safeGetUniform("EchoFade").set(mc.options.getCameraType().isFirstPerson() ? 1.0f : 0.0f);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            chain.process(tick);
        }
        catch (Exception failure) {
            Chromeware.LOGGER.error("Sandevistan post effect failed; retaining direct afterimages", (Throwable)failure);
            if (chain != null) {
                chain.close();
            }
            chain = null;
            retryAt = mc.level.getGameTime() + 100L;
        }
        finally {
            entityDepth = false;
            sceneDepth = false;
            mc.getMainRenderTarget().bindWrite(false);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    private static RenderTarget layer(Minecraft mc, String name) {
        RenderTarget target = chain.getTempTarget(name);
        target.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        target.clear(Minecraft.ON_OSX);
        target.copyDepthFrom(mc.getMainRenderTarget());
        return target;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean renderWorldGeometry(RenderLevelStageEvent event, RenderTarget echoes, RenderTarget live) {
        Matrix4fStack view = RenderSystem.getModelViewStack();
        view.pushMatrix();
        view.mul((Matrix4fc)event.getModelViewMatrix());
        RenderSystem.applyModelViewMatrix();
        try {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
            SandevistanFieldWalls.render(event);
            SandevistanHistory.begin(event);
            SandevistanAfterimages.prepare(event);
            SandevistanRigEffects.prepare(event, echoes != null && SandevistanClient.isLocalInsideSandevistanField());
            boolean drawn = SandevistanAfterimages.renderEchoes(event, echoes);
            drawn |= SandevistanRigEffects.renderEchoes(event);
            boolean bl = SandevistanRigEffects.renderLive(event) || (drawn |= SandevistanAfterimages.renderLive(event, live));
            return bl;
        }
        finally {
            EchoCache.endFrame();
            view.popMatrix();
            RenderSystem.applyModelViewMatrix();
            Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        }
    }

    private static void ensure(Minecraft mc) throws IOException {
        if (chain == null && mc.level.getGameTime() >= retryAt) {
            chain = new PostChain(mc.getTextureManager(), (ResourceProvider)mc.getResourceManager(), mc.getMainRenderTarget(), ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"shaders/post/sandevistan.json"));
            height = -1;
            width = -1;
        }
        if (chain != null && (width != mc.getWindow().getWidth() || height != mc.getWindow().getHeight())) {
            width = mc.getWindow().getWidth();
            height = mc.getWindow().getHeight();
            chain.resize(width, height);
        }
    }
}

