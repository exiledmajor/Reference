/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.client.cyberware;

import java.util.List;

public record CyberwareMenuLayout(float scale, float offsetX, float offsetY) {
    public static final int WIDTH = 640;
    public static final int HEIGHT = 360;
    public static final int SLOT_SIZE = 28;
    public static final int SLOT_GAP = 10;
    public static final Rect MODEL = new Rect(248, 40, 144, 286);
    public static final Rect AMOUNT = new Rect(42, 58, 12, 252);
    public static final Rect TOXICITY = new Rect(586, 58, 12, 252);
    public static final List<Group> GROUPS = List.of(CyberwareMenuLayout.left("frontal_cortex", 3, 244, 58), CyberwareMenuLayout.left("arms", 2, 244, 112), CyberwareMenuLayout.left("skeleton", 3, 244, 166), CyberwareMenuLayout.left("nervous_system", 3, 244, 220), CyberwareMenuLayout.left("integumentary_system", 3, 244, 274), CyberwareMenuLayout.right("operating_system", 1, 396, 58), CyberwareMenuLayout.right("face", 2, 396, 112), CyberwareMenuLayout.right("hands", 2, 396, 166), CyberwareMenuLayout.right("circulatory_system", 3, 396, 220), CyberwareMenuLayout.right("legs", 1, 396, 274));
    public static final int CANVAS_WIDTH = 427;
    public static final int CANVAS_HEIGHT = 240;

    public static CyberwareMenuLayout fit(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Positive screen dimensions required");
        }
        float scale = CyberwareMenuLayout.canvasScale(width, height) * 427.0f / 640.0f;
        return new CyberwareMenuLayout(scale, (float)Math.floor(((float)width - 640.0f * scale) / 2.0f), (float)Math.floor(((float)height - 360.0f * scale) / 2.0f));
    }

    public static float canvasScale(int width, int height) {
        float fit = Math.min((float)width / 427.0f, (float)height / 240.1875f);
        float whole = (float)Math.floor(fit);
        return whole >= 1.0f && whole >= 0.85f * fit ? whole : fit;
    }

    public float screenX(double x) {
        return this.offsetX + (float)x * this.scale;
    }

    public float screenY(double y) {
        return this.offsetY + (float)y * this.scale;
    }

    private static Group left(String id, int count, int edge, int y) {
        return new Group(id, count, edge - count * 28 - (count - 1) * 10, y, true);
    }

    private static Group right(String id, int count, int edge, int y) {
        return new Group(id, count, edge, y, false);
    }

    public record Group(String id, int count, int x, int y, boolean left) {
        public Rect slot(int index) {
            if (index < 0 || index >= this.count) {
                throw new IndexOutOfBoundsException(index);
            }
            return new Rect(this.x + index * 38, this.y, 28, 28);
        }

        public int labelAnchor() {
            return this.left ? this.x + this.count * 28 + (this.count - 1) * 10 : this.x;
        }
    }

    public record Rect(int x, int y, int width, int height) {
        public int right() {
            return this.x + this.width;
        }

        public int bottom() {
            return this.y + this.height;
        }

        public boolean contains(double px, double py) {
            return px >= (double)this.x && px < (double)this.right() && py >= (double)this.y && py < (double)this.bottom();
        }
    }

    public record Slot(String category, int index, Rect bounds) {
    }
}

