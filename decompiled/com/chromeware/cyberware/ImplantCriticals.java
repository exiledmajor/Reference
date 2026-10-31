/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.tags.ItemTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.Items
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.CriticalHitEvent
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.KiroshiOptics;
import com.chromeware.monowire.MonowireCombat;
import com.chromeware.sandevistan.Sandevistan;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@EventBusSubscriber(modid="chromeware")
public final class ImplantCriticals {
    private static final ThreadLocal<Hit> HIT = new ThreadLocal();

    public static Scope hit(Player p, Entity target, Weapon weapon) {
        return new Scope(new Hit(p, target, weapon));
    }

    public static Weapon weapon(Player p, DamageSource source) {
        if (source.getDirectEntity() instanceof ThrownTrident) {
            return Weapon.BLADE;
        }
        if (!ImplantEffects.melee(source) || source.is(MonowireCombat.DAMAGE_TYPE)) {
            return Weapon.OTHER;
        }
        Hit hit = HIT.get();
        if (hit != null && hit.player == p) {
            return hit.weapon;
        }
        return Weapon.HAND;
    }

    public static double distanceBonus(double distance, double maximum, double capDistance) {
        return maximum * Math.clamp((double)(distance / capDistance), (double)0.0, (double)1.0);
    }

    public static float bonus(Player p, Entity target, Weapon weapon) {
        double bonus = Sandevistan.isActive((Entity)p) ? ImplantSettings.stat("sandevistan", "critical_damage_bonus", 0.2) : 0.0;
        double distance = p.distanceTo(target);
        if (Implant.DEEP_FIELD_VISUAL_INTERFACE.active(p)) {
            bonus += ImplantCriticals.distanceBonus(distance, ImplantSettings.stat("deep_field_visual_interface", "maximum_critical_bonus", 1.0), ImplantSettings.stat("deep_field_visual_interface", "critical_bonus_distance", 100.0));
        }
        if (Implant.VISUAL_CORTEX_SUPPORT.active(p)) {
            bonus += ImplantCriticals.distanceBonus(distance, ImplantSettings.stat("visual_cortex_support", "maximum_critical_bonus", 0.3), ImplantSettings.stat("visual_cortex_support", "critical_bonus_distance", 30.0));
        }
        if (Implant.COCKATRICE.active(p) && KiroshiOptics.scanned(p)) {
            bonus += (double)ImplantSettings.fstat("cockatrice", "critical_damage_bonus", 0.35);
        }
        if (Implant.STABBER.active(p) && (weapon == Weapon.BLADE || weapon == Weapon.HAND && (p.getMainHandItem().is(ItemTags.SWORDS) || p.getMainHandItem().is(Items.TRIDENT)))) {
            bonus += ImplantSettings.stat("stabber", "critical_damage_bonus", 0.2);
        }
        return (float)bonus;
    }

    public static float multiplier(Player p, Entity target, Weapon weapon, float base) {
        return base + ImplantCriticals.bonus(p, target, weapon);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void critical(CriticalHitEvent event) {
        if (!event.isCriticalHit()) {
            return;
        }
        Player p = event.getEntity();
        Hit hit = HIT.get();
        Weapon weapon = hit != null && hit.player == p && hit.target == event.getTarget() ? hit.weapon : Weapon.HAND;
        event.setDamageMultiplier(ImplantCriticals.multiplier(p, event.getTarget(), weapon, event.getDamageMultiplier()));
    }

    public static final class Scope
    implements AutoCloseable {
        private final Hit previous = HIT.get();

        private Scope(Hit next) {
            HIT.set(next);
        }

        @Override
        public void close() {
            if (this.previous == null) {
                HIT.remove();
            } else {
                HIT.set(this.previous);
            }
        }
    }

    private static final class Hit {
        final Player player;
        final Entity target;
        final Weapon weapon;

        Hit(Player p, Entity target, Weapon weapon) {
            this.player = p;
            this.target = target;
            this.weapon = weapon;
        }
    }

    public static enum Weapon {
        HAND,
        BLADE,
        FIST,
        OTHER;

    }
}

