/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.api.physics.PhysicsPipeline
 *  dev.ryanhcode.sable.api.physics.PhysicsPipelineBody
 *  dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer
 *  dev.ryanhcode.sable.companion.math.Pose3d
 *  dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData
 *  dev.ryanhcode.sable.sublevel.ServerSubLevel
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.world.level.Level
 *  org.joml.Quaterniondc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package com.chromeware.sandevistan;

import com.chromeware.sandevistan.SandevistanContraptions;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.PhysicsPipelineBody;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.IdentityHashMap;
import net.minecraft.world.level.Level;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class SandevistanPhysics {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void step(ServerSubLevelContainer container, PhysicsPipeline pipeline, double dt, Runnable solve) {
        IdentityHashMap<ServerSubLevel, Double> scaled = new IdentityHashMap<ServerSubLevel, Double>();
        for (ServerSubLevel body2 : container.getAllSubLevels()) {
            double scale2 = SandevistanContraptions.scale((SubLevel)body2);
            if (body2.isRemoved() || scale2 >= 1.0) continue;
            scale2 = Math.max(0.01, scale2);
            scaled.put(body2, scale2);
            Vector3d v = pipeline.getLinearVelocity((PhysicsPipelineBody)body2, new Vector3d());
            Vector3d w = pipeline.getAngularVelocity((PhysicsPipelineBody)body2, new Vector3d());
            Vector3d correction = DimensionPhysicsData.getGravity((Level)body2.getLevel()).mul((scale2 * scale2 - 1.0) * dt);
            pipeline.addLinearAndAngularVelocity((PhysicsPipelineBody)body2, (Vector3dc)v.mul(scale2 - 1.0).add((Vector3dc)correction), (Vector3dc)w.mul(scale2 - 1.0));
        }
        try {
            solve.run();
        }
        finally {
            scaled.forEach((body, scale) -> {
                if (body.isRemoved()) {
                    return;
                }
                Vector3d v = pipeline.getLinearVelocity((PhysicsPipelineBody)body, new Vector3d());
                Vector3d w = pipeline.getAngularVelocity((PhysicsPipelineBody)body, new Vector3d());
                pipeline.addLinearAndAngularVelocity((PhysicsPipelineBody)body, (Vector3dc)v.mul(1.0 / scale - 1.0), (Vector3dc)w.mul(1.0 / scale - 1.0));
                Pose3d pose = pipeline.readPose(body, new Pose3d());
                pose.position().add((Vector3dc)DimensionPhysicsData.getGravity((Level)body.getLevel()).mul((1.0 - scale * scale) * dt * dt * 0.5));
                pipeline.teleport((PhysicsPipelineBody)body, (Vector3dc)pose.position(), (Quaterniondc)pose.orientation());
            });
        }
    }
}

