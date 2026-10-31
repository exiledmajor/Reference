/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 */
package com.chromeware.cyberware;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public final class TrueDamage {
    public static final ResourceKey<DamageType> TYPE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"true_damage"));
    public static final ResourceKey<DamageType> SUICIDE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"suicide"));
    public static final ResourceKey<DamageType> SYSTEM_COLLAPSE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"system_collapse"));

    public static DamageSource source(Entity attacker) {
        return new DamageSource((Holder)attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TYPE), attacker);
    }

    public static DamageSource source(ResourceKey<DamageType> type, Entity attacker) {
        return new DamageSource((Holder)attacker.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type), attacker);
    }

    public static boolean is(DamageSource source) {
        return source.is(TYPE) || source.is(SUICIDE) || source.is(SYSTEM_COLLAPSE);
    }
}

