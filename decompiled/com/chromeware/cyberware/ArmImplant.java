/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemStack
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.CyberwareItems;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Predicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public record ArmImplant(ItemStack item, boolean left) {
    private final ItemStack item;
    public static final ArmImplant EMPTY = new ArmImplant(ItemStack.EMPTY, false);
    public static final Codec<ArmImplant> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemStack.OPTIONAL_CODEC.fieldOf("item").forGetter(ArmImplant::item), (App)Codec.BOOL.fieldOf("left").forGetter(ArmImplant::left)).apply((Applicative)instance, ArmImplant::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmImplant> STREAM_CODEC = StreamCodec.composite((StreamCodec)ItemStack.OPTIONAL_STREAM_CODEC, ArmImplant::item, (StreamCodec)ByteBufCodecs.BOOL, ArmImplant::left, ArmImplant::new);

    public ArmImplant(ItemStack item, HumanoidArm side) {
        this(item, side == HumanoidArm.LEFT);
    }

    public ArmImplant(ItemStack item, boolean left) {
        this.item = item = item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(1);
        this.left = left;
    }

    public ItemStack item() {
        return this.item.copy();
    }

    public boolean installed() {
        return !this.item.isEmpty();
    }

    public boolean is(Predicate<ItemStack> test) {
        return !this.item.isEmpty() && test.test(this.item);
    }

    public boolean replacesLimb() {
        return this.item.is(CyberwareItems.GORILLA_ARMS);
    }

    public HumanoidArm arm() {
        return this.left ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }
}

