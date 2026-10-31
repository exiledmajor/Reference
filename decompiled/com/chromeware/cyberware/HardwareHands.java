/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent$Start
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickItem
 */
package com.chromeware.cyberware;

import com.chromeware.body.OneHanded;
import com.chromeware.cyberware.ArmControls;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="chromeware")
public final class HardwareHands {
    public static boolean locked(Player player, InteractionHand hand) {
        HumanoidArm arm = OneHanded.resolve(player, hand);
        return arm != null && ArmControls.deployedOn(player, arm);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        if (HardwareHands.locked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void useOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (HardwareHands.locked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void useOnEntity(PlayerInteractEvent.EntityInteract event) {
        if (HardwareHands.locked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void useOnEntityPart(PlayerInteractEvent.EntityInteractSpecific event) {
        if (HardwareHands.locked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.PASS);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void startUsing(LivingEntityUseItemEvent.Start event) {
        InteractionHand hand;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        Object object = player.getItemInHand(InteractionHand.MAIN_HAND) == event.getItem() ? InteractionHand.MAIN_HAND : (hand = player.getItemInHand(InteractionHand.OFF_HAND) == event.getItem() ? InteractionHand.OFF_HAND : null);
        if (hand != null && HardwareHands.locked(player, hand)) {
            event.setCanceled(true);
        }
    }
}

