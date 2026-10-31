/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.zigythebird.playeranim.accessors.IAnimatedPlayer
 *  com.zigythebird.playeranim.animation.PlayerAnimManager
 *  com.zigythebird.playeranim.api.PlayerAnimationAccess
 *  com.zigythebird.playeranimcore.animation.AnimationController
 *  com.zigythebird.playeranimcore.animation.layered.IAnimation
 *  com.zigythebird.playeranimcore.bones.PlayerAnimBone
 *  it.unimi.dsi.fastutil.Pair
 *  net.minecraft.client.player.AbstractClientPlayer
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.AfterimagePlayerEntity;
import com.chromeware.mixin.client.AnimationControllerAccessor;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.client.player.AbstractClientPlayer;

public final class FrozenPlayerAnimation {
    static List<Layer> capture(AbstractClientPlayer player, float tickDelta) {
        ((IAnimatedPlayer)player).playerAnimLib$getAnimProcessor().handleAnimations(tickDelta, false);
        PlayerAnimManager manager = PlayerAnimationAccess.getPlayerAnimManager((AbstractClientPlayer)player);
        HashSet<String> names = new HashSet<String>(List.of("body", "head", "torso", "right_arm", "left_arm", "right_leg", "left_leg", "right_item", "left_item"));
        for (Pair entry : manager.getLayers()) {
            Object object = entry.right();
            if (!(object instanceof AnimationController)) continue;
            AnimationController controller = (AnimationController)object;
            AnimationControllerAccessor access = (AnimationControllerAccessor)controller;
            names.addAll(access.chrome$registeredBones().keySet());
            names.addAll(access.chrome$activeBones().keySet());
        }
        ArrayList<Layer> layers = new ArrayList<Layer>();
        for (Pair entry : manager.getLayers()) {
            IAnimation animation = (IAnimation)entry.right();
            if (!animation.isActive()) continue;
            layers.add(Layer.capture((Integer)entry.left(), animation, names));
        }
        return List.copyOf(layers);
    }

    public static Layer layer(AbstractClientPlayer player, Class<?> controllerType) {
        if (!(player instanceof AfterimagePlayerEntity)) {
            return null;
        }
        AfterimagePlayerEntity echo = (AfterimagePlayerEntity)player;
        return echo.animations.stream().filter(l -> l.type().equals(controllerType.getName())).findFirst().orElse(null);
    }

    public record Layer(int priority, String type, Map<String, BoneTransform> bones, Map<String, BoneTransform> rawBones) implements IAnimation
    {
        public Layer {
            bones = Map.copyOf(bones);
            rawBones = Map.copyOf(rawBones);
        }

        public boolean isActive() {
            return true;
        }

        public PlayerAnimBone get3DTransform(PlayerAnimBone bone) {
            BoneTransform transform = this.bones.get(bone.getName());
            return transform == null ? bone : transform.apply(bone);
        }

        public PlayerAnimBone raw(PlayerAnimBone bone) {
            BoneTransform transform = this.rawBones.get(bone.getName());
            return transform == null ? bone : transform.apply(bone);
        }

        static Layer capture(int priority, IAnimation animation, Collection<String> names) {
            HashMap<String, BoneTransform> bones = new HashMap<String, BoneTransform>();
            HashMap<String, BoneTransform> raw = new HashMap<String, BoneTransform>();
            for (String name : names) {
                bones.put(name, BoneTransform.capture(name, arg_0 -> ((IAnimation)animation).get3DTransform(arg_0)));
                if (!(animation instanceof AnimationController)) continue;
                AnimationController controller = (AnimationController)animation;
                raw.put(name, BoneTransform.capture(name, arg_0 -> ((AnimationController)controller).get3DTransformRaw(arg_0)));
            }
            return new Layer(priority, animation.getClass().getName(), bones, raw);
        }
    }

    public record Values(float x, float y, float z, float rx, float ry, float rz, float sx, float sy, float sz, float bend) {
        static final Values ZERO = new Values(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        static final Values ONE = new Values(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);

        static Values of(PlayerAnimBone b) {
            return new Values(b.positionX, b.positionY, b.positionZ, b.rotX, b.rotY, b.rotZ, b.scaleX, b.scaleY, b.scaleZ, b.bend);
        }

        PlayerAnimBone bone(String name) {
            PlayerAnimBone b = new PlayerAnimBone(name);
            b.updatePosition(this.x, this.y, this.z);
            b.updateRotation(this.rx, this.ry, this.rz);
            b.setScaleX(this.sx);
            b.setScaleY(this.sy);
            b.setScaleZ(this.sz);
            b.setBend(this.bend);
            return b;
        }

        Values subtract(Values v) {
            return new Values(this.x - v.x, this.y - v.y, this.z - v.z, this.rx - v.rx, this.ry - v.ry, this.rz - v.rz, this.sx - v.sx, this.sy - v.sy, this.sz - v.sz, this.bend - v.bend);
        }
    }

    public record BoneTransform(Values offset, Values factor) {
        public static BoneTransform capture(String name, UnaryOperator<PlayerAnimBone> transform) {
            Values zero = Values.of((PlayerAnimBone)transform.apply(Values.ZERO.bone(name)));
            Values one = Values.of((PlayerAnimBone)transform.apply(Values.ONE.bone(name)));
            return new BoneTransform(zero, one.subtract(zero));
        }

        public PlayerAnimBone apply(PlayerAnimBone bone) {
            bone.positionX = this.offset.x + this.factor.x * bone.positionX;
            bone.positionY = this.offset.y + this.factor.y * bone.positionY;
            bone.positionZ = this.offset.z + this.factor.z * bone.positionZ;
            bone.rotX = this.offset.rx + this.factor.rx * bone.rotX;
            bone.rotY = this.offset.ry + this.factor.ry * bone.rotY;
            bone.rotZ = this.offset.rz + this.factor.rz * bone.rotZ;
            bone.scaleX = this.offset.sx + this.factor.sx * bone.scaleX;
            bone.scaleY = this.offset.sy + this.factor.sy * bone.scaleY;
            bone.scaleZ = this.offset.sz + this.factor.sz * bone.scaleZ;
            bone.bend = this.offset.bend + this.factor.bend * bone.bend;
            return bone;
        }
    }
}

