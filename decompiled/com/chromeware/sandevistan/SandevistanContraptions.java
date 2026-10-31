/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.contraptions.AbstractContraptionEntity
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.api.physics.PhysicsPipelineBody
 *  dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle
 *  dev.ryanhcode.sable.api.sublevel.SubLevelContainer
 *  dev.ryanhcode.sable.companion.math.BoundingBox3dc
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package com.chromeware.sandevistan;

import com.chromeware.compat.SableCompatibility;
import com.chromeware.mixin.SandevistanControlledContraptionAccessor;
import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.SandevistanMotorAccess;
import com.chromeware.sandevistan.TimeField;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.physics.constraint.PhysicsConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class SandevistanContraptions {
    private static final Map<Level, List<Link>> LINKS = new WeakHashMap<Level, List<Link>>();
    private static final Map<SubLevel, TimeField> CLOCKS = new WeakHashMap<SubLevel, TimeField>();
    private static final Map<SubLevel, UUID> OWNERS = new WeakHashMap<SubLevel, UUID>();
    private static final Map<Level, Set<BlockPos>> CONTROLLERS = new WeakHashMap<Level, Set<BlockPos>>();

    public static void link(Level level, PhysicsPipelineBody a, PhysicsPipelineBody b, PhysicsConstraintHandle handle) {
        if (handle != null && a instanceof SubLevel && b instanceof SubLevel) {
            LINKS.computeIfAbsent(level, ignored -> new ArrayList()).add(new Link(a, b, handle));
        }
    }

    public static void refresh(Level level) {
        SubLevelContainer container;
        SandevistanContraptions.refreshControllers(level);
        if (!SableCompatibility.loaded()) {
            return;
        }
        List links = LINKS.computeIfAbsent(level, ignored -> new ArrayList());
        links.removeIf(l -> l.a.isRemoved() || l.b.isRemoved() || !l.handle.isValid());
        Collection<TimeField> fields = Sandevistan.fields(level);
        if (fields.isEmpty()) {
            boolean hadClock = CLOCKS.keySet().removeIf(body -> body.getLevel() == level);
            OWNERS.keySet().removeIf(body -> body.getLevel() == level);
            CONTROLLERS.remove(level);
            if (hadClock) {
                for (Link link : links) {
                    PhysicsConstraintHandle physicsConstraintHandle = link.handle;
                    if (!(physicsConstraintHandle instanceof SandevistanMotorAccess)) continue;
                    SandevistanMotorAccess access = (SandevistanMotorAccess)physicsConstraintHandle;
                    access.chrome$refreshMotors();
                }
            }
            return;
        }
        SubLevelContainer subLevelContainer = container = SableCompatibility.loaded() ? SubLevelContainer.getContainer((Level)level) : null;
        if (container == null) {
            return;
        }
        List bodies = container.getAllSubLevels();
        IdentityHashMap graph = new IdentityHashMap();
        for (SubLevel body2 : bodies) {
            CLOCKS.remove(body2);
            OWNERS.remove(body2);
            graph.put(body2, new HashSet());
        }
        for (Object link : links) {
            SubLevel a = (SubLevel)((Link)link).a;
            SubLevel b = (SubLevel)((Link)link).b;
            if (!graph.containsKey(a) || !graph.containsKey(b)) continue;
            ((Set)graph.get(a)).add(b);
            ((Set)graph.get(b)).add(a);
        }
        IdentityHashMap<SubLevel, UUID> roots = new IdentityHashMap<SubLevel, UUID>();
        for (ServerPlayer player : Sandevistan.activePlayers(level)) {
            for (Entity vehicle = player.getVehicle(); vehicle != null; vehicle = vehicle.getVehicle()) {
                SubLevel body3;
                SubLevel subLevel = body3 = SableCompatibility.loaded() ? Sable.HELPER.getContaining(level, (Position)vehicle.position()) : null;
                if (body3 == null) continue;
                roots.put(body3, player.getUUID());
            }
        }
        Set visited = Collections.newSetFromMap(new IdentityHashMap());
        for (SubLevel root : bodies) {
            if (!visited.add(root)) continue;
            ArrayList<SubLevel> group = new ArrayList<SubLevel>();
            ArrayDeque<SubLevel> queue = new ArrayDeque<SubLevel>();
            queue.add(root);
            TimeField clock = null;
            UUID owner = null;
            while (!queue.isEmpty()) {
                BoundingBox3dc bounds;
                SubLevel body4 = (SubLevel)queue.removeFirst();
                group.add(body4);
                UUID candidate = (UUID)roots.get(body4);
                if (candidate != null && (owner == null || candidate.toString().compareTo(owner.toString()) < 0)) {
                    owner = candidate;
                }
                boolean uninitialized = (bounds = body4.boundingBox()).maxX() <= bounds.minX() || bounds.maxZ() <= bounds.minZ();
                Vector3d position = body4.logicalPose().position();
                for (TimeField f : fields) {
                    if (!uninitialized ? bounds.maxX() < f.x() - f.radius() || bounds.minX() >= f.x() + f.radius() || bounds.maxZ() < f.z() - f.radius() || bounds.minZ() >= f.z() + f.radius() : !f.contains(position.x(), position.z())) continue;
                    if (clock != null && f.slowdown() <= clock.slowdown() && (f.slowdown() != clock.slowdown() || f.owner().toString().compareTo(clock.owner().toString()) >= 0)) continue;
                    clock = f;
                }
                for (SubLevel next : (Set)graph.get(body4)) {
                    if (!visited.add(next)) continue;
                    queue.add(next);
                }
            }
            for (SubLevel body5 : group) {
                if (owner != null) {
                    OWNERS.put(body5, owner);
                    continue;
                }
                if (clock == null) continue;
                CLOCKS.put(body5, clock);
            }
        }
        for (Link link : links) {
            PhysicsConstraintHandle physicsConstraintHandle = link.handle;
            if (!(physicsConstraintHandle instanceof SandevistanMotorAccess)) continue;
            SandevistanMotorAccess access = (SandevistanMotorAccess)physicsConstraintHandle;
            access.chrome$refreshMotors();
        }
    }

    private static void refreshControllers(Level level) {
        BlockPos pos;
        if (Sandevistan.fields(level).isEmpty()) {
            CONTROLLERS.remove(level);
            return;
        }
        HashSet<BlockPos> controllers = new HashSet<BlockPos>();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            for (Entity entity : server.getAllEntities()) {
                if (!(entity instanceof SandevistanControlledContraptionAccessor)) continue;
                SandevistanControlledContraptionAccessor controlled = (SandevistanControlledContraptionAccessor)entity;
                if (!SandevistanContraptions.entityImmune(entity, Sandevistan::immuneOwner) || (pos = controlled.chrome$controllerPosition()) == null) continue;
                controllers.add(pos.immutable());
            }
        }
        for (ServerPlayer player : Sandevistan.activePlayers(level)) {
            for (Entity vehicle = player.getVehicle(); vehicle != null; vehicle = vehicle.getVehicle()) {
                SandevistanControlledContraptionAccessor controlled;
                if (!(vehicle instanceof SandevistanControlledContraptionAccessor) || (pos = (controlled = (SandevistanControlledContraptionAccessor)vehicle).chrome$controllerPosition()) == null) continue;
                controllers.add(pos.immutable());
            }
        }
        CONTROLLERS.put(level, Set.copyOf(controllers));
    }

    public static TimeField clock(SubLevel body) {
        return CLOCKS.get(body);
    }

    public static double scale(SubLevel body) {
        TimeField f = SandevistanContraptions.clock(body);
        return f == null ? 1.0 : (double)(100 - f.slowdown()) / 100.0;
    }

    public static boolean immune(SubLevel body) {
        return OWNERS.containsKey(body);
    }

    public static Set<BlockPos> controllers(Level level) {
        return CONTROLLERS.getOrDefault(level, Set.of());
    }

    public static boolean controllerImmune(Level level, BlockPos position) {
        return SandevistanContraptions.controllers(level).contains(position);
    }

    public static Map<UUID, UUID> owners(Level level) {
        if (!SableCompatibility.loaded()) {
            return Map.of();
        }
        HashMap result = new HashMap();
        OWNERS.forEach((body, owner) -> {
            if (body.getLevel() == level && !body.isRemoved()) {
                result.put(body.getUniqueId(), owner);
            }
        });
        return Map.copyOf(result);
    }

    public static Map<UUID, UUID> clocks(Level level) {
        if (!SableCompatibility.loaded()) {
            return Map.of();
        }
        HashMap result = new HashMap();
        CLOCKS.forEach((body, field) -> {
            if (body.getLevel() == level && !body.isRemoved()) {
                result.put(body.getUniqueId(), field.owner());
            }
        });
        return Map.copyOf(result);
    }

    public static boolean entityImmune(Entity entity, Predicate<Entity> active) {
        if (active.test(entity)) {
            return true;
        }
        if (entity instanceof AbstractContraptionEntity) {
            AbstractContraptionEntity contraption = (AbstractContraptionEntity)entity;
            if (contraption.getControllingPlayer().map(arg_0 -> ((Level)entity.level()).getPlayerByUUID(arg_0)).filter(active).isPresent()) {
                return true;
            }
        }
        for (Entity passenger : entity.getIndirectPassengers()) {
            if (!active.test(passenger)) continue;
            return true;
        }
        return false;
    }

    public static Vector3d worldPosition(Level level, BlockPos pos) {
        SubLevel body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(level, (Vec3i)pos) : null;
        Vector3d point = new Vector3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        return body == null ? point : body.logicalPose().transformPosition((Vector3dc)point, new Vector3d());
    }

    public static Vector3d worldPosition(Entity entity) {
        SubLevel body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(entity.level(), (Position)entity.position()) : null;
        Vector3d point = new Vector3d(entity.getX(), entity.getY(), entity.getZ());
        return body == null ? point : body.logicalPose().transformPosition((Vector3dc)point, new Vector3d());
    }

    public static void clear() {
        LINKS.clear();
        CLOCKS.clear();
        OWNERS.clear();
        CONTROLLERS.clear();
    }

    private record Link(PhysicsPipelineBody a, PhysicsPipelineBody b, PhysicsConstraintHandle handle) {
    }
}

