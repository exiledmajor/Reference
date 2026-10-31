/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  top.ribs.scguns.common.Gun
 */
package com.chromeware.mixin.compat;

import com.chromeware.compat.guns.GunCyberware;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.ribs.scguns.common.Gun;

@Pseudo
@Mixin(targets={"top.ribs.scguns.common.ReloadTracker"}, remap=false)
public abstract class ScorchedReloadMixin {
    @Shadow
    @Final
    private ItemStack stack;
    @Unique
    private boolean chrome$empty;
    @Unique
    private int chrome$loaded;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void chrome$start(Player player, CallbackInfo ci) {
        this.chrome$empty = Gun.getAmmoCount((ItemStack)this.stack) == 0;
    }

    @WrapMethod(method={"increaseMagAmmo", "reloadItem", "increaseAmmo"})
    private void chrome$reload(Player player, Operation<Void> original) {
        int before = Gun.getAmmoCount((ItemStack)this.stack);
        original.call(new Object[]{player});
        int after = Gun.getAmmoCount((ItemStack)this.stack);
        if (!player.level().isClientSide() && this.chrome$empty && after > before) {
            this.chrome$loaded += after - before;
            GunCyberware.reloadStarted(player, this.stack, 0);
            GunCyberware.reloadFinished(player, this.stack, this.chrome$loaded);
        }
    }
}

