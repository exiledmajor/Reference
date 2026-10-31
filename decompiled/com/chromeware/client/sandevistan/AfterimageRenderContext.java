/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.FastColor$ARGB32
 */
package com.chromeware.client.sandevistan;

import net.minecraft.util.FastColor;

public final class AfterimageRenderContext {
    private static final ThreadLocal<State> CURRENT = new ThreadLocal();

    private AfterimageRenderContext() {
    }

    public static Scope push(int color, float alpha) {
        return AfterimageRenderContext.push(color, alpha, false);
    }

    public static Scope pushPalette(int color) {
        return AfterimageRenderContext.push(color, 1.0f, true);
    }

    private static Scope push(int color, float alpha, boolean palette) {
        State previous = CURRENT.get();
        CURRENT.set(new State((float)FastColor.ARGB32.red((int)color) / 255.0f, (float)FastColor.ARGB32.green((int)color) / 255.0f, (float)FastColor.ARGB32.blue((int)color) / 255.0f, alpha, palette, color));
        return new Scope(previous);
    }

    public static boolean isRendering() {
        return CURRENT.get() != null;
    }

    public static State current() {
        return CURRENT.get();
    }

    public static final class Scope
    implements AutoCloseable {
        private final State previous;
        private boolean closed;

        private Scope(State previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (this.previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(this.previous);
            }
        }
    }

    public record State(float red, float green, float blue, float alpha, boolean palette, int color) {
    }
}

