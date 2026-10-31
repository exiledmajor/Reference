/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer
 *  dev.ryanhcode.sable.api.sublevel.SubLevelContainer
 *  dev.ryanhcode.sable.companion.math.BoundingBox3d
 *  dev.ryanhcode.sable.companion.math.BoundingBox3dc
 *  dev.ryanhcode.sable.companion.math.BoundingBox3ic
 *  dev.ryanhcode.sable.companion.math.Pose3d
 *  dev.ryanhcode.sable.companion.math.Pose3dc
 *  dev.ryanhcode.sable.sublevel.ClientSubLevel
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderStateShard$LineStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent$Stage
 */
package com.chromeware.client.cyberware;

import com.chromeware.compat.SableCompatibility;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.KiroshiOptics;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid="chromeware", value={Dist.CLIENT})
public final class KiroshiHighlights {
    private static final Map<UUID, Long> ENTITIES = new HashMap<UUID, Long>();
    private static final Map<BlockPos, Long> TRAPS = new HashMap<BlockPos, Long>();
    private static final Map<BlockPos, Long> DEVICES = new HashMap<BlockPos, Long>();
    private static Object level;

    public static boolean highlighted(Entity entity) {
        Long until = ENTITIES.get(entity.getUUID());
        return until != null && until > entity.level().getGameTime();
    }

