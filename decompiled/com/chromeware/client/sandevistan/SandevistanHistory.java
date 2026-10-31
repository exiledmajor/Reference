/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer
 *  dev.ryanhcode.sable.api.sublevel.SubLevelContainer
 *  dev.ryanhcode.sable.sublevel.ClientSubLevel
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.core.Position
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.AfterimageTimeline;
import com.chromeware.client.sandevistan.EchoCache;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.compat.SableCompatibility;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

final class SandevistanHistory {
    static final AfterimageTimeline TIMELINE = new AfterimageTimeline();
    private static Object level;
    private static final AfterimageClock CLOCK;

    SandevistanHistory() {
    }

    static void begin(RenderLevelStageEvent event) {
        ClientSubLevelContainer container;
        ClientLevel world = Minecraft.getInstance().level;
        if (world != level) {
            SandevistanHistory.clear();
            level = world;
        }
        double visualTicks = CLOCK.advance(System.nanoTime(), Minecraft.getInstance().isPaused());
        HashSet<UUID> owners = new HashSet<UUID>();
        for (AbstractClientPlayer player : world.players()) {
            if (!SandevistanClient.isActive((Entity)player)) continue;
            owners.add(player.getUUID());
        }
        ClientSubLevelContainer clientSubLevelContainer = container = SableCompatibility.loaded() ? SubLevelContainer.getContainer((ClientLevel)world) : null;
        if (container != null) {
            for (ClientSubLevel body : container.getAllSubLevels()) {
                UUID owner = SandevistanClient.rigOwner(body.getUniqueId());
                if (!SandevistanClient.afterimages(owner) || body.isRemoved()) continue;
                owners.add(owner);
            }
        }
        for (Entity entity : world.entitiesForRendering()) {
            if (!(entity instanceof AbstractContraptionEntity) || !SandevistanClient.isImmune(entity) || !SandevistanClient.afterimages(SandevistanHistory.owner(entity))) continue;
            owners.add(SandevistanHistory.owner(entity));
        }
        TIMELINE.update(visualTicks, owners);
        EchoCache.beginFrame();
    }

    static UUID owner(Entity entity) {
        if (entity instanceof AbstractContraptionEntity) {
            AbstractContraptionEntity contraption = (AbstractContraptionEntity)entity;
            Optional<Player> owner = contraption.getControllingPlayer().map(arg_0 -> ((Level)entity.level()).getPlayerByUUID(arg_0)).filter(SandevistanClient::isImmune);
            if (owner.isPresent()) {
                return owner.get().getUUID();
            }
        }
        for (Entity passenger : entity.getIndirectPassengers()) {
            if (!SandevistanClient.isActive(passenger)) continue;
            return passenger.getUUID();
        }
        SubLevel body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(entity.level(), (Position)entity.position()) : null;
        return body != null && SandevistanClient.rigOwner(body.getUniqueId()) != null ? SandevistanClient.rigOwner(body.getUniqueId()) : entity.getUUID();
    }

    static void clear() {
        TIMELINE.clear();
        EchoCache.clear();
        level = null;
        CLOCK.reset();
    }

    static {
        CLOCK = new AfterimageClock();
    }

    static final class AfterimageClock {
        private long last;
        private boolean initialized;
        private boolean wasPaused;
        private double ticks;

        AfterimageClock() {
        }

        double advance(long nanos, boolean paused) {
            if (this.initialized && !paused && !this.wasPaused) {
                this.ticks += (double)Math.max(0L, nanos - this.last) / 5.0E7;
            }
            this.initialized = true;
            this.last = nanos;
            this.wasPaused = paused;
            return this.ticks;
        }

        void reset() {
            this.initialized = false;
            this.wasPaused = false;
            this.ticks = 0.0;
        }
    }
}

