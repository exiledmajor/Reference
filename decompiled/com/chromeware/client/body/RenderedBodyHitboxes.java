/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.ModelPart$Cube
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package com.chromeware.client.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.body.hitbox.BodyBox;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class RenderedBodyHitboxes {
    private static final Map<Player, Sample> CACHE = new WeakHashMap<Player, Sample>();

    public static List<BodyBox> get(Player player) {
        Sample sample = CACHE.get(player);
        return sample != null && Math.abs(player.tickCount - sample.tick) <= 1 && sample.mask == LimbAttachments.functional(player).missingMask() ? sample.boxes : null;
    }

    public static void capture(Player player, HumanoidModel<?> model, PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.level != player.level()) {
            return;
        }
        LimbState state = LimbAttachments.functional(player);
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        ArrayList<BodyBox> boxes = new ArrayList<BodyBox>();
        RenderedBodyHitboxes.add(boxes, state, model.head, BodyPart.HEAD, null, stack, camera);
        RenderedBodyHitboxes.add(boxes, state, model.body, BodyPart.BODY, null, stack, camera);
        RenderedBodyHitboxes.add(boxes, state, model.leftArm, BodyPart.LEFT_ARM, BodyPart.LEFT_HAND, stack, camera);
        RenderedBodyHitboxes.add(boxes, state, model.rightArm, BodyPart.RIGHT_ARM, BodyPart.RIGHT_HAND, stack, camera);
        RenderedBodyHitboxes.add(boxes, state, model.leftLeg, BodyPart.LEFT_LEG, BodyPart.LEFT_FOOT, stack, camera);
        RenderedBodyHitboxes.add(boxes, state, model.rightLeg, BodyPart.RIGHT_LEG, BodyPart.RIGHT_FOOT, stack, camera);
        CACHE.put(player, new Sample(player.tickCount, state.missingMask(), List.copyOf(boxes)));
    }

    private static void add(List<BodyBox> boxes, LimbState state, ModelPart model, BodyPart part, BodyPart end, PoseStack stack, Vec3 camera) {
        if (state.isMissing(part)) {
            return;
        }
        model.visit(stack, (pose, path, index, cube) -> {
            float boundary = Math.min(cube.maxY, cube.minY + 9.0f);
            RenderedBodyHitboxes.box(boxes, part, cube, cube.minY, end == null ? cube.maxY : boundary, pose.pose(), camera);
            if (end != null && !state.isMissing(end)) {
                RenderedBodyHitboxes.box(boxes, end, cube, boundary, cube.maxY, pose.pose(), camera);
            }
        });
    }

    private static void box(List<BodyBox> boxes, BodyPart part, ModelPart.Cube cube, float minY, float maxY, Matrix4f matrix, Vec3 camera) {
        Vector3f center = matrix.transformPosition(new Vector3f((cube.minX + cube.maxX) / 32.0f, (minY + maxY) / 32.0f, (cube.minZ + cube.maxZ) / 32.0f));
        Vector3f x = matrix.transformDirection(new Vector3f(1.0f, 0.0f, 0.0f));
        Vector3f y = matrix.transformDirection(new Vector3f(0.0f, 1.0f, 0.0f));
        Vector3f z = matrix.transformDirection(new Vector3f(0.0f, 0.0f, 1.0f));
        boxes.add(new BodyBox(part, camera.add((double)center.x, (double)center.y, (double)center.z), RenderedBodyHitboxes.vec(x), RenderedBodyHitboxes.vec(y), RenderedBodyHitboxes.vec(z), new Vec3((double)((cube.maxX - cube.minX) * x.length() / 32.0f), (double)((maxY - minY) * y.length() / 32.0f), (double)((cube.maxZ - cube.minZ) * z.length() / 32.0f))));
    }

    private static Vec3 vec(Vector3f vector) {
        return new Vec3((double)vector.x, (double)vector.y, (double)vector.z);
    }

    private record Sample(int tick, int mask, List<BodyBox> boxes) {
    }
}

