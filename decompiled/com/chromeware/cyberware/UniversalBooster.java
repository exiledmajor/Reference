/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.food.FoodProperties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.alchemy.PotionContents
 *  net.minecraft.world.item.component.SuspiciousStewEffects
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent$Finish
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.ImplantEffects;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid="chromeware")
public final class UniversalBooster {
    public static boolean healing(Holder<MobEffect> effect) {
        return effect.is(MobEffects.HEAL) || effect.is(MobEffects.REGENERATION);
    }

    public static boolean healthItem(ItemStack stack) {
        for (MobEffectInstance effect : ((PotionContents)stack.getOrDefault(DataComponents.POTION_CONTENTS, (Object)PotionContents.EMPTY)).getAllEffects()) {
            if (!UniversalBooster.healing((Holder<MobEffect>)effect.getEffect())) continue;
            return true;
        }
        FoodProperties food = (FoodProperties)stack.get(DataComponents.FOOD);
        if (food != null && food.effects().stream().anyMatch(e -> e.probability() > 0.0f && UniversalBooster.healing((Holder<MobEffect>)e.effect().getEffect()))) {
            return true;
        }
        SuspiciousStewEffects stew = (SuspiciousStewEffects)stack.get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
        return stew != null && stew.effects().stream().anyMatch(e -> UniversalBooster.healing((Holder<MobEffect>)e.effect()));
    }

    @SubscribeEvent
    public static void consumed(LivingEntityUseItemEvent.Finish event) {
        Player player;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player && !(player = (Player)livingEntity).level().isClientSide() && UniversalBooster.healthItem(event.getItem())) {
            ImplantEffects.healthItem(player);
        }
    }
}

