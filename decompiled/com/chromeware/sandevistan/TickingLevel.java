/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 */
package com.chromeware.sandevistan;

import net.minecraft.server.level.ServerLevel;

public final class TickingLevel {
    private static final ThreadLocal<ServerLevel> LEVEL = new ThreadLocal();

    public static void enter(ServerLevel level) {
        LEVEL.set(level);
    }

    public static void exit() {
        LEVEL.remove();
    }

    public static ServerLevel level() {
        return LEVEL.get();
    }
}

