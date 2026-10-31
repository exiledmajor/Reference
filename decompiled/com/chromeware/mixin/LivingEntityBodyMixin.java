/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyReturnValue
 *  net.minecraft.world.entity.EntityDimensions
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Pose
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.body.BodyFunctions;
import com.chromeware.body.BodyGeometry;
import com.chromeware.body.LimbAttachments;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntity.class})
public abstract class LivingEntityBodyMixin {
    @ModifyReturnValue(method={"getDimensions"}, at={@At(value="RETURN")})
    private EntityDimensions chrome$bodyDimensions(EntityDimensions original, Pose pose) {
        LivingEntityBodyMixin livingEntityBodyMixin = this;
        if (livingEntityBodyMixin instanceof Player) {
            Player player = (Player)livingEntityBodyMixin;
            if (player.tickCount > 0) {
                return BodyGeometry.dimensions(player, original);
            }
        }
        return original;
    }

    @Inject(method={"jumpFromGround"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$noLeglessJump(CallbackInfo ci) {
        Player player;
        LivingEntityBodyMixin livingEntityBodyMixin = this;
        if (livingEntityBodyMixin instanceof Player && !BodyFunctions.canJump(LimbAttachments.get(player = (Player)livingEntityBodyMixin))) {
            ci.cancel();
        }
    }
}

