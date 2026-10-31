/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
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
 *  net.neoforged.neoforge.event.entity.player.CriticalHitEvent
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.mantis;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.ImplantCriticals;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.humanity.Humanity;
import com.chromeware.mantis.MantisState;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
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
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class MantisImplant {
    public static final int DEPLOY_TICKS = 16;
    public static final int SLASH_TICKS = 10;
    public static final int STRIKE_TICK = 2;
    public static final int ACTIVE_TICKS = 7;
    public static final int BUFFER_TICKS = 3;
    public static final float DAMAGE = 10.0f;
    public static final float CROSS_DAMAGE = 20.0f;
    public static final int CROSS_TICKS = 12;
    public static final int CROSS_STRIKE_TICK = 2;
    public static final int CHARGE_TICKS = 6;
    public static final int AXE_RECHARGE_TICKS = 20;
    public static final double SWORD_RECHARGE_TICKS = 12.5;
    public static final int RECHARGE_TICKS = (int)Math.ceil(12.5);
    public static final double REACH = 3.5;
    public static final double CONE = 0.35;
    public static final double LEAP_RANGE = 12.0;
    public static final double LEAP_SPEED = 1.25;
    public static final int LEAP_TICKS = 14;
    public static final int LUNGE_TICKS = 6;
    private static final Set<Player> LEAPT = Collections.newSetFromMap(new WeakHashMap());
    private static final ArmHardware.Sessions<Session> SESSIONS = new ArmHardware.Sessions();

    private static Session session(Player p, HumanoidArm side) {
        return SESSIONS.get(p, side);
    }

    public static boolean installed(Player p) {
        return ArmImplants.has(p, s -> s.is(CyberwareItems.MANTIS_BLADES));
    }

    public static boolean installed(Player p, HumanoidArm side) {
        return ArmImplants.has(p, side, s -> s.is(CyberwareItems.MANTIS_BLADES));
    }

    public static boolean hasHardware(Player p, HumanoidArm side) {
        return ArmHardware.fitted(p, side, CyberwareItems.MANTIS_BLADES);
    }

    public static boolean canDeploy(Player p, HumanoidArm side) {
        return MantisImplant.hasHardware(p, side) && ArmHardware.free(p, side);
    }

    public static MantisState state(Player p, HumanoidArm side) {
        return (MantisState)p.getData(side == HumanoidArm.LEFT ? PlayerData.MANTIS_STATE_LEFT : PlayerData.MANTIS_STATE);
    }

    private static void setState(Player p, HumanoidArm side, MantisState state) {
        p.setData(side == HumanoidArm.LEFT ? PlayerData.MANTIS_STATE_LEFT : PlayerData.MANTIS_STATE, (Object)state);
    }

    public static boolean deployed(Player p, HumanoidArm side) {
        return MantisImplant.installed(p, side) && MantisImplant.state(p, side).deployed();
    }

    public static boolean deployed(Player p) {
        return MantisImplant.deployed(p, HumanoidArm.RIGHT) || MantisImplant.deployed(p, HumanoidArm.LEFT);
    }

    public static boolean installHeld(Player p) {
        return ArmHardware.installHeld(p, s -> s.is(CyberwareItems.MANTIS_BLADES), true);
    }

    private static void action(Player p, HumanoidArm side, int action) {
        MantisImplant.setState(p, side, new MantisState(true, action, MantisImplant.state(p, side).sequence() + 1));
    }

    public static boolean deploy(Player p, HumanoidArm side) {
        if (p.level().isClientSide() || !MantisImplant.installed(p, side)) {
            return false;
        }
        if (MantisImplant.state(p, side).deployed()) {
            return true;
        }
        if (!MantisImplant.canDeploy(p, side)) {
            return false;
        }
        SESSIONS.set(p, side, new Session(p));
        MantisImplant.action(p, side, 0);
        p.level().playSound(null, p.blockPosition(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.4f, 1.4f);
        return true;
    }

    public static void toggle(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        if (MantisImplant.deployed(p)) {
            MantisImplant.retract(p);
            return;
        }
        MantisImplant.deploy(p, HumanoidArm.RIGHT);
        MantisImplant.deploy(p, HumanoidArm.LEFT);
    }

    public static void retract(Player p, HumanoidArm side) {
        if (p.level().isClientSide()) {
            return;
        }
        SESSIONS.clearBuffers(p);
        SESSIONS.set(p, side, null);
        if (MantisImplant.state(p, side).deployed()) {
            MantisImplant.setState(p, side, new MantisState(false, 0, MantisImplant.state(p, side).sequence() + 1));
            p.level().playSound(null, p.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.35f, 1.4f);
        }
    }

    public static void retract(Player p) {
        MantisImplant.retract(p, HumanoidArm.RIGHT);
        MantisImplant.retract(p, HumanoidArm.LEFT);
    }

    public static boolean slash(Player p, HumanoidArm side) {
        if (p.level().isClientSide() || !MantisImplant.deployed(p, side) || !MantisImplant.canDeploy(p, side)) {
            return false;
        }
        Session s = MantisImplant.session(p, side);
        if (s == null || s.level != p.level() || s.age < 16 || MantisImplant.charging(p)) {
            return false;
        }
        int wait = Math.max(s.remaining, Math.max(s.cooldown, ArmHardware.blocking(p, side.getOpposite())));
        if (wait == 0) {
            MantisImplant.begin(p, side, s);
            return true;
        }
        if (wait > 3) {
            return false;
        }
        SESSIONS.queue(p, s, 3);
        return true;
    }

    private static int blocking(Session s) {
        return s == null ? 0 : (s.leaping ? s.remaining + 12 : (s.cross ? s.remaining : Math.max(0, s.remaining - 3)));
    }

    public static int blockingTicks(Player p, HumanoidArm side) {
        return MantisImplant.blocking(MantisImplant.session(p, side));
    }

    public static void interrupt(Player p, HumanoidArm side) {
        Session s = MantisImplant.session(p, side);
        if (s != null && s.remaining > 0 && !s.cross && !s.leaping) {
            s.remaining = 0;
            MantisImplant.action(p, side, 0);
        }
    }

    public static boolean canCross(Player p) {
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MantisImplant.session(p, side);
            if (MantisImplant.deployed(p, side) && MantisImplant.canDeploy(p, side) && s != null && s.level == p.level() && s.age >= 16) continue;
            return false;
        }
        return true;
    }

    public static int cooldown(Player p, HumanoidArm side) {
        Session s = MantisImplant.session(p, side);
        return s == null ? 0 : s.cooldown;
    }

    public static int cooldown(Player p) {
        return Math.max(MantisImplant.cooldown(p, HumanoidArm.LEFT), MantisImplant.cooldown(p, HumanoidArm.RIGHT));
    }

    public static void reduceCooldown(Player p, int ticks) {
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MantisImplant.session(p, side);
            if (s == null) continue;
            s.cooldown = Math.max(0, s.cooldown - Math.max(0, ticks));
        }
    }

    public static boolean charging(Player p) {
        return SESSIONS.charging(p);
    }

    public static boolean charge(Player p) {
        if (p.level().isClientSide() || !MantisImplant.canCross(p) || MantisImplant.charging(p)) {
            return false;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            if (MantisImplant.session((Player)p, (HumanoidArm)side).remaining <= 0) continue;
            return false;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MantisImplant.session(p, side);
            s.charge = 0;
            s.buffer = 0;
            MantisImplant.action(p, side, 2);
        }
        return true;
    }

    public static boolean release(Player p) {
        if (p.level().isClientSide() || !MantisImplant.charging(p)) {
            return false;
        }
        boolean ready = MantisImplant.canCross(p) && MantisImplant.session((Player)p, (HumanoidArm)HumanoidArm.RIGHT).charge >= 6;
        for (HumanoidArm side : HumanoidArm.values()) {
            ready &= MantisImplant.session(p, side) != null && MantisImplant.session((Player)p, (HumanoidArm)side).cooldown == 0;
        }
        if (!ready) {
            MantisImplant.cancel(p);
            return false;
        }
        int recharge = QuantumTuner.duration(p, ImplantEffects.meleeInterval(p, ImplantSettings.ticks("mantis_blades", "cross_cooldown_seconds", 20)));
        LivingEntity target = MantisImplant.leapTarget(p);
        boolean close = target != null && p.getEyePosition().distanceTo(target.getBoundingBox().getCenter()) <= 3.5 + (double)(target.getBbWidth() / 2.0f);
        boolean leap = !close && !LEAPT.contains(p);
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MantisImplant.session(p, side);
            s.charge = -1;
            s.cooldown = recharge;
            s.buffer = 0;
            if (leap) {
                s.leaping = true;
                s.target = target;
                s.remaining = target != null ? 14 : 6;
                MantisImplant.action(p, side, 4);
                continue;
            }
            MantisImplant.cross(p, side, s);
        }
        if (leap) {
            LEAPT.add(p);
            p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0f, 0.7f);
        }
        ImplantCooldowns.fullyUsed(p, ImplantCooldowns.MANTIS);
        return true;
    }

    private static void cross(Player p, HumanoidArm side, Session s) {
        s.leaping = false;
        s.target = null;
        s.cross = true;
        s.remaining = 12;
        MantisImplant.action(p, side, 3);
    }

    private static LivingEntity leapTarget(Player p) {
        LivingEntity target;
        Entity entity;
        EntityHitResult hit;
        double range = ImplantSettings.stat("mantis_blades", "leap_range", 12.0);
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult block = p.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        return (hit = ProjectileUtil.getEntityHitResult((Entity)p, (Vec3)eye, (Vec3)end, (AABB)p.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0), e -> {
            LivingEntity l;
            return e instanceof LivingEntity && (l = (LivingEntity)e).isAlive() && !e.isSpectator() && !e.isAlliedTo((Entity)p);
        }, (double)(range * range))) != null && (entity = hit.getEntity()) instanceof LivingEntity ? (target = (LivingEntity)entity) : null;
    }

    private static void leap(Player p) {
        boolean arrived;
        Session s = MantisImplant.session(p, HumanoidArm.RIGHT);
        LivingEntity target = s.target;
        int length = target != null ? 14 : 6;
        Vec3 to = target != null ? target.getBoundingBox().getCenter().subtract(p.getEyePosition()) : p.getLookAngle();
        boolean bl = arrived = target != null && (to.length() <= 2.4499999999999997 + (double)(target.getBbWidth() / 2.0f) || !target.isAlive() || target.level() != p.level());
        if (arrived || s.remaining <= 1 || s.remaining < length - 2 && p.horizontalCollision) {
            p.fallDistance = 0.0f;
            for (HumanoidArm side : HumanoidArm.values()) {
                Session arm = MantisImplant.session(p, side);
                if (arm == null) continue;
                MantisImplant.cross(p, side, arm);
            }
            return;
        }
        Vec3 velocity = (target != null ? target.position().add(0.0, (double)target.getBbHeight() * 0.4, 0.0).subtract(p.position()) : to).normalize().scale(1.25);
        if (s.remaining > length - 3) {
            velocity = velocity.add(0.0, 0.25, 0.0);
        }
        p.setDeltaMovement(velocity);
        p.hurtMarked = true;
        p.fallDistance = 0.0f;
        for (HumanoidArm side : HumanoidArm.values()) {
            Session arm = MantisImplant.session(p, side);
            if (arm == null) continue;
            --arm.remaining;
        }
        Level level = p.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            server.sendParticles((ParticleOptions)ParticleTypes.CLOUD, p.getX(), p.getY() + 0.2, p.getZ(), 1, 0.1, 0.0, 0.1, 0.0);
        }
    }

    public static void cancel(Player p) {
        if (p.level().isClientSide()) {
            return;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MantisImplant.session(p, side);
            if (s == null || s.charge < 0) continue;
            s.charge = -1;
            MantisImplant.action(p, side, 0);
        }
    }

    private static void begin(Player p, HumanoidArm side, Session s) {
        ArmHardware.interrupt(p, side.getOpposite());
        s.remaining = 10;
        s.cooldown = QuantumTuner.duration(p, ImplantEffects.meleeInterval(p, ImplantSettings.cooldown("mantis_blades", RECHARGE_TICKS)));
        s.buffer = 0;
        MantisImplant.action(p, side, 1);
        ImplantCooldowns.fullyUsed(p, ImplantCooldowns.MANTIS);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Session s;
        Session right;
        Player p = event.getEntity();
        if (p.level().isClientSide()) {
            return;
        }
        if (MantisImplant.charging(p) && !MantisImplant.canCross(p)) {
            MantisImplant.cancel(p);
        }
        if ((right = MantisImplant.session(p, HumanoidArm.RIGHT)) != null && right.leaping) {
            if (MantisImplant.canCross(p)) {
                MantisImplant.leap(p);
            } else {
                for (HumanoidArm side : HumanoidArm.values()) {
                    s = MantisImplant.session(p, side);
                    if (s == null) continue;
                    s.leaping = false;
                    s.target = null;
                    s.remaining = 0;
                    if (!MantisImplant.state(p, side).deployed()) continue;
                    MantisImplant.action(p, side, 0);
                }
            }
        } else if (p.onGround()) {
            LEAPT.remove(p);
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            s = MantisImplant.session(p, side);
            if (s == null) {
                if (!MantisImplant.state(p, side).deployed()) continue;
                MantisImplant.retract(p, side);
                continue;
            }
            if (!MantisImplant.canDeploy(p, side) || s.level != p.level() || !MantisImplant.state(p, side).deployed()) {
                MantisImplant.retract(p, side);
                continue;
            }
            ++s.age;
            if (s.cooldown > 0 && !s.leaping) {
                --s.cooldown;
            }
            if (s.charge >= 0) {
                ++s.charge;
            }
            if (s.remaining <= 0 || s.leaping) continue;
            --s.remaining;
            if (s.cross) {
                if (side == HumanoidArm.RIGHT && 12 - s.remaining == 2) {
                    MantisImplant.strike(p, true);
                }
            } else if (10 - s.remaining == 2) {
                MantisImplant.strike(p, false);
            }
            if (s.remaining != 0) continue;
            s.cross = false;
            MantisImplant.action(p, side, 0);
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            s = MantisImplant.session(p, side);
            if (s == null || s.buffer <= 0 || s.remaining != 0 || s.cooldown != 0 || s.age < 16 || ArmHardware.blocking(p, side.getOpposite()) != 0) continue;
            MantisImplant.begin(p, side, s);
            break;
        }
        SESSIONS.tickBuffers(p);
    }

    static void strike(Player p, boolean cross) {
        Level level = p.level();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        boolean hit = false;
        boolean crit = false;
        boolean falling = p.fallDistance > 0.0f && !p.onGround() && !p.onClimbable() && !p.isInWater() && !p.isPassenger() && !p.isSprinting() && !p.hasEffect(MobEffects.BLINDNESS);
        for (LivingEntity e2 : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(3.5), e -> e != p && e.isAlive() && !e.isSpectator() && !e.isAlliedTo((Entity)p))) {
            Player target;
            Vec3 to = e2.getBoundingBox().getCenter().subtract(eye);
            if (to.length() > 3.5 + (double)(e2.getBbWidth() / 2.0f) || to.normalize().dot(look) < 0.35 || !p.hasLineOfSight((Entity)e2)) continue;
            DamageSource source = p.damageSources().playerAttack(p);
            boolean shielded = cross && e2 instanceof Player && (target = (Player)e2).isBlocking() && target.isDamageSourceBlocked(source);
            e2.invulnerableTime = 0;
            float damage = cross ? ImplantSettings.fstat("mantis_blades", "cross_damage", 20.0) : ImplantSettings.fstat("mantis_blades", "damage", 10.0);
            ImplantCriticals.Scope scope = ImplantCriticals.hit(p, (Entity)e2, ImplantCriticals.Weapon.BLADE);
            try {
                CriticalHitEvent critical = CommonHooks.fireCriticalHit((Player)p, (Entity)e2, (boolean)falling, (float)1.5f);
                if (critical.isCriticalHit()) {
                    damage *= critical.getDamageMultiplier();
                }
                if (e2.hurt(source, damage)) {
                    e2.knockback(cross ? 0.8 : 0.4, -look.x, -look.z);
                    hit = true;
                    Humanity.armHit(p);
                    if (critical.isCriticalHit()) {
                        p.crit((Entity)e2);
                        crit = true;
                    }
                }
                if (!shielded) continue;
                ((Player)e2).disableShield();
                hit = true;
            }
            finally {
                if (scope == null) continue;
                scope.close();
            }
        }
        if (hit) {
            p.causeFoodExhaustion(ImplantEffects.attackExhaustion(p, 0.1f));
        }
        level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, cross ? 0.8f : 1.0f);
        if (cross) {
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (crit) {
            level.playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            server.sendParticles((ParticleOptions)ParticleTypes.SWEEP_ATTACK, eye.x + look.x * 1.5, eye.y - 0.3 + look.y * 1.5, eye.z + look.z * 1.5, cross ? 3 : 1, 0.2, cross ? 0.2 : 0.0, 0.2, 0.0);
        }
    }

    private static final class Session
    extends ArmHardware.Session {
        int cooldown;
        boolean cross;
        boolean leaping;
        LivingEntity target;

        Session(Player p) {
            super(p);
        }
    }
}

