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
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.AttackEntityEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.body;

import com.chromeware.body.BodyFunctions;
import com.chromeware.body.BodyGeometry;
import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.OneHanded;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class BodyRestrictions {
    private static final ResourceLocation SPEED = BodyRestrictions.id("limb_movement_speed");
    private static final ResourceLocation JUMP = BodyRestrictions.id("limb_jump_strength");
    private static final ResourceLocation ATTACK = BodyRestrictions.id("limb_attack_speed");
    private static final Map<Player, Long> LAST_DIMENSIONS = Collections.synchronizedMap(new WeakHashMap());

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)name);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        long dimensions;
        Player player = event.getEntity();
        Long old = LAST_DIMENSIONS.put(player, dimensions = Integer.toUnsignedLong(Float.floatToIntBits(BodyGeometry.drop(player))) << 32 | (long)LimbAttachments.functional(player).missingMask() | (long)(player.isPassenger() ? 65536 : 0) | (long)player.getPose().ordinal() << 17);
        if (old == null || old != dimensions || Math.abs(player.getEyeHeight() - player.getDimensions(player.getPose()).eyeHeight()) > 1.0E-4f) {
            player.refreshDimensions();
        }
        if (LimbAttachments.get(player).isMissing(BodyPart.BODY)) {
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(0.0, Math.min(0.0, velocity.y), 0.0);
            player.setSprinting(false);
            if (player.isUsingItem()) {
                player.stopUsingItem();
            }
        }
        if (!player.level().isClientSide() && player.tickCount % 10 == 0) {
            BodyRestrictions.apply(player);
        }
    }

    public static void apply(Player player) {
        BodyFunctions.Penalties penalty = BodyFunctions.penalties(LimbAttachments.functional(player));
        BodyRestrictions.penalty(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED, penalty.speed());
        BodyRestrictions.penalty(player.getAttribute(Attributes.JUMP_STRENGTH), JUMP, penalty.jump());
        BodyRestrictions.penalty(player.getAttribute(Attributes.ATTACK_SPEED), ATTACK, penalty.attackSpeed());
        if (player.isUsingItem() && OneHanded.resolve(player, player.getUsedItemHand()) == null) {
            player.stopUsingItem();
        }
        if (!BodyFunctions.canJump(LimbAttachments.functional(player))) {
            player.setSprinting(false);
        }
    }

    private static void penalty(AttributeInstance attribute, ResourceLocation id, double amount) {
        if (attribute == null) {
            return;
        }
        AttributeModifier old = attribute.getModifier(id);
        if (old != null && old.amount() == -amount) {
            return;
        }
        attribute.removeModifier(id);
        if (amount > 0.0) {
            attribute.addTransientModifier(new AttributeModifier(id, -amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event) {
        if (BodyFunctions.usableHands(LimbAttachments.functional(event.getEntity())) == 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void breakBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (BodyFunctions.usableHands(LimbAttachments.functional(event.getEntity())) == 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void rightBlock(PlayerInteractEvent.RightClickBlock event) {
        if (OneHanded.resolve(event.getEntity(), event.getHand()) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void rightItem(PlayerInteractEvent.RightClickItem event) {
        if (OneHanded.resolve(event.getEntity(), event.getHand()) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (OneHanded.resolve(event.getEntity(), event.getHand()) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void interactSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (OneHanded.resolve(event.getEntity(), event.getHand()) == null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void breakSpeed(PlayerEvent.BreakSpeed event) {
        event.setNewSpeed(event.getNewSpeed() * (float)BodyFunctions.penalties(LimbAttachments.functional(event.getEntity())).mining());
    }
}

