/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.monowire;

import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.DenseMarrow;
import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.humanity.Humanity;
import com.chromeware.monowire.MonowireAnchors;
import com.chromeware.monowire.MonowireImplant;
import com.chromeware.monowire.MonowireObstacles;
import com.chromeware.monowire.MonowirePhysics;
import com.chromeware.monowire.MonowireState;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class MonowireCombat {
    public static final int COOLDOWN = 12;
    public static final int FEED_TICKS = 8;
    public static final int ACTIVE_TICKS = 7;
    public static final int BUFFER_TICKS = 3;
    public static final int STRIKE_SOUND_TICK = 2;
    public static final float DAMAGE = 7.0f;
    public static final float CROSS_DAMAGE = 14.0f;
    public static final int CROSS_COOLDOWN = 20;
    public static final int CHARGE_TICKS = 6;
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create((ResourceKey)Registries.DAMAGE_TYPE, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"monowire"));
    private static final ArmHardware.Sessions<Session> SESSIONS = new ArmHardware.Sessions();
    private static final ArmHardware.Cooldowns COOLDOWNS = new ArmHardware.Cooldowns();

    private static Session session(Player player, HumanoidArm side) {
        return SESSIONS.get(player, side);
    }

    public static int cooldown(Player player, HumanoidArm side) {
        return COOLDOWNS.get(player, side);
    }

    public static int cooldown(Player player) {
        return COOLDOWNS.max(player);
    }

    public static void reduceCooldown(Player player, int ticks) {
        COOLDOWNS.reduce(player, ticks);
    }

    public static InteractionHand hand(Player player, HumanoidArm side) {
        return ArmImplants.hand(player, side);
    }

    public static boolean otherHandFree(Player player) {
        return MonowireCombat.otherHandFree(player, MonowireImplant.side(player));
    }

    public static boolean otherHandFree(Player player, HumanoidArm side) {
        HumanoidArm other = side.getOpposite();
        LimbState body = LimbAttachments.functional(player);
        boolean ownHardware = ArmImplants.has(player, other, s -> s.is(CyberwareItems.MANTIS_BLADES) || s.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM) || s.is(CyberwareItems.MONOWIRE) || s.is(CyberwareItems.GORILLA_ARMS));
        return !body.isMissing(ArmImplants.part(other)) && !body.isMissing(ArmImplants.handPart(other)) && player.getItemInHand(ArmImplants.hand(player, other)).isEmpty() && (!player.isUsingItem() || player.getUsedItemHand() != ArmImplants.hand(player, other)) && !ownHardware && !ArmControls.deployedOn(player, other);
    }

    public static boolean canUse(Player player, HumanoidArm side) {
        return MonowireImplant.hasHardware(player, side) && ArmHardware.free(player, side);
    }

    public static void toggle(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        if (DisabledNotice.blocked(player)) {
            return;
        }
        if (MonowireImplant.state(player, HumanoidArm.LEFT).deployed() || MonowireImplant.state(player, HumanoidArm.RIGHT).deployed()) {
            MonowireCombat.retract(player);
            return;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            if (!MonowireImplant.installed(player, side)) continue;
            MonowireCombat.deploy(player, side);
        }
    }

    public static boolean deploy(Player player, HumanoidArm side) {
        if (player.level().isClientSide() || MonowireImplant.state(player, side).deployed() || !MonowireCombat.canUse(player, side)) {
            return false;
        }
        SESSIONS.set(player, side, new Session(player));
        player.setData(MonowireImplant.stateKey(side), (Object)new MonowireState(true, 0, 0));
        player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.45f, 1.5f);
        return true;
    }

    public static void retract(Player player) {
        for (HumanoidArm side : HumanoidArm.values()) {
            MonowireCombat.retract(player, side);
        }
    }

    public static void retract(Player player, HumanoidArm side) {
        if (player.level().isClientSide()) {
            return;
        }
        SESSIONS.set(player, side, null);
        if (MonowireImplant.state(player, side).deployed()) {
            player.setData(MonowireImplant.stateKey(side), (Object)MonowireState.RETRACTED);
            player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.35f, 1.6f);
        }
    }

    private static void action(Player player, HumanoidArm side, int stroke, int action) {
        player.setData(MonowireImplant.stateKey(side), (Object)MonowireImplant.state(player, side).with(stroke, action));
    }

    private static int blocking(Session s) {
        return s == null ? 0 : (s.cross ? s.remaining : Math.max(0, s.remaining - 3));
    }

    public static int blockingTicks(Player player, HumanoidArm side) {
        return MonowireCombat.blocking(MonowireCombat.session(player, side));
    }

    public static void interrupt(Player player, HumanoidArm side) {
        Session s = MonowireCombat.session(player, side);
        if (s != null && s.remaining > 0 && !s.cross) {
            s.remaining = 0;
            s.physics.cancelSlash();
            MonowireCombat.action(player, side, MonowireImplant.state(player, side).stroke(), 0);
        }
    }

    private static boolean ready(Player player, HumanoidArm side, Session s) {
        return s != null && s.level == player.level() && s.age >= 8 && MonowireCombat.canUse(player, side) && MonowireImplant.deployed(player, side);
    }

    public static boolean attack(Player player, HumanoidArm side) {
        if (player.level().isClientSide()) {
            return false;
        }
        Session session = MonowireCombat.session(player, side);
        if (!MonowireCombat.ready(player, side, session) || MonowireCombat.charging(player)) {
            return false;
        }
        int wait = Math.max(session.remaining, Math.max(MonowireCombat.cooldown(player, side), ArmHardware.blocking(player, side.getOpposite())));
        if (wait == 0) {
            MonowireCombat.lash(player, side, session);
            return true;
        }
        if (wait > 3) {
            return false;
        }
        SESSIONS.queue(player, session, 3);
        return true;
    }

    private static void lash(Player player, HumanoidArm side, Session session) {
        ArmHardware.interrupt(player, side.getOpposite());
        COOLDOWNS.set(player, side, QuantumTuner.duration(player, ImplantEffects.meleeInterval(player, ImplantSettings.cooldown("monowire", 12))));
        ImplantCooldowns.fullyUsed(player, ImplantCooldowns.MONOWIRE);
        if (DenseMarrow.active(player)) {
            player.causeFoodExhaustion(ImplantEffects.attackExhaustion(player, 0.1f));
        }
        session.hit = new HashSet<UUID>();
        session.cross = false;
        session.buffer = 0;
        session.remaining = 10;
        int stroke = MonowireImplant.state(player, side).stroke() + 1;
        session.physics.slash(player.getLookAngle(), side == HumanoidArm.LEFT, stroke, MonowirePhysics.Lash.of(stroke));
        MonowireCombat.action(player, side, stroke, 1);
        player.swing(MonowireCombat.hand(player, side), true);
    }

    public static boolean canCross(Player player) {
        for (HumanoidArm side : HumanoidArm.values()) {
            if (MonowireCombat.ready(player, side, MonowireCombat.session(player, side))) continue;
            return false;
        }
        return true;
    }

    public static boolean charging(Player player) {
        return SESSIONS.charging(player);
    }

    public static boolean charge(Player player) {
        if (player.level().isClientSide() || !MonowireCombat.canCross(player) || MonowireCombat.charging(player)) {
            return false;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            if (MonowireCombat.session((Player)player, (HumanoidArm)side).remaining <= 0) continue;
            return false;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MonowireCombat.session(player, side);
            s.charge = 0;
            s.buffer = 0;
            MonowireCombat.action(player, side, MonowireImplant.state(player, side).stroke(), 2);
        }
        return true;
    }

    public static boolean release(Player player) {
        if (player.level().isClientSide() || !MonowireCombat.charging(player)) {
            return false;
        }
        boolean full = MonowireCombat.canCross(player) && MonowireCombat.session((Player)player, (HumanoidArm)HumanoidArm.RIGHT).charge >= 6;
        for (HumanoidArm side : HumanoidArm.values()) {
            full &= MonowireCombat.cooldown(player, side) == 0;
        }
        if (!full) {
            MonowireCombat.cancel(player);
            return false;
        }
        int recharge = QuantumTuner.duration(player, ImplantEffects.meleeInterval(player, ImplantSettings.ticks("monowire", "cross_cooldown_seconds", 20)));
        ImplantCooldowns.fullyUsed(player, ImplantCooldowns.MONOWIRE);
        if (DenseMarrow.active(player)) {
            player.causeFoodExhaustion(ImplantEffects.attackExhaustion(player, 0.1f));
        }
        HashSet<UUID> hit = new HashSet<UUID>();
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MonowireCombat.session(player, side);
            s.charge = -1;
            s.cross = true;
            s.hit = hit;
            s.buffer = 0;
            s.remaining = 10;
            COOLDOWNS.set(player, side, recharge);
            int stroke = MonowireImplant.state(player, side).stroke() + 1;
            s.physics.slash(player.getLookAngle(), side == HumanoidArm.LEFT, stroke, MonowirePhysics.Lash.CROSS);
            MonowireCombat.action(player, side, stroke, 3);
        }
        return true;
    }

    public static void cancel(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MonowireCombat.session(player, side);
            if (s == null || s.charge < 0) continue;
            s.charge = -1;
            MonowireCombat.action(player, side, MonowireImplant.state(player, side).stroke(), 0);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (MonowireCombat.charging(player) && !MonowireCombat.canCross(player)) {
            MonowireCombat.cancel(player);
        }
        COOLDOWNS.tick(player);
        for (HumanoidArm side : HumanoidArm.values()) {
            MonowireCombat.tick(player, side);
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = MonowireCombat.session(player, side);
            if (s == null || s.buffer <= 0 || s.remaining != 0 || MonowireCombat.cooldown(player, side) != 0 || !MonowireCombat.ready(player, side, s) || ArmHardware.blocking(player, side.getOpposite()) != 0 || MonowireCombat.charging(player)) continue;
            MonowireCombat.lash(player, side, s);
            break;
        }
        SESSIONS.tickBuffers(player);
    }

    private static void tick(Player player, HumanoidArm side) {
        Session session = MonowireCombat.session(player, side);
        if (!MonowireImplant.state(player, side).deployed() && session == null) {
            return;
        }
        if (!MonowireCombat.canUse(player, side) || session == null || session.level != player.level()) {
            MonowireCombat.retract(player, side);
            return;
        }
        ++session.age;
        if (session.charge >= 0) {
            ++session.charge;
        }
        Vec3 root = MonowireAnchors.wrist(player, side);
        session.physics.aim(player.getLookAngle(), player.getYRot());
        session.physics.bodies(MonowireObstacles.of(player, side, !session.physics.slashing(), 1.0f));
        Vec3 grip = session.physics.slashing() || !MonowireCombat.otherHandFree(player, side) ? null : MonowireAnchors.grip(player, side);
        session.physics.tick(root, Math.min(1.0, (double)session.age / 8.0), grip, player.level());
        if (session.remaining > 0) {
            int elapsed = 10 - session.remaining;
            if (!(elapsed != 2 || session.cross && side != HumanoidArm.RIGHT)) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, session.cross ? 0.8f : 1.0f);
                if (session.cross) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
            if (--session.remaining == 0) {
                session.cross = false;
                MonowireCombat.action(player, side, MonowireImplant.state(player, side).stroke(), 0);
            }
        }
        if (!session.physics.striking()) {
            return;
        }
        Vec3[] before = session.physics.beforeTick();
        Vec3[] after = session.physics.points();
        AABB bounds = new AABB(root, root).inflate(8.5);
        boolean cross = session.physics.lash() == MonowirePhysics.Lash.CROSS;
        DamageSource source = new DamageSource((Holder)player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE_TYPE), (Entity)player, (Entity)player);
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, bounds, e -> e != player && e.isAlive() && !e.isSpectator() && !player.isAlliedTo((Entity)e))) {
            Vec3 hitPoint;
            Player other;
            if (session.hit.contains(target.getUUID()) || target instanceof Player && !player.canHarmPlayer(other = (Player)target) || !MonowirePhysics.intersects(before, after, target.getBoundingBox(), 0.065) || root.distanceTo(hitPoint = target.getBoundingBox().getCenter()) > 8.0 + (double)target.getBbWidth() || player.level().clip(new ClipContext(root, hitPoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).getType() != HitResult.Type.MISS) continue;
            session.hit.add(target.getUUID());
            target.invulnerableTime = 0;
            if (!target.hurt(source, cross ? ImplantSettings.fstat("monowire", "cross_damage", 14.0) : ImplantSettings.fstat("monowire", "damage", 7.0))) continue;
            target.knockback(cross ? 0.45 : 0.25, player.getX() - target.getX(), player.getZ() - target.getZ());
            Humanity.armHit(player);
        }
    }

    private static final class Session
    extends ArmHardware.Session {
        final MonowirePhysics physics = new MonowirePhysics();
        Set<UUID> hit = new HashSet<UUID>();
        boolean cross;

        Session(Player player) {
            super(player);
        }
    }
}

