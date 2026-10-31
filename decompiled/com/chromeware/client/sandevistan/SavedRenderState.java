/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.client.sandevistan;

import com.chromeware.mixin.client.LimbAnimatorAccessor;
import com.chromeware.mixin.client.LivingEntityAfterimageAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public record SavedRenderState(Vec3 position, double xo, double yo, double zo, double xOld, double yOld, double zOld, float yRotO, float yaw, float xRotO, float pitch, int tickCount, LivingRestoreState livingState) {
    public static SavedRenderState capture(Entity entity) {
        LivingRestoreState livingRestoreState;
        Vec3 vec3 = entity.position();
        double d = entity.xo;
        double d2 = entity.yo;
        double d3 = entity.zo;
        double d4 = entity.xOld;
        double d5 = entity.yOld;
        double d6 = entity.zOld;
        float f = entity.yRotO;
        float f2 = entity.getYRot();
        float f3 = entity.xRotO;
        float f4 = entity.getXRot();
        int n = entity.tickCount;
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            livingRestoreState = LivingRestoreState.capture(living);
        } else {
            livingRestoreState = null;
        }
        return new SavedRenderState(vec3, d, d2, d3, d4, d5, d6, f, f2, f3, f4, n, livingRestoreState);
    }

    public void restore(Entity entity) {
        entity.setPos(this.position.x, this.position.y, this.position.z);
        entity.xo = this.xo;
        entity.yo = this.yo;
        entity.zo = this.zo;
        entity.xOld = this.xOld;
        entity.yOld = this.yOld;
        entity.zOld = this.zOld;
        entity.yRotO = this.yRotO;
        entity.setYRot(this.yaw);
        entity.xRotO = this.xRotO;
        entity.setXRot(this.pitch);
        entity.tickCount = this.tickCount;
        if (this.livingState != null && entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            this.livingState.restore(living);
        }
    }

    record LivingRestoreState(float yBodyRotO, float yBodyRot, float yHeadRotO, float yHeadRot, Pose pose, boolean sneaking, boolean sprinting, boolean swimming, float previousLimbSpeed, float limbSpeed, float limbPosition, float previousLeaningPitch, float swimAmount, float previousHandSwingProgress, float attackAnim, boolean swinging, boolean usingItem, InteractionHand activeHand, int useItemRemaining) {
        static LivingRestoreState capture(LivingEntity living) {
            LimbAnimatorAccessor limb = (LimbAnimatorAccessor)living.walkAnimation;
            LivingEntityAfterimageAccessor accessor = (LivingEntityAfterimageAccessor)living;
            return new LivingRestoreState(living.yBodyRotO, living.yBodyRot, living.yHeadRotO, living.yHeadRot, living.getPose(), living.isShiftKeyDown(), living.isSprinting(), living.isSwimming(), limb.chrome$getPreviousSpeed(), limb.chrome$speed(), limb.chrome$getPosition(), accessor.chrome$getPreviousLeaningPitch(), accessor.chrome$getSwimAmount(), accessor.chrome$getPreviousHandSwingProgress(), accessor.chrome$getAttackAnim(), accessor.chrome$isHandSwinging(), living.isUsingItem(), living.getUsedItemHand(), living.getUseItemRemainingTicks());
        }

        void restore(LivingEntity living) {
            living.yBodyRotO = this.yBodyRotO;
            living.yBodyRot = this.yBodyRot;
            living.yHeadRotO = this.yHeadRotO;
            living.yHeadRot = this.yHeadRot;
            living.setPose(this.pose);
            living.setShiftKeyDown(this.sneaking);
            living.setSprinting(this.sprinting);
            living.setSwimming(this.swimming);
            LimbAnimatorAccessor limb = (LimbAnimatorAccessor)living.walkAnimation;
            limb.chrome$setPreviousSpeed(this.previousLimbSpeed);
            limb.chrome$setSpeed(this.limbSpeed);
            limb.chrome$setPos(this.limbPosition);
            LivingEntityAfterimageAccessor accessor = (LivingEntityAfterimageAccessor)living;
            accessor.chrome$setPreviousLeaningPitch(this.previousLeaningPitch);
            accessor.chrome$setLeaningPitch(this.swimAmount);
            accessor.chrome$setPreviousHandSwingProgress(this.previousHandSwingProgress);
            accessor.chrome$setHandSwingProgress(this.attackAnim);
            accessor.chrome$setHandSwinging(this.swinging);
            if (this.usingItem) {
                living.startUsingItem(this.activeHand);
                accessor.chrome$setItemUseTimeLeft(this.useItemRemaining);
            } else {
                living.stopUsingItem();
            }
        }
    }
}

