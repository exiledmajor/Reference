/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvent
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.chromeware.humanity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class HumanitySounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create((ResourceKey)Registries.SOUND_EVENT, (String)"chromeware");
    public static final SoundEvent GLITCH = HumanitySounds.event("psychosis_glitch");
    public static final SoundEvent GLITCH_THRESHOLD = HumanitySounds.event("psychosis_glitch_threshold");
    public static final SoundEvent HEARTBEAT = HumanitySounds.event("psychosis_heartbeat");
    public static final SoundEvent HEARTBEAT_THRESHOLD = HumanitySounds.event("psychosis_heartbeat_threshold");

    private static SoundEvent event(String name) {
        SoundEvent sound = SoundEvent.createVariableRangeEvent((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name));
        SOUNDS.register(name, () -> sound);
        return sound;
    }
}

