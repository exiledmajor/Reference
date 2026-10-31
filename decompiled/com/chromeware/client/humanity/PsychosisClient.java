/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Util
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.EffectInstance
 *  net.minecraft.client.renderer.PostChain
 *  net.minecraft.client.renderer.PostPass
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.server.packs.resources.ResourceProvider
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.common.NeoForge
 *  org.lwjgl.glfw.GLFW
 */
package com.chromeware.client.humanity;

import com.chromeware.Chromeware;
import com.chromeware.config.ChromewareConfig;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.humanity.BerserkReach;
import com.chromeware.humanity.HumanityPackets;
import com.chromeware.humanity.HumanityRules;
import com.chromeware.humanity.HumanitySounds;
import com.chromeware.mixin.client.SandevistanPostChainAccessor;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

public final class PsychosisClient {
    private static PostChain chain;
    private static int width;
    private static int height;
    private static long retryAt;
    private static long startedAt;
    private static long endsAt;
    private static float strength;
    private static float heavy;
    private static float seed;
    private static float organic;

    public static void register(IEventBus modBus) {
        HumanityPackets.clientReceiver = PsychosisClient::receive;
        modBus.addListener(event -> event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)manager -> PsychosisClient.close())));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, event -> PsychosisClient.render(event.getGuiGraphics(), event.getPartialTick()));
        NeoForge.EVENT_BUS.addListener(event -> {
            if (ChromewareConfig.humanityEnabled()) {
                PsychosisBerserk.tick();
            } else {
                endsAt = 0L;
                PsychosisBerserk.release();
            }
        });
        NeoForge.EVENT_BUS.addListener(event -> {
            endsAt = 0L;
            PsychosisBerserk.release();
        });
    }

    static void receive(HumanityPackets.Episode episode) {
        if (episode.kind() == 2) {
            endsAt = 0L;
            strength = 0.0f;
            PsychosisBerserk.release();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !ChromewareConfig.humanityEnabled()) {
            return;
        }
        boolean threshold = episode.kind() == 0;
        float intensity = Math.clamp((float)episode.intensity(), (float)0.0f, (float)1.0f);
        long now = Util.getMillis();
        float f = organic = ImplantEffects.hasOptics((Player)mc.player) ? 0.0f : 1.0f;
        int ticks = organic > 0.0f ? (threshold ? 30 : Math.round(14.0f - 5.0f * intensity)) : (threshold ? 20 : Math.round(8.0f - 3.0f * intensity));
        startedAt = now;
        endsAt = now + (long)ticks * 50L;
        strength = threshold ? 1.0f : 0.45f + 0.55f * intensity;
        heavy = threshold ? 1.0f : 0.0f;
        seed = mc.player.getRandom().nextFloat() * 100.0f;
        SoundEvent sound = organic > 0.0f ? (threshold ? HumanitySounds.HEARTBEAT_THRESHOLD : HumanitySounds.HEARTBEAT) : (threshold ? HumanitySounds.GLITCH_THRESHOLD : HumanitySounds.GLITCH);
        mc.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)sound, (float)1.0f, (float)(threshold ? 1.0f : HumanityRules.episodeVolume(60.0 + (double)intensity * 40.0))));
        if (episode.berserk()) {
            PsychosisBerserk.trigger();
        }
    }

    public static float strength() {
        return PsychosisClient.envelope(Util.getMillis());
    }

    static float envelope(long now) {
        if (now >= endsAt) {
            return 0.0f;
        }
        float t = (float)(now - startedAt) / (float)Math.max(1L, endsAt - startedAt);
        if (organic > 0.0f) {
            return (float)Math.sin(Math.PI * (double)t) * strength;
        }
        float shape = t < 0.15f ? t / 0.15f : (t > 0.7f ? (1.0f - t) / 0.3f : 1.0f);
        boolean stutter = (now / 40L * 31L + (long)seed) % 7L == 0L;
        return Math.clamp((float)(shape * (stutter ? 0.35f : 1.0f)), (float)0.0f, (float)1.0f) * strength;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        long now = Util.getMillis();
        float s = PsychosisClient.envelope(now);
        Minecraft mc = Minecraft.getInstance();
        if (!ChromewareConfig.humanityEnabled() || !ChromewareConfig.psychosisVfx() || s <= 0.0f || mc.level == null) {
            return;
        }
        graphics.flush();
        try {
            PsychosisClient.ensure(mc, now);
            if (chain == null) {
                PsychosisClient.fallback(graphics, s, now);
                return;
            }
            EffectInstance effect = ((PostPass)((SandevistanPostChainAccessor)chain).chrome$passes().getFirst()).getEffect();
            effect.safeGetUniform("Time").set((float)(now - startedAt) / 1000.0f);
            effect.safeGetUniform("Strength").set(s);
            effect.safeGetUniform("Seed").set(seed);
            effect.safeGetUniform("Heavy").set(heavy);
            effect.safeGetUniform("Organic").set(organic);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            chain.process(delta.getGameTimeDeltaPartialTick(false));
        }
        catch (Exception failure) {
            Chromeware.LOGGER.error("Cyberpsychosis glitch failed; using the plain overlay", (Throwable)failure);
            PsychosisClient.close();
            retryAt = now + 5000L;
        }
        finally {
            mc.getMainRenderTarget().bindWrite(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    private static void fallback(GuiGraphics graphics, float s, long now) {
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        if (organic > 0.0f) {
            int band = Math.max(1, Math.min(w, h) / 24);
            for (int i = 0; i < 6; ++i) {
                int a = i * band;
                int b = a + band;
                int color = Math.max(0, (int)(s * (float)(150 - i * 25))) << 24 | 0x2A0000;
                graphics.fill(a, a, w - a, b, color);
                graphics.fill(a, h - b, w - a, h - a, color);
                graphics.fill(a, b, b, h - b, color);
                graphics.fill(w - b, b, w - a, h - b, color);
            }
            return;
        }
        graphics.fill(0, 0, w, h, (int)(60.0f * s) << 24 | 0xB01020);
        Random random = new Random(now / 50L + (long)seed);
        int i = 0;
        while ((float)i < 6.0f + 10.0f * s) {
            int y = random.nextInt(h);
            int height = 1 + random.nextInt(4);
            int x = random.nextInt(w / 2);
            graphics.fill(x, y, x + w / 3 + random.nextInt(w / 2), y + height, (int)(160.0f * s) << 24 | (random.nextBoolean() ? 16721984 : 0x30F0FF));
            ++i;
        }
    }

    private static void ensure(Minecraft mc, long now) throws IOException {
        if (chain == null && now >= retryAt) {
            chain = new PostChain(mc.getTextureManager(), (ResourceProvider)mc.getResourceManager(), mc.getMainRenderTarget(), ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"shaders/post/psychosis.json"));
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
    }

    public static final class PsychosisBerserk {
        private static final int HOLD_TICKS = 3;
        private static KeyMapping held;
        private static int releaseIn;

        static void trigger() {
            double use;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer p = mc.player;
            if (p == null || mc.screen != null || mc.gameMode == null || !p.isAlive() || p.isSpectator() || held != null) {
                return;
            }
            double attack = BerserkReach.reach((Player)p, true);
            Entity target = PsychosisBerserk.target((Player)p, Math.max(attack, use = BerserkReach.reach((Player)p, false)));
            if (target == null) {
                return;
            }
            double distance = Math.sqrt(target.getBoundingBox().distanceToSqr(p.getEyePosition()));
            if (distance <= attack) {
                PsychosisBerserk.press(mc.options.keyAttack);
            } else if (distance <= use) {
                PsychosisBerserk.press(mc.options.keyUse);
            }
        }

        static Entity target(Player p, double reach) {
            if (reach <= 0.0) {
                return null;
            }
            Vec3 eye = p.getEyePosition();
            Vec3 look = p.getViewVector(1.0f);
            BlockHitResult blocked = p.level().clip(new ClipContext(eye, eye.add(look.scale(reach)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
            double max = blocked.getType() == HitResult.Type.MISS ? reach : blocked.getLocation().distanceTo(eye);
            Vec3 end = eye.add(look.scale(max));
            EntityHitResult hit = ProjectileUtil.getEntityHitResult((Entity)p, (Vec3)eye, (Vec3)end, (AABB)p.getBoundingBox().expandTowards(look.scale(max)).inflate(1.0), e -> {
                LivingEntity living;
                return !e.isSpectator() && e.isPickable() && e instanceof LivingEntity && (living = (LivingEntity)e).isAlive() && e != p.getVehicle();
            }, (double)(max * max));
            return hit == null ? null : hit.getEntity();
        }

        private static void press(KeyMapping mapping) {
            KeyMapping.click((InputConstants.Key)mapping.getKey());
            KeyMapping.set((InputConstants.Key)mapping.getKey(), (boolean)true);
            held = mapping;
            releaseIn = 3;
        }

        static void tick() {
            if (held != null && --releaseIn <= 0) {
                PsychosisBerserk.release();
            }
        }

        static void release() {
            if (held == null) {
                return;
            }
            InputConstants.Key key = held.getKey();
            long window = Minecraft.getInstance().getWindow().getWindow();
            boolean down = key.getType() == InputConstants.Type.MOUSE ? GLFW.glfwGetMouseButton((long)window, (int)key.getValue()) == 1 : key.getType() == InputConstants.Type.KEYSYM && InputConstants.isKeyDown((long)window, (int)key.getValue());
            KeyMapping.set((InputConstants.Key)key, (boolean)down);
            held = null;
        }
    }
}

