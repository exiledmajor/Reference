/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.Mth
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.chromeware.monowire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MonowireBlockCollision {
    public static final double CONTACT_SKIN = 0.0125;
    private static final double BROADPHASE_MARGIN = 0.22;
    private static final long MAX_BROADPHASE_BLOCK_CELLS = 512L;
    private static final long MAX_BROADPHASE_AXIS_CELLS = 64L;
    private static final int MAX_TICK_CACHE_ENTRIES = 2048;
    private static final double[] MATERIAL_SAMPLES = new double[]{0.0, 0.25, 0.5, 0.75, 1.0};

    public static SegmentEnvironment[] buildBroadphase(Level level, Vec3[] before, Vec3[] predicted, double[] radii, double tipScale, TickCache tickCache) {
        int segments = Math.min(radii.length, Math.min(before.length, predicted.length) - 1);
        SegmentEnvironment[] result = new SegmentEnvironment[segments];
        for (int i = 0; i < segments; ++i) {
            double radius = radii[i] * (i == segments - 1 ? Math.max(1.0, tipScale) : 1.0);
            AABB bounds = new AABB(before[i], before[i + 1]).minmax(new AABB(predicted[i], predicted[i + 1])).inflate(radius + 0.0125 + 0.22);
            result[i] = MonowireBlockCollision.collect(level, bounds, tickCache);
        }
        return result;
    }

    private static SegmentEnvironment collect(Level level, AABB bounds, TickCache tickCache) {
        if (!(MonowireBlockCollision.finite(bounds.minX) && MonowireBlockCollision.finite(bounds.minY) && MonowireBlockCollision.finite(bounds.minZ) && MonowireBlockCollision.finite(bounds.maxX) && MonowireBlockCollision.finite(bounds.maxY) && MonowireBlockCollision.finite(bounds.maxZ))) {
            return SegmentEnvironment.EMPTY;
        }
        int minX = Mth.floor((double)bounds.minX);
        int minY = Math.max(level.getMinBuildHeight(), Mth.floor((double)bounds.minY));
        int minZ = Mth.floor((double)bounds.minZ);
        int maxX = Mth.floor((double)bounds.maxX);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, Mth.floor((double)bounds.maxY));
        int maxZ = Mth.floor((double)bounds.maxZ);
        if (maxY < minY) {
            return SegmentEnvironment.EMPTY;
        }
        long cellsX = (long)maxX - (long)minX + 1L;
        long cellsY = (long)maxY - (long)minY + 1L;
        long cellsZ = (long)maxZ - (long)minZ + 1L;
        if (cellsX <= 0L || cellsY <= 0L || cellsZ <= 0L || cellsX > 64L || cellsY > 64L || cellsZ > 64L || cellsX > 512L / cellsY || cellsX * cellsY > 512L / cellsZ) {
            return SegmentEnvironment.EMPTY;
        }
        ArrayList<AABB> solids = null;
        for (BlockPos pos : BlockPos.betweenClosed((int)minX, (int)minY, (int)minZ, (int)maxX, (int)maxY, (int)maxZ)) {
            List<AABB> blockBoxes = tickCache != null ? tickCache.collisionBoxes(level, pos) : MonowireBlockCollision.worldCollisionBoxes(level, pos);
            for (AABB world : blockBoxes) {
                if (!world.intersects(bounds)) continue;
                if (solids == null) {
                    solids = new ArrayList<AABB>();
                }
                solids.add(world);
            }
        }
        return solids == null ? SegmentEnvironment.EMPTY : new SegmentEnvironment(Collections.unmodifiableList(solids));
    }

    private static boolean finite(double value) {
        return Double.isFinite(value);
    }

    private static List<AABB> worldCollisionBoxes(Level level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return List.of();
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return List.of();
        }
        VoxelShape shape = state.getCollisionShape((BlockGetter)level, pos);
        if (shape.isEmpty()) {
            return List.of();
        }
        ArrayList<AABB> boxes = new ArrayList<AABB>();
        for (AABB local : shape.toAabbs()) {
            boxes.add(local.move((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()));
        }
        return boxes.isEmpty() ? List.of() : List.copyOf(boxes);
    }

    public static SegmentContact findContact(SegmentEnvironment environment, Vec3 beforeStart, Vec3 beforeEnd, Vec3 afterStart, Vec3 afterEnd, double radius, double tangentRetention) {
        Vec3 remaining;
        double inward;
        if (environment == null || environment.solids.isEmpty()) {
            return null;
        }
        Candidate best = null;
        double expandedBy = radius + 0.0125;
        for (AABB solid : environment.solids) {
            AABB expanded = solid.inflate(expandedBy);
            for (double sample : MATERIAL_SAMPLES) {
                Vec3 to;
                Vec3 from = beforeStart.lerp(beforeEnd, sample);
                Entry entry = MonowireBlockCollision.sweepPointAabb(from, to = afterStart.lerp(afterEnd, sample), expanded);
                if (entry == null || best != null && !(entry.toi < best.entry.toi) && (!(Math.abs(entry.toi - best.entry.toi) < 1.0E-8) || !(entry.pushDistance > best.entry.pushDistance))) continue;
                best = new Candidate(sample, from, to, solid, entry);
            }
        }
        if (best == null) {
            return null;
        }
        Vec3 centerAtHit = best.from.lerp(best.to, best.entry.toi);
        if (best.entry.pushDistance > 0.0) {
            centerAtHit = centerAtHit.add(best.entry.normal.scale(best.entry.pushDistance));
        }
        if ((inward = (remaining = best.to.subtract(centerAtHit)).dot(best.entry.normal)) < 0.0) {
            remaining = remaining.subtract(best.entry.normal.scale(inward));
        }
        remaining = remaining.scale(tangentRetention);
        Vec3 desiredSample = centerAtHit.add(best.entry.normal.scale(0.0125)).add(remaining);
        Vec3 correction = desiredSample.subtract(best.to);
        Vec3 physicalSurface = centerAtHit.subtract(best.entry.normal.scale(radius));
        return new SegmentContact(best.sample, best.entry.toi, best.entry.normal, correction, physicalSurface);
    }

    private static Entry sweepPointAabb(Vec3 start, Vec3 end, AABB box) {
        Vec3 delta = end.subtract(start);
        if (box.contains(start)) {
            Exit exit = MonowireBlockCollision.nearestExit(start, box);
            if (exit.distance <= 0.018750000000000003 && delta.dot(exit.normal) >= -1.0E-9) {
                return null;
            }
            return new Entry(0.0, exit.normal, exit.distance);
        }
        double tEnter = 0.0;
        double tExit = 1.0;
        Vec3 enterNormal = Vec3.ZERO;
        AxisHit x = MonowireBlockCollision.axis(start.x, delta.x, box.minX, box.maxX, new Vec3(-1.0, 0.0, 0.0), new Vec3(1.0, 0.0, 0.0));
        if (x == null) {
            return null;
        }
        if (x.enter > tEnter) {
            tEnter = x.enter;
            enterNormal = x.enterNormal;
        }
        if (tEnter > (tExit = Math.min(tExit, x.exit))) {
            return null;
        }
        AxisHit y = MonowireBlockCollision.axis(start.y, delta.y, box.minY, box.maxY, new Vec3(0.0, -1.0, 0.0), new Vec3(0.0, 1.0, 0.0));
        if (y == null) {
            return null;
        }
        if (y.enter > tEnter) {
            tEnter = y.enter;
            enterNormal = y.enterNormal;
        }
        if (tEnter > (tExit = Math.min(tExit, y.exit))) {
            return null;
        }
        AxisHit z = MonowireBlockCollision.axis(start.z, delta.z, box.minZ, box.maxZ, new Vec3(0.0, 0.0, -1.0), new Vec3(0.0, 0.0, 1.0));
        if (z == null) {
            return null;
        }
        if (z.enter > tEnter) {
            tEnter = z.enter;
            enterNormal = z.enterNormal;
        }
        if (tEnter > (tExit = Math.min(tExit, z.exit)) || tEnter < 0.0 || tEnter > 1.0) {
            return null;
        }
        if (enterNormal.lengthSqr() < 1.0E-12) {
            Vec3 opposite = delta.lengthSqr() < 1.0E-12 ? new Vec3(0.0, 1.0, 0.0) : delta.normalize().scale(-1.0);
            enterNormal = MonowireBlockCollision.dominantAxis(opposite);
        }
        return new Entry(tEnter, enterNormal, 0.0);
    }

    private static AxisHit axis(double start, double delta, double min, double max, Vec3 minNormal, Vec3 maxNormal) {
        if (Math.abs(delta) < 1.0E-12) {
            return start >= min && start <= max ? new AxisHit(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Vec3.ZERO) : null;
        }
        double a = (min - start) / delta;
        double b = (max - start) / delta;
        Vec3 enterNormal = minNormal;
        if (a > b) {
            double swap = a;
            a = b;
            b = swap;
            enterNormal = maxNormal;
        }
        return new AxisHit(a, b, enterNormal);
    }

    private static Exit nearestExit(Vec3 point, AABB box) {
        double dxMin = Math.abs(point.x - box.minX);
        double dxMax = Math.abs(box.maxX - point.x);
        double dyMin = Math.abs(point.y - box.minY);
        double dyMax = Math.abs(box.maxY - point.y);
        double dzMin = Math.abs(point.z - box.minZ);
        double dzMax = Math.abs(box.maxZ - point.z);
        double best = dxMin;
        Vec3 normal = new Vec3(-1.0, 0.0, 0.0);
        if (dxMax < best) {
            best = dxMax;
            normal = new Vec3(1.0, 0.0, 0.0);
        }
        if (dyMin < best) {
            best = dyMin;
            normal = new Vec3(0.0, -1.0, 0.0);
        }
        if (dyMax < best) {
            best = dyMax;
            normal = new Vec3(0.0, 1.0, 0.0);
        }
        if (dzMin < best) {
            best = dzMin;
            normal = new Vec3(0.0, 0.0, -1.0);
        }
        if (dzMax < best) {
            best = dzMax;
            normal = new Vec3(0.0, 0.0, 1.0);
        }
        return new Exit(normal, best);
    }

    private static Vec3 dominantAxis(Vec3 vector) {
        double ax = Math.abs(vector.x);
        double ay = Math.abs(vector.y);
        double az = Math.abs(vector.z);
        if (ax >= ay && ax >= az) {
            return new Vec3(Math.copySign(1.0, vector.x), 0.0, 0.0);
        }
        if (ay >= az) {
            return new Vec3(0.0, Math.copySign(1.0, vector.y), 0.0);
        }
        return new Vec3(0.0, 0.0, Math.copySign(1.0, vector.z));
    }

    public static final class SegmentEnvironment {
        private static final SegmentEnvironment EMPTY = new SegmentEnvironment(List.of());
        private final List<AABB> solids;

        public SegmentEnvironment(List<AABB> solids) {
            this.solids = solids;
        }
    }

    public static final class TickCache {
        private final Map<Long, List<AABB>> collisionBoxes = new HashMap<Long, List<AABB>>();

        public List<AABB> collisionBoxes(Level level, BlockPos pos) {
            long key = pos.asLong();
            List<AABB> cached = this.collisionBoxes.get(key);
            if (cached != null) {
                return cached;
            }
            List<AABB> computed = MonowireBlockCollision.worldCollisionBoxes(level, pos);
            if (this.collisionBoxes.size() < 2048) {
                this.collisionBoxes.put(key, computed);
            }
            return computed;
        }
    }

    private record Entry(double toi, Vec3 normal, double pushDistance) {
    }

    private record Candidate(double sample, Vec3 from, Vec3 to, AABB solid, Entry entry) {
    }

    public record SegmentContact(double sample, double toi, Vec3 normal, Vec3 correction, Vec3 surfacePoint) {
    }

    private record Exit(Vec3 normal, double distance) {
    }

    private record AxisHit(double enter, double exit, Vec3 enterNormal) {
    }
}

