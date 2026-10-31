/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.compat.guns.GunCyberware;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets={"com.tacz.guns.client.event.CameraSetupEvent"}, remap=false)
public abstract class TaczRecoilMixin {
    @ModifyArg(method={"initialCameraRecoil"}, at=@At(value="INVOKE", target="Lcom/tacz/guns/resource/pojo/data/gun/GunRecoil;genPitchSplineFunction(F)Lorg/apache/commons/math3/analysis/polynomials/PolynomialSplineFunction;"), index=0)
    private static float chrome$pitch(float amount) {
        return TaczRecoilMixin.chrome$factor(amount);
    }

    @ModifyArg(method={"initialCameraRecoil"}, at=@At(value="INVOKE", target="Lcom/tacz/guns/resource/pojo/data/gun/GunRecoil;genYawSplineFunction(F)Lorg/apache/commons/math3/analysis/polynomials/PolynomialSplineFunction;"), index=0)
    private static float chrome$yaw(float amount) {
        return TaczRecoilMixin.chrome$factor(amount);
    }

    @Unique
    private static float chrome$factor(float amount) {
        LocalPlayer p = Minecraft.getInstance().player;
        return p == null ? amount : amount * (float)GunCyberware.recoil((Player)p);
    }
}

