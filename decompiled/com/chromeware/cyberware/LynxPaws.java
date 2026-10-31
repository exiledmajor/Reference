/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.Implant;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

public final class LynxPaws {
    public static boolean silences(Entity source) {
        Player rider;
        LivingEntity livingEntity;
        ItemEntity item;
        Projectile projectile;
        Entity entity;
        if (source instanceof Player) {
            Player player = (Player)source;
            return Implant.LYNX_PAWS.active(player);
        }
        if (source instanceof Projectile && (entity = (projectile = (Projectile)source).getOwner()) instanceof Player) {
            Player owner = (Player)entity;
            return Implant.LYNX_PAWS.active(owner);
        }
        if (source instanceof ItemEntity && (entity = (item = (ItemEntity)source).getOwner()) instanceof Player) {
            Player owner = (Player)entity;
            return Implant.LYNX_PAWS.active(owner);
        }
        return source != null && (livingEntity = source.getControllingPassenger()) instanceof Player && Implant.LYNX_PAWS.active(rider = (Player)livingEntity);
    }
}

