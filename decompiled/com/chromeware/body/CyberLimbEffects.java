/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.config.ImplantSettings;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class CyberLimbEffects {
    private static final ResourceLocation DAMAGE = CyberLimbEffects.id("cyber_arm_damage");
    private static final ResourceLocation FALL = CyberLimbEffects.id("cyber_leg_fall");
    private static final ResourceLocation SAFE_FALL = CyberLimbEffects.id("cyber_leg_safe_fall");

    public static int arms(Player p) {
        return CyberLimbEffects.count(p, BodyPart.LEFT_ARM) + CyberLimbEffects.count(p, BodyPart.RIGHT_ARM);
    }

    public static int legs(Player p) {
        return CyberLimbEffects.count(p, BodyPart.LEFT_LEG) + CyberLimbEffects.count(p, BodyPart.RIGHT_LEG);
    }

    private static int count(Player p, BodyPart part) {
        return CyberLimbs.has(p, part) ? 1 : 0;
    }

    public static void update(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        CyberLimbEffects.modifier(p, (Holder<Attribute>)Attributes.ATTACK_DAMAGE, DAMAGE, (double)CyberLimbEffects.arms(p) * ImplantSettings.stat("cyber_arm", "attack_damage_bonus", 2.0), AttributeModifier.Operation.ADD_VALUE);
        CyberLimbEffects.modifier(p, (Holder<Attribute>)Attributes.FALL_DAMAGE_MULTIPLIER, FALL, (double)(-CyberLimbEffects.legs(p)) * ImplantSettings.stat("cyber_leg", "fall_damage_reduction_fraction", 0.25), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        CyberLimbEffects.modifier(p, (Holder<Attribute>)Attributes.SAFE_FALL_DISTANCE, SAFE_FALL, (double)CyberLimbEffects.legs(p) * ImplantSettings.stat("cyber_leg", "safe_fall_distance", 2.0), AttributeModifier.Operation.ADD_VALUE);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 5 == 0) {
            CyberLimbEffects.update(event.getEntity());
        }
    }

    private static void modifier(Player p, Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = p.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier old = instance.getModifier(id);
        if (old != null && old.amount() == amount) {
            return;
        }
        instance.removeModifier(id);
        if (amount != 0.0) {
            instance.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }
}

