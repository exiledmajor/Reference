/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Camera
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package com.chromeware.client.monowire;

import com.chromeware.mixin.client.MonowireFovAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public final class MonowireProjection {
    public static Vec3 toCamera(Vec3 vector, Camera camera) {
        Vector3f v = vector.toVector3f().rotate((Quaternionfc)new Quaternionf((Quaternionfc)camera.rotation()).conjugate());
        return new Vec3((double)v.x, (double)v.y, (double)v.z);
    }

    public static Vec3 toWorld(Vec3 vector, Camera camera) {
        Vector3f v = vector.toVector3f().rotate((Quaternionfc)camera.rotation());
        return new Vec3((double)v.x, (double)v.y, (double)v.z);
    }

    public static double ratio(float tick) {
        Minecraft mc = Minecraft.getInstance();
        MonowireFovAccessor fov = (MonowireFovAccessor)mc.gameRenderer;
        Camera camera = mc.gameRenderer.getMainCamera();
        double world = fov.chrome$wireFov(camera, tick, true);
        double hand = fov.chrome$wireFov(camera, tick, false);
        return Math.tan(Math.toRadians(world * 0.5)) / Math.tan(Math.toRadians(hand * 0.5));
    }

    public static Vec3 handToWorld(Vec3 relative, Camera camera, double ratio) {
        Vec3 v = MonowireProjection.toCamera(relative, camera);
        return MonowireProjection.toWorld(new Vec3(v.x * ratio, v.y * ratio, v.z), camera);
    }
}

