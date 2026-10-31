/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.InventoryImplants;
import com.chromeware.cyberware.Scarab;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class CyberwareArmor {
    private static final ResourceLocation ARMOR = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"cyberware_armor");
    private static final ResourceLocation RARA_AVIS = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"rara_avis_armor");

    public static double amount(Player player) {
        if (player.isSpectator() || LimbAttachments.get(player).isMissing(BodyPart.BODY)) {
            return 0.0;
        }
        double base = CyberwareCapacity.installed(player).stream().filter(i -> Implant.of(i) == null || Implant.of(i).active(player)).mapToDouble(i -> CyberwareStats.get(i).armor()).sum();
        return base + (Scarab.crouched(player) ? Scarab.bonusArmor() : 0.0) + ImplantEffects.conditionalArmor(player);
    }

    public static double multiplier(Player player) {
        return !player.isSpectator() && !LimbAttachments.get(player).isMissing(BodyPart.BODY) && InventoryImplants.hasRaraAvis(player) ? ImplantSettings.stat("rara_avis", "armor_bonus_fraction", 0.4) : 0.0;
    }

    public static void update(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        Scarab.updateMovement(player);
        AttributeInstance attribute = player.getAttribute(Attributes.ARMOR);
        if (attribute == null) {
            return;
        }
        double amount = CyberwareArmor.amount(player);
        CyberwareArmor.modifier(attribute, ARMOR, amount, AttributeModifier.Operation.ADD_VALUE);
        CyberwareArmor.modifier(attribute, RARA_AVIS, CyberwareArmor.multiplier(player), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        CyberwareArmor.modifier(attribute, ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"catalogue_armor"), ImplantEffects.armorMultiplier(player), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static void modifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeModifier existing = attribute.getModifier(id);
        if (existing != null && existing.amount() == amount) {
            return;
        }
        attribute.removeModifier(id);
        if (amount != 0.0) {
            attribute.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        CyberwareArmor.update(event.getEntity());
    }
}

