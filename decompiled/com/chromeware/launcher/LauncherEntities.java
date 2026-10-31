/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.MobCategory
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.chromeware.launcher;

import com.chromeware.launcher.NuggetShot;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LauncherEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"chromeware");
    public static final DeferredHolder<EntityType<?>, EntityType<NuggetShot>> NUGGET = ENTITIES.register("launcher_nugget", () -> EntityType.Builder.of(NuggetShot::new, (MobCategory)MobCategory.MISC).sized(0.2f, 0.2f).clientTrackingRange(4).updateInterval(10).build("launcher_nugget"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}

