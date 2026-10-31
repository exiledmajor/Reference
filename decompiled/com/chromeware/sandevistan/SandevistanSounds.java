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
package com.chromeware.sandevistan;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SandevistanSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create((ResourceKey)Registries.SOUND_EVENT, (String)"chromeware");
    public static final SoundEvent SANDY_ON = SoundEvent.createVariableRangeEvent((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandy_on"));
    public static final SoundEvent SANDY_OFF = SoundEvent.createVariableRangeEvent((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandy_off"));

    static {
        SOUNDS.register("sandy_on", () -> SANDY_ON);
        SOUNDS.register("sandy_off", () -> SANDY_OFF);
    }
}

