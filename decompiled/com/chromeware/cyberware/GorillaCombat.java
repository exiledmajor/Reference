/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.common.CommonHooks
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 *  net.neoforged.neoforge.event.entity.player.CriticalHitEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.GorillaBleeding;
import com.chromeware.cyberware.GorillaState;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.ImplantCriticals;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.humanity.Humanity;
import com.chromeware.monowire.MonowireCombat;
import com.chromeware.monowire.MonowireImplant;
import com.chromeware.netrunning.Netrunning;
import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class GorillaCombat {
    public static final double DAMAGE_BONUS = 7.0;
    public static final int BASE_RECHARGE = 5;
    public static final int CHARGE_TICKS = 10;
    public static final int SMASH_RECHARGE = 20;
    public static final double SHOCKWAVE_RADIUS = 3.0;
    public static final double SMASH_REACH = 3.0;
    private static final ResourceLocation BONUS = ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"gorilla_punch");
    private static final ArmHardware.Cooldowns COOLDOWNS = new ArmHardware.Cooldowns();
    private static final Map<Player, int[]> CHARGES = new WeakHashMap<Player, int[]>();
    private static final ThreadLocal<Punch> PUNCH = new ThreadLocal();

    public static GorillaState state(Player p) {
        return (GorillaState)p.getData(PlayerData.GORILLA_STATE);
    }

    private static void state(Player p, GorillaState s) {
        if (!s.equals(GorillaCombat.state(p))) {
            p.setData(PlayerData.GORILLA_STATE, (Object)s);
        }
    }

    public static boolean functional(Player p, HumanoidArm side) {
        return p.isAlive() && !p.isSpectator() && (Netrunning.flags((Entity)p) & 4) == 0 && ArmImplants.has(p, side, s -> s.is(CyberwareItems.GORILLA_ARMS)) && ArmImplants.active(p, side);
    }

    public static boolean deployed(Player p, HumanoidArm side) {
        return GorillaCombat.functional(p, side) && GorillaCombat.state(p).deployed(side);
    }

    public static boolean deployed(Player p) {
        return GorillaCombat.deployed(p, HumanoidArm.LEFT) || GorillaCombat.deployed(p, HumanoidArm.RIGHT);
    }

    public static boolean grippingWire(Player p, HumanoidArm side) {
        return MonowireImplant.deployed(p, side.getOpposite()) && MonowireCombat.otherHandFree(p, side.getOpposite());
    }

    public static void deploy(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        int mask = 0;
        for (HumanoidArm side : HumanoidArm.values()) {
            if (!GorillaCombat.functional(p, side)) continue;
            mask |= GorillaState.bit(side);
        }
        if (mask != 0) {
            p.stopUsingItem();
        }
        GorillaCombat.state(p, GorillaCombat.state(p).deploy(mask));
    }

    public static void retract(Player p) {
        GorillaCombat.state(p, GorillaState.NONE);
        CHARGES.remove(p);
    }

    public static int cooldown(Player p, HumanoidArm side) {
        return COOLDOWNS.get(p, side);
    }

    public static int cooldown(Player p) {
        return COOLDOWNS.max(p);
    }

    public static void reduceCooldown(Player p, int ticks) {
        COOLDOWNS.reduce(p, ticks);
    }

    public static int blocking(Player p, HumanoidArm side) {
        return GorillaCombat.deployed(p, side) ? GorillaCombat.state(p).ticks(side) : 0;
    }

    public static boolean charging(Player p, HumanoidArm side) {
        int[] c = CHARGES.get(p);
        return c != null && c[side.ordinal()] >= 0;
    }

    public static boolean charge(Player p, HumanoidArm side) {
        if (p.level().isClientSide() || !GorillaCombat.deployed(p, side) || GorillaCombat.charging(p, side) || GorillaCombat.cooldown(p, side) > 0 || GorillaCombat.blocking(p, side) > 0 || ArmHardware.blocking(p, side.getOpposite()) > 0 || GorillaCombat.grippingWire(p, side) || p.isUsingItem()) {
            return false;
        }
        GorillaCombat.CHARGES.computeIfAbsent((Player)p, (Function<Player, int[]>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, lambda$charge$1(net.minecraft.world.entity.player.Player ), (Lnet/minecraft/world/entity/player/Player;)[I)())[side.ordinal()] = 0;
        GorillaCombat.state(p, GorillaCombat.state(p).charge(side, true));
        return true;
    }

    public static void release(Player p, HumanoidArm side) {
        if (p.level().isClientSide()) {
            return;
        }
        int[] c = CHARGES.get(p);
        boolean full = c != null && c[side.ordinal()] >= 10;
        GorillaCombat.cancel(p, side);
        GorillaCombat.punch(p, side, full);
    }

    public static void cancel(Player p, HumanoidArm side) {
        int[] c = CHARGES.get(p);
        if (c != null && c[side.ordinal()] >= 0) {
            c[side.ordinal()] = -1;
            GorillaCombat.state(p, GorillaCombat.state(p).charge(side, false));
        }
    }

    public static void interrupt(Player p, HumanoidArm side) {
        if (GorillaCombat.state(p).ticks(side) > 0) {
            GorillaCombat.state(p, GorillaCombat.state(p).interrupt(side));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void vanillaAttack(Player p, Entity target, Runnable original) {
        if (!p.getMainHandItem().isEmpty() || !GorillaCombat.functional(p, p.getMainArm()) || GorillaCombat.grippingWire(p, p.getMainArm())) {
            original.run();
            return;
        }
        AttributeInstance attribute = p.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeModifier previous = attribute.getModifier(BONUS);
        Punch context = PUNCH.get();
        attribute.removeModifier(BONUS);
        attribute.addTransientModifier(new AttributeModifier(BONUS, ImplantSettings.stat("gorilla_arms", "attack_damage_bonus", 7.0), AttributeModifier.Operation.ADD_VALUE));
        PUNCH.set(new Punch(p, target, false));
        try {
            original.run();
        }
        finally {
            PUNCH.set(context);
            attribute.removeModifier(BONUS);
            if (previous != null) {
                attribute.addTransientModifier(previous);
            }
        }
    }

    public static double fistAttribute(Player p, Holder<Attribute> type) {
        return GorillaCombat.fistAttribute(p, type, 0.0);
    }

    private static double fistAttribute(Player p, Holder<Attribute> type, double bonus) {
        AttributeInstance copy = new AttributeInstance(type, ignored -> {});
        copy.replaceFrom(p.getAttribute(type));
        copy.removeModifier(BONUS);
        for (EquipmentSlot slot : List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)) {
            p.getItemBySlot(slot).forEachModifier(slot, (attribute, modifier) -> {
                if (attribute.equals((Object)type)) {
                    copy.removeModifier(modifier.id());
                }
            });
        }
        if (bonus != 0.0) {
            copy.addTransientModifier(new AttributeModifier(BONUS, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
        return copy.getValue();
    }

    public static boolean punch(Player p, HumanoidArm side) {
        return GorillaCombat.punch(p, side, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean punch(Player p, HumanoidArm side, boolean smash) {
        if (p.level().isClientSide() || !GorillaCombat.deployed(p, side) || GorillaCombat.cooldown(p, side) > 0 || GorillaCombat.blocking(p, side) > 0 || GorillaCombat.charging(p, side) || !smash && ArmHardware.blocking(p, side.getOpposite()) > 0 || GorillaCombat.grippingWire(p, side) || p.isUsingItem()) {
            return false;
        }
        int recharge = QuantumTuner.duration(p, smash ? ImplantEffects.meleeInterval(p, ImplantSettings.ticks("gorilla_arms", "charged_cooldown_seconds", 20)) : Math.max(1, (int)Math.ceil(20.0 / Math.max(0.01, GorillaCombat.fistAttribute(p, (Holder<Attribute>)Attributes.ATTACK_SPEED)) * (double)ImplantSettings.cooldown("gorilla_arms", 5) / 5.0)));
        COOLDOWNS.set(p, side, recharge);
        ImplantCooldowns.fullyUsed(p, ImplantCooldowns.GORILLA);
        if (!smash) {
            ArmHardware.interrupt(p, side.getOpposite());
        }
        GorillaCombat.state(p, GorillaCombat.state(p).punch(side, Math.min(smash ? 7 : 4, recharge), smash));
        p.swing(ArmImplants.hand(p, side), true);
        double reach = smash ? Math.min(3.0, p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) : p.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);
        Vec3 eye = p.getEyePosition();
        Vec3 ray = p.getLookAngle().scale(reach);
        Vec3 end = eye.add(ray);
        BlockHitResult block = p.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
        double distance = block.getType() == HitResult.Type.MISS ? reach * reach : eye.distanceToSqr(block.getLocation());
        EntityHitResult hit = ProjectileUtil.getEntityHitResult((Entity)p, (Vec3)eye, (Vec3)end, (AABB)p.getBoundingBox().expandTowards(ray).inflate(1.0), e -> e.isPickable() && !e.isSpectator(), (double)distance);
        float damage = (float)GorillaCombat.fistAttribute(p, (Holder<Attribute>)Attributes.ATTACK_DAMAGE, ImplantSettings.stat("gorilla_arms", "attack_damage_bonus", 7.0));
        if (smash) {
            damage *= ImplantSettings.fstat("gorilla_arms", "charged_damage_multiplier", 2.0);
            if (hit != null || block.getType() != HitResult.Type.MISS) {
                GorillaCombat.shockwave(p, hit != null ? hit.getLocation() : block.getLocation(), hit == null ? null : hit.getEntity(), damage * 0.5f);
            }
        }
        if (hit == null) {
            return true;
        }
        Entity target = hit.getEntity();
        boolean falling = p.fallDistance > 0.0f && !p.onGround() && !p.onClimbable() && !p.isInWater() && !p.isPassenger() && !p.isSprinting() && !p.hasEffect(MobEffects.BLINDNESS);
        try (ImplantCriticals.Scope scope = ImplantCriticals.hit(p, target, ImplantCriticals.Weapon.FIST);){
            CriticalHitEvent crit = CommonHooks.fireCriticalHit((Player)p, (Entity)target, (boolean)falling, (float)(falling ? 1.5f : 1.0f));
            if (crit.isCriticalHit()) {
                damage *= crit.getDamageMultiplier();
            }
            Punch context = PUNCH.get();
            PUNCH.set(new Punch(p, target, smash));
            try {
                if (target instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity)target;
                    living.invulnerableTime = 0;
                }
                if (target.hurt(p.damageSources().playerAttack(p), damage)) {
                    LivingEntity living;
                    if (target instanceof LivingEntity) {
                        Humanity.armHit(p);
                    }
                    p.setLastHurtMob(target);
                    p.causeFoodExhaustion(ImplantEffects.attackExhaustion(p, 0.1f));
                    if (crit.isCriticalHit()) {
                        p.crit(target);
                    }
                    double knockback = GorillaCombat.fistAttribute(p, (Holder<Attribute>)Attributes.ATTACK_KNOCKBACK) + (double)(p.isSprinting() ? 1 : 0) + (double)(smash ? 3 : 0);
                    if (target instanceof LivingEntity) {
                        living = (LivingEntity)target;
                        if (knockback > 0.0) {
                            living.knockback(knockback * 0.5, Math.sin(Math.toRadians(p.getYRot())), -Math.cos(Math.toRadians(p.getYRot())));
                        }
                    }
                    if (smash && target instanceof LivingEntity && !(living = (LivingEntity)target).isAlive()) {
                        living.setDeltaMovement(p.getLookAngle().scale(1.6).add(0.0, 0.6, 0.0));
                    }
                }
            }
            finally {
                PUNCH.set(context);
            }
        }
        return true;
    }

    private static void shockwave(Player p, Vec3 centre, Entity struck, float damage) {
        Level level = p.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        double radius = ImplantSettings.stat("gorilla_arms", "shockwave_radius", 3.0);
        level2.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION, centre.x, centre.y, centre.z, 1, 0.0, 0.0, 0.0, 0.0);
        level2.sendParticles((ParticleOptions)ParticleTypes.CRIT, centre.x, centre.y, centre.z, 24, radius * 0.3, 0.2, radius * 0.3, 0.4);
        level2.playSound(null, centre.x, centre.y, centre.z, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.0f, 0.8f);
        for (LivingEntity e2 : level2.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(radius), e -> e != p && e != struck && e.isAlive() && !e.isSpectator() && !e.isAlliedTo((Entity)p) && e.distanceToSqr(centre) <= radius * radius)) {
            e2.invulnerableTime = 0;
            if (!e2.hurt(p.damageSources().playerAttack(p), damage)) continue;
            Humanity.armHit(p);
            GorillaBleeding.apply(e2, p);
            Vec3 away = e2.position().subtract(centre).multiply(1.0, 0.0, 1.0);
            if (away.lengthSqr() < 1.0E-4) {
                away = p.getLookAngle().multiply(1.0, 0.0, 1.0);
            }
            away = away.normalize();
            e2.knockback(1.2, -away.x, -away.z);
            e2.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3));
        }
    }

    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post event) {
        Punch context = PUNCH.get();
        if (context != null && event.getEntity() == context.target && event.getNewDamage() > 0.0f && event.getSource().getDirectEntity() == context.player && ImplantEffects.melee(event.getSource()) && (context.smash || context.player.getRandom().nextFloat() < ImplantSettings.fstat("gorilla_arms", "bleed_chance", 0.2))) {
            GorillaBleeding.apply(event.getEntity(), context.player);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.level().isClientSide()) {
            return;
        }
        COOLDOWNS.tick(p);
        int[] charges = CHARGES.get(p);
        if (charges != null) {
            for (HumanoidArm side : HumanoidArm.values()) {
                if (charges[side.ordinal()] < 0) continue;
                if (GorillaCombat.deployed(p, side) && !GorillaCombat.grippingWire(p, side) && !p.isUsingItem()) {
                    int n = side.ordinal();
                    charges[n] = charges[n] + 1;
                    continue;
                }
                GorillaCombat.cancel(p, side);
            }
        }
        if (!p.hasData(PlayerData.GORILLA_STATE)) {
            return;
        }
        GorillaState s = GorillaCombat.state(p).tick();
        int mask = s.deployed();
        for (HumanoidArm side : HumanoidArm.values()) {
            if (GorillaCombat.functional(p, side)) continue;
            mask &= ~GorillaState.bit(side);
        }
        GorillaCombat.state(p, s.deploy(mask));
    }

    private static /* synthetic */ int[] lambda$charge$1(Player key) {
        return new int[]{-1, -1};
    }

    private record Punch(Player player, Entity target, boolean smash) {
    }
}

