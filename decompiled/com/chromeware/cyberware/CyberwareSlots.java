/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.InstalledImplant;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record CyberwareSlots(List<InstalledImplant> slots) {
    public static final int SIZE = 3;
    public static final CyberwareSlots EMPTY = new CyberwareSlots(List.of(InstalledImplant.EMPTY, InstalledImplant.EMPTY, InstalledImplant.EMPTY));
    public static final Codec<CyberwareSlots> CODEC = InstalledImplant.CODEC.listOf(3, 3).xmap(CyberwareSlots::new, CyberwareSlots::slots);
    public static final StreamCodec<RegistryFriendlyByteBuf, CyberwareSlots> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, CyberwareSlots>(){

        public CyberwareSlots decode(RegistryFriendlyByteBuf buffer) {
            ArrayList<InstalledImplant> slots = new ArrayList<InstalledImplant>();
            for (int i = 0; i < 3; ++i) {
                slots.add((InstalledImplant)InstalledImplant.STREAM_CODEC.decode((Object)buffer));
            }
            return new CyberwareSlots(slots);
        }

        public void encode(RegistryFriendlyByteBuf buffer, CyberwareSlots value) {
            value.slots.forEach(slot -> InstalledImplant.STREAM_CODEC.encode((Object)buffer, slot));
        }
    };

    public CyberwareSlots {
        if (slots.size() != 3) {
            throw new IllegalArgumentException("Exactly three cyberware sockets required");
        }
        slots = List.copyOf(slots);
    }

    public InstalledImplant get(int index) {
        return this.slots.get(index);
    }

    public CyberwareSlots with(int index, InstalledImplant implant) {
        ArrayList<InstalledImplant> next = new ArrayList<InstalledImplant>(this.slots);
        next.set(index, implant);
        return new CyberwareSlots(next);
    }
}

