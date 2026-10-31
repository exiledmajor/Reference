/*
 * Decompiled with CFR 0.152.
 */
package com.chromeware.cyberware;

public enum ImplantCategory {
    CIRCULATORY(0, "circulatory_system", 3),
    FRONTAL_CORTEX(1, "frontal_cortex", 3),
    SKELETON(2, "skeleton", 3),
    NERVOUS_SYSTEM(3, "nervous_system", 3),
    INTEGUMENTARY_SYSTEM(4, "integumentary_system", 3),
    LEGS(5, "legs", 1),
    HANDS(6, "hands", 2),
    FACE(7, "face", 2),
    OPERATING_SYSTEM(8, "operating_system", 1);

    public final int index;
    public final int sockets;
    public final String key;

    private ImplantCategory(int index, String key, int sockets) {
        this.index = index;
        this.key = key;
        this.sockets = sockets;
    }

    public static ImplantCategory byIndex(int index) {
        for (ImplantCategory category : ImplantCategory.values()) {
            if (category.index != index) continue;
            return category;
        }
        return null;
    }

    public static ImplantCategory byKey(String key) {
        for (ImplantCategory category : ImplantCategory.values()) {
            if (!category.key.equals(key)) continue;
            return category;
        }
        return null;
    }
}

