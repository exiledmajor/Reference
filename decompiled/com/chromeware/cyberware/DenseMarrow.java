/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.InventoryImplants;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid="chromeware")
public final class DenseMarrow {
    public static boolean active(Player player) {
        return InventoryImplants.hasDenseMarrow(player) && player.isAlive() && !player.isSpectator() && !LimbAttachments.get(player).isMissing(BodyPart.BODY);
    }

    private static Player meleePlayer(DamageSource source) {
        Player player;
        Entity entity;
        return ImplantEffects.melee(source) && (entity = source.getDirectEntity()) instanceof Player ? (player = (Player)entity) : null;
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void incoming(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        Player player = DenseMarrow.meleePlayer(event.getSource());
        if (player != null && DenseMarrow.active(player) && event.getAmount() > 0.0f) {
            event.setAmount(event.getAmount() * ImplantSettings.fstat("dense_marrow", "melee_damage_multiplier", 1.3));
        }
    }
}

