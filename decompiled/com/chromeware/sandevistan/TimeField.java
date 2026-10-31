/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.level.Level
 */
package com.chromeware.sandevistan;

import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record TimeField(UUID owner, ResourceKey<Level> dimension, double x, double y, double z, double radius, int slowdown, long origin, int mode) {
    public static final int SANDEVISTAN = 0;
    public static final int REFLEX = 1;
    public static final int REVULSOR = 2;

    public TimeField(UUID owner, ResourceKey<Level> dimension, double x, double y, double z, double radius, int slowdown, long origin) {
        this(owner, dimension, x, y, z, radius, slowdown, origin, 0);
    }

    public boolean ownerImmune() {
        return this.mode != 1;
    }

    public boolean contains(double px, double pz) {
        return TimeField.contains(this.x, this.z, px, pz, this.radius);
    }

    public static boolean contains(double centerX, double centerZ, double x, double z, double halfSize) {
        return Math.abs(x - centerX) <= halfSize && Math.abs(z - centerZ) <= halfSize;
    }
}

