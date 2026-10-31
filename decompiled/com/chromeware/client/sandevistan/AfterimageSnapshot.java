/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.player.PlayerModelPart
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.client.sandevistan;

import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.client.mantis.MantisAnimation;
import com.chromeware.client.monowire.MonowireClient;
import com.chromeware.client.sandevistan.FrozenPlayerAnimation;
import com.chromeware.cyberware.ArmImplant;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.InstalledImplant;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.mantis.MantisState;
import com.chromeware.monowire.MonowireState;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

record AfterimageSnapshot(AfterimageHardware hardware, List<FrozenPlayerAnimation.Layer> animations, float mantisHeadFollow, FrozenMonowires wires, Vec3 position, Vec3 velocity, float yaw, float yBodyRot, float yHeadRot, float pitch, Pose pose, float animationAge, float limbPosition, float limbSpeed, float attackAnim, float swimAmount, boolean swinging, InteractionHand swingingArm, boolean sneaking, boolean sprinting, boolean swimming, boolean fallFlying, boolean onGround, boolean usingItem, InteractionHand activeHand, int useItemRemaining, HumanoidArm mainArm, byte visibleModelParts, Map<EquipmentSlot, ItemStack> equipment, double xCloakO, double yCloakO, double zCloakO, double xCloak, double yCloak, double zCloak, float elytraRotX, float elytraRotY, float elytraRotZ, int color, float opacity, float createdAt) {
    static AfterimageSnapshot capture(AbstractClientPlayer player, Vec3 position, int color, float opacity, float createdAt, float tickDelta) {
        EnumMap<EquipmentSlot, ItemStack> equipment = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, player.getItemBySlot(slot).copy());
        }
        byte visibleParts = 0;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (!player.isModelPartShown(part)) continue;
            visibleParts = (byte)(visibleParts | (byte)part.getMask());
        }
        Vec3 sourcePosition = player.getPosition(tickDelta);
        return new AfterimageSnapshot(AfterimageHardware.capture(player, tickDelta), FrozenPlayerAnimation.capture(player, tickDelta), MantisAnimation.headFollowWeight(player, tickDelta), FrozenMonowires.capture(side -> MonowireClient.snapshot(player, side, tickDelta)), position, player.getDeltaMovement(), Mth.rotLerp((float)tickDelta, (float)player.yRotO, (float)player.getYRot()), Mth.rotLerp((float)tickDelta, (float)player.yBodyRotO, (float)player.yBodyRot), Mth.rotLerp((float)tickDelta, (float)player.yHeadRotO, (float)player.yHeadRot), Mth.lerp((float)tickDelta, (float)player.xRotO, (float)player.getXRot()), player.getPose(), (float)player.tickCount + tickDelta, player.walkAnimation.position(tickDelta), player.walkAnimation.speed(tickDelta), player.getAttackAnim(tickDelta), player.getSwimAmount(tickDelta), player.swinging, player.swingingArm, player.isShiftKeyDown(), player.isSprinting(), player.isSwimming(), player.isFallFlying(), player.onGround(), player.isUsingItem(), player.getUsedItemHand(), player.getUseItemRemainingTicks(), player.getMainArm(), visibleParts, Map.copyOf(equipment), position.x + (player.xCloakO - sourcePosition.x), position.y + (player.yCloakO - sourcePosition.y), position.z + (player.zCloakO - sourcePosition.z), position.x + (player.xCloak - sourcePosition.x), position.y + (player.yCloak - sourcePosition.y), position.z + (player.zCloak - sourcePosition.z), player.elytraRotX, player.elytraRotY, player.elytraRotZ, color, opacity, createdAt);
    }

    record AfterimageHardware(LimbState limbs, ArmImplant arms, ArmImplant leftArm, InstalledImplant os, MonowireState monowire, MonowireState leftMonowire, boolean launcher, int shot, boolean rightLauncher, int rightShot, MantisState mantis, MantisState leftMantis, CyberLimbs frames, ImplantColors colors) {
        static AfterimageHardware capture(AbstractClientPlayer player, float tick) {
            return new AfterimageHardware(LimbAttachments.get((Player)player), (ArmImplant)player.getData(PlayerData.ARM_IMPLANT), (ArmImplant)player.getData(PlayerData.ARM_IMPLANT_LEFT), (InstalledImplant)player.getData(PlayerData.OPERATING_SYSTEM), (MonowireState)player.getData(PlayerData.MONOWIRE_STATE), (MonowireState)player.getData(PlayerData.MONOWIRE_STATE_LEFT), (Boolean)player.getData(PlayerData.LAUNCHER_DEPLOYED), (Integer)player.getData(PlayerData.LAUNCHER_SHOT), (Boolean)player.getData(PlayerData.LAUNCHER_DEPLOYED_RIGHT), (Integer)player.getData(PlayerData.LAUNCHER_SHOT_RIGHT), (MantisState)player.getData(PlayerData.MANTIS_STATE), (MantisState)player.getData(PlayerData.MANTIS_STATE_LEFT), CyberLimbs.get((Player)player), (ImplantColors)player.getData(PlayerData.IMPLANT_COLORS));
        }

        void apply(AbstractClientPlayer player) {
            player.setData(LimbAttachments.LIMBS, (Object)this.limbs);
            player.setData(PlayerData.ARM_IMPLANT, (Object)this.arms);
            player.setData(PlayerData.ARM_IMPLANT_LEFT, (Object)this.leftArm);
            player.setData(PlayerData.OPERATING_SYSTEM, (Object)this.os);
            player.setData(PlayerData.MONOWIRE_STATE, (Object)this.monowire);
            player.setData(PlayerData.MONOWIRE_STATE_LEFT, (Object)this.leftMonowire);
            player.setData(PlayerData.LAUNCHER_DEPLOYED, (Object)this.launcher);
            player.setData(PlayerData.LAUNCHER_SHOT, (Object)this.shot);
            player.setData(PlayerData.LAUNCHER_DEPLOYED_RIGHT, (Object)this.rightLauncher);
            player.setData(PlayerData.LAUNCHER_SHOT_RIGHT, (Object)this.rightShot);
            player.setData(PlayerData.MANTIS_STATE, (Object)this.mantis);
            player.setData(PlayerData.MANTIS_STATE_LEFT, (Object)this.leftMantis);
            player.setData(CyberLimbs.FRAMES, (Object)this.frames);
            player.setData(PlayerData.IMPLANT_COLORS, (Object)this.colors);
        }
    }

    record FrozenMonowires(Map<HumanoidArm, MonowireClient.WireSnapshot> sides) {
        FrozenMonowires {
            sides = Map.copyOf(sides);
        }

        static FrozenMonowires capture(Function<HumanoidArm, MonowireClient.WireSnapshot> capture) {
            EnumMap<HumanoidArm, MonowireClient.WireSnapshot> sides = new EnumMap<HumanoidArm, MonowireClient.WireSnapshot>(HumanoidArm.class);
            for (HumanoidArm side : HumanoidArm.values()) {
                MonowireClient.WireSnapshot snapshot = capture.apply(side);
                if (snapshot == null) continue;
                sides.put(side, snapshot);
            }
            return new FrozenMonowires(sides);
        }

        List<Vec3[]> points(Map<HumanoidArm, Vec3> sockets, Map<HumanoidArm, Vec3> grips) {
            ArrayList<Vec3[]> wires = new ArrayList<Vec3[]>();
            for (HumanoidArm side : HumanoidArm.values()) {
                MonowireClient.WireSnapshot snapshot = this.sides.get(side);
                Vec3 socket = sockets.get(side);
                if (snapshot == null || socket == null) continue;
                wires.add(snapshot.points(socket, grips.get(side)));
            }
            return List.copyOf(wires);
        }
    }
}

