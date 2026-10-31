/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.NeutralMob
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 */
package com.chromeware.cyberware;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid="chromeware")
public final class EnemyKills {
    private static final String ATTACKED = "chromeware_attacked_players";

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void attacked(LivingIncomingDamageEvent event) {
        EnemyKills.recordAttack(event);
    }

    public static void recordAttack(LivingIncomingDamageEvent event) {
        Player p;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player && !(p = (Player)livingEntity).level().isClientSide() && event.getAmount() > 0.0f && (livingEntity = event.getSource().getEntity()) instanceof Mob) {
            Mob mob = (Mob)livingEntity;
            CompoundTag data = mob.getPersistentData();
            CompoundTag victims = data.getCompound(ATTACKED);
            victims.putBoolean(p.getUUID().toString(), true);
            data.put(ATTACKED, (Tag)victims);
        }
    }

    public static boolean eligible(Player player, LivingEntity target) {
        Mob mob;
        if (target == player || target.isAlliedTo((Entity)player) || player.isAlliedTo((Entity)target)) {
            return false;
        }
        if (target instanceof Player) {
            Player other = (Player)target;
            return !other.isSpectator() && player.canHarmPlayer(other);
        }
        return target instanceof Enemy || target instanceof NeutralMob || target instanceof Mob && (mob = (Mob)target).getPersistentData().getCompound(ATTACKED).getBoolean(player.getUUID().toString());
    }

    public static Player credit(LivingDeathEvent event) {
        Player p;
        LivingEntity attacker;
        if (event.isCanceled() || event.getEntity().level().isClientSide()) {
            return null;
        }
        Entity entity = event.getSource().getEntity();
        LivingEntity credit = entity instanceof LivingEntity && (attacker = (LivingEntity)entity) != event.getEntity() ? attacker : event.getEntity().getKillCredit();
        return credit instanceof Player && (p = (Player)credit).isAlive() && !p.isSpectator() && EnemyKills.eligible(p, event.getEntity()) ? p : null;
    }
}

