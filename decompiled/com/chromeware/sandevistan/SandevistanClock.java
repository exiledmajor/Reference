/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.sandevistan;

public final class SandevistanClock {
    private SandevistanClock() {
    }

    public static TimeSlice sample(long serverTick, long originTick, int slowdownPercent) {
        int slowdown = SandevistanClock.clampSlowdown(slowdownPercent);
        double scale = (100.0 - (double)slowdown) / 100.0;
        long elapsed = Math.max(0L, serverTick - originTick);
        long previousElapsed = Math.max(0L, elapsed - 1L);
        double logicalTime = (double)elapsed * scale;
        long logicalTick = (long)Math.floor(logicalTime);
        long previousLogicalTick = (long)Math.floor((double)previousElapsed * scale);
        float phase = (float)(logicalTime - Math.floor(logicalTime));
        boolean logicalStep = slowdown == 0 || slowdown < 100 && logicalTick != previousLogicalTick;
        return new TimeSlice(slowdown, (float)scale, logicalTick, phase, (float)scale, logicalStep);
    }

    public static boolean shouldAdvance(long serverTick, long originTick, int slowdownPercent) {
        return SandevistanClock.sample(serverTick, originTick, slowdownPercent).logicalStep();
    }

    public static float renderPhase(long serverTick, float tickDelta, long originTick, int slowdownPercent) {
        long nextBoundary;
        long previousBoundary;
        TimeSlice now = SandevistanClock.sample(serverTick, originTick, slowdownPercent);
        if (now.timeScale() >= 1.0f) {
            return Math.max(0.0f, Math.min(1.0f, tickDelta));
        }
        if (now.timeScale() <= 0.0f) {
            return 1.0f;
        }
        for (previousBoundary = serverTick; previousBoundary > serverTick - 200L && !SandevistanClock.shouldAdvance(previousBoundary, originTick, slowdownPercent); --previousBoundary) {
        }
        for (nextBoundary = serverTick + 1L; nextBoundary < serverTick + 200L && !SandevistanClock.shouldAdvance(nextBoundary, originTick, slowdownPercent); ++nextBoundary) {
        }
        float span = Math.max(1.0f, (float)(nextBoundary - previousBoundary));
        return Math.max(0.0f, Math.min(0.999f, ((float)(serverTick - previousBoundary) + tickDelta) / span));
    }

    public static int clampSlowdown(int slowdownPercent) {
        return Math.max(0, Math.min(100, slowdownPercent));
    }

    public record TimeSlice(int slowdownPercent, float timeScale, long logicalTick, float phase, float logicalDelta, boolean logicalStep) {
    }
}

