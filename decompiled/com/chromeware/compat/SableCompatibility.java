/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.fml.ModList
 */
package com.chromeware.compat;

import net.neoforged.fml.ModList;

public final class SableCompatibility {
    public static boolean loaded() {
        ModList mods = ModList.get();
        return mods != null && mods.isLoaded("sable");
    }
}

