/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.body;

import java.util.Locale;

public enum BodyPart {
    LEFT_ARM,
    RIGHT_ARM,
    LEFT_HAND,
    RIGHT_HAND,
    LEFT_LEG,
    RIGHT_LEG,
    LEFT_FOOT,
    RIGHT_FOOT,
    HEAD,
    BODY;


    public String key() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public int bit() {
        return 1 << this.ordinal();
    }

    public BodyPart parent() {
        return switch (this.ordinal()) {
            case 2 -> LEFT_ARM;
            case 3 -> RIGHT_ARM;
            case 6 -> LEFT_LEG;
            case 7 -> RIGHT_LEG;
            case 0, 1, 4, 5 -> BODY;
            default -> null;
        };
    }

    public int subtreeMask() {
        return switch (this.ordinal()) {
            case 0 -> this.bit() | LEFT_HAND.bit();
            case 1 -> this.bit() | RIGHT_HAND.bit();
            case 4 -> this.bit() | LEFT_FOOT.bit();
            case 5 -> this.bit() | RIGHT_FOOT.bit();
            case 9 -> this.bit() | 0xFF;
            default -> this.bit();
        };
    }

    public static BodyPart fromKey(String key) {
        if (key.equalsIgnoreCase("torso")) {
            return BODY;
        }
        return BodyPart.valueOf(key.toUpperCase(Locale.ROOT));
    }

    public boolean isVital() {
        return this == HEAD;
    }

    public float maxIntegrity(float maxHealth) {
        return Math.max(1.0f, maxHealth * (switch (this.ordinal()) {
            case 8 -> 1.0f;
            case 9 -> 2.0f;
            case 0, 1, 4, 5 -> 0.75f;
            default -> 0.4f;
        }));
    }
}

