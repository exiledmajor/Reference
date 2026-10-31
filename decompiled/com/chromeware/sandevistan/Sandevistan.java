/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.server.network.ServerGamePacketListenerImpl
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$Clone
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.server.ServerStoppedEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Pre
 *  org.joml.Vector3d
 */
package com.chromeware.sandevistan;

import com.chromeware.compat.SableCompatibility;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.CooldownNotice;
import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.cyberware.SandevistanState;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.sandevistan.SandevistanClock;
import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.SandevistanMovementAccess;
import com.chromeware.sandevistan.SandevistanPacketQueue;
import com.chromeware.sandevistan.SandevistanPackets;
import com.chromeware.sandevistan.TimeField;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

@EventBusSubscriber(modid="chromeware")
public final class Sandevistan {
    public static final double RADIUS = 48.0;
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_speed");
    private static final ResourceLocation STEP = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_step");
    private static final Map<UUID, TimeField> FIELDS = new HashMap<UUID, TimeField>();
    private static final Map<UUID, ServerPlayer> OWNERS = new HashMap<UUID, ServerPlayer>();
    private static final Map<UUID, Long> AUTOMATIC_ENDS = new HashMap<UUID, Long>();
    private static long ticks;

    public static long serverTicks() {
        return ticks;
    }

    public static SandevistanState state(Player p) {
        return (SandevistanState)p.getData(PlayerData.SANDEVISTAN_STATE);
    }

    public static void state(Player p, SandevistanState state) {
        if (!state.equals(Sandevistan.state(p))) {
            p.setData(PlayerData.SANDEVISTAN_STATE, (Object)state);
        }
    }

    public static void tickTimer(Player p) {
        ServerPlayer player;
        if (p.level().isClientSide() || !p.hasData(PlayerData.SANDEVISTAN_STATE)) {
            return;
        }
        Sandevistan.state(p, Sandevistan.state(p).tick());
        if (p instanceof ServerPlayer && Sandevistan.isActive((Entity)(player = (ServerPlayer)p)) && Sandevistan.state((Player)player).activeTicks() == 0) {
            Sandevistan.deactivate(player);
        }
    }

    public static boolean isActive(Entity entity) {
        TimeField field = FIELDS.get(entity.getUUID());
        return field != null && field.mode() == 0;
    }

    public static boolean immuneOwner(Entity entity) {
        TimeField field = FIELDS.get(entity.getUUID());
        return field != null && field.ownerImmune();
    }

    public static Collection<ServerPlayer> activePlayers(Level level) {
        return OWNERS.values().stream().filter(p -> p.level() == level && Sandevistan.immuneOwner((Entity)p) && p.isAlive() && !p.isSpectator()).toList();
    }

    public static Collection<TimeField> fields(Level level) {
        return FIELDS.values().stream().filter(f -> f.dimension().equals((Object)level.dimension())).toList();
    }

    public static boolean toggle(ServerPlayer player) {
        if (Sandevistan.isActive((Entity)player)) {
            Sandevistan.deactivate(player);
            return false;
        }
        if (!player.isAlive() || player.isSpectator() || !SandevistanImplant.active((Player)player) || (Netrunning.flags((Entity)player) & 4) != 0) {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.sandevistan_required"), true);
            return false;
        }
        if (Sandevistan.state((Player)player).cooldown() > 0) {
            CooldownNotice.show((Player)player, SandevistanImplant.get((Player)player).item(), Sandevistan.state((Player)player).cooldown());
            return false;
        }
        FIELDS.put(player.getUUID(), Sandevistan.field(player, ticks));
        Sandevistan.state((Player)player, new SandevistanState(ImplantSettings.duration("sandevistan", 200), 0));
        AUTOMATIC_ENDS.remove(player.getUUID());
        OWNERS.put(player.getUUID(), player);
        SandevistanContraptions.refresh(player.level());
        Sandevistan.removeBoosts(player);
        SandevistanPackets.broadcast(player.serverLevel());
        return true;
    }

    private static TimeField field(ServerPlayer player, long origin) {
        Vector3d pos = SandevistanContraptions.worldPosition((Entity)player);
        return new TimeField(player.getUUID(), (ResourceKey<Level>)player.level().dimension(), pos.x, pos.y + (double)player.getBbHeight() * 0.5, pos.z, ImplantSettings.stat("sandevistan", "field_radius", 48.0), ImplantSettings.istat("sandevistan", "slowdown_percent", 90), origin);
    }

    private static TimeField follow(ServerPlayer player, TimeField old) {
        TimeField center = Sandevistan.field(player, old.origin());
        return new TimeField(old.owner(), old.dimension(), center.x(), center.y(), center.z(), old.radius(), old.slowdown(), old.origin(), old.mode());
    }

