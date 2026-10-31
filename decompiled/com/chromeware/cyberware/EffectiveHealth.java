/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.CombatRules
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class EffectiveHealth {
    public static final float REFERENCE_HIT = 9.0f;

    public static float of(LivingEntity entity) {
        float share = EffectiveHealth.share(entity);
        float pool = entity.getHealth() + entity.getAbsorptionAmount();
        return share <= 1.0E-4f ? Float.POSITIVE_INFINITY : pool / share;
    }

    public static float share(LivingEntity entity) {
        float taken = CombatRules.getDamageAfterAbsorb((LivingEntity)entity, (float)9.0f, (DamageSource)entity.damageSources().generic(), (float)entity.getArmorValue(), (float)((float)entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS)));
        MobEffectInstance resistance = entity.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance != null) {
            taken *= Math.max(0.0f, 1.0f - (float)(resistance.getAmplifier() + 1) * 0.2f);
        }
        if (entity instanceof Player) {
            Player p = (Player)entity;
            if (Implant.PAIN_EDITOR.active(p)) {
                taken *= 0.9f;
            }
            taken *= (float)(1.0 - ImplantEffects.mitigationChance(p) * ImplantEffects.mitigationStrength(p));
        }
        return Math.max(0.0f, taken / 9.0f);
    }
}

