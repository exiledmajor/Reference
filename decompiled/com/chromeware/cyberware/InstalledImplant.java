/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record InstalledImplant(ItemStack item) {
    private final ItemStack item;
    public static final InstalledImplant EMPTY = new InstalledImplant(ItemStack.EMPTY);
    public static final Codec<InstalledImplant> CODEC = ItemStack.OPTIONAL_CODEC.xmap(InstalledImplant::new, InstalledImplant::item);
    public static final StreamCodec<RegistryFriendlyByteBuf, InstalledImplant> STREAM_CODEC = ItemStack.OPTIONAL_STREAM_CODEC.map(InstalledImplant::new, InstalledImplant::item);

    public InstalledImplant(ItemStack item) {
        this.item = item = item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(1);
    }

    public ItemStack item() {
        return this.item.copy();
    }

    public boolean installed() {
        return !this.item.isEmpty();
    }
}