    public static boolean automatic(ServerPlayer player, boolean revulsor) {
        Implant implant;
        if (FIELDS.containsKey(player.getUUID()) || !player.isAlive() || player.isSpectator()) {
            return false;
        }
        Implant implant2 = implant = revulsor ? Implant.REVULSOR : Implant.REFLEX_TUNER;
        if (!implant.active((Player)player)) {
            return false;
        }
        TimeField center = Sandevistan.field(player, ticks);
        FIELDS.put(player.getUUID(), new TimeField(center.owner(), center.dimension(), center.x(), center.y(), center.z(), ImplantSettings.stat(implant.id, "field_radius", 48.0), ImplantSettings.istat(implant.id, "slowdown_percent", 60), ticks, revulsor ? 2 : 1));
        AUTOMATIC_ENDS.put(player.getUUID(), ticks + (long)ImplantSettings.duration(implant.id, 90));
        OWNERS.put(player.getUUID(), player);
        Sandevistan.removeBoosts(player);
        SandevistanContraptions.refresh(player.level());
        SandevistanPackets.broadcast(player.serverLevel());
        return true;
    }

    public static void deactivate(ServerPlayer player) {
        Sandevistan.deactivate(player, true);
    }

    public static void interrupt(ServerPlayer player) {
        Sandevistan.deactivate(player, false);
    }

    private static void deactivate(ServerPlayer player, boolean refund) {
        ServerLevel level;
        TimeField old = FIELDS.remove(player.getUUID());
        OWNERS.remove(player.getUUID());
        AUTOMATIC_ENDS.remove(player.getUUID());
        SandevistanContraptions.refresh(player.level());
        Sandevistan.removeBoosts(player);
        if (old != null && old.mode() == 0) {
            Sandevistan.state((Player)player, new SandevistanState(0, QuantumTuner.duration((Player)player, ImplantSettings.cooldown("sandevistan", 600))));
            if (refund && player.isAlive() && !player.isSpectator()) {
                ImplantCooldowns.fullyUsed((Player)player, ImplantCooldowns.SANDEVISTAN);
            }
        }
        if (old != null && (level = player.server.getLevel(old.dimension())) != null) {
            SandevistanPackets.broadcast(level);
        }
    }

    public static TimeField clockAt(Level level, double x, double z) {
        TimeField strongest = null;
        for (TimeField f : FIELDS.values()) {
            if (!f.dimension().equals((Object)level.dimension()) || !f.contains(x, z) || strongest != null && f.slowdown() <= strongest.slowdown() && (f.slowdown() != strongest.slowdown() || f.owner().toString().compareTo(strongest.owner().toString()) >= 0)) continue;
            strongest = f;
        }
        return strongest;
    }

    public static boolean shouldAdvance(Entity entity) {
        SubLevel body;
        if (SandevistanContraptions.entityImmune(entity, Sandevistan::immuneOwner)) {
            return true;
        }
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(entity.level(), (Position)entity.position()) : null;
        if (body != null) {
            return Sandevistan.shouldAdvance(body);
        }
        TimeField f = Sandevistan.clockAt(entity.level(), entity.getX(), entity.getZ());
        return f == null || SandevistanClock.shouldAdvance(ticks, f.origin(), f.slowdown());
    }

    public static boolean shouldAdvance(Level level, BlockPos pos) {
        SubLevel body;
        if (SandevistanContraptions.controllerImmune(level, pos)) {
            return true;
        }
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(level, (Vec3i)pos) : null;
        if (body != null) {
            return Sandevistan.shouldAdvance(body);
        }
        TimeField f = Sandevistan.clockAt(level, (double)pos.getX() + 0.5, (double)pos.getZ() + 0.5);
        return f == null || SandevistanClock.shouldAdvance(ticks, f.origin(), f.slowdown());
    }

    public static boolean shouldAdvance(Object body) {
        TimeField f = SandevistanContraptions.clock((SubLevel)body);
        return f == null || SandevistanClock.shouldAdvance(ticks, f.origin(), f.slowdown());
    }

    public static boolean shouldAdvanceChunk(Level level, long packed) {
        SubLevel body;
        ChunkPos chunk = new ChunkPos(packed);
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(level, (Vec3i)new BlockPos(chunk.getMiddleBlockX(), 0, chunk.getMiddleBlockZ())) : null;
        if (body != null) {
            return Sandevistan.shouldAdvance(body);
        }
        TimeField strongest = null;
        for (TimeField f : Sandevistan.fields(level)) {
            if ((double)chunk.x < Math.floor((f.x() - f.radius()) / 16.0) || (double)chunk.x > Math.floor(Math.nextDown(f.x() + f.radius()) / 16.0) || (double)chunk.z < Math.floor((f.z() - f.radius()) / 16.0) || (double)chunk.z > Math.floor(Math.nextDown(f.z() + f.radius()) / 16.0) || strongest != null && f.slowdown() <= strongest.slowdown() && (f.slowdown() != strongest.slowdown() || f.owner().toString().compareTo(strongest.owner().toString()) >= 0)) continue;
            strongest = f;
        }
        return strongest == null || SandevistanClock.shouldAdvance(ticks, strongest.origin(), strongest.slowdown());
    }

