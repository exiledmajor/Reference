/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 */
package com.chromeware.mixin.compat.client;

import com.chromeware.compat.guns.GunCyberware;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets={"top.ribs.scguns.util.GunModifierHelper"}, remap=false)
public abstract class ScorchedRecoilMixin {
    @Unique
    private static final ThreadLocal<Boolean> chrome$nested = ThreadLocal.withInitial(() -> false);

    @WrapMethod(method={"getRecoilModifier(Lnet/minecraft/world/item/ItemStack;)F", "getKickReduction(Lnet/minecraft/world/item/ItemStack;)F"})
    private static float chrome$recoil(ItemStack weapon, Operation<Float> original) {
        float value = ((Float)original.call(new Object[]{weapon})).floatValue();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || chrome$nested.get().booleanValue()) {
            return value;
        }
        Inventory inventory = player.getInventory();
        return inventory.getSelected() == weapon || inventory.offhand.getFirst() == weapon ? 1.0f - (1.0f - value) * (float)GunCyberware.recoil((Player)player) : value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"getRecoilModifier(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)F", "getKickReduction(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)F"})
    private static float chrome$playerRecoil(Player player, ItemStack weapon, Operation<Float> original) {
        boolean previous = chrome$nested.get();
        chrome$nested.set(true);
        try {
            float value = ((Float)original.call(new Object[]{player, weapon})).floatValue();
            float f = previous ? value : 1.0f - (1.0f - value) * (float)GunCyberware.recoil(player);
            return f;
        }
        finally {
            if (previous) {
                chrome$nested.set(true);
            } else {
                chrome$nested.remove();
            }
        }
    }
}

