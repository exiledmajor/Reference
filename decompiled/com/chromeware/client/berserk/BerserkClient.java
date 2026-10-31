/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.EffectInstance
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.server.packs.resources.ResourceProvider
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderGuiEvent$Pre
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.client.berserk;

import com.chromeware.Chromeware;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.cyberware.BerserkState;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.mixin.client.SandevistanPostChainAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class BerserkClient {
    private static final BerserkVisualState VISUAL = new BerserkVisualState();
    private static PostChain chain;
    private static int width;
    private static int height;
    private static long retryAt;
    private static UUID owner;
    private static Object world;

    public static void register(IEventBus bus) {
        bus.addListener(event -> event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> BerserkClient.close())));
        NeoForge.EVENT_BUS.addListener(BerserkClient::tick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, BerserkClient::vision);
        NeoForge.EVENT_BUS.addListener(event -> BerserkClient.clear());
    }

    private static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            BerserkClient.clear();
            return;
        }
        if (!mc.player.getUUID().equals(owner) || world != mc.level) {
            BerserkClient.clear();
            owner = mc.player.getUUID();
            world = mc.level;
        }
        if (!mc.player.isAlive() || mc.player.isSpectator()) {
            VISUAL.clear();
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        BerserkState state = ZetatechBerserk.state((Player)mc.player);
        VISUAL.tick(ZetatechBerserk.active((Player)mc.player), state.activeTicks(), state.kills());
    }

    private static boolean viewing(Minecraft mc) {
        return mc.player != null && mc.level != null && mc.player.isAlive() && !mc.player.isSpectator() && mc.getCameraEntity() == mc.player && mc.screen == null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void vision(RenderGuiEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!ChromewareConfig.berserkVfx() || !BerserkClient.viewing(mc)) {
            return;
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float strength = VISUAL.strength(partial);
        if (strength <= 0.001f) {
            return;
        }
        GuiGraphics g = event.getGuiGraphics();
        g.flush();
        try {
            BerserkClient.ensure(mc);
            if (chain == null) {
                BerserkClient.fallback(g, strength);
                return;
            }
            EffectInstance effect = ((PostPass)((SandevistanPostChainAccessor)chain).chrome$passes().getFirst()).getEffect();
            effect.safeGetUniform("Strength").set(strength);
            effect.safeGetUniform("BerserkTime").set(VISUAL.time(partial));
            effect.safeGetUniform("Impulse").set(VISUAL.impulse(partial));
            effect.safeGetUniform("Distortion").set(((Double)mc.options.screenEffectScale().get()).floatValue());
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            chain.process(partial);
        }
        catch (Exception error) {
            Chromeware.LOGGER.error("Berserk vision failed; retaining the red overlay", (Throwable)error);
            BerserkClient.close();
            retryAt = mc.level.getGameTime() + 100L;
            BerserkClient.fallback(g, strength);
        }
        finally {
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    private static void fallback(GuiGraphics g, float strength) {
        int w = g.guiWidth();
        int h = g.guiHeight();
        g.fill(0, 0, w, h, (int)(strength * 45.0f) << 24 | 0xC51E18);
        int bands = 18;
        int step = Math.max(1, Math.min(w, h) / 3 / bands);
        for (int i = 0; i < bands; ++i) {
            int a = i * step;
            int b = a + step;
            int color = (int)(strength * 110.0f * (1.0f - (float)i / (float)bands)) << 24 | 0x5E080B;
            g.fill(a, a, w - a, b, color);
            g.fill(a, h - b, w - a, h - a, color);
            g.fill(a, b, b, h - b, color);
            g.fill(w - b, b, w - a, h - b, color);
        }
    }

    private static void ensure(Minecraft mc) throws IOException {
        if (chain == null && mc.level.getGameTime() >= retryAt) {
            chain = new PostChain(mc.getTextureManager(), (ResourceProvider)mc.getResourceManager(), mc.getMainRenderTarget(), ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"shaders/post/berserk.json"));
            height = -1;
            width = -1;
        }
        if (chain != null && (width != mc.getWindow().getWidth() || height != mc.getWindow().getHeight())) {
            width = mc.getWindow().getWidth();
            height = mc.getWindow().getHeight();
            chain.resize(width, height);
        }
    }

    private static void close() {
        if (chain != null) {
            chain.close();
        }
        chain = null;
        retryAt = 0L;
    }

    private static void clear() {
        BerserkClient.close();
        VISUAL.clear();
        owner = null;
        world = null;
    }

    public static final class BerserkVisualState {
        private boolean active;
        private float strength;
        private float previousStrength;
        private float impulse;
        private float previousImpulse;
        private int age;
        private int remaining;
        private int kills;

        public void tick(boolean enabled, int ticksLeft, int killCount) {
            this.previousStrength = this.strength;
            this.previousImpulse = this.impulse;
            if (enabled && !this.active) {
                this.age = 0;
                this.impulse = 1.0f;
                this.kills = killCount;
            } else {
                this.impulse = Math.max(0.0f, this.impulse - 0.125f);
                if (enabled && killCount > this.kills) {
                    this.impulse = Math.max(this.impulse, 0.42f);
                }
            }
            this.active = enabled;
            this.kills = enabled ? killCount : 0;
            this.remaining = enabled ? Math.max(0, ticksLeft) : 0;
            float f = this.strength = enabled ? Math.min(1.0f, this.strength + 0.25f) : Math.max(0.0f, this.strength - 0.1f);
            if (this.strength > 0.0f) {
                ++this.age;
            }
        }

        public float strength(float partial) {
            return Mth.lerp((float)Mth.clamp((float)partial, (float)0.0f, (float)1.0f), (float)this.previousStrength, (float)this.strength);
        }

        public float impulse(float partial) {
            return Mth.lerp((float)Mth.clamp((float)partial, (float)0.0f, (float)1.0f), (float)this.previousImpulse, (float)this.impulse);
        }

        public float time(float partial) {
            return ((float)Math.max(0, this.age - 1) + Math.clamp((float)partial, (float)0.0f, (float)1.0f)) / 20.0f;
        }

        public float remaining(float partial) {
            return this.active ? Math.max(0.0f, (float)this.remaining - Math.clamp((float)partial, (float)0.0f, (float)1.0f)) : 0.0f;
        }

        public boolean active() {
            return this.active;
        }

        public void clear() {
            this.active = false;
            this.previousImpulse = 0.0f;
            this.impulse = 0.0f;
            this.previousStrength = 0.0f;
            this.strength = 0.0f;
            this.kills = 0;
            this.remaining = 0;
            this.age = 0;
        }
    }
}

