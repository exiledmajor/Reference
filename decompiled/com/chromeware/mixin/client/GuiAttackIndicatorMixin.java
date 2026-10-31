/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.AttackIndicatorStatus
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin.client;

import com.chromeware.client.cyberware.ArmRechargeHud;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Gui.class})
public abstract class GuiAttackIndicatorMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyExpressionValue(method={"renderCrosshair", "renderItemHotbar"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;")})
    private Object chrome$perArmIndicator(Object value) {
        return value instanceof AttackIndicatorStatus && ArmRechargeHud.replacesVanilla((Player)this.minecraft.player) ? AttackIndicatorStatus.OFF : value;
    }
}

