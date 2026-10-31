/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.ImplantCriticals;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={Player.class})
public abstract class GorillaPunchMixin {
    @WrapMethod(method={"attack"})
    private void chrome$punch(Entity target, Operation<Void> original) {
        Player player = (Player)this;
        try (ImplantCriticals.Scope scope = ImplantCriticals.hit(player, target, ImplantCriticals.Weapon.HAND);){
            GorillaCombat.vanillaAttack(player, target, () -> original.call(new Object[]{target}));
        }
    }
}

