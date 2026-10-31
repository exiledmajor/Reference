/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.monowire;

import com.chromeware.monowire.MonowireAim;
import com.chromeware.monowire.MonowireCombat;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class MonowireAnchors {
    public static Vec3 wrist(Player player, HumanoidArm arm) {
        MonowireAim aim = MonowireAim.of(player.getLookAngle(), player.getYRot());
        double side = arm == HumanoidArm.RIGHT ? 1.0 : -1.0;
        double scale = player.getScale();
        boolean held = MonowireCombat.otherHandFree(player, arm);
        return player.getEyePosition().add(aim.up().scale((held ? -0.34 : -0.36) * scale)).add(aim.right().scale((held ? 0.31 : 0.19) * side * scale)).add(aim.forward().scale((held ? 0.93 : 1.06) * scale));
    }

    public static Vec3 grip(Player player, HumanoidArm arm) {
        MonowireAim aim = MonowireAim.of(player.getLookAngle(), player.getYRot());
        double side = arm == HumanoidArm.LEFT ? -1.0 : 1.0;
        double scale = player.getScale();
        return MonowireAnchors.wrist(player, arm).add(aim.right().scale(-0.7 * side * scale)).add(aim.up().scale(0.06 * scale)).add(aim.forward().scale(-0.05 * scale));
    }
}

