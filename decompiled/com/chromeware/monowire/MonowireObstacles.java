/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.monowire;

import com.chromeware.body.BodyPart;
import com.chromeware.body.hitbox.BodyBox;
import com.chromeware.body.hitbox.BodyHitboxes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class MonowireObstacles {
    public static List<Box> of(Player owner, HumanoidArm side, boolean creatures, float tick) {
        return MonowireObstacles.of(owner, side, creatures, tick, null);
    }

    public static List<Box> of(Player owner, HumanoidArm side, boolean creatures, float tick, List<Box> drawnArms) {
        ArrayList<Box> result = new ArrayList<Box>();
        for (BodyBox box : BodyHitboxes.boxes(owner, tick)) {
            if (drawnArms != null && MonowireObstacles.arm(box.part())) continue;
            result.add(new Box(box.center(), box.axisX(), box.axisY(), box.axisZ(), box.half()));
        }
        if (drawnArms != null) {
            result.addAll(drawnArms);
        }
        if (creatures) {
            AABB range = owner.getBoundingBox().inflate(9.0);
            for (LivingEntity entity : owner.level().getEntitiesOfClass(LivingEntity.class, range, e -> e != owner && e.isAlive() && !e.isSpectator() && !e.isInvisible())) {
                result.add(Box.aabb(entity.getBoundingBox()));
            }
        }
        return result;
    }

    private static boolean arm(BodyPart part) {
        return switch (part) {
            case BodyPart.LEFT_ARM, BodyPart.RIGHT_ARM, BodyPart.LEFT_HAND, BodyPart.RIGHT_HAND -> true;
            default -> false;
        };
    }

    public record Box(Vec3 center, Vec3 x, Vec3 y, Vec3 z, Vec3 half) {
        static Box aabb(AABB box) {
            return new Box(box.getCenter(), new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 1.0, 0.0), new Vec3(0.0, 0.0, 1.0), new Vec3(box.getXsize() / 2.0, box.getYsize() / 2.0, box.getZsize() / 2.0));
        }
    }
}

