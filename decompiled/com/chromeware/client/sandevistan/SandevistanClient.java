/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.resources.sounds.AbstractSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance$Attenuation
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Pre
 *  net.neoforged.neoforge.common.NeoForge
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.SandevistanEffects;
import com.chromeware.client.sandevistan.SandevistanKinetics;
import com.chromeware.client.sandevistan.SandevistanRenderTypes;
import com.chromeware.compat.SableCompatibility;
import com.chromeware.mixin.client.LimbAnimatorAccessor;
import com.chromeware.mixin.client.LivingEntityAfterimageAccessor;
import com.chromeware.sandevistan.SandevistanClock;
import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.SandevistanPackets;
import com.chromeware.sandevistan.SandevistanSounds;
import com.chromeware.sandevistan.TimeDilation;
import com.chromeware.sandevistan.TimeField;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class SandevistanClient {
    private static final Map<UUID, Field> FIELDS = new HashMap<UUID, Field>();
    private static final Map<Long, ChunkClock> AFFECTED_CHUNKS = new HashMap<Long, ChunkClock>();
    private static long estimatedServerTick;
    private static final SmoothTime SYNC_CLOCK;
    private static Map<UUID, UUID> RIG_OWNERS;
    private static Map<UUID, UUID> BODY_CLOCKS;
    private static Set<BlockPos> CONTROLLERS;
    private static Map<UUID, Integer> FIELD_MODES;
    private static double globalVisualTime;
    private static final Map<Integer, RenderState> RENDER_STATES;
    private static final Map<Long, SpatialClock> VISUAL_CLOCKS;
    private static final Map<UUID, List<SoundInstance>> TRANSITION_SOUNDS;

    public static UUID rigOwner(UUID body) {
        return RIG_OWNERS.get(body);
    }

    public static boolean afterimages(UUID owner) {
        return owner != null && FIELDS.containsKey(owner) && FIELD_MODES.getOrDefault(owner, 0) == 0;
    }

    static List<TimeField> fieldViews() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? List.of() : FIELDS.values().stream().map(f -> new TimeField(f.ownerUuid, (ResourceKey<Level>)level.dimension(), f.centerX, f.centerY, f.centerZ, f.radius, f.slowdownPercent, f.originTick, FIELD_MODES.getOrDefault(f.ownerUuid, 0))).toList();
    }

    private SandevistanClient() {
    }

    public static void register(IEventBus modBus) {
        SandevistanPackets.clientReceiver = SandevistanClient::receive;
        TimeDilation.client = new TimeDilation.ClientClock(){

            @Override
            public boolean advance(BlockPos position) {
                return SandevistanClient.shouldAdvance((Level)Minecraft.getInstance().level, position);
            }
        };
        modBus.addListener(SandevistanEffects::registerReloads);
        modBus.addListener(SandevistanRenderTypes::registerPaletteShaders);
        NeoForge.EVENT_BUS.addListener(SandevistanClient::preTick);
        NeoForge.EVENT_BUS.addListener(SandevistanClient::postTick);
        NeoForge.EVENT_BUS.addListener(SandevistanEffects::render);
        NeoForge.EVENT_BUS.addListener(SandevistanClient::logout);
        NeoForge.EVENT_BUS.addListener(event -> SandevistanKinetics.frame());
    }

    private static void receive(SandevistanPackets.Snapshot packet) {
        if (!SYNC_CLOCK.receive(packet.tick())) {
            return;
        }
        RIG_OWNERS = packet.rigs();
        BODY_CLOCKS = packet.bodyClocks();
        CONTROLLERS = packet.controllers();
        HashMap modes = new HashMap();
        packet.fields().forEach(f -> modes.put(f.owner(), f.mode()));
        FIELD_MODES = Map.copyOf(modes);
        HashMap<UUID, Field> received = new HashMap<UUID, Field>();
        for (TimeField f2 : packet.fields()) {
            received.put(f2.owner(), new Field(f2.owner(), f2.owner(), f2.x(), f2.y(), f2.z(), f2.radius(), f2.slowdown(), f2.origin(), f2.mode() == 0));
        }
        SandevistanClient.applySnapshot(received);
    }

    private static void preTick(ClientTickEvent.Pre event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.isPaused()) {
            return;
        }
        SYNC_CLOCK.advance();
        estimatedServerTick = SYNC_CLOCK.tick();
        globalVisualTime += 1.0;
        SandevistanClient.rebuildAffectedChunks();
        SandevistanClient.advanceVisualClocks();
    }

    private static void postTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (!client.isPaused()) {
            SandevistanClient.captureRenderStates(client);
        }
    }

    private static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        SandevistanClient.clear();
        SandevistanEffects.clear();
    }

    public static boolean isActive(Entity entity) {
        if (entity == null) {
            return false;
        }
        for (Field field : FIELDS.values()) {
            if (!field.ownerUuid.equals(entity.getUUID()) || FIELD_MODES.getOrDefault(field.ownerUuid, 0) != 0) continue;
            return true;
        }
        return false;
    }

    public static boolean isDilated(Entity entity) {
        return entity != null && !SandevistanClient.isImmune(entity) && SandevistanClient.slowdownPercentAt(entity) > 0;
    }

    public static boolean isImmune(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (SandevistanContraptions.entityImmune(entity, SandevistanClient::immuneOwner)) {
            return true;
        }
        SubLevel body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(entity.level(), (Position)entity.position()) : null;
        return body != null && SandevistanClient.rigOwner(body.getUniqueId()) != null;
    }

    private static boolean immuneOwner(Entity entity) {
        return FIELDS.values().stream().anyMatch(f -> f.ownerUuid.equals(entity.getUUID()) && FIELD_MODES.getOrDefault(f.ownerUuid, 0) != 1);
    }

    public static boolean isLocalInsideSandevistanField() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return false;
        }
        Vector3d pos = SandevistanContraptions.worldPosition((Entity)client.player);
        for (Field field : FIELDS.values()) {
            if (!SandevistanClient.contains(field, pos.x, pos.z, 0.0f)) continue;
            return true;
        }
        return false;
    }

    public static boolean shouldAdvance(Entity entity) {
        if (entity == null || !SandevistanClient.isDilated(entity)) {
            return true;
        }
        ChunkClock clock = SandevistanClient.clockAt(entity);
        return clock == null || SandevistanClock.shouldAdvance(estimatedServerTick, clock.originTick(), clock.slowdownPercent());
    }

    public static boolean shouldAdvance(Level world, BlockPos position) {
        ChunkClock clock = SandevistanClient.clockAt(world, position);
        return clock == null || SandevistanClock.shouldAdvance(estimatedServerTick, clock.originTick(), clock.slowdownPercent());
    }

    public static boolean shouldAdvanceAt(double x, double z) {
        ChunkClock clock = SandevistanClient.clockAt(x, z);
        return clock == null || SandevistanClock.shouldAdvance(estimatedServerTick, clock.originTick(), clock.slowdownPercent());
    }

    public static boolean isDilatedAt(double x, double z) {
        return SandevistanClient.slowdownPercentAt(x, z) > 0;
    }

    public static boolean hasTimeFields() {
        return !FIELDS.isEmpty();
    }

    public static float tickDelta(Entity entity, float vanillaTickDelta) {
        if (!SandevistanClient.isDilated(entity)) {
            return vanillaTickDelta;
        }
        RenderState state = RENDER_STATES.get(entity.getId());
        return state == null ? SandevistanClient.renderProgress(SandevistanClient.clockAt(entity), vanillaTickDelta) : state.phase(globalVisualTime + (double)vanillaTickDelta);
    }

    public static float tickDelta(Level world, BlockPos pos, float vanillaTickDelta) {
        return SandevistanClient.renderProgress(SandevistanClient.clockAt(world, pos), vanillaTickDelta);
    }

    public static float tickDeltaAt(double x, double z, float vanillaTickDelta) {
        if (!SandevistanClient.isDilatedAt(x, z)) {
            return vanillaTickDelta;
        }
        return SandevistanClient.renderProgress(x, z, vanillaTickDelta);
    }

    public static float mouseScale() {
        Minecraft client = Minecraft.getInstance();
        return client.player == null ? 1.0f : 1.0f - (float)SandevistanClient.slowdownPercentAt((Entity)client.player) / 100.0f;
    }

    public static RenderSample renderSample(Entity entity, float vanillaTickDelta) {
        RenderState state = RENDER_STATES.get(entity.getId());
        if (state == null || !SandevistanClient.isDilated(entity)) {
            return SandevistanClient.liveSample(entity, vanillaTickDelta);
        }
        float bodyProgress = SandevistanClient.tickDelta(entity, vanillaTickDelta);
        return state.sample(bodyProgress, globalVisualTime + (double)vanillaTickDelta);
    }

    public static Vec3 renderPosition(Entity entity, float vanillaTickDelta) {
        return SandevistanClient.renderSample(entity, vanillaTickDelta).position();
    }

    private static RenderSample liveSample(Entity entity, float tickDelta) {
        LivingEntity value;
        LivingEntity living = entity instanceof LivingEntity ? (value = (LivingEntity)entity) : null;
        return new RenderSample(entity.getPosition(tickDelta), Mth.rotLerp((float)tickDelta, (float)entity.yRotO, (float)entity.getYRot()), living == null ? Mth.rotLerp((float)tickDelta, (float)entity.yRotO, (float)entity.getYRot()) : Mth.rotLerp((float)tickDelta, (float)living.yBodyRotO, (float)living.yBodyRot), living == null ? Mth.rotLerp((float)tickDelta, (float)entity.yRotO, (float)entity.getYRot()) : Mth.rotLerp((float)tickDelta, (float)living.yHeadRotO, (float)living.yHeadRot), Mth.lerp((float)tickDelta, (float)entity.xRotO, (float)entity.getXRot()), living == null ? Pose.STANDING : living.getPose(), (float)entity.tickCount + tickDelta, living == null ? 0.0f : living.walkAnimation.position(tickDelta), living == null ? 0.0f : living.walkAnimation.speed(tickDelta), living == null ? 0.0f : living.getAttackAnim(tickDelta), living == null ? 0.0f : living.getSwimAmount(tickDelta), living != null && living.swinging, living != null && living.isShiftKeyDown(), living != null && living.isSprinting(), living != null && living.isSwimming(), living != null && living.isUsingItem(), living == null ? InteractionHand.MAIN_HAND : living.getUsedItemHand(), living == null ? 0 : living.getUseItemRemainingTicks(), living == null ? 0.0f : ((LimbAnimatorAccessor)living.walkAnimation).chrome$getPreviousSpeed(), living == null ? 0.0f : ((LivingEntityAfterimageAccessor)living).chrome$getPreviousHandSwingProgress(), living == null ? 0.0f : ((LivingEntityAfterimageAccessor)living).chrome$getPreviousLeaningPitch());
    }

    public static int slowdownPercentAt(double x, double z) {
        ChunkClock clock = SandevistanClient.clockAt(x, z);
        return clock == null ? 0 : clock.slowdownPercent();
    }

    public static int slowdownPercentAt(Level world, BlockPos position) {
        ChunkClock clock = SandevistanClient.clockAt(world, position);
        return clock == null ? 0 : clock.slowdownPercent();
    }

    private static ChunkClock clockAt(Level world, BlockPos position) {
        Field bodyClock;
        SubLevel body;
        if (world == null || position == null || world != Minecraft.getInstance().level || CONTROLLERS.contains(position)) {
            return null;
        }
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(world, (Vec3i)position) : null;
        if (body != null && SandevistanClient.rigOwner(body.getUniqueId()) != null) {
            return null;
        }
        Field field = bodyClock = body == null ? null : FIELDS.get(BODY_CLOCKS.get(body.getUniqueId()));
        if (bodyClock != null) {
            return new ChunkClock(bodyClock.slowdownPercent, bodyClock.originTick, bodyClock.sourceId);
        }
        Vector3d point = SandevistanContraptions.worldPosition(world, position);
        return SandevistanClient.clockAt(point.x, point.z);
    }

    private static ChunkClock clockAt(Entity entity) {
        SubLevel body;
        if (entity == null || SandevistanClient.isImmune(entity)) {
            return null;
        }
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(entity.level(), (Position)entity.position()) : null;
        if (body != null) {
            return SandevistanClient.clockAt(entity.level(), entity.blockPosition());
        }
        return SandevistanClient.clockAt(entity.getX(), entity.getZ());
    }

    public static int slowdownPercentAt(Entity entity) {
        ChunkClock clock = SandevistanClient.clockAt(entity);
        return clock == null ? 0 : clock.slowdownPercent();
    }

    public static float timeScaleAt(double x, double z) {
        return 1.0f - (float)SandevistanClient.slowdownPercentAt(x, z) / 100.0f;
    }

    public static float timeScaleAt(Entity entity) {
        return 1.0f - (float)SandevistanClient.slowdownPercentAt(entity) / 100.0f;
    }

    private static ChunkClock clockAt(double x, double z) {
        ChunkClock strongest = null;
        for (Field field : FIELDS.values()) {
            if (!SandevistanClient.contains(field, x, z, 0.0f)) continue;
            ChunkClock candidate = new ChunkClock(field.slowdownPercent, field.originTick, field.sourceId);
            if (strongest != null && candidate.slowdownPercent() <= strongest.slowdownPercent() && (candidate.slowdownPercent() != strongest.slowdownPercent() || candidate.sourceId().toString().compareTo(strongest.sourceId().toString()) >= 0)) continue;
            strongest = candidate;
        }
        return strongest;
    }

    public static boolean localSandevistan() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return false;
        }
        Field selected = null;
        for (Field field : FIELDS.values()) {
            if (!SandevistanClient.contains(field, client.player.getX(), client.player.getZ(), 0.0f)) continue;
            if (field.ownerUuid.equals(client.player.getUUID())) {
                return field.sandevistan;
            }
            if (selected != null && field.sourceId.toString().compareTo(selected.sourceId.toString()) >= 0) continue;
            selected = field;
        }
        return selected != null && selected.sandevistan;
    }

    public static double visualTimeAt(double x, double z, float tickDelta) {
        long chunk = ChunkPos.asLong((BlockPos)BlockPos.containing((double)x, (double)0.0, (double)z));
        SpatialClock clock = VISUAL_CLOCKS.get(chunk);
        float scale = SandevistanClient.timeScaleAt(x, z);
        return (clock == null ? globalVisualTime : clock.time) + (double)(scale * tickDelta);
    }

    public static double visualTimeAt(Entity entity, float tickDelta) {
        if (entity == null) {
            return globalVisualTime + (double)tickDelta;
        }
        if (SandevistanClient.isImmune(entity)) {
            return globalVisualTime + (double)tickDelta;
        }
        Vector3d world = SandevistanContraptions.worldPosition(entity);
        return SandevistanClient.visualTimeAt(world.x, world.z, tickDelta);
    }

    private static float renderProgress(double x, double z, float vanillaTickDelta) {
        ChunkClock clock = SandevistanClient.clockAt(x, z);
        return SandevistanClient.renderProgress(clock, vanillaTickDelta);
    }

    private static float renderProgress(ChunkClock clock, float vanillaTickDelta) {
        return clock == null ? vanillaTickDelta : SandevistanClock.renderPhase(estimatedServerTick, vanillaTickDelta, clock.originTick(), clock.slowdownPercent());
    }

    private static Vec3 fieldCenter(Field field, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            for (AbstractClientPlayer player : client.level.players()) {
                SubLevel body;
                if (!player.getUUID().equals(field.ownerUuid)) continue;
                Vec3 position = player.getPosition(tickDelta);
                SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(player.level(), (Position)player.position()) : null;
                if (body != null) {
                    Vector3d p = body.logicalPose().transformPosition((Vector3dc)new Vector3d(position.x, position.y, position.z), new Vector3d());
                    position = new Vec3(p.x, p.y, p.z);
                }
                return new Vec3(position.x, position.y + (double)player.getBbHeight() * 0.5, position.z);
            }
        }
        return new Vec3(field.centerX, field.centerY, field.centerZ);
    }

    private static boolean contains(Field field, double x, double z, float tickDelta) {
        Vec3 center = SandevistanClient.fieldCenter(field, tickDelta);
        return TimeField.contains(center.x, center.z, x, z, field.radius);
    }

    private static int chunkCoordinate(double coordinate) {
        return (int)Math.floor(coordinate / 16.0);
    }

    private static void applySnapshot(Map<UUID, Field> received) {
        estimatedServerTick = SYNC_CLOCK.tick();
        HashMap<UUID, Field> previous = new HashMap<UUID, Field>(FIELDS);
        FIELDS.clear();
        FIELDS.putAll(received);
        SandevistanClient.rebuildAffectedChunks();
        SandevistanClient.playFieldTransitionSounds(previous, received);
    }

    private static void playFieldTransitionSounds(Map<UUID, Field> previous, Map<UUID, Field> received) {
        for (Field field : received.values()) {
            if (previous.containsKey(field.sourceId) && previous.get((Object)field.sourceId).sandevistan == field.sandevistan) continue;
            SandevistanClient.stopTransition(field.ownerUuid);
            if (!SandevistanClient.localPlayerInside(field)) continue;
            SandevistanClient.playActivationTransition(field);
        }
        for (Field field : previous.values()) {
            if (received.containsKey(field.sourceId)) continue;
            SandevistanClient.stopTransition(field.ownerUuid);
            if (!SandevistanClient.localPlayerInside(field)) continue;
            SandevistanClient.playDeactivationTransition(field);
        }
    }

    private static boolean localPlayerInside(Field field) {
        Minecraft client = Minecraft.getInstance();
        return client.player != null && SandevistanClient.contains(field, client.player.getX(), client.player.getZ(), 0.0f);
    }

    private static void playActivationTransition(Field field) {
        if (field.sandevistan) {
            SandevistanClient.playTransition(field.ownerUuid, field, new SoundCue(SandevistanSounds.SANDY_ON, 2.0f, 1.0f));
            return;
        }
        SandevistanClient.playTransition(field.ownerUuid, field, new SoundCue(SoundEvents.WARDEN_SONIC_CHARGE, 0.72f, 1.65f), new SoundCue(SoundEvents.BEACON_POWER_SELECT, 0.82f, 1.8f));
    }

    private static void playDeactivationTransition(Field field) {
        if (field.sandevistan) {
            SandevistanClient.playTransition(field.ownerUuid, field, new SoundCue(SandevistanSounds.SANDY_OFF, 2.0f, 1.0f));
            return;
        }
        SandevistanClient.playTransition(field.ownerUuid, field, new SoundCue(SoundEvents.LAVA_EXTINGUISH, 0.62f, 1.25f), new SoundCue(SoundEvents.BEACON_DEACTIVATE, 0.58f, 1.55f));
    }

    private static void playTransition(UUID ownerUuid, Field field, SoundCue ... cues) {
        Minecraft client = Minecraft.getInstance();
        SandevistanClient.stopTransition(ownerUuid);
        Vec3 position = new Vec3(field.centerX, field.centerY, field.centerZ);
        ArrayList<TransitionSound> sounds = new ArrayList<TransitionSound>(cues.length);
        for (SoundCue cue : cues) {
            TransitionSound sound = new TransitionSound(cue.event, position, cue.volume, cue.pitch);
            sounds.add(sound);
            client.getSoundManager().play((SoundInstance)sound);
        }
        TRANSITION_SOUNDS.put(ownerUuid, sounds);
    }

    private static void stopTransition(UUID ownerUuid) {
        List<SoundInstance> previous = TRANSITION_SOUNDS.remove(ownerUuid);
        if (previous != null) {
            for (SoundInstance sound : previous) {
                Minecraft.getInstance().getSoundManager().stop(sound);
            }
        }
    }

    public static boolean isTransitionSound(SoundInstance sound) {
        return sound instanceof TransitionSound;
    }

    private static void rebuildAffectedChunks() {
        AFFECTED_CHUNKS.clear();
        for (Field field : FIELDS.values()) {
            Vec3 center = SandevistanClient.fieldCenter(field, 0.0f);
            double halfSize = field.radius;
            int minimumChunkX = SandevistanClient.chunkCoordinate(center.x - halfSize);
            int maximumChunkX = Math.max(minimumChunkX, SandevistanClient.chunkCoordinate(Math.nextDown(center.x + halfSize)));
            int minimumChunkZ = SandevistanClient.chunkCoordinate(center.z - halfSize);
            int maximumChunkZ = Math.max(minimumChunkZ, SandevistanClient.chunkCoordinate(Math.nextDown(center.z + halfSize)));
            for (int x = minimumChunkX; x <= maximumChunkX; ++x) {
                for (int z = minimumChunkZ; z <= maximumChunkZ; ++z) {
                    long packed = ChunkPos.asLong((int)x, (int)z);
                    ChunkClock candidate = new ChunkClock(field.slowdownPercent, field.originTick, field.sourceId);
                    ChunkClock current = AFFECTED_CHUNKS.get(packed);
                    if (current != null && candidate.slowdownPercent() <= current.slowdownPercent() && (candidate.slowdownPercent() != current.slowdownPercent() || candidate.sourceId().toString().compareTo(current.sourceId().toString()) >= 0)) continue;
                    AFFECTED_CHUNKS.put(packed, candidate);
                }
            }
        }
    }

    public static void clear() {
        SYNC_CLOCK.clear();
        RIG_OWNERS = Map.of();
        BODY_CLOCKS = Map.of();
        CONTROLLERS = Set.of();
        FIELD_MODES = Map.of();
        Minecraft client = Minecraft.getInstance();
        for (List<SoundInstance> sounds : TRANSITION_SOUNDS.values()) {
            for (SoundInstance sound : sounds) {
                client.getSoundManager().stop(sound);
            }
        }
        TRANSITION_SOUNDS.clear();
        FIELDS.clear();
        AFFECTED_CHUNKS.clear();
        estimatedServerTick = 0L;
        globalVisualTime = 0.0;
        RENDER_STATES.clear();
        VISUAL_CLOCKS.clear();
    }

    private static void advanceVisualClocks() {
        for (Map.Entry<Long, SpatialClock> entry2 : VISUAL_CLOCKS.entrySet()) {
            int slowdown = AFFECTED_CHUNKS.containsKey(entry2.getKey()) ? AFFECTED_CHUNKS.get(entry2.getKey()).slowdownPercent() : 0;
            entry2.getValue().time += 1.0 - (double)slowdown / 100.0;
            if (slowdown <= 0) continue;
            entry2.getValue().lastAffectedTick = estimatedServerTick;
        }
        Iterator<Object> iterator = AFFECTED_CHUNKS.keySet().iterator();
        while (iterator.hasNext()) {
            long chunk = (Long)iterator.next();
            VISUAL_CLOCKS.computeIfAbsent(chunk, ignored -> new SpatialClock(globalVisualTime, estimatedServerTick));
        }
        VISUAL_CLOCKS.entrySet().removeIf(entry -> !AFFECTED_CHUNKS.containsKey(entry.getKey()) && estimatedServerTick - ((SpatialClock)entry.getValue()).lastAffectedTick > 600L);
    }

    private static void captureRenderStates(Minecraft client) {
        if (client.level == null) {
            RENDER_STATES.clear();
            return;
        }
        HashSet<Integer> seen = new HashSet<Integer>();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (!SandevistanClient.isDilated(entity)) continue;
            seen.add(entity.getId());
            RenderState state = RENDER_STATES.computeIfAbsent(entity.getId(), ignored -> new RenderState(entity));
            if (SandevistanClient.shouldAdvance(entity)) {
                state.captureBody(entity);
                continue;
            }
            if (SandevistanClient.slowdownPercentAt(entity) >= 100) {
                state.observePosition(entity);
                continue;
            }
            state.observePosition(entity);
        }
        RENDER_STATES.keySet().removeIf(id -> !seen.contains(id));
    }

    static {
        SYNC_CLOCK = new SmoothTime();
        RIG_OWNERS = Map.of();
        BODY_CLOCKS = Map.of();
        CONTROLLERS = Set.of();
        FIELD_MODES = Map.of();
        RENDER_STATES = new HashMap<Integer, RenderState>();
        VISUAL_CLOCKS = new HashMap<Long, SpatialClock>();
        TRANSITION_SOUNDS = new HashMap<UUID, List<SoundInstance>>();
    }

    public static final class SmoothTime {
        private long tick;
        private long snapshot = Long.MIN_VALUE;
        private boolean initialized;

        public long tick() {
            return this.tick;
        }

        public void advance() {
            ++this.tick;
        }

        public boolean receive(long serverTick) {
            if (this.initialized && serverTick < this.snapshot) {
                return false;
            }
            this.snapshot = serverTick;
            this.tick = this.initialized ? Math.max(this.tick, serverTick) : serverTick;
            this.initialized = true;
            return true;
        }

        public void clear() {
            this.tick = 0L;
            this.snapshot = Long.MIN_VALUE;
            this.initialized = false;
        }
    }

    private record Field(UUID sourceId, UUID ownerUuid, double centerX, double centerY, double centerZ, double radius, int slowdownPercent, long originTick, boolean sandevistan) {
    }

    public record ChunkClock(int slowdownPercent, long originTick, UUID sourceId) {
    }

    private static final class RenderState {
        private final InterpolatedPosition smoothPosition;
        private float previousYaw;
        private float currentYaw;
        private float previousBodyYaw;
        private float currentBodyYaw;
        private float previousHeadYaw;
        private float currentHeadYaw;
        private float previousPitch;
        private float currentPitch;
        private Pose currentPose;
        private float currentAnimationAge;
        private float currentLimbPosition;
        private float previousLimbSpeed;
        private float currentLimbSpeed;
        private float previousHandSwingProgress;
        private float currentHandSwingProgress;
        private float previousLeaningPitch;
        private float currentLeaningPitch;
        private boolean currentHandSwinging;
        private boolean currentSneaking;
        private boolean currentSprinting;
        private boolean currentSwimming;
        private boolean currentUsingItem;
        private InteractionHand currentActiveHand;
        private int currentItemUseTimeLeft;
        private double capturedAt = globalVisualTime;
        private double interval = 1.0;

        private RenderState(Entity entity) {
            float yHeadRot;
            float f;
            this.smoothPosition = new InterpolatedPosition(entity.position(), globalVisualTime);
            this.previousYaw = this.currentYaw = entity.getYRot();
            this.previousBodyYaw = this.currentBodyYaw = RenderState.yBodyRot(entity);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                f = living.yHeadRot;
            } else {
                f = entity.getYRot();
            }
            this.previousHeadYaw = this.currentHeadYaw = (yHeadRot = f);
            this.previousPitch = this.currentPitch = entity.getXRot();
            this.currentPose = RenderState.pose(entity);
            this.currentAnimationAge = entity.tickCount;
            this.currentLimbPosition = RenderState.limbPosition(entity);
            this.previousLimbSpeed = this.currentLimbSpeed = RenderState.limbSpeed(entity);
            this.previousHandSwingProgress = this.currentHandSwingProgress = RenderState.attackAnim(entity);
            this.previousLeaningPitch = this.currentLeaningPitch = RenderState.swimAmount(entity);
            this.currentHandSwinging = RenderState.swinging(entity);
            this.currentSneaking = entity.isShiftKeyDown();
            this.currentSprinting = entity.isSprinting();
            this.currentSwimming = entity.isSwimming();
            this.currentUsingItem = RenderState.usingItem(entity);
            this.currentActiveHand = RenderState.activeHand(entity);
            this.currentItemUseTimeLeft = RenderState.useItemRemaining(entity);
        }

        private float phase(double now) {
            return (float)Math.clamp((double)((now - this.capturedAt) / this.interval), (double)0.0, (double)0.999);
        }

        private void captureBody(Entity entity) {
            float f;
            this.smoothPosition.target(entity.position(), globalVisualTime, 1.0 / Math.max(0.01, (double)SandevistanClient.timeScaleAt(entity)));
            float oldPhase = this.phase(globalVisualTime);
            this.previousYaw = Mth.rotLerp((float)oldPhase, (float)this.previousYaw, (float)this.currentYaw);
            this.currentYaw = entity.getYRot();
            this.previousBodyYaw = Mth.rotLerp((float)oldPhase, (float)this.previousBodyYaw, (float)this.currentBodyYaw);
            this.currentBodyYaw = RenderState.yBodyRot(entity);
            this.previousHeadYaw = Mth.rotLerp((float)oldPhase, (float)this.previousHeadYaw, (float)this.currentHeadYaw);
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                f = living.yHeadRot;
            } else {
                f = entity.getYRot();
            }
            this.currentHeadYaw = f;
            this.previousPitch = Mth.lerp((float)oldPhase, (float)this.previousPitch, (float)this.currentPitch);
            this.currentPitch = entity.getXRot();
            this.capturedAt = globalVisualTime;
            this.interval = 1.0 / Math.max(0.01, (double)SandevistanClient.timeScaleAt(entity));
            this.currentPose = RenderState.pose(entity);
            this.currentAnimationAge = entity.tickCount;
            this.currentLimbPosition = RenderState.limbPosition(entity);
            this.previousLimbSpeed = this.currentLimbSpeed;
            this.currentLimbSpeed = RenderState.limbSpeed(entity);
            this.previousHandSwingProgress = this.currentHandSwingProgress;
            this.currentHandSwingProgress = RenderState.attackAnim(entity);
            this.previousLeaningPitch = this.currentLeaningPitch;
            this.currentLeaningPitch = RenderState.swimAmount(entity);
            this.currentHandSwinging = RenderState.swinging(entity);
            this.currentSneaking = entity.isShiftKeyDown();
            this.currentSprinting = entity.isSprinting();
            this.currentSwimming = entity.isSwimming();
            this.currentUsingItem = RenderState.usingItem(entity);
            this.currentActiveHand = RenderState.activeHand(entity);
            this.currentItemUseTimeLeft = RenderState.useItemRemaining(entity);
        }

        private void observePosition(Entity entity) {
            this.smoothPosition.target(entity.position(), globalVisualTime, 1.0 / Math.max(0.01, (double)SandevistanClient.timeScaleAt(entity)));
        }

        private RenderSample sample(float progress, double now) {
            return new RenderSample(this.smoothPosition.sample(now), Mth.rotLerp((float)progress, (float)this.previousYaw, (float)this.currentYaw), Mth.rotLerp((float)progress, (float)this.previousBodyYaw, (float)this.currentBodyYaw), Mth.rotLerp((float)progress, (float)this.previousHeadYaw, (float)this.currentHeadYaw), Mth.lerp((float)progress, (float)this.previousPitch, (float)this.currentPitch), this.currentPose, this.currentAnimationAge, this.currentLimbPosition, this.currentLimbSpeed, this.currentHandSwingProgress, this.currentLeaningPitch, this.currentHandSwinging, this.currentSneaking, this.currentSprinting, this.currentSwimming, this.currentUsingItem, this.currentActiveHand, this.currentItemUseTimeLeft, this.previousLimbSpeed, this.previousHandSwingProgress, this.previousLeaningPitch);
        }

        private static float yBodyRot(Entity entity) {
            float f;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                f = living.yBodyRot;
            } else {
                f = entity.getYRot();
            }
            return f;
        }

        private static Pose pose(Entity entity) {
            Pose pose;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                pose = living.getPose();
            } else {
                pose = Pose.STANDING;
            }
            return pose;
        }

        private static float limbPosition(Entity entity) {
            float f;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                f = ((LimbAnimatorAccessor)living.walkAnimation).chrome$getPosition();
            } else {
                f = 0.0f;
            }
            return f;
        }

        private static float limbSpeed(Entity entity) {
            float f;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                f = ((LimbAnimatorAccessor)living.walkAnimation).chrome$speed();
            } else {
                f = 0.0f;
            }
            return f;
        }

        private static float attackAnim(Entity entity) {
            float f;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                f = ((LivingEntityAfterimageAccessor)living).chrome$getAttackAnim();
            } else {
                f = 0.0f;
            }
            return f;
        }

        private static float swimAmount(Entity entity) {
            float f;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                f = ((LivingEntityAfterimageAccessor)living).chrome$getSwimAmount();
            } else {
                f = 0.0f;
            }
            return f;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        private static boolean swinging(Entity entity) {
            if (!(entity instanceof LivingEntity)) return false;
            LivingEntity living = (LivingEntity)entity;
            if (!living.swinging) return false;
            return true;
        }

        private static boolean usingItem(Entity entity) {
            LivingEntity living;
            return entity instanceof LivingEntity && (living = (LivingEntity)entity).isUsingItem();
        }

        private static InteractionHand activeHand(Entity entity) {
            InteractionHand interactionHand;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                interactionHand = living.getUsedItemHand();
            } else {
                interactionHand = InteractionHand.MAIN_HAND;
            }
            return interactionHand;
        }

        private static int useItemRemaining(Entity entity) {
            int n;
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)entity;
                n = living.getUseItemRemainingTicks();
            } else {
                n = 0;
            }
            return n;
        }
    }

    public record RenderSample(Vec3 position, float yaw, float yBodyRot, float yHeadRot, float pitch, Pose pose, float animationAge, float limbPosition, float limbSpeed, float attackAnim, float swimAmount, boolean swinging, boolean sneaking, boolean sprinting, boolean swimming, boolean usingItem, InteractionHand activeHand, int useItemRemaining, float previousLimbSpeed, float previousHandSwingProgress, float previousLeaningPitch) {
    }

    private static final class SpatialClock {
        private double time;
        private long lastAffectedTick;

        private SpatialClock(double visualTime, long serverTick) {
            this.time = visualTime;
            this.lastAffectedTick = serverTick;
        }
    }

    private record SoundCue(SoundEvent event, float volume, float pitch) {
    }

    private static final class TransitionSound
    extends AbstractSoundInstance {
        private TransitionSound(SoundEvent event, Vec3 position, float volume, float pitch) {
            super(event, SoundSource.PLAYERS, RandomSource.create());
            this.volume = volume;
            this.pitch = pitch;
            this.x = position.x;
            this.y = position.y;
            this.z = position.z;
            this.attenuation = SoundInstance.Attenuation.LINEAR;
            this.relative = false;
            this.looping = false;
        }
    }

    public static final class InterpolatedPosition {
        private Vec3 from;
        private Vec3 to;
        private double start;
        private double duration = 1.0;

        public InterpolatedPosition(Vec3 initial, double now) {
            this.from = this.to = initial;
            this.start = now;
        }

        public Vec3 sample(double now) {
            return this.from.lerp(this.to, Math.clamp((double)((now - this.start) / this.duration), (double)0.0, (double)1.0));
        }

        public void target(Vec3 next, double now, double interval) {
            if (next.distanceToSqr(this.to) < 1.0E-12) {
                return;
            }
            this.from = next.distanceToSqr(this.to) > 4096.0 ? next : this.sample(now);
            this.to = next;
            this.start = now;
            this.duration = Math.max(1.0, interval);
        }
    }
}

