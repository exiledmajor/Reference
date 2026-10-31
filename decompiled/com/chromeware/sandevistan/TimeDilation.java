/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.Level
 */
package com.chromeware.sandevistan;

import com.chromeware.sandevistan.Sandevistan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class TimeDilation {
    public static ClientClock client;

    public static boolean advance(Level level, BlockPos pos) {
        return level.isClientSide ? client == null || client.advance(pos) : Sandevistan.shouldAdvance(level, pos);
    }

    public static interface ClientClock {
        public boolean advance(BlockPos var1);
    }
}