    public static Set<BlockPos> revealed() {
        HashSet<BlockPos> all = new HashSet<BlockPos>(TRAPS.keySet());
        all.addAll(DEVICES.keySet());
        return all;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (level != mc.level) {
            level = mc.level;
            KiroshiHighlights.clear();
        }
        if (mc.level == null || mc.player == null) {
            return;
        }
        LocalPlayer p = mc.player;
        ClientLevel world = mc.level;
        long time = world.getGameTime();
        ENTITIES.values().removeIf(until -> until <= time);
        TRAPS.entrySet().removeIf(e -> (Long)e.getValue() <= time || !KiroshiOptics.trap((Level)world, world.getBlockState((BlockPos)e.getKey())));
        DEVICES.entrySet().removeIf(e -> (Long)e.getValue() <= time || !KiroshiOptics.device(world.getBlockState((BlockPos)e.getKey())));
        Implant optic = KiroshiOptics.installed((Player)p);
        if (optic == null) {
            KiroshiHighlights.clear();
            return;
        }
        if (!ImplantEffects.scanning((Player)p)) {
            return;
        }
        long until2 = time + (long)ImplantSettings.ticks(optic.id, "after_scan_seconds", 1200);
        double enemyRange = ImplantSettings.stat("clairvoyant", "enemy_range", 20.0);
        double stalkerRange = ImplantSettings.stat("stalker", "horizontal_range", 20.0);
        double stalkerDepth = ImplantSettings.stat("stalker", "vertical_range", 50.0);
        double trapRange = ImplantSettings.stat("doomsayer", "trap_range", 30.0);
        double deviceRange = ImplantSettings.stat("sentry", "device_range", 40.0);
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        if (KiroshiOptics.enemies((Player)p)) {
            for (LivingEntity e2 : world.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(enemyRange), e -> KiroshiOptics.clairvoyant((Player)p, e))) {
                ENTITIES.put(e2.getUUID(), until2);
            }
        }
        if (KiroshiOptics.stalker((Player)p)) {
            for (LivingEntity e2 : world.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(stalkerRange, stalkerDepth, stalkerRange), e -> KiroshiOptics.stalked((Player)p, e))) {
                ENTITIES.put(e2.getUUID(), until2);
            }
        }
        if (KiroshiOptics.traps((Player)p) && time % 10L == 0L) {
            for (LivingEntity e2 : world.getEntities((Entity)p, p.getBoundingBox().inflate(trapRange), e -> KiroshiOptics.dangerous(e, eye, look))) {
                ENTITIES.put(e2.getUUID(), until2);
            }
            KiroshiHighlights.scan(world, eye, trapRange, state -> KiroshiOptics.trap((Level)world, state), center -> KiroshiOptics.nearCrosshair(eye, look, center), TRAPS, until2);
        }
        if (KiroshiOptics.devices((Player)p) && time % 20L == 5L) {
            KiroshiHighlights.scan(world, eye, deviceRange, KiroshiOptics::device, center -> center.distanceToSqr(eye) <= deviceRange * deviceRange, DEVICES, until2);
        }
    }

    private static void clear() {
        ENTITIES.clear();
        TRAPS.clear();
        DEVICES.clear();
    }

    private static void scan(ClientLevel world, Vec3 eye, double range, Predicate<BlockState> match, Predicate<Vec3> where, Map<BlockPos, Long> found, long until) {
        int r = (int)Math.ceil(range);
        BlockPos center = BlockPos.containing((Position)eye);
        KiroshiHighlights.blocks(world, center.offset(-r, -r, -r), center.offset(r, r, r), match, Vec3::atCenterOf, where, found, until);
        if (!SableCompatibility.loaded()) {
            return;
        }
        SableBlocks.scan(world, eye, range, match, where, found, until);
    }

    private static void blocks(ClientLevel world, BlockPos min, BlockPos max, Predicate<BlockState> match, Function<BlockPos, Vec3> toWorld, Predicate<Vec3> where, Map<BlockPos, Long> found, long until) {
        int minY = Math.max(world.getMinSection(), min.getY() >> 4);
        int maxY = Math.min(world.getMaxSection() - 1, max.getY() >> 4);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = min.getX() >> 4; cx <= max.getX() >> 4; ++cx) {
            for (int cz = min.getZ() >> 4; cz <= max.getZ() >> 4; ++cz) {
                LevelChunk chunk = KiroshiHighlights.chunk(world, cx, cz);
                if (chunk == null) continue;
                for (int sy = minY; sy <= maxY; ++sy) {
                    LevelChunkSection section = chunk.getSection(world.getSectionIndexFromSectionY(sy));
                    if (section.hasOnlyAir() || !section.maybeHas(match)) continue;
                    for (int y = 0; y < 16; ++y) {
                        for (int z = 0; z < 16; ++z) {
                            for (int x = 0; x < 16; ++x) {
                                if (!match.test(section.getBlockState(x, y, z))) continue;
                                pos.set((cx << 4) + x, (sy << 4) + y, (cz << 4) + z);
                                if (!where.test(toWorld.apply((BlockPos)pos))) continue;
                                found.put(pos.immutable(), until);
                            }
                        }
                    }
                }
            }
        }
    }

    private static LevelChunk chunk(ClientLevel world, int cx, int cz) {
        if (world.hasChunk(cx, cz)) {
            return world.getChunk(cx, cz);
        }
        return SableCompatibility.loaded() ? SableBlocks.chunk(world, cx, cz) : null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || TRAPS.isEmpty() && DEVICES.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack poses = event.getPoseStack();
        poses.pushPose();
        poses.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffer.getBuffer(Xray.LINES);
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for (BlockPos pos : DEVICES.keySet()) {
            KiroshiHighlights.box(poses, lines, (Level)mc.level, pos, 0.003, 0.3f, 0.85f, 1.0f, 1.0f, partial);
        }
        for (BlockPos pos : TRAPS.keySet()) {
            KiroshiHighlights.box(poses, lines, (Level)mc.level, pos, 0.005, 1.0f, 0.65f, 0.1f, 1.0f, partial);
        }
        RenderSystem.disableDepthTest();
        buffer.endBatch(Xray.LINES);
        RenderSystem.enableDepthTest();
        poses.popPose();
    }

    public static void box(PoseStack poses, VertexConsumer lines, Level level, BlockPos pos, double grow, float r, float g, float b, float a, float partial) {
        if (!SableCompatibility.loaded()) {
            LevelRenderer.renderLineBox((PoseStack)poses, (VertexConsumer)lines, (AABB)new AABB(pos).inflate(grow), (float)r, (float)g, (float)b, (float)a);
            return;
        }
        SableBlocks.box(poses, lines, level, pos, grow, r, g, b, a, partial);
    }

    private static final class SableBlocks {
        private SableBlocks() {
        }

        private static void scan(ClientLevel world, Vec3 eye, double range, Predicate<BlockState> match, Predicate<Vec3> where, Map<BlockPos, Long> found, long until) {
            BoundingBox3d area = new BoundingBox3d(eye.x - range, eye.y - range, eye.z - range, eye.x + range, eye.y + range, eye.z + range);
            for (SubLevel ship : Sable.HELPER.getAllIntersecting((Level)world, (BoundingBox3dc)area)) {
                BoundingBox3ic plot = ship.getPlot() == null ? null : ship.getPlot().getBoundingBox();
                if (plot == null || ship.isRemoved()) continue;
                Pose3d pose = ship.logicalPose();
                KiroshiHighlights.blocks(world, new BlockPos(plot.minX(), plot.minY(), plot.minZ()), new BlockPos(plot.maxX(), plot.maxY(), plot.maxZ()), match, pos -> pose.transformPosition(Vec3.atCenterOf((Vec3i)pos)), where, found, until);
            }
        }

        private static LevelChunk chunk(ClientLevel world, int cx, int cz) {
            ClientSubLevelContainer container = SubLevelContainer.getContainer((ClientLevel)world);
            return container == null ? null : container.getChunk(new ChunkPos(cx, cz));
        }

        private static void box(PoseStack poses, VertexConsumer lines, Level level, BlockPos pos, double grow, float r, float g, float b, float a, float partial) {
            AABB aabb = new AABB(pos).inflate(grow);
            SubLevel subLevel = Sable.HELPER.getContaining(level, (Vec3i)pos);
            if (!(subLevel instanceof ClientSubLevel)) {
                LevelRenderer.renderLineBox((PoseStack)poses, (VertexConsumer)lines, (AABB)aabb, (float)r, (float)g, (float)b, (float)a);
                return;
            }
            ClientSubLevel ship = (ClientSubLevel)subLevel;
            Pose3dc pose = ship.renderPose(partial);
            Vec3[] corner = new Vec3[8];
            for (int i = 0; i < 8; ++i) {
                corner[i] = pose.transformPosition(new Vec3((i & 1) == 0 ? aabb.minX : aabb.maxX, (i & 2) == 0 ? aabb.minY : aabb.maxY, (i & 4) == 0 ? aabb.minZ : aabb.maxZ));
            }
            PoseStack.Pose last = poses.last();
            for (int i = 0; i < 8; ++i) {
                for (int bit = 1; bit < 8; bit <<= 1) {
                    if ((i & bit) != 0) continue;
                    Vec3 from = corner[i];
                    Vec3 to = corner[i | bit];
                    Vec3 n = to.subtract(from).normalize();
                    lines.addVertex(last, (float)from.x, (float)from.y, (float)from.z).setColor(r, g, b, a).setNormal(last, (float)n.x, (float)n.y, (float)n.z);
                    lines.addVertex(last, (float)to.x, (float)to.y, (float)to.z).setColor(r, g, b, a).setNormal(last, (float)n.x, (float)n.y, (float)n.z);
                }
            }
        }
    }

    private static final class Xray
    extends RenderType {
        static final RenderType LINES = Xray.create((String)"chromeware_optics_xray_lines", (VertexFormat)DefaultVertexFormat.POSITION_COLOR_NORMAL, (VertexFormat.Mode)VertexFormat.Mode.LINES, (int)1536, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.builder().setShaderState(RENDERTYPE_LINES_SHADER).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).setLayeringState(VIEW_OFFSET_Z_LAYERING).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setOutputState(ITEM_ENTITY_TARGET).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(NO_DEPTH_TEST).createCompositeState(false));

        private Xray(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling, boolean sort, Runnable setup, Runnable clear) {
            super(name, format, mode, size, crumbling, sort, setup, clear);
        }
    }
}

