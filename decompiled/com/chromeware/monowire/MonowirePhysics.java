/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.minecraft.util.Mth
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.monowire;

import com.chromeware.monowire.MonowireAim;
import com.chromeware.monowire.MonowireBlockCollision;
import com.chromeware.monowire.MonowireObstacles;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class MonowirePhysics {
    public static final int SEGMENTS = 40;
    public static final double LENGTH = 8.0;
    public static final double RADIUS = 0.018;
    public static final int STROKE_TICKS = 10;
    public static final double STANDBY = 0.03;
    public static final double TIP = 0.00375;
    public static final double REEL_STEP = 0.15;
    public static final double OUTLET_CLEARANCE = 0.04;
    private static final int SUBSTEPS = 8;
    private static final int ITERATIONS = 12;
    private static final double DT = 0.00625;
    private final Vec3[] points = new Vec3[41];
    private final Vec3[] previous = new Vec3[41];
    private final Vec3[] renderedPrevious = new Vec3[41];
    private final double[] lambdas = new double[40];
    private final double[] radii = new double[40];
    private boolean initialized;
    private MonowireAim aim = MonowireAim.of(new Vec3(0.0, 0.0, 1.0), 0.0f);
    private int strokeAge = 10;
    private int strokeSide = 1;
    private int strokeNumber;
    private double strokeRoll;
    private Lash lash = Lash.INWARD;
    private boolean launchPending;
    private boolean tipPinned;
    private double reel = 0.00375;
    private double length = 0.03;
    private List<MonowireObstacles.Box> bodies = List.of();
    private static final double[] LINK_SAMPLES = new double[]{0.25, 0.5, 0.75, 1.0};

    public MonowirePhysics() {
        Arrays.fill(this.radii, 0.018);
    }

    public void slash(Vec3 look, boolean left) {
        this.slash(look, left, this.strokeNumber + 1);
    }

    public void aim(Vec3 look, float yaw) {
        this.aim = MonowireAim.of(look, yaw);
    }

    public void slash(Vec3 look, boolean left, int stroke) {
        this.slash(look, left, stroke, Lash.of(stroke));
    }

    public void slash(Vec3 look, boolean left, int stroke, Lash lash) {
        this.aim = MonowireAim.of(look, (float)Math.toDegrees(Math.atan2(-look.x, look.z)));
        this.strokeAge = 0;
        this.strokeNumber = stroke;
        this.lash = lash;
        this.strokeSide = switch (lash.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 3 -> {
                if (left) {
                    yield 1;
                }
                yield -1;
            }
            case 1 -> {
                if (left) {
                    yield -1;
                }
                yield 1;
            }
            case 2 -> -1;
        };
        this.strokeRoll = Math.toRadians(switch (lash.ordinal()) {
            case 2 -> 90.0;
            case 3 -> {
                if (left) {
                    yield -35.0;
                }
                yield 35.0;
            }
            default -> 0.0;
        });
        this.launchPending = true;
    }

    public Lash lash() {
        return this.lash;
    }

    public boolean striking() {
        return this.strokeAge >= 1 && this.strokeAge <= 7;
    }

    public boolean slashing() {
        return this.strokeAge < 10;
    }

    public void cancelSlash() {
        this.strokeAge = 10;
        this.launchPending = false;
    }

    public boolean initialized() {
        return this.initialized;
    }

    public void bodies(List<MonowireObstacles.Box> bodies) {
        this.bodies = bodies == null ? List.of() : bodies;
    }

    public double reel() {
        return this.reel;
    }

    public double length() {
        return this.length;
    }

    public boolean standby() {
        return !this.tipPinned && this.length <= 0.045;
    }

    public Vec3[] points() {
        return (Vec3[])this.points.clone();
    }

    public Vec3[] beforeTick() {
        return (Vec3[])this.renderedPrevious.clone();
    }

    public Vec3 point(int index, float partialTick) {
        return this.renderedPrevious[index].lerp(this.points[index], (double)Mth.clamp((float)partialTick, (float)0.0f, (float)1.0f));
    }

    public void reset(Vec3 root, double length) {
        for (int i = 0; i <= 40; ++i) {
            this.points[i] = root.add(0.0, -length * (double)i / 40.0 * 0.98, length * (double)i / 40.0 * 0.16);
            this.previous[i] = this.renderedPrevious[i] = this.points[i];
        }
        this.initialized = true;
    }

    public void tick(Vec3 root, double feed, Level level) {
        this.tick(root, feed, null, level);
    }

    public void tick(Vec3 root, double feed, Vec3 grip, Level level) {
        double length;
        this.tipPinned = grip != null;
        this.reel = grip != null || this.slashing() ? 1.0 : Math.max(0.00375, this.reel - 0.15);
        this.length = length = grip == null ? 8.0 * Mth.clamp((double)Math.min(feed, this.reel), (double)0.00375, (double)1.0) : Math.max(0.04, root.distanceTo(grip) * 1.015);
        if (!this.initialized || this.points[0].distanceToSqr(root) > 16.0) {
            this.reset(root, length);
        }
        System.arraycopy(this.points, 0, this.renderedPrevious, 0, this.points.length);
        if (this.launchPending && grip == null) {
            Vec3 plane = this.sweepPlane();
            for (int i = 1; i <= 40; ++i) {
                this.previous[i] = this.previous[i].subtract(this.aim.forward().scale(this.lash.impulse * Math.sqrt((double)i / 40.0))).add(plane.scale((double)this.strokeSide * 0.1 * (double)i / 40.0));
            }
            this.launchPending = false;
        }
        MonowireBlockCollision.TickCache cache = new MonowireBlockCollision.TickCache();
        Vec3 oldRoot = this.points[0];
        double rest = length / 40.0;
        for (int step = 0; step < 8; ++step) {
            Vec3 anchor = oldRoot.lerp(root, ((double)step + 1.0) / 8.0);
            Vec3[] before = (Vec3[])this.points.clone();
            this.points[0] = this.previous[0] = anchor;
            for (int i = 1; i <= 40; ++i) {
                Vec3 current = this.points[i];
                Vec3 velocity = current.subtract(this.previous[i]).scale(0.996);
                if (velocity.length() > 0.22) {
                    velocity = velocity.normalize().scale(0.22);
                }
                this.previous[i] = current;
                this.points[i] = current.add(velocity).add(0.0, -3.8281250000000007E-4, 0.0);
            }
            if (grip != null) {
                this.points[40] = this.previous[40] = grip;
            }
            if (this.strokeAge < 10 && grip == null) {
                this.driveWrist(anchor, rest, ((double)this.strokeAge + ((double)step + 1.0) / 8.0) / 10.0);
            }
            MonowireBlockCollision.SegmentEnvironment[] blocks = level == null ? null : MonowireBlockCollision.buildBroadphase(level, before, this.points, this.radii, 1.0, cache);
            Arrays.fill(this.lambdas, 0.0);
            for (int iteration = 0; iteration < 12; ++iteration) {
                this.points[0] = anchor;
                for (int k = 0; k < 40; ++k) {
                    int i = iteration % 2 == 0 ? k : 40 - k - 1;
                    this.solveDistance(i, i + 1, rest, 2.0E-9, this.lambdas, i);
                }
                for (int i = 1; i < 40; ++i) {
                    if (!(this.points[i - 1].distanceTo(this.points[i + 1]) < rest * 0.6)) continue;
                    this.separate(i - 1, i + 1, rest * 0.6);
                }
                if (blocks != null && iteration % 2 == 1) {
                    this.contacts(blocks, before, false);
                }
                if (grip == null) continue;
                this.points[40] = grip;
            }
            this.selfCollision();
            if (blocks != null) {
                this.contacts(blocks, before, true);
            }
            this.points[0] = anchor;
            this.limitStretch(anchor, length);
            if (grip != null) continue;
            this.pushOutOfBodies();
        }
        this.points[0] = root;
        if (grip != null) {
            this.points[40] = this.previous[40] = grip;
        }
        this.strokeAge = Math.min(10, this.strokeAge + 1);
    }

    private Vec3 sweepPlane() {
        return this.aim.right().scale(Math.cos(this.strokeRoll)).add(this.aim.up().scale(Math.sin(this.strokeRoll)));
    }

    private void driveWrist(Vec3 root, double rest, double progress) {
        if (progress > 0.7) {
            return;
        }
        double sweep = Math.toRadians((-this.lash.sweep + 2.0 * this.lash.sweep * Mth.clamp((double)(progress / 0.7), (double)0.0, (double)1.0)) * (double)this.strokeSide);
        Vec3 axis = this.aim.forward().scale(Math.cos(sweep)).add(this.sweepPlane().scale(Math.sin(sweep))).normalize();
        for (int i = 1; i <= 40; ++i) {
            double spring = 0.3 * (1.0 - (double)i / 41.0);
            Vec3 target = root.add(axis.scale(rest * (double)i));
            this.points[i] = this.points[i].add(target.subtract(this.points[i]).scale(spring));
        }
    }

    private void limitStretch(Vec3 root, double length) {
        for (int i = 1; i <= 40; ++i) {
            Vec3 delta = this.points[i].subtract(root);
            double max = length * (double)i / 40.0;
            if (delta.lengthSqr() <= max * max) continue;
            Vec3 normal = delta.normalize();
            Vec3 velocity = this.points[i].subtract(this.previous[i]);
            this.points[i] = root.add(normal.scale(max));
            velocity = velocity.subtract(normal.scale(Math.max(0.0, velocity.dot(normal))));
            this.previous[i] = this.points[i].subtract(velocity);
        }
    }

    private void pushOutOfBodies() {
        for (int i = 0; i < 40; ++i) {
            for (double t : LINK_SAMPLES) {
                this.pushOut(i, t);
            }
        }
    }

    private void pushOut(int a, double t) {
        int b = a + 1;
        for (MonowireObstacles.Box box : this.bodies) {
            double depth;
            Vec3 normal;
            Vec3 sample = this.points[a].lerp(this.points[b], t);
            if (sample.distanceTo(this.points[0]) < 0.04) {
                return;
            }
            Vec3 d = sample.subtract(box.center());
            double lx = d.dot(box.x());
            double ly = d.dot(box.y());
            double lz = d.dot(box.z());
            double px = box.half().x + 0.018 - Math.abs(lx);
            double py = box.half().y + 0.018 - Math.abs(ly);
            double pz = box.half().z + 0.018 - Math.abs(lz);
            if (px <= 0.0 || py <= 0.0 || pz <= 0.0) continue;
            if (px <= py && px <= pz) {
                normal = box.x().scale(Math.signum(lx) == 0.0 ? 1.0 : Math.signum(lx));
                depth = px;
            } else if (py <= pz) {
                normal = box.y().scale(Math.signum(ly) == 0.0 ? 1.0 : Math.signum(ly));
                depth = py;
            } else {
                normal = box.z().scale(Math.signum(lz) == 0.0 ? 1.0 : Math.signum(lz));
                depth = pz;
            }
            double wa = (1.0 - t) * this.mass(a);
            double wb = t * this.mass(b);
            double divisor = (1.0 - t) * wa + t * wb;
            if (divisor < 1.0E-12) continue;
            Vec3 correction = normal.scale(depth / divisor);
            if (wa > 0.0) {
                this.points[a] = this.points[a].add(correction.scale(wa));
                this.dampContact(a, normal);
            }
            if (!(wb > 0.0)) continue;
            this.points[b] = this.points[b].add(correction.scale(wb));
            this.dampContact(b, normal);
        }
    }

    private double mass(int i) {
        return i == 0 || this.tipPinned && i == 40 ? 0.0 : 1.0;
    }

    private void solveDistance(int a, int b, double rest, double compliance, double[] lambda, int index) {
        Vec3 delta = this.points[b].subtract(this.points[a]);
        double distance = delta.length();
        if (distance < 1.0E-9) {
            return;
        }
        double wa = this.mass(a);
        double wb = this.mass(b);
        double alpha = compliance / 3.906250000000001E-5;
        double change = (-(distance - rest) - alpha * lambda[index]) / (wa + wb + alpha);
        int n = index;
        lambda[n] = lambda[n] + change;
        Vec3 normal = delta.scale(1.0 / distance);
        if (wa > 0.0) {
            this.points[a] = this.points[a].add(normal.scale(-wa * change));
        }
        if (wb > 0.0) {
            this.points[b] = this.points[b].add(normal.scale(wb * change));
        }
    }

    private void separate(int a, int b, double minimum) {
        Vec3 delta = this.points[b].subtract(this.points[a]);
        double distance = delta.length();
        double weights = this.mass(a) + this.mass(b);
        if (distance < 1.0E-9 || distance >= minimum || weights == 0.0) {
            return;
        }
        Vec3 correction = delta.scale((minimum - distance) / distance / weights);
        if (this.mass(a) > 0.0) {
            this.points[a] = this.points[a].subtract(correction);
        }
        if (this.mass(b) > 0.0) {
            this.points[b] = this.points[b].add(correction);
        }
    }

    private void selfCollision() {
        for (int i = 1; i <= 40; ++i) {
            for (int j = i + 3; j <= 40; ++j) {
                this.separate(i, j, 0.036);
            }
        }
    }

    private void contacts(MonowireBlockCollision.SegmentEnvironment[] blocks, Vec3[] before, boolean damp) {
        for (int i = 0; i < 40; ++i) {
            MonowireBlockCollision.SegmentContact contact = MonowireBlockCollision.findContact(blocks[i], before[i], before[i + 1], this.points[i], this.points[i + 1], 0.018, damp ? 0.98 : 1.0);
            if (contact == null || contact.correction().lengthSqr() < 1.0E-16) continue;
            double u = Mth.clamp((double)contact.sample(), (double)0.0, (double)1.0);
            double a = 1.0 - u;
            double b = u;
            double divisor = a * a * this.mass(i) + b * b * this.mass(i + 1);
            if (divisor < 1.0E-12) continue;
            if (this.mass(i) > 0.0) {
                this.points[i] = this.points[i].add(contact.correction().scale(a / divisor));
            }
            if (this.mass(i + 1) > 0.0) {
                this.points[i + 1] = this.points[i + 1].add(contact.correction().scale(b / divisor));
            }
            if (!damp) continue;
            this.dampContact(i, contact.normal());
            this.dampContact(i + 1, contact.normal());
        }
    }

    private void dampContact(int i, Vec3 normal) {
        if (i == 0) {
            return;
        }
        Vec3 velocity = this.points[i].subtract(this.previous[i]);
        double inward = velocity.dot(normal);
        if (inward < 0.0) {
            velocity = velocity.subtract(normal.scale(inward));
        }
        this.previous[i] = this.points[i].subtract(velocity.scale(0.98));
    }

    public static boolean intersects(Vec3[] before, Vec3[] after, AABB target, double radius) {
        AABB box = target.inflate(radius);
        for (int i = 0; i < after.length - 1; ++i) {
            double travel = Math.max(before[i].distanceTo(after[i]), before[i + 1].distanceTo(after[i + 1]));
            int samples = Mth.clamp((int)((int)Math.ceil(travel / 0.08)), (int)1, (int)32);
            for (int s = 0; s <= samples; ++s) {
                double t = (double)s / (double)samples;
                Vec3 start = before[i].lerp(after[i], t);
                Vec3 end = before[i + 1].lerp(after[i + 1], t);
                if (!box.contains(start) && !box.contains(end) && !box.clip(start, end).isPresent()) continue;
                return true;
            }
        }
        return false;
    }

    public static enum Lash {
        INWARD(50.0, 0.16),
        BACKHAND(50.0, 0.16),
        OVERHEAD(50.0, 0.16),
        CROSS(60.0, 0.22);

        final double sweep;
        final double impulse;

        private Lash(double sweep, double impulse) {
            this.sweep = sweep;
            this.impulse = impulse;
        }

        public static Lash of(int stroke) {
            return Lash.values()[Math.floorMod(stroke - 1, 3)];
        }
    }
}

