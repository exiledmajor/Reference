/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ComputeFovModifierEvent
 */
package com.chromeware.client.body;

import java.util.Collection;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;

@EventBusSubscriber(modid="chromeware", value={Dist.CLIENT})
public final class CyberwareFov {
    @SubscribeEvent
    public static void fov(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        float walk = player.getAbilities().getWalkingSpeed();
        if (speed == null || !(walk > 0.0f)) {
            return;
        }
        Attribute attribute = (Attribute)speed.getAttribute().value();
        float ratio = CyberwareFov.ratio(speed.getBaseValue(), speed.getModifiers(), walk, arg_0 -> ((Attribute)attribute).sanitizeValue(arg_0));
        if (ratio != 1.0f) {
            event.setNewFovModifier(event.getNewFovModifier() * ratio);
        }
    }

    public static float ratio(double base, Collection<AttributeModifier> modifiers, float walk, DoubleUnaryOperator sanitize) {
        if (modifiers.stream().noneMatch(CyberwareFov::ours)) {
            return 1.0f;
        }
        double with = CyberwareFov.term(CyberwareFov.value(base, modifiers, false, sanitize), walk);
        double without = CyberwareFov.term(CyberwareFov.value(base, modifiers, true, sanitize), walk);
        double ratio = without / with;
        return Double.isFinite(ratio) && ratio > 0.0 ? (float)ratio : 1.0f;
    }

    private static boolean ours(AttributeModifier modifier) {
        return "chromeware".equals(modifier.id().getNamespace());
    }

    private static double term(double speed, float walk) {
        return (speed / (double)walk + 1.0) / 2.0;
    }

    static double value(double base, Collection<AttributeModifier> modifiers, boolean skipOurs, DoubleUnaryOperator sanitize) {
        double added = base;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.operation() != AttributeModifier.Operation.ADD_VALUE || skipOurs && CyberwareFov.ours(modifier)) continue;
            added += modifier.amount();
        }
        double value = added;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE || skipOurs && CyberwareFov.ours(modifier)) continue;
            value += added * modifier.amount();
        }
        for (AttributeModifier modifier : modifiers) {
            if (modifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL || skipOurs && CyberwareFov.ours(modifier)) continue;
            value *= 1.0 + modifier.amount();
        }
        return sanitize.applyAsDouble(value);
    }
}

