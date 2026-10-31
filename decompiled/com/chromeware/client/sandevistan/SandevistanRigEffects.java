/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.ByteBufferBuilder
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.simibubi.create.api.behaviour.movement.MovementBehaviour
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  com.simibubi.create.content.contraptions.Contraption
 *  com.simibubi.create.content.contraptions.behaviour.MovementContext
 *  com.simibubi.create.content.contraptions.render.ClientContraption
 *  com.simibubi.create.content.contraptions.render.ContraptionMatrices
 *  com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer
 *  dev.ryanhcode.sable.api.sublevel.SubLevelContainer
 *  dev.ryanhcode.sable.companion.math.Pose3dc
 *  dev.ryanhcode.sable.sublevel.ClientSubLevel
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot
 *  dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.blockentity.BlockEntityRenderer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.BlockAndTintGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateHolder
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.level.chunk.LevelChunkSection
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate$StructureBlockInfo
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.client.event.RenderLevelStageEvent
 *  net.neoforged.neoforge.client.model.data.ModelData
 *  org.apache.commons.lang3.tuple.MutablePair
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 *  org.joml.Vector3f
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.AfterimageTimeline;
import com.chromeware.client.sandevistan.ContraptionMesh;
import com.chromeware.client.sandevistan.EchoCache;
import com.chromeware.client.sandevistan.SandevistanClient;
import com.chromeware.client.sandevistan.SandevistanHistory;
import com.chromeware.client.sandevistan.SandevistanRenderTypes;
import com.chromeware.compat.SableCompatibility;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ClientContraption;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.plot.ClientLevelPlot;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.apache.commons.lang3.tuple.MutablePair;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;

public final class SandevistanRigEffects {
    private static final Map<UUID, Trail> TRAILS = new HashMap<UUID, Trail>();
    private static final Map<UUID, StaticMesh> MESHES = new HashMap<UUID, StaticMesh>();
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediate((ByteBufferBuilder)new ByteBufferBuilder(4096));
    private static final Set<UUID> DIRTY = new HashSet<UUID>();
    private static final List<LiveRig> CURRENT = new ArrayList<LiveRig>();
    private static boolean capturing;
    private static Object level;

    public static boolean capturing() {
        return capturing;
    }

    public static boolean hasTrails() {
        return !TRAILS.isEmpty();
    }

    public static void clear() {
        TRAILS.clear();
        MESHES.clear();
        CURRENT.clear();
        DIRTY.clear();
        level = null;
    }

    public static void invalidate(BlockPos pos) {
        SubLevel body;
        if (MESHES.isEmpty()) {
            return;
        }
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining((Level)world, (Vec3i)pos) : null;
        if (body != null) {
            DIRTY.add(body.getUniqueId());
        }
    }

