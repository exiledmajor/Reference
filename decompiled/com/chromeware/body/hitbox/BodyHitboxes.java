/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 */
package com.chromeware.body.hitbox;

import com.chromeware.body.BodyGeometry;
import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbRegrowth;
import com.chromeware.body.LimbState;
import com.chromeware.body.hitbox.BodyBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;

public final class BodyHitboxes {
    public static final double SKIN_SCALE = 0.9375;
    public static Function<Player, List<BodyBox>> clientBoxes;

    public static double modelDrop(Player player) {
        return BodyGeometry.drop(player);
    }

    public static List<BodyBox> boxes(Player player, float tick) {
        List<BodyBox> sampled;
        if (player.level().isClientSide() && clientBoxes != null && (sampled = clientBoxes.apply(player)) != null) {
            return sampled;
        }
        double swing = (double)player.walkAnimation.position(tick) * 0.6662;
        double amount = Math.min(1.0f, player.walkAnimation.speed(tick));
        double yaw = Math.toRadians(-Mth.rotLerp((float)tick, (float)player.yBodyRotO, (float)player.yBodyRot));
        double headYaw = Math.toRadians(-Mth.wrapDegrees((float)(player.getYHeadRot() - player.yBodyRot)));
        double headPitch = Math.toRadians(-player.getXRot());
        return BodyHitboxes.build(LimbAttachments.functional(player), player.position().add(0.0, -BodyHitboxes.modelDrop(player), 0.0), yaw, headYaw, headPitch, player.getScale(), swing, amount, player.isCrouching(), player.isPassenger(), player.isVisuallySwimming() || player.isFallFlying(), false);
    }

    public static List<BodyBox> build(LimbState state, Vec3 position, double yaw, double headYaw, double headPitch, double scale, double phase, double amount, boolean crouch, boolean passenger, boolean horizontal) {
        return BodyHitboxes.build(state, position, yaw, headYaw, headPitch, scale, phase, amount, crouch, passenger, horizontal, true);
    }

    private static List<BodyBox> build(LimbState state, Vec3 position, double yaw, double headYaw, double headPitch, double scale, double phase, double amount, boolean crouch, boolean passenger, boolean horizontal, boolean lowerRoot) {
        double drop = lowerRoot ? (double)BodyGeometry.drop(state, LimbRegrowth.NONE, passenger) * scale : 0.0;
        Vec3 origin = position.add(0.0, -drop, 0.0);
        scale *= 0.9375;
        Quaterniond root = new Quaterniond().rotationY(yaw);
        if (horizontal) {
            root.rotateX(1.5707963267948966);
        }
        Quaterniond torso = new Quaterniond((Quaterniondc)root).rotateX(crouch ? -0.5 : 0.0);
        double lower = crouch ? 0.2 : 0.0;
        ArrayList<BodyBox> result = new ArrayList<BodyBox>();
        Vec3 torsoPivot = origin.add(BodyBox.transform(root, new Vec3(0.0, (1.5 - lower) * scale, 0.0)));
        BodyHitboxes.add(result, state, BodyPart.BODY, torsoPivot, torso, new Vec3(0.0, -0.375, 0.0), new Vec3(0.25, 0.375, 0.125), scale);
        Quaterniond head = new Quaterniond((Quaterniondc)root).rotateY(headYaw).rotateX(headPitch);
        Vec3 headPivot = origin.add(BodyBox.transform(root, new Vec3(0.0, (1.5 - lower) * scale, 0.0)));
        BodyHitboxes.add(result, state, BodyPart.HEAD, headPivot, head, new Vec3(0.0, 0.25, 0.0), new Vec3(0.25, 0.25, 0.25), scale);
        for (boolean right : new boolean[]{false, true}) {
            double side = right ? -1.0 : 1.0;
            Quaterniond arm = new Quaterniond((Quaterniondc)torso).rotateX(-Math.cos(phase + (right ? Math.PI : 0.0)) * amount);
            Vec3 shoulder = origin.add(BodyBox.transform(root, new Vec3(side * 0.375 * scale, (1.5 - lower) * scale, 0.0)));
            BodyHitboxes.add(result, state, right ? BodyPart.RIGHT_ARM : BodyPart.LEFT_ARM, shoulder, arm, new Vec3(0.0, -0.28125, 0.0), new Vec3(0.125, 0.28125, 0.125), scale);
            BodyHitboxes.add(result, state, right ? BodyPart.RIGHT_HAND : BodyPart.LEFT_HAND, shoulder, arm, new Vec3(0.0, -0.65625, 0.0), new Vec3(0.125, 0.09375, 0.125), scale);
            Quaterniond leg = new Quaterniond((Quaterniondc)root).rotateX(-Math.cos(phase + (right ? 0.0 : Math.PI)) * 1.4 * amount);
            Vec3 hip = origin.add(BodyBox.transform(root, new Vec3(side * 1.9 / 16.0 * scale, (0.75 - lower) * scale, 0.0)));
            BodyHitboxes.add(result, state, right ? BodyPart.RIGHT_LEG : BodyPart.LEFT_LEG, hip, leg, new Vec3(0.0, -0.28125, 0.0), new Vec3(0.125, 0.28125, 0.125), scale);
            BodyHitboxes.add(result, state, right ? BodyPart.RIGHT_FOOT : BodyPart.LEFT_FOOT, hip, leg, new Vec3(0.0, -0.65625, 0.0), new Vec3(0.125, 0.09375, 0.125), scale);
        }
        return result;
    }

    private static void add(List<BodyBox> boxes, LimbState state, BodyPart part, Vec3 origin, Quaterniond rotation, Vec3 offset, Vec3 half, double scale) {
        if (!state.isMissing(part)) {
            boxes.add(BodyBox.rotated(part, origin.add(BodyBox.transform(rotation, offset.scale(scale))), rotation, half.scale(scale)));
        }
    }

    public static Optional<BodyBox.Hit> raycast(Player player, Vec3 start, Vec3 end, double padding) {
        return BodyBox.nearest(BodyHitboxes.boxes(player, 1.0f), start, end, padding);
    }
}

