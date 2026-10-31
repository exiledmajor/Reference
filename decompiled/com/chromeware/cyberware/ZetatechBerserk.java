/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDeathEvent
 *  net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent$Start
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$Clone
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.Axolotl;
import com.chromeware.cyberware.BerserkState;
import com.chromeware.cyberware.CooldownNotice;
import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.TrueDamage;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.netrunning.Netrunning;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class ZetatechBerserk {
    public static final ResourceLocation TIMER = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"zetatech_berserk");
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"berserk_attack_speed");
    private static final ResourceLocation MOVEMENT = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"berserk_movement_speed");
    public static final TagKey<Item> BLUNT_WEAPONS = TagKey.create((ResourceKey)Registries.ITEM, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"berserk_blunt_weapons"));

    public static BerserkState state(Player p) {
        return (BerserkState)p.getData(PlayerData.BERSERK_STATE);
    }

    private static void state(Player p, BerserkState s) {
        if (!s.equals(ZetatechBerserk.state(p))) {
            p.setData(PlayerData.BERSERK_STATE, (Object)s);
        }
    }

    public static boolean active(Player p) {
        return ZetatechBerserk.state(p).activeTicks() > 0 && Implant.ZETATECH_BERSERK.active(p);
    }

    public static boolean rangedAllowed(Player p) {
        return !ZetatechBerserk.active(p);
    }

    public static boolean toggle(Player p) {
        if (p.level().isClientSide() || !Installing.canOperate(p) || !Implant.ZETATECH_BERSERK.active(p) || (Netrunning.flags((Entity)p) & 4) != 0) {
            return false;
        }
        if (ZetatechBerserk.active(p)) {
            ZetatechBerserk.end(p, true);
            return true;
        }
        if (ZetatechBerserk.state(p).cooldown() > 0) {
            CooldownNotice.show(p, Implant.ZETATECH_BERSERK.stack(), ZetatechBerserk.state(p).cooldown());
            return false;
        }
        p.stopUsingItem();
        ProjectileLauncher.retract(p);
        ZetatechBerserk.state(p, new BerserkState(ImplantSettings.duration("zetatech_berserk", 220), 0, 0, false));
        ZetatechBerserk.attributes(p);
        p.displayClientMessage((Component)Component.translatable((String)"message.chromeware.berserk_active"), true);
        return true;
    }

    public static void end(Player p, boolean reward) {
        if (p.level().isClientSide() || ZetatechBerserk.state(p).activeTicks() == 0) {
            return;
        }
        int kills = ZetatechBerserk.state(p).kills();
        ZetatechBerserk.state(p, new BerserkState(0, QuantumTuner.duration(p, ImplantSettings.cooldown("zetatech_berserk", 500)), 0, false));
        ZetatechBerserk.attributes(p);
        if (reward && p.isAlive() && !p.isSpectator()) {
            p.heal(p.getMaxHealth() * ImplantSettings.fstat("zetatech_berserk", "heal_per_kill_fraction", 0.25) * (float)kills);
            ImplantCooldowns.fullyUsed(p, TIMER);
            p.displayClientMessage((Component)Component.translatable((String)"message.chromeware.berserk_ended"), true);
        }
    }

    public static void attributes(Player p) {
        ZetatechBerserk.attribute(p, (Holder<Attribute>)Attributes.ATTACK_SPEED, SPEED);
        ZetatechBerserk.attribute(p, (Holder<Attribute>)Attributes.MOVEMENT_SPEED, MOVEMENT);
    }

    private static void attribute(Player p, Holder<Attribute> type, ResourceLocation key) {
        AttributeInstance attribute = p.getAttribute(type);
        if (attribute == null) {
            return;
        }
        boolean enabled = ZetatechBerserk.active(p);
        double amount = !enabled ? 0.0 : (type == Attributes.ATTACK_SPEED ? ImplantSettings.stat("zetatech_berserk", "attack_speed_bonus", 0.3) : ImplantSettings.stat("zetatech_berserk", "movement_speed_bonus", 0.5));
        AttributeModifier old = attribute.getModifier(key);
        if (old != null && old.amount() == amount) {
            return;
        }
        attribute.removeModifier(key);
        if (amount != 0.0) {
            attribute.addTransientModifier(new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void tick(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        if (ZetatechBerserk.state(p).activeTicks() > 0) {
            if (!Implant.ZETATECH_BERSERK.active(p)) {
                ZetatechBerserk.end(p, false);
                return;
            }
            if (ZetatechBerserk.state(p).slamming() && p.onGround()) {
                ZetatechBerserk.impact(p);
            }
            if (ZetatechBerserk.state(p).activeTicks() == 1) {
                ZetatechBerserk.end(p, true);
                return;
            }
            p.stopUsingItem();
        }
        ZetatechBerserk.state(p, ZetatechBerserk.state(p).tick());
        ZetatechBerserk.attributes(p);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer p : level.players()) {
                ZetatechBerserk.tick((Player)p);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void incoming(LivingIncomingDamageEvent event) {
        Player p;
        Player attacker;
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (entity instanceof Player && ZetatechBerserk.active(attacker = (Player)entity) && event.getSource().getDirectEntity() == attacker && GunCyberware.firearmDamage(event.getSource())) {
            event.setCanceled(true);
            return;
        }
        entity = event.getEntity();
        if (entity instanceof Player && ZetatechBerserk.active(p = (Player)entity) && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !TrueDamage.is(event.getSource())) {
            EnemyKills.recordAttack(event);
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void meleeDamage(LivingIncomingDamageEvent event) {
        Player attacker;
        Entity entity;
        if (!event.getEntity().level().isClientSide() && (entity = event.getSource().getEntity()) instanceof Player && ZetatechBerserk.active(attacker = (Player)entity) && ImplantEffects.melee(event.getSource())) {
            event.setAmount(event.getAmount() * (1.0f + ZetatechBerserk.damageBonus(attacker)));
        }
    }

    public static float damageBonus(Player p) {
        return ImplantSettings.fstat("zetatech_berserk", "maximum_melee_damage_bonus_fraction", 0.5) * Math.clamp((float)((1.0f - p.getHealth() / p.getMaxHealth()) / ImplantSettings.fstat("zetatech_berserk", "maximum_bonus_missing_health_fraction", 0.8)), (float)0.0f, (float)1.0f);
    }

    public static float protectedHealth(Player p, DamageSource source, float proposed) {
        return ZetatechBerserk.active(p) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? Math.max(proposed, p.getHealth()) : proposed;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void killed(LivingDeathEvent event) {
        Player p;
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            Player dead = (Player)livingEntity;
            ZetatechBerserk.end(dead, false);
        }
        if ((p = EnemyKills.credit(event)) != null && ZetatechBerserk.active(p)) {
            BerserkState s = ZetatechBerserk.state(p);
            ZetatechBerserk.state(p, new BerserkState(s.activeTicks(), s.cooldown(), s.kills() + 1, s.slamming()));
        }
    }

    @SubscribeEvent
    public static void mine(PlayerInteractEvent.LeftClickBlock event) {
        if (ZetatechBerserk.active(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void use(LivingEntityUseItemEvent.Start event) {
        Player p;
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof Player && ZetatechBerserk.active(p = (Player)livingEntity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void rightClick(PlayerInteractEvent.RightClickItem event) {
        if (ZetatechBerserk.active(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void rightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (ZetatechBerserk.active(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (ZetatechBerserk.active(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void interactAt(PlayerInteractEvent.EntityInteractSpecific event) {
        if (ZetatechBerserk.active(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    public static boolean blunt(Player p) {
        return p.getMainHandItem().isEmpty() || p.getMainHandItem().is(BLUNT_WEAPONS);
    }

    public static boolean land(Player p) {
        if (p.level().isClientSide() || !ZetatechBerserk.active(p) || p.onGround() || p.isPassenger() || p.isInWaterOrBubble() || p.isFallFlying() || !ZetatechBerserk.blunt(p) || ZetatechBerserk.state(p).slamming()) {
            return false;
        }
        BerserkState s = ZetatechBerserk.state(p);
        ZetatechBerserk.state(p, new BerserkState(s.activeTicks(), s.cooldown(), s.kills(), true));
        p.setDeltaMovement(p.getDeltaMovement().multiply(0.25, 0.0, 0.25).add(0.0, -1.5, 0.0));
        p.hurtMarked = true;
        return true;
    }

    private static void impact(Player p) {
        BerserkState s = ZetatechBerserk.state(p);
        ZetatechBerserk.state(p, new BerserkState(s.activeTicks(), s.cooldown(), s.kills(), false));
        if (!ZetatechBerserk.blunt(p) || p.isInWaterOrBubble()) {
            return;
        }
        for (LivingEntity enemy : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(ImplantSettings.stat("zetatech_berserk", "slam_radius", 4.0)), e -> e.isAlive() && Axolotl.enemy(p, e) && p.distanceToSqr((Entity)e) <= Math.pow(ImplantSettings.stat("zetatech_berserk", "slam_radius", 4.0), 2.0) && p.hasLineOfSight((Entity)e))) {
            enemy.hurt(p.damageSources().playerAttack(p), ImplantSettings.fstat("zetatech_berserk", "slam_damage", 6.0));
            enemy.knockback(ImplantSettings.stat("zetatech_berserk", "slam_knockback", 0.8), p.getX() - enemy.getX(), p.getZ() - enemy.getZ());
        }
        Level level = p.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            level2.sendParticles((ParticleOptions)ParticleTypes.POOF, p.getX(), p.getY() + 0.1, p.getZ(), 30, 1.5, 0.1, 1.5, 0.1);
            level2.playSound(null, p.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8f, 0.8f);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        ZetatechBerserk.end(event.getEntity(), false);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ZetatechBerserk.end(event.getEntity(), false);
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        Player p = event.getEntity();
        BerserkState s = ZetatechBerserk.state(p);
        ZetatechBerserk.state(p, new BerserkState(0, Math.max(s.cooldown(), s.activeTicks() > 0 ? ImplantSettings.cooldown("zetatech_berserk", 500) : 0), 0, false));
        ZetatechBerserk.attributes(p);
    }
}

