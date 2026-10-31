/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.core.Position
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  org.joml.Vector3d
 */
package com.chromeware.sandevistan;

import com.chromeware.compat.SableCompatibility;
import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.TimeField;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.Position;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3d;

@EventBusSubscriber(modid="chromeware")
public final class SandevistanCombat {
    public static final String SHOT = "chromeware_sandevistan_shot";

    public static boolean combo(DamageSource source) {
        Player p;
        Entity entity = source.getEntity();
        if (entity instanceof Player && Sandevistan.isActive((Entity)(p = (Player)entity))) {
            return true;
        }
        return source.getDirectEntity() != null && source.getDirectEntity().getPersistentData().getBoolean(SHOT);
    }

    public static boolean combo(DamageSource source, LivingEntity target) {
        return SandevistanCombat.combo(source) || SandevistanCombat.slowed((Entity)target);
    }

    public static boolean slowed(Entity entity) {
        if (entity.level().isClientSide() || SandevistanContraptions.entityImmune(entity, Sandevistan::immuneOwner)) {
            return false;
        }
        TimeField field = Sandevistan.clockAt(entity.level(), entity.getX(), entity.getZ());
        return field != null && field.slowdown() > 0;
    }

    @SubscribeEvent
    public static void launched(EntityJoinLevelEvent event) {
        Player p;
        Entity entity;
        if (event.getLevel().isClientSide() || !((entity = event.getEntity()) instanceof Projectile)) {
            return;
        }
        Projectile shot = (Projectile)entity;
        Entity entity2 = shot.getOwner();
        if (entity2 instanceof Player && Sandevistan.isActive((Entity)(p = (Player)entity2))) {
            shot.getPersistentData().putBoolean(SHOT, true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void incoming(LivingIncomingDamageEvent event) {
        TimeField field;
        SubLevel body;
        Mob mob;
        block8: {
            block7: {
                Player wearer;
                LivingEntity livingEntity;
                if (event.getEntity().level().isClientSide() || !((livingEntity = event.getEntity()) instanceof Player) || !Sandevistan.isActive((Entity)(wearer = (Player)livingEntity))) {
                    return;
                }
                DamageSource source = event.getSource();
                if (!source.is(DamageTypes.MOB_ATTACK) && !source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)) {
                    return;
                }
                Entity entity = source.getDirectEntity();
                if (!(entity instanceof Mob)) break block7;
                mob = (Mob)entity;
                if (source.getEntity() == mob && !SandevistanContraptions.entityImmune((Entity)mob, Sandevistan::immuneOwner)) break block8;
            }
            return;
        }
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(mob.level(), (Position)mob.position()) : null;
        if (body != null && SandevistanContraptions.immune(body)) {
            return;
        }
        Vector3d world = SandevistanContraptions.worldPosition((Entity)mob);
        TimeField timeField = field = body != null ? SandevistanContraptions.clock(body) : Sandevistan.clockAt(mob.level(), world.x, world.z);
        if (field != null && field.slowdown() > 0) {
            event.setCanceled(true);
        }
    }
}

