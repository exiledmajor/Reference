/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.AttackEntityEvent
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbIntegrity;
import com.chromeware.body.hitbox.BodyBox;
import com.chromeware.body.hitbox.BodyHitboxes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid="chromeware")
public final class BodyDamage {
    public static void remember(Player player, Entity attacker, BodyBox.Hit hit) {
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void aim(AttackEntityEvent event) {
        Entity entity;
        if (event.isCanceled() || event.getEntity().level().isClientSide() || !((entity = event.getTarget()) instanceof Player)) {
            return;
        }
        Player target = (Player)entity;
        Player attacker = event.getEntity();
        Vec3 start = attacker.getEyePosition();
        if (BodyHitboxes.raycast(target, start, start.add(attacker.getLookAngle().scale(attacker.entityInteractionRange() + 0.5)), 0.0625).isEmpty()) {
            event.setCanceled(true);
        }
    }

    public static boolean damage(Player player, BodyPart part, float amount) {
        if (player.level().isClientSide() || LimbAttachments.get(player).isMissing(part) || !Float.isFinite(amount) || amount <= 0.0f) {
            return false;
        }
        LimbIntegrity next = ((LimbIntegrity)player.getData(LimbAttachments.INTEGRITY)).damage(part, amount, player.getMaxHealth());
        player.setData(LimbAttachments.INTEGRITY, (Object)next);
        if (next.ratio(part) > 1.0E-4f) {
            return false;
        }
        LimbAttachments.set(player, LimbAttachments.get(player).remove(part));
        return true;
    }
}

