/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.InventoryImplants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid="chromeware")
public final class Axolotl {
    public static boolean enemy(Player player, LivingEntity target) {
        Mob mob;
        if (target == player || target.isAlliedTo((Entity)player) || player.isAlliedTo((Entity)target)) {
            return false;
        }
        if (target instanceof Player) {
            Player other = (Player)target;
            return !other.isSpectator() && player.canHarmPlayer(other);
        }
        return target instanceof Enemy || target instanceof Mob && (mob = (Mob)target).getTarget() == player;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void killed(LivingDeathEvent event) {
        Player player = EnemyKills.credit(event);
        if (player == null || !InventoryImplants.hasAxolotl(player) || LimbAttachments.get(player).isMissing(BodyPart.HEAD) || LimbAttachments.get(player).isMissing(BodyPart.BODY)) {
            return;
        }
        ImplantCooldowns.reduce(player, ImplantSettings.stat("axolotl", "kill_refund_fraction", 0.075));
    }
}

