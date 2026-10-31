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
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.InventoryImplants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class Scarab {
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"scarab_crouched_speed");

    public static double bonusArmor() {
        return ImplantSettings.stat("scarab", "crouched_armor_multiplier", 3.0) * CyberwareStats.get(CyberwareItems.SCARAB.toStack()).armor();
    }

    public static boolean crouched(Player player) {
        return player.isAlive() && !player.isSpectator() && player.isCrouching() && !LimbAttachments.get(player).isMissing(BodyPart.BODY) && InventoryImplants.hasScarab(player);
    }

    public static void updateMovement(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean enabled = Scarab.crouched(player);
        double amount = enabled ? ImplantSettings.stat("scarab", "movement_speed_penalty_fraction", -0.3) : 0.0;
        AttributeModifier old = speed.getModifier(SPEED);
        if (old != null && old.amount() == amount) {
            return;
        }
        speed.removeModifier(SPEED);
        if (amount != 0.0) {
            speed.addTransientModifier(new AttributeModifier(SPEED, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}

