/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.neoforged.fml.ModList
 */
package com.chromeware.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.ModList;

public final class HemodynamicsCompatibility {
    private static final String API = "breenihilation.hemodynamics.client.api.HemodynamicsClientAPI";
    private static final MethodHandle BEGIN = HemodynamicsCompatibility.handle("beginArmHardware", MethodType.methodType(Void.TYPE, LivingEntity.class, HumanoidArm.class, Boolean.TYPE));
    private static final MethodHandle END = HemodynamicsCompatibility.handle("endArmHardware", MethodType.methodType(Void.TYPE, LivingEntity.class));

    private HemodynamicsCompatibility() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void arm(LivingEntity entity, HumanoidArm side, boolean blade, Runnable draw) {
        if (BEGIN == null || END == null) {
            draw.run();
            return;
        }
        try {
            BEGIN.invokeExact(entity, side, blade);
        }
        catch (Throwable failed) {
            draw.run();
            return;
        }
        try {
            draw.run();
        }
        finally {
            try {
                END.invokeExact(entity);
            }
            catch (Throwable throwable) {}
        }
    }

    private static MethodHandle handle(String name, MethodType type) {
        ModList mods = ModList.get();
        if (mods == null || !mods.isLoaded("hemodynamics")) {
            return null;
        }
        try {
            return MethodHandles.publicLookup().findStatic(Class.forName(API), name, type);
        }
        catch (LinkageError | ReflectiveOperationException absent) {
            return null;
        }
    }
}

