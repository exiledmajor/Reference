/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.resources.sounds.EntityBoundSoundInstance
 *  net.minecraft.world.entity.Entity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 */
package com.chromeware.mixin.client;

import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={EntityBoundSoundInstance.class})
public interface SandevistanSoundAccessor {
    @Accessor(value="entity")
    public Entity chrome$entity();
}

