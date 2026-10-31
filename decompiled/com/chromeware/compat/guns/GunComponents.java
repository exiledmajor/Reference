/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.resources.ResourceKey
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.chromeware.compat.guns;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GunComponents {
    public static final DeferredRegister<DataComponentType<?>> TYPES = DeferredRegister.create((ResourceKey)Registries.DATA_COMPONENT_TYPE, (String)"chromeware");
    public static final Supplier<DataComponentType<Integer>> MICROGENERATOR = TYPES.register("microgenerator_charge", () -> DataComponentType.builder().persistent(Codec.intRange((int)0, (int)5)).networkSynchronized(ByteBufCodecs.VAR_INT).build());
}