    @SubscribeEvent
    public static void pre(ServerTickEvent.Pre event) {
        ++ticks;
        MinecraftServer server = event.getServer();
        HashSet<ServerPlayer> timerPlayers = new HashSet<ServerPlayer>(OWNERS.values());
        for (ServerLevel serverLevel : server.getAllLevels()) {
            timerPlayers.addAll(serverLevel.players());
        }
        for (Player player : timerPlayers) {
            Sandevistan.tickTimer(player);
        }
        for (ServerPlayer serverPlayer : new ArrayList<ServerPlayer>(OWNERS.values())) {
            boolean hardware;
            TimeField old = FIELDS.get(serverPlayer.getUUID());
            boolean bl = old == null || (old.mode() == 0 ? SandevistanImplant.active((Player)serverPlayer) : (old.mode() == 2 ? Implant.REVULSOR : Implant.REFLEX_TUNER).active((Player)serverPlayer)) ? true : (hardware = false);
            if (!(old == null || serverPlayer.isAlive() && !serverPlayer.isSpectator() && hardware && old.dimension().equals((Object)serverPlayer.level().dimension()))) {
                Sandevistan.deactivate(serverPlayer, false);
                continue;
            }
            if (old != null && (old.mode() == 0 && Sandevistan.state((Player)serverPlayer).activeTicks() == 0 || AUTOMATIC_ENDS.getOrDefault(serverPlayer.getUUID(), Long.MAX_VALUE) <= ticks)) {
                Sandevistan.deactivate(serverPlayer);
                continue;
            }
            if (old != null) {
                FIELDS.put(serverPlayer.getUUID(), Sandevistan.follow(serverPlayer, old));
                Sandevistan.removeBoosts(serverPlayer);
                continue;
            }
            Sandevistan.removeBoosts(serverPlayer);
        }
        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            ServerGamePacketListenerImpl serverGamePacketListenerImpl = serverPlayer.connection;
            if (!(serverGamePacketListenerImpl instanceof SandevistanMovementAccess)) continue;
            SandevistanMovementAccess access = (SandevistanMovementAccess)serverGamePacketListenerImpl;
            access.chrome$flushMovement();
        }
        SandevistanPacketQueue.tick(server);
    }

    @SubscribeEvent
    public static void post(ServerTickEvent.Post event) {
        for (ServerPlayer player : new ArrayList<ServerPlayer>(OWNERS.values())) {
            TimeField old = FIELDS.get(player.getUUID());
            if (old == null) continue;
            FIELDS.put(player.getUUID(), Sandevistan.follow(player, old));
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (Sandevistan.fields((Level)level).isEmpty() && ticks % 20L != 0L) continue;
            SandevistanPackets.broadcast(level);
        }
    }

    private static void removeBoosts(ServerPlayer player) {
        AttributeInstance step;
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPEED);
        }
        if ((step = player.getAttribute(Attributes.STEP_HEIGHT)) != null) {
            step.removeModifier(STEP);
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            SandevistanPackets.send(player2);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            Sandevistan.deactivate(player2, false);
            SandevistanPacketQueue.clear(player2.getUUID());
        }
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            Sandevistan.deactivate(player2, false);
            SandevistanPacketQueue.clear(player2.getUUID());
            SandevistanPackets.send(player2);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void killed(LivingDeathEvent event) {
        LivingEntity livingEntity;
        Player killer = EnemyKills.credit(event);
        if (killer != null && Sandevistan.isActive((Entity)killer)) {
            Sandevistan.state(killer, Sandevistan.state(killer).extend());
            killer.heal(killer.getMaxHealth() * ImplantSettings.fstat("sandevistan", "heal_per_kill_fraction", 0.1));
        }
        if ((livingEntity = event.getEntity()) instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            Sandevistan.deactivate(player, false);
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        Player player = event.getEntity();
        SandevistanState saved = Sandevistan.state(player);
        if (saved.activeTicks() > 0) {
            Sandevistan.state(player, new SandevistanState(0, Math.max(saved.cooldown(), ImplantSettings.cooldown("sandevistan", 600))));
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        FIELDS.clear();
        OWNERS.clear();
        AUTOMATIC_ENDS.clear();
        SandevistanContraptions.clear();
        SandevistanPacketQueue.clearAll();
        ticks = 0L;
    }
}

