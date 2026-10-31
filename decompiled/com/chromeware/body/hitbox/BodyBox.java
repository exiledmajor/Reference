/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaterniond
 *  org.joml.Vector3d
 */
package com.chromeware.body.hitbox;

import com.chromeware.body.BodyPart;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public record BodyBox(BodyPart part, Vec3 center, Vec3 axisX, Vec3 axisY, Vec3 axisZ, Vec3 half) {
    public BodyBox {
        axisX = axisX.normalize();
        axisY = axisY.normalize();
        axisZ = axisZ.normalize();
    }

    public static BodyBox rotated(BodyPart part, Vec3 center, Quaterniond rotation, Vec3 half) {
        return new BodyBox(part, center, BodyBox.transform(rotation, new Vec3(1.0, 0.0, 0.0)), BodyBox.transform(rotation, new Vec3(0.0, 1.0, 0.0)), BodyBox.transform(rotation, new Vec3(0.0, 0.0, 1.0)), half);
    }

    public static Vec3 transform(Quaterniond rotation, Vec3 vector) {
        Vector3d result = rotation.transform(new Vector3d(vector.x, vector.y, vector.z));
        return new Vec3(result.x, result.y, result.z);
    }

    public Optional<Vec3> raycast(Vec3 start, Vec3 end, double padding) {
        Vec3 local = start.subtract(this.center);
        Vec3 delta = end.subtract(start);
        double[] origin = new double[]{local.dot(this.axisX), local.dot(this.axisY), local.dot(this.axisZ)};
        double[] direction = new double[]{delta.dot(this.axisX), delta.dot(this.axisY), delta.dot(this.axisZ)};
        double[] extent = new double[]{this.half.x + padding, this.half.y + padding, this.half.z + padding};
        double near = 0.0;
        double far = 1.0;
        for (int axis = 0; axis < 3; ++axis) {
            double b;
            double a;
            if (!(Math.abs(direction[axis]) < 1.0E-8 ? Math.abs(origin[axis]) > extent[axis] : (near = Math.max(near, Math.min(a = (-extent[axis] - origin[axis]) / direction[axis], b = (extent[axis] - origin[axis]) / direction[axis]))) > (far = Math.min(far, Math.max(a, b))))) continue;
            return Optional.empty();
        }
        return Optional.of(start.add(delta.scale(near)));
    }

    public static Optional<Hit> nearest(List<BodyBox> boxes, Vec3 start, Vec3 end, double padding) {
        Hit best = null;
        for (BodyBox box : boxes) {
            Optional<Vec3> point = box.raycast(start, end, padding);
            if (!point.isPresent()) continue;
            double distance = start.distanceTo(point.get());
            if (best != null && !(distance < best.distance())) continue;
            best = new Hit(box.part, point.get(), distance);
        }
        return Optional.ofNullable(best);
    }

    public record Hit(BodyPart part, Vec3 point, double distance) {
    }
}

