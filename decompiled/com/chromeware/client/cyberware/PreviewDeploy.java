/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.zigythebird.playeranim.animation.PlayerAnimResources
 *  com.zigythebird.playeranimcore.animation.Animation
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.HumanoidArm
 */
package com.chromeware.client.cyberware;

import com.chromeware.client.cyberware.CyberwareRig;
import com.chromeware.client.cyberware.HardwareItemGeometry;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import com.zigythebird.playeranimcore.animation.Animation;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;

public final class PreviewDeploy {
    private static final Map<String, Sample> CACHE = new HashMap<String, Sample>();
    private static AbstractClientPlayer previewed;
    private static long since;

    public static String clip(String implant, HumanoidArm side) {
        return switch (implant) {
            case "mantis_blades" -> {
                if (side == HumanoidArm.LEFT) {
                    yield "mantis_deploy_left";
                }
                yield "mantis_deploy_right";
            }
            case "projectile_launch_system" -> "launcher_deploy";
            case "monowire" -> "monowire_draw_alone";
            default -> null;
        };
    }

    public static Function<String, CyberwareRig.Transform> pose(String clip, float seconds) {
        Animation animation = PlayerAnimResources.getAnimationOptional((ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)clip)).orElse(null);
        if (animation == null) {
            return null;
        }
        float t = Math.clamp((float)seconds, (float)0.0f, (float)(animation.length() / 20.0f - 0.001f));
        Sample cached = CACHE.get(clip);
        if (cached != null && Math.abs(cached.seconds - t) < 1.0E-4f) {
            return cached.pose;
        }
        Function<String, CyberwareRig.Transform> pose = HardwareItemGeometry.pose(animation, t, animation.boneAnimations().keySet());
        CACHE.put(clip, new Sample(t, pose));
        return pose;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void during(AbstractClientPlayer player, long startMillis, Runnable render) {
        AbstractClientPlayer outer = previewed;
        long outerSince = since;
        previewed = player;
        since = startMillis;
        try {
            render.run();
        }
        finally {
            previewed = outer;
            since = outerSince;
        }
    }

    public static float seconds(AbstractClientPlayer player) {
        return previewed != null && player == previewed ? (float)(System.currentTimeMillis() - since) / 1000.0f : -1.0f;
    }

    public static CyberwareRig.Transform transform(AbstractClientPlayer player, String implant, HumanoidArm side, String bone) {
        if (previewed == null || player != previewed) {
            return null;
        }
        String clip = PreviewDeploy.clip(implant, side);
        Function<String, CyberwareRig.Transform> pose = clip == null ? null : PreviewDeploy.pose(clip, (float)(System.currentTimeMillis() - since) / 1000.0f);
        return pose == null ? null : (CyberwareRig.Transform)pose.apply(bone);
    }

    private record Sample(float seconds, Function<String, CyberwareRig.Transform> pose) {
    }
}