    public static void prepare(RenderLevelStageEvent event, boolean exemption) {
        Trail trail;
        Placement placement;
        ClientSubLevelContainer container;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        if (level != mc.level) {
            SandevistanRigEffects.clear();
            level = mc.level;
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        CURRENT.clear();
        HashSet<UUID> active = new HashSet<UUID>();
        HashSet<UUID> existing = new HashSet<UUID>();
        ClientSubLevelContainer clientSubLevelContainer = container = SableCompatibility.loaded() ? SubLevelContainer.getContainer((ClientLevel)mc.level) : null;
        if (container != null) {
            for (ClientSubLevel body : container.getAllSubLevels()) {
                UUID id2 = body.getUniqueId();
                existing.add(id2);
                UUID owner = SandevistanClient.rigOwner(body.getUniqueId());
                if (!SandevistanClient.afterimages(owner) || body.isRemoved()) continue;
                active.add(id2);
                Pose3dc pose = body.renderPose(partial);
                placement = new Placement(new Vec3(pose.position().x(), pose.position().y(), pose.position().z()), new Matrix4f().rotate((Quaternionfc)new Quaternionf(pose.orientation())).scale((float)pose.scale().x(), (float)pose.scale().y(), (float)pose.scale().z()));
                trail = TRAILS.computeIfAbsent(id2, ignored -> new Trail());
                StaticMesh cached = MESHES.get(id2);
                boolean sample = trail.wantsSample(owner, placement, cached == null ? 1.0 : cached.mesh.radius());
                if (!exemption && !sample) continue;
                StaticMesh fixed = SandevistanRigEffects.sableStatic(body, new Vector3d(pose.rotationPoint()));
                if (sample) {
                    ContraptionMesh.Builder dynamic = new ContraptionMesh.Builder();
                    SandevistanRigEffects.capture(() -> SandevistanRigEffects.sableDynamic(fixed, partial, new PoseStack(), dynamic));
                    trail.sample(owner, placement, new Shape(fixed.mesh, dynamic.build()));
                }
                if (!exemption) continue;
                CURRENT.add(new LiveRig(placement, fixed, true, null));
            }
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            AbstractContraptionEntity contraption;
            if (!(entity instanceof AbstractContraptionEntity) || (contraption = (AbstractContraptionEntity)entity).getContraption() == null) continue;
            UUID id3 = entity.getUUID();
            existing.add(id3);
            if (!SandevistanClient.isImmune(entity) || !SandevistanClient.afterimages(SandevistanHistory.owner(entity))) continue;
            active.add(id3);
            PoseStack stack = new PoseStack();
            contraption.applyLocalTransforms(stack, partial);
            placement = new Placement(entity.getPosition(partial), new Matrix4f((Matrix4fc)stack.last().pose()));
            trail = TRAILS.computeIfAbsent(id3, ignored -> new Trail());
            UUID owner = SandevistanHistory.owner(entity);
            StaticMesh cached = MESHES.get(id3);
            boolean sample = trail.wantsSample(owner, placement, cached == null ? 1.0 : cached.mesh.radius());
            if (!exemption && !sample) continue;
            StaticMesh fixed = SandevistanRigEffects.createStatic(contraption);
            if (sample) {
                ContraptionMesh.Builder dynamic = new ContraptionMesh.Builder();
                SandevistanRigEffects.capture(() -> SandevistanRigEffects.createDynamic(contraption, placement, partial, new PoseStack(), dynamic));
                trail.sample(owner, placement, new Shape(fixed.mesh, dynamic.build()));
            }
            if (!exemption) continue;
            CURRENT.add(new LiveRig(placement, fixed, false, contraption));
        }
        TRAILS.values().forEach(t -> t.echoes.removeIf(e -> !e.frame.alive()));
        TRAILS.entrySet().removeIf(e -> !active.contains(e.getKey()) && ((Trail)e.getValue()).echoes.isEmpty());
        MESHES.keySet().removeIf(id -> !existing.contains(id) || !active.contains(id) && !TRAILS.containsKey(id));
        DIRTY.retainAll(existing);
    }

    static boolean renderEchoes(RenderLevelStageEvent event) {
        boolean drawn = false;
        for (Trail trail : TRAILS.values()) {
            trail.echoes.removeIf(e -> !e.frame.alive());
            for (Echo echo : trail.echoes) {
                EchoCache.keep(echo.shape.fixed);
                EchoCache.keep(echo.shape.moving);
                drawn |= SandevistanRigEffects.draw(event, echo);
            }
        }
        BUFFERS.endBatch();
        return drawn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean renderLive(RenderLevelStageEvent event) {
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        boolean drawn = false;
        for (LiveRig rig : CURRENT) {
            EchoCache.keep(rig.fixed.mesh);
            double radius = rig.fixed.mesh.radius() * (double)rig.placement.local.getScale(new Vector3f()).length() + (double)rig.placement.local.getTranslation(new Vector3f()).length() + 0.5;
            Vec3 origin = rig.placement.origin;
            if (event.getFrustum() != null && !event.getFrustum().isVisible(new AABB(origin.x - radius, origin.y - radius, origin.z - radius, origin.x + radius, origin.y + radius, origin.z + radius))) continue;
            PoseStack stack = event.getPoseStack();
            Vec3 camera = event.getCamera().getPosition();
            stack.pushPose();
            try {
                stack.translate(rig.placement.origin.x - camera.x, rig.placement.origin.y - camera.y, rig.placement.origin.z - camera.z);
                stack.mulPose(rig.placement.local);
                EchoCache.draw(rig.fixed.mesh, stack.last(), BUFFERS, 0xFFFFFF, false);
                MultiBufferSource normal = source -> BUFFERS.getBuffer(SandevistanRenderTypes.map(source));
                SandevistanRigEffects.capture(() -> {
                    if (rig.sable) {
                        SandevistanRigEffects.sableDynamic(rig.fixed, partial, stack, normal);
                    } else {
                        SandevistanRigEffects.createDynamic(rig.contraption, rig.placement, partial, stack, normal);
                    }
                });
                drawn |= rig.fixed.mesh.vertexCount() > 0 || rig.sable && !rig.fixed.blockEntities.isEmpty() || rig.contraption != null;
            }
            finally {
                stack.popPose();
            }
        }
        BUFFERS.endBatch();
        return drawn;
    }

    private static StaticMesh sableStatic(Object optionalBody, Vector3d pivot) {
        ClientSubLevel body = (ClientSubLevel)optionalBody;
        Minecraft mc = Minecraft.getInstance();
        ClientLevelPlot plot = body.getPlot();
        long tick = mc.level.getGameTime();
        StaticMesh cached = MESHES.get(body.getUniqueId());
        boolean dirty = DIRTY.remove(body.getUniqueId());
        if (cached == null || dirty || !pivot.equals((Object)cached.pivot) || tick - cached.checked >= 40L) {
            ContraptionMesh mesh;
            LinkedHashMap<BlockPos, BlockState> states = new LinkedHashMap<BlockPos, BlockState>();
            ArrayList bes = new ArrayList();
            int hash = pivot.hashCode();
            for (PlotChunkHolder holder : plot.getLoadedChunks()) {
                LevelChunk chunk = holder.getChunk();
                bes.addAll(chunk.getBlockEntities().values());
                for (int index = 0; index < chunk.getSectionsCount(); ++index) {
                    LevelChunkSection section = chunk.getSections()[index];
                    if (section.hasOnlyAir()) continue;
                    int baseY = chunk.getSectionYFromSectionIndex(index) * 16;
                    for (int x = 0; x < 16; ++x) {
                        for (int y = 0; y < 16; ++y) {
                            for (int z = 0; z < 16; ++z) {
                                BlockState state2 = section.getBlockState(x, y, z);
                                if (state2.isAir()) continue;
                                BlockPos pos2 = new BlockPos(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z);
                                states.put(pos2, state2);
                                hash = 31 * hash + pos2.hashCode();
                                hash = 31 * hash + state2.hashCode();
                            }
                        }
                    }
                }
            }
            if (cached != null && !dirty && cached.hash == hash) {
                mesh = cached.mesh;
            } else {
                ContraptionMesh.Builder builder = new ContraptionMesh.Builder();
                states.forEach((pos, state) -> SandevistanRigEffects.bakeBlock((BlockAndTintGetter)mc.level, pos, state, pivot, builder));
                mesh = builder.build();
            }
            cached = new StaticMesh(mesh, hash, tick, List.copyOf(bes), new Vector3d((Vector3dc)pivot));
            MESHES.put(body.getUniqueId(), cached);
        }
        return cached;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void sableDynamic(StaticMesh cached, float partial, PoseStack stack, MultiBufferSource buffer) {
        Vector3d pivot = cached.pivot;
        for (BlockEntity be : cached.blockEntities) {
            if (be.isRemoved()) continue;
            BlockPos pos = be.getBlockPos();
            stack.pushPose();
            try {
                stack.translate((double)pos.getX() - pivot.x, (double)pos.getY() - pivot.y, (double)pos.getZ() - pivot.z);
                SandevistanRigEffects.renderBlockEntity(be, partial, stack, buffer, LevelRenderer.getLightColor((BlockAndTintGetter)Minecraft.getInstance().level, (BlockPos)pos));
            }
            finally {
                stack.popPose();
            }
        }
    }

    private static StaticMesh createStatic(AbstractContraptionEntity entity) {
        Contraption contraption = entity.getContraption();
        ClientContraption client = contraption.getOrCreateClientContraptionLazy();
        VirtualRenderWorld world = client.getRenderLevel();
        int version = client.structureVersion();
        StaticMesh cached = MESHES.get(entity.getUUID());
        if (cached == null || cached.hash != version) {
            ContraptionMesh.Builder builder = new ContraptionMesh.Builder();
            for (StructureTemplate.StructureBlockInfo info : contraption.getBlocks().values()) {
                if (contraption.isHiddenInPortal(info.pos())) continue;
                SandevistanRigEffects.bakeBlock((BlockAndTintGetter)world, info.pos(), info.state(), new Vector3d(), builder);
            }
            cached = new StaticMesh(builder.build(), version, 0L, List.of(), new Vector3d());
            MESHES.put(entity.getUUID(), cached);
        }
        return cached;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void createDynamic(AbstractContraptionEntity entity, Placement placement, float partial, PoseStack stack, MultiBufferSource buffer) {
        Contraption contraption = entity.getContraption();
        ClientContraption client = contraption.getOrCreateClientContraptionLazy();
        VirtualRenderWorld world = client.getRenderLevel();
        for (BlockEntity be : client.renderedBlockEntityView) {
            if (be.isRemoved()) continue;
            BlockPos p = be.getBlockPos();
            stack.pushPose();
            Vector3f lp = placement.local.transformPosition((float)p.getX() + 0.5f, (float)p.getY() + 0.5f, (float)p.getZ() + 0.5f, new Vector3f());
            int light = LevelRenderer.getLightColor((BlockAndTintGetter)entity.level(), (BlockPos)BlockPos.containing((double)(placement.origin.x + (double)lp.x), (double)(placement.origin.y + (double)lp.y), (double)(placement.origin.z + (double)lp.z)));
            try {
                stack.translate((float)p.getX(), (float)p.getY(), (float)p.getZ());
                SandevistanRigEffects.renderBlockEntity(be, partial, stack, buffer, light);
            }
            finally {
                stack.popPose();
            }
        }
        ContraptionMatrices matrices = new ContraptionMatrices();
        matrices.getModel().last().pose().set((Matrix4fc)stack.last().pose());
        matrices.getModel().last().normal().set((Matrix3fc)stack.last().normal());
        matrices.getWorld().translation((float)placement.origin.x, (float)placement.origin.y, (float)placement.origin.z).mul((Matrix4fc)placement.local);
        matrices.getLight().set((Matrix4fc)matrices.getWorld());
        for (MutablePair actor : contraption.getActors()) {
            MovementBehaviour behaviour;
            MovementContext context = (MovementContext)actor.getRight();
            StructureTemplate.StructureBlockInfo info = (StructureTemplate.StructureBlockInfo)actor.getLeft();
            if (context == null || contraption.isHiddenInPortal(info.pos())) continue;
            if (context.world == null) {
                context.world = entity.level();
            }
            if ((behaviour = (MovementBehaviour)MovementBehaviour.REGISTRY.get((StateHolder)info.state())) == null) continue;
            PoseStack model = matrices.getModel();
            model.pushPose();
            try {
                model.translate((float)info.pos().getX(), (float)info.pos().getY(), (float)info.pos().getZ());
                behaviour.renderInContraption(context, world, matrices, buffer);
            }
            finally {
                model.popPose();
            }
        }
    }

    private static void bakeBlock(BlockAndTintGetter world, BlockPos pos, BlockState state, Vector3d pivot, ContraptionMesh.Builder builder) {
        if (state.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        ModelData data = model.getModelData(world, pos, state, world.getModelData(pos));
        RandomSource random = RandomSource.create((long)state.getSeed(pos));
        PoseStack stack = new PoseStack();
        stack.translate((double)pos.getX() - pivot.x, (double)pos.getY() - pivot.y, (double)pos.getZ() - pivot.z);
        for (RenderType layer : model.getRenderTypes(state, random, data)) {
            random.setSeed(state.getSeed(pos));
            dispatcher.getModelRenderer().tesselateBlock(world, model, state, pos, stack, builder.getBuffer(layer), true, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY, data, layer);
        }
    }

    private static <T extends BlockEntity> void renderBlockEntity(T be, float partial, PoseStack stack, MultiBufferSource buffer, int light) {
        BlockEntityRenderer renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
        if (renderer != null) {
            renderer.render(be, partial, stack, buffer, light, OverlayTexture.NO_OVERLAY);
        }
    }

    private static void capture(Runnable draw) {
        boolean old = capturing;
        capturing = true;
        try {
            draw.run();
        }
        finally {
            capturing = old;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean draw(RenderLevelStageEvent event, Echo echo) {
        Placement placement = echo.placement;
        Vector3f scale = placement.local.getScale(new Vector3f());
        double radius = Math.max(echo.shape.fixed.radius(), echo.shape.moving.radius()) * (double)Math.max(scale.x, Math.max(scale.y, scale.z)) + (double)placement.local.getTranslation(new Vector3f()).length() + 0.5;
        Vec3 origin = placement.origin;
        if (event.getFrustum() != null && !event.getFrustum().isVisible(new AABB(origin.x - radius, origin.y - radius, origin.z - radius, origin.x + radius, origin.y + radius, origin.z + radius))) {
            return false;
        }
        PoseStack stack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        stack.pushPose();
        try {
            Placement p = echo.placement;
            stack.translate(p.origin.x - camera.x, p.origin.y - camera.y, p.origin.z - camera.z);
            stack.mulPose(p.local);
            EchoCache.draw(echo.shape.fixed, stack.last(), BUFFERS, echo.frame.color(), true);
            EchoCache.draw(echo.shape.moving, stack.last(), BUFFERS, echo.frame.color(), true);
            boolean bl = echo.shape.vertices() > 0;
            return bl;
        }
        finally {
            stack.popPose();
        }
    }

    private record Placement(Vec3 origin, Matrix4f local) {
    }

    private static final class Trail {
        final Deque<Echo> echoes = new ArrayDeque<Echo>();
        final AfterimageMotion motion = new AfterimageMotion();
        UUID owner;
        long generation = -1L;

        private Trail() {
        }

        boolean wantsSample(UUID currentOwner, Placement placement, double radius) {
            AfterimageTimeline timeline = SandevistanHistory.TIMELINE;
            long slot = timeline.slot(currentOwner);
            long currentGeneration = timeline.generation(currentOwner);
            if (!currentOwner.equals(this.owner) || this.generation != currentGeneration) {
                this.owner = currentOwner;
                this.generation = currentGeneration;
                this.motion.reset();
            }
            return this.motion.sample(slot, placement.origin, placement.local, radius);
        }

        void sample(UUID owner, Placement placement, Shape shape) {
            AfterimageTimeline.Frame frame = SandevistanHistory.TIMELINE.frame(owner);
            this.echoes.addLast(new Echo(placement, shape, frame));
            SandevistanHistory.TIMELINE.add(frame, shape.vertices());
        }
    }

    private record StaticMesh(ContraptionMesh mesh, int hash, long checked, List<BlockEntity> blockEntities, Vector3d pivot) {
    }

    private record Shape(ContraptionMesh fixed, ContraptionMesh moving) {
        int vertices() {
            return this.fixed.vertexCount() + this.moving.vertexCount();
        }
    }

    private record LiveRig(Placement placement, StaticMesh fixed, boolean sable, AbstractContraptionEntity contraption) {
    }

    private record Echo(Placement placement, Shape shape, AfterimageTimeline.Frame frame) {
    }

    static final class AfterimageMotion {
        private Vec3 position;
        private Matrix4f matrix;
        private long observed = -1L;

        AfterimageMotion() {
        }

        boolean sample(long slot, Vec3 current, Matrix4f local, double radius) {
            boolean moved;
            if (slot < 0L || slot == this.observed) {
                return false;
            }
            this.observed = slot;
            if (this.position == null || current.distanceToSqr(this.position) > 64.0) {
                this.accept(current, local);
                return false;
            }
            double distance = current.distanceTo(this.position);
            float angular = this.matrix.getUnnormalizedRotation(new Quaternionf()).difference(local.getUnnormalizedRotation(new Quaternionf())).angle();
            angular = Math.min(angular, (float)Math.PI * 2 - angular);
            Vector3f scale = local.getScale(new Vector3f());
            boolean bl = moved = distance >= 0.86 || angular >= 0.14f || (double)angular * radius * (double)Math.max(scale.x, Math.max(scale.y, scale.z)) >= 0.86;
            if (moved) {
                this.accept(current, local);
            }
            return moved;
        }

        private void accept(Vec3 current, Matrix4f local) {
            this.position = current;
            this.matrix = new Matrix4f((Matrix4fc)local);
        }

        void reset() {
            this.position = null;
            this.matrix = null;
            this.observed = -1L;
        }
    }
}

