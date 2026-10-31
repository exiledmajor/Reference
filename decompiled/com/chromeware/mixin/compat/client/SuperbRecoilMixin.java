/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.compat.guns.GunCyberware;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets={"com.atsuishio.superbwarfare.event.ClientEventHandler"}, remap=false)
public abstract class SuperbRecoilMixin {
    @WrapMethod(method={"handleGunRecoil"})
    private void chrome$recoil(Operation<Void> original) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            original.call(new Object[0]);
            return;
        }
        float pitch = p.getXRot();
        float yaw = p.getYRot();
        original.call(new Object[0]);
        double factor = GunCyberware.recoil((Player)p);
        p.setXRot(pitch + (p.getXRot() - pitch) * (float)factor);
        p.setYRot(yaw + (p.getYRot() - yaw) * (float)factor);
        p.xRotO = p.getXRot();
        p.yRotO = p.getYRot();
    }
}

