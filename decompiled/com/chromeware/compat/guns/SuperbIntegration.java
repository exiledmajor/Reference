/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.atsuishio.superbwarfare.api.event.ProjectileHitEvent$HitBlock
 *  com.atsuishio.superbwarfare.api.event.ProjectileHitEvent$HitEntity
 *  com.atsuishio.superbwarfare.api.event.ReloadEvent$Post
 *  com.atsuishio.superbwarfare.api.event.ReloadEvent$Pre
 *  com.atsuishio.superbwarfare.data.gun.GunData
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.common.NeoForge
 */
package com.chromeware.compat.guns;

import com.atsuishio.superbwarfare.api.event.ProjectileHitEvent;
import com.atsuishio.superbwarfare.api.event.ReloadEvent;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.chromeware.compat.guns.GunCyberware;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public final class SuperbIntegration {
    public static boolean techWeapon(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey((Object)stack.getItem()).toString().equals("superbwarfare:ql_1031");
    }

    public static boolean chargedTech(GunData data) {
        return SuperbIntegration.techWeapon(data.stack()) && data.selectedFireModeInfo().name.equals("Hold");
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(SuperbIntegration::reloadStart);
        NeoForge.EVENT_BUS.addListener(SuperbIntegration::reloadEnd);
        NeoForge.EVENT_BUS.addListener(SuperbIntegration::entityHit);
        NeoForge.EVENT_BUS.addListener(SuperbIntegration::blockHit);
    }

    private static void reloadStart(ReloadEvent.Pre event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player p = (Player)entity;
            GunCyberware.reloadStarted(p, event.getStack(), event.getData().ammo.get());
        }
    }

    private static void reloadEnd(ReloadEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player p = (Player)entity;
            GunCyberware.reloadFinished(p, event.getStack(), event.getData().ammo.get());
        }
    }

    private static void entityHit(ProjectileHitEvent.HitEntity event) {
        Entity entity;
        if (!event.getProjectile().level().isClientSide() && (entity = event.getOwner()) instanceof Player) {
            Player p = (Player)entity;
            GunCyberware.impact((Entity)event.getProjectile(), p, event.getHitVec());
        }
    }

    private static void blockHit(ProjectileHitEvent.HitBlock event) {
        Entity entity;
        if (!event.getProjectile().level().isClientSide() && (entity = event.getOwner()) instanceof Player) {
            Player p = (Player)entity;
            GunCyberware.impact((Entity)event.getProjectile(), p, event.getHitVec().add((double)event.getFace().getStepX() * 0.02, (double)event.getFace().getStepY() * 0.02, (double)event.getFace().getStepZ() * 0.02));
        }
    }
}

