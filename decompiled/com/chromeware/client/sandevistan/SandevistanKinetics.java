/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.content.kinetics.base.KineticBlockEntity
 *  com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer
 *  com.simibubi.create.content.kinetics.base.RotatingInstance
 *  net.createmod.catnip.animation.AnimationTickHolder
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.world.level.LevelAccessor
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.SandevistanClient;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

public final class SandevistanKinetics {
    private static final Map<KineticBlockEntity, Clock> CLOCKS = Collections.synchronizedMap(new WeakHashMap());
    private static final Map<RotatingInstance, Binding> INSTANCES = Collections.synchronizedMap(new WeakHashMap());
    private static final ThreadLocal<Boolean> UPDATING = ThreadLocal.withInitial(() -> false);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static double time(KineticBlockEntity be) {
        double now = AnimationTickHolder.getRenderTime((LevelAccessor)be.getLevel());
        if (!CLOCKS.containsKey(be) && SandevistanClient.slowdownPercentAt(be.getLevel(), be.getBlockPos()) == 0) {
            return now;
        }
        Map<KineticBlockEntity, Clock> map = CLOCKS;
        synchronized (map) {
            Clock clock = CLOCKS.computeIfAbsent(be, ignored -> new Clock(now));
            double elapsed = Math.max(0.0, now - clock.last);
            clock.time += elapsed * (1.0 - (double)SandevistanClient.slowdownPercentAt(be.getLevel(), be.getBlockPos()) / 100.0);
            clock.last = now;
            return clock.time;
        }
    }

    public static boolean affected(KineticBlockEntity be) {
        return CLOCKS.containsKey(be) || SandevistanClient.slowdownPercentAt(be.getLevel(), be.getBlockPos()) > 0;
    }

    public static float angle(KineticBlockEntity be, BlockPos pos, Direction.Axis axis) {
        float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition((KineticBlockEntity)be, (BlockPos)pos, (Direction.Axis)axis);
        return (float)((SandevistanKinetics.time(be) * (double)be.getSpeed() * 0.3 + (double)offset) % 360.0 * Math.PI / 180.0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void bind(RotatingInstance instance, KineticBlockEntity be, float speed, float offset) {
        INSTANCES.put(instance, new Binding(be, speed, offset));
        if (CLOCKS.containsKey(be)) {
            double now = AnimationTickHolder.getRenderTime((LevelAccessor)be.getLevel());
            UPDATING.set(true);
            try {
                instance.setRotationOffset((float)(((double)offset + (SandevistanKinetics.time(be) - now) * (double)speed * 0.3) % 360.0));
            }
            finally {
                UPDATING.set(false);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void speed(RotatingInstance instance, float degreesPerSecond) {
        if (UPDATING.get().booleanValue()) {
            return;
        }
        Map<RotatingInstance, Binding> map = INSTANCES;
        synchronized (map) {
            Binding b = INSTANCES.get(instance);
            if (b != null) {
                b.speed = degreesPerSecond / 6.0f;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void offset(RotatingInstance instance, float offset) {
        if (UPDATING.get().booleanValue()) {
            return;
        }
        Map<RotatingInstance, Binding> map = INSTANCES;
        synchronized (map) {
            Binding b = INSTANCES.get(instance);
            if (b != null) {
                b.offset = offset;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void frame() {
        Map<RotatingInstance, Binding> map = INSTANCES;
        synchronized (map) {
            UPDATING.set(true);
            try {
                INSTANCES.entrySet().removeIf(e -> ((Binding)e.getValue()).be.isRemoved() || ((Binding)e.getValue()).be.getLevel() != Minecraft.getInstance().level);
                INSTANCES.forEach((instance, b) -> {
                    if (SandevistanClient.slowdownPercentAt(b.be.getLevel(), b.be.getBlockPos()) > 0) {
                        instance.setRotationalSpeed(0.0f).setRotationOffset((float)((SandevistanKinetics.time(b.be) * (double)b.speed * 0.3 + (double)b.offset) % 360.0)).setChanged();
                        b.overridden = true;
                    } else if (b.overridden) {
                        double now = AnimationTickHolder.getRenderTime((LevelAccessor)b.be.getLevel());
                        instance.setRotationalSpeed(b.speed * 6.0f).setRotationOffset((float)(((double)b.offset + (SandevistanKinetics.time(b.be) - now) * (double)b.speed * 0.3) % 360.0)).setChanged();
                        b.overridden = false;
                    }
                });
            }
            finally {
                UPDATING.set(false);
            }
        }
    }

    public static void clear() {
        CLOCKS.clear();
        INSTANCES.clear();
    }

    private static final class Clock {
        double time;
        double last;

        Clock(double time) {
            this.time = this.last = time;
        }
    }

    private static final class Binding {
        final KineticBlockEntity be;
        float speed;
        float offset;
        boolean overridden;

        Binding(KineticBlockEntity be, float speed, float offset) {
            this.be = be;
            this.speed = speed;
            this.offset = offset;
        }
    }
}

