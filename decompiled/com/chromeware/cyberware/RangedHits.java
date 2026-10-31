/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid="chromeware")
public final class RangedHits {
    public static final String CHARGED = "chromeware_charged_tech";

    public static boolean ranged(DamageSource source) {
        return GunCyberware.firearmDamage(source) || !ImplantEffects.melee(source) && (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile);
    }

    public static void charged(Projectile projectile) {
        projectile.getPersistentData().putBoolean(CHARGED, true);
    }

    @SubscribeEvent
    public static void trident(EntityJoinLevelEvent event) {
        ThrownTrident trident;
        Entity entity;
        if (!event.getLevel().isClientSide() && (entity = event.getEntity()) instanceof ThrownTrident && (trident = (ThrownTrident)entity).getOwner() instanceof Player && !trident.getPersistentData().contains(CHARGED)) {
            RangedHits.charged((Projectile)trident);
        }
    }

    @SubscribeEvent
    public static void hit(LivingDamageEvent.Post event) {
        Player p;
        block7: {
            block6: {
                Entity entity;
                if (event.getEntity().level().isClientSide() || event.getNewDamage() <= 0.0f || !RangedHits.ranged(event.getSource()) || !((entity = event.getSource().getEntity()) instanceof Player)) break block6;
                p = (Player)entity;
                if (event.getSource().getDirectEntity() != null && EnemyKills.eligible(p, event.getEntity())) break block7;
            }
            return;
        }
        CompoundTag data = event.getSource().getDirectEntity().getPersistentData();
        if (!data.getBoolean(CHARGED)) {
            return;
        }
        data.putBoolean(CHARGED, false);
        if (Implant.FEEDBACK_CIRCUIT.active(p)) {
            p.heal(p.getMaxHealth() * ImplantSettings.fstat("feedback_circuit", "restore_fraction", 0.035f));
            ImplantEffects.restoreHunger(p, ImplantSettings.fstat("feedback_circuit", "restore_fraction", 0.035));
        }
        if (Implant.ELECTROMAG_RECYCLER.active(p)) {
            p.heal(p.getMaxHealth() * ImplantSettings.fstat("electromag_recycler", "restore_fraction", 0.05f));
            ImplantEffects.restoreHunger(p, ImplantSettings.fstat("electromag_recycler", "restore_fraction", 0.05));
        }
    }
}

