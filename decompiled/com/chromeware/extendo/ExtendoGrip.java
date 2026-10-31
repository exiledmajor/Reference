/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.AllItems
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.extendo;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.body.OneHanded;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.netrunning.Netrunning;
import com.simibubi.create.AllItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class ExtendoGrip {
    public static final String ID = "extendo_grip";
    private static final ResourceLocation REACH = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"extendo_reach");
    private static DamageSource lastHit;

    public static boolean is(ItemStack stack) {
        return AllItems.EXTENDO_GRIP.isIn(stack);
    }

    public static boolean installed(Player player, HumanoidArm side) {
        return ArmImplants.has(player, side, ExtendoGrip::is);
    }

    public static boolean installed(Player player) {
        return ExtendoGrip.installed(player, HumanoidArm.RIGHT) || ExtendoGrip.installed(player, HumanoidArm.LEFT);
    }

    public static boolean active(Player player, HumanoidArm side) {
        LimbState body = LimbAttachments.get(player);
        return side == HumanoidArm.RIGHT && ExtendoGrip.installed(player, side) && !body.isMissing(BodyPart.BODY) && !body.isMissing(ArmImplants.part(side)) && !body.isMissing(ArmImplants.handPart(side)) && (Netrunning.flags((Entity)player) & 4) == 0;
    }

    public static double reach(Player player) {
        return ExtendoGrip.active(player, HumanoidArm.RIGHT) && !ArmControls.anyDeployed(player) ? ImplantSettings.stat(ID, "reach", 3.0) : 0.0;
    }

    public static boolean installHeld(Player player) {
        return ArmHardware.installHeld(player, ExtendoGrip::is, true);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        double bonus = ExtendoGrip.reach(player);
        ExtendoGrip.apply(player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE), bonus);
        ExtendoGrip.apply(player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE), bonus);
    }

    private static void apply(AttributeInstance attribute, double bonus) {
        if (attribute == null) {
            return;
        }
        AttributeModifier current = attribute.getModifier(REACH);
        if (current == null ? bonus == 0.0 : current.amount() == bonus) {
            return;
        }
        attribute.removeModifier(REACH);
        if (bonus != 0.0) {
            attribute.addTransientModifier(new AttributeModifier(REACH, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void hit(LivingIncomingDamageEvent event) {
        lastHit = event.getSource();
    }

    @SubscribeEvent
    public static void knockback(LivingKnockBackEvent event) {
        Player player;
        Entity entity;
        DamageSource source = lastHit;
        lastHit = null;
        if (source == null || !((entity = source.getDirectEntity()) instanceof Player) || (player = (Player)entity).level().isClientSide()) {
            return;
        }
        HumanoidArm arm = OneHanded.resolve(player, InteractionHand.MAIN_HAND);
        if (arm != null && ExtendoGrip.active(player, arm)) {
            event.setStrength(event.getStrength() + ImplantSettings.fstat(ID, "knockback", 2.0));
        }
    }
}

