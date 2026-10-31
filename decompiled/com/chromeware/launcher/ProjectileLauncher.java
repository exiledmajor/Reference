/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.attachment.AttachmentType
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.launcher;

import com.chromeware.body.hitbox.BodyHitboxes;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmHardware;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.ImplantCooldowns;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.QuantumTuner;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.launcher.LauncherShots;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class ProjectileLauncher {
    public static final int DEPLOY_TICKS = 16;
    public static final int SHOT_DELAY = 3;
    public static final int COOLDOWN = 10;
    public static final int FIRE_TICKS = 10;
    public static final int ACTIVE_TICKS = 5;
    public static final int BUFFER_TICKS = 3;
    public static final int DRY_TICKS = 8;
    public static final int CHARGE_TICKS = 15;
    public static final int VOLLEY_SHOTS = 5;
    public static final int VOLLEY_INTERVAL = 3;
    public static final int VOLLEY_COOLDOWN = 40;
    private static final ArmHardware.Sessions<Session> SESSIONS = new ArmHardware.Sessions();
    private static final ArmHardware.Cooldowns COOLDOWNS = new ArmHardware.Cooldowns();

    public static Supplier<AttachmentType<Boolean>> deployedKey(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? PlayerData.LAUNCHER_DEPLOYED : PlayerData.LAUNCHER_DEPLOYED_RIGHT;
    }

    public static Supplier<AttachmentType<Integer>> shotKey(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? PlayerData.LAUNCHER_SHOT : PlayerData.LAUNCHER_SHOT_RIGHT;
    }

    public static Supplier<AttachmentType<Boolean>> firingKey(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? PlayerData.LAUNCHER_FIRING : PlayerData.LAUNCHER_FIRING_RIGHT;
    }

    public static Supplier<AttachmentType<Integer>> heavyKey(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? PlayerData.LAUNCHER_HEAVY : PlayerData.LAUNCHER_HEAVY_RIGHT;
    }

    public static boolean chargeShown(Player player, HumanoidArm side) {
        return ProjectileLauncher.deployed(player, side) && ((Integer)player.getData(ProjectileLauncher.heavyKey(side)) & 1) != 0;
    }

    public static int volleys(Player player, HumanoidArm side) {
        return (Integer)player.getData(ProjectileLauncher.heavyKey(side)) >> 1;
    }

    public static boolean firing(Player player, HumanoidArm side) {
        return ProjectileLauncher.deployed(player, side) && (Boolean)player.getData(ProjectileLauncher.firingKey(side)) != false;
    }

    public static int blockingTicks(Player player, HumanoidArm side) {
        Session s = ProjectileLauncher.session(player, side);
        return s == null ? 0 : Math.max(0, s.remaining - 5);
    }

    public static void interrupt(Player player, HumanoidArm side) {
        Session s = ProjectileLauncher.session(player, side);
        if (s != null && s.pending == 0 && ProjectileLauncher.blockingTicks(player, side) == 0) {
            s.remaining = 0;
            player.setData(ProjectileLauncher.firingKey(side), (Object)false);
        }
    }

    private static Session session(Player p, HumanoidArm side) {
        return SESSIONS.get(p, side);
    }

    public static int cooldown(Player player) {
        return COOLDOWNS.max(player);
    }

    public static int cooldown(Player player, HumanoidArm side) {
        return COOLDOWNS.get(player, side);
    }

    public static void reduceCooldown(Player player, int ticks) {
        COOLDOWNS.reduce(player, ticks);
    }

    public static boolean installed(Player player) {
        return ArmImplants.has(player, s -> s.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM));
    }

    public static boolean installed(Player player, HumanoidArm side) {
        return ArmImplants.has(player, side, s -> s.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM));
    }

    public static boolean active(Player player, HumanoidArm side) {
        return ArmHardware.fitted(player, side, CyberwareItems.PROJECTILE_LAUNCH_SYSTEM);
    }

    public static boolean active(Player player) {
        return ProjectileLauncher.active(player, HumanoidArm.RIGHT) || ProjectileLauncher.active(player, HumanoidArm.LEFT);
    }

    public static boolean canDeploy(Player player, HumanoidArm side) {
        return ProjectileLauncher.active(player, side) && ZetatechBerserk.rangedAllowed(player) && ArmHardware.free(player, side);
    }

    public static boolean canDeploy(Player player) {
        return ProjectileLauncher.canDeploy(player, HumanoidArm.RIGHT) || ProjectileLauncher.canDeploy(player, HumanoidArm.LEFT);
    }

    public static boolean deployed(Player player, HumanoidArm side) {
        return ProjectileLauncher.active(player, side) && (Boolean)player.getData(ProjectileLauncher.deployedKey(side)) != false;
    }

    public static boolean deployed(Player player) {
        return ProjectileLauncher.deployed(player, HumanoidArm.RIGHT) || ProjectileLauncher.deployed(player, HumanoidArm.LEFT);
    }

    public static boolean installHeld(Player player) {
        return ArmHardware.installHeld(player, s -> s.is(CyberwareItems.PROJECTILE_LAUNCH_SYSTEM), true);
    }

    public static void toggle(Player player) {
        if (player.level().isClientSide()) {
            return;
        }
        if (ProjectileLauncher.deployed(player)) {
            ProjectileLauncher.retract(player);
            return;
        }
        ProjectileLauncher.deploy(player, HumanoidArm.RIGHT);
        ProjectileLauncher.deploy(player, HumanoidArm.LEFT);
    }

    public static boolean deploy(Player player, HumanoidArm side) {
        if (player.level().isClientSide() || !ProjectileLauncher.installed(player, side)) {
            return false;
        }
        if (((Boolean)player.getData(ProjectileLauncher.deployedKey(side))).booleanValue()) {
            return true;
        }
        if (!ProjectileLauncher.canDeploy(player, side)) {
            return false;
        }
        player.setData(ProjectileLauncher.deployedKey(side), (Object)true);
        SESSIONS.set(player, side, new Session(player));
        player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.4f, 1.2f);
        return true;
    }

    public static void retract(Player player, HumanoidArm side) {
        if (player.level().isClientSide()) {
            return;
        }
        SESSIONS.set(player, side, null);
        player.setData(ProjectileLauncher.firingKey(side), (Object)false);
        player.setData(ProjectileLauncher.heavyKey(side), (Object)((Integer)player.getData(ProjectileLauncher.heavyKey(side)) & 0xFFFFFFFE));
        if (((Boolean)player.getData(ProjectileLauncher.deployedKey(side))).booleanValue()) {
            player.setData(ProjectileLauncher.deployedKey(side), (Object)false);
            player.level().playSound(null, player.blockPosition(), SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 0.35f, 1.2f);
        }
    }

    public static void retract(Player player) {
        ProjectileLauncher.retract(player, HumanoidArm.RIGHT);
        ProjectileLauncher.retract(player, HumanoidArm.LEFT);
    }

    public static boolean fire(Player player, HumanoidArm side) {
        if (player.level().isClientSide() || !ProjectileLauncher.deployed(player, side) || !ProjectileLauncher.canDeploy(player, side)) {
            return false;
        }
        Session session = ProjectileLauncher.session(player, side);
        if (session == null || session.level != player.level() || session.age < 16 || session.charge >= 0) {
            return false;
        }
        int wait = Math.max(session.remaining, Math.max(ProjectileLauncher.cooldown(player, side), ArmHardware.blocking(player, side.getOpposite())));
        if (wait == 0 && LauncherShots.pick(player, side).isEmpty()) {
            ProjectileLauncher.dryFire(player, side);
            return false;
        }
        if (wait == 0) {
            ProjectileLauncher.begin(player, side, session);
            return true;
        }
        if (wait > 3) {
            return false;
        }
        SESSIONS.queue(player, session, 3);
        return true;
    }

    public static boolean charge(Player player, HumanoidArm side) {
        if (player.level().isClientSide() || !ProjectileLauncher.deployed(player, side) || !ProjectileLauncher.canDeploy(player, side)) {
            return false;
        }
        Session s = ProjectileLauncher.session(player, side);
        if (s == null || s.level != player.level() || s.age < 16 || s.charge >= 0 || s.remaining > 0 || s.pending > 0 || ProjectileLauncher.cooldown(player, side) > 0 || ArmHardware.blocking(player, side.getOpposite()) > 0 || LauncherShots.pick(player, side).isEmpty()) {
            return false;
        }
        s.charge = 0;
        s.buffer = 0;
        player.setData(ProjectileLauncher.heavyKey(side), (Object)((Integer)player.getData(ProjectileLauncher.heavyKey(side)) | 1));
        player.level().playSound(null, player.blockPosition(), (SoundEvent)SoundEvents.CROSSBOW_LOADING_START.value(), SoundSource.PLAYERS, 0.7f, 0.8f);
        return true;
    }

    public static void release(Player player, HumanoidArm side) {
        if (player.level().isClientSide()) {
            return;
        }
        Session s = ProjectileLauncher.session(player, side);
        boolean full = s != null && s.charge >= 15 && s.remaining == 0 && s.pending == 0 && ProjectileLauncher.cooldown(player, side) == 0 && ProjectileLauncher.deployed(player, side) && ProjectileLauncher.canDeploy(player, side);
        ProjectileLauncher.cancel(player, side);
        if (full) {
            ProjectileLauncher.volley(player, side, s);
        } else {
            ProjectileLauncher.fire(player, side);
        }
    }

    public static void cancel(Player player, HumanoidArm side) {
        Session s = ProjectileLauncher.session(player, side);
        if (s != null && s.charge >= 0) {
            s.charge = -1;
            player.setData(ProjectileLauncher.heavyKey(side), (Object)((Integer)player.getData(ProjectileLauncher.heavyKey(side)) & 0xFFFFFFFE));
        }
    }

    private static void volley(Player player, HumanoidArm side, Session session) {
        int shots = Math.max(1, ImplantSettings.istat("projectile_launch_system", "volley_shots", 5));
        COOLDOWNS.set(player, side, QuantumTuner.duration(player, ImplantSettings.ticks("projectile_launch_system", "volley_cooldown_seconds", 40)));
        session.volley = shots;
        session.shots = 0;
        session.pending = 1;
        session.remaining = shots * 3 + 10;
        session.buffer = 0;
        player.setData(ProjectileLauncher.firingKey(side), (Object)true);
        player.setData(ProjectileLauncher.heavyKey(side), (Object)((Integer)player.getData(ProjectileLauncher.heavyKey(side)) + 2));
        player.level().playSound(null, player.blockPosition(), (SoundEvent)SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 0.8f, 0.7f);
    }

    private static void dryFire(Player player, HumanoidArm side) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6f, 1.4f);
        COOLDOWNS.set(player, side, 8);
    }

    private static void begin(Player player, HumanoidArm side, Session session) {
        ArmHardware.interrupt(player, side.getOpposite());
        COOLDOWNS.set(player, side, QuantumTuner.duration(player, ImplantSettings.cooldown("projectile_launch_system", 10)));
        session.pending = 3;
        session.remaining = 10;
        session.buffer = 0;
        player.setData(ProjectileLauncher.firingKey(side), (Object)true);
        player.setData(ProjectileLauncher.shotKey(side), (Object)((Integer)player.getData(ProjectileLauncher.shotKey(side)) + 1));
    }

    private static void shoot(Player player, HumanoidArm side, Session session) {
        ItemStack ammo;
        Vec3 movement;
        Vec3 end;
        BlockHitResult block;
        Vec3 target;
        EntityHitResult entity;
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ImplantCooldowns.fullyUsed(player, ImplantCooldowns.LAUNCHER);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 muzzle = LauncherAim.muzzle(player, side);
        BlockHitResult obstruction = level2.clip(new ClipContext(eye, muzzle, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        if (obstruction.getType() != HitResult.Type.MISS) {
            muzzle = LauncherShots.worldLocation((Level)level2, (HitResult)obstruction).subtract(muzzle.subtract(eye).normalize().scale(0.06));
        }
        if ((entity = ProjectileUtil.getEntityHitResult((Entity)player, (Vec3)eye, (Vec3)(target = (block = level2.clip(new ClipContext(eye, end = eye.add(look.scale(64.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player))).getType() == HitResult.Type.MISS ? end : LauncherShots.worldLocation((Level)level2, (HitResult)block)), (AABB)player.getBoundingBox().expandTowards(look.scale(64.0)).inflate(1.0), e -> e.isPickable() && !e.isSpectator() && e != player && !(e instanceof Projectile), (double)eye.distanceToSqr(target))) != null) {
            target = entity.getLocation();
        }
        if ((movement = target.subtract(muzzle).normalize()).lengthSqr() < 0.5) {
            movement = look;
        }
        if (session.volley > 0 && session.shots++ > 0) {
            movement = LauncherShots.spread(movement, player.getRandom().nextDouble() * Math.PI * 2.0, Math.toRadians(2.0 + player.getRandom().nextDouble() * 5.0));
        }
        if (LauncherShots.fire(level2, player, ammo = LauncherShots.pick(player, side), muzzle, movement, session.volley > 0)) {
            if (session.volley > 0 && --session.volley > 0) {
                session.pending = 3;
            }
        } else if (session.volley > 0) {
            session.volley = 0;
            level2.playSound(null, player.blockPosition(), SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.6f, 1.4f);
        } else {
            ProjectileLauncher.dryFire(player, side);
        }
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        COOLDOWNS.tick(player);
        for (HumanoidArm side : HumanoidArm.values()) {
            Session session = ProjectileLauncher.session(player, side);
            if (!((Boolean)player.getData(ProjectileLauncher.deployedKey(side))).booleanValue() && session == null) continue;
            if (!ProjectileLauncher.canDeploy(player, side) || session == null || session.level != player.level()) {
                ProjectileLauncher.retract(player, side);
                continue;
            }
            ++session.age;
            if (session.charge >= 0) {
                ++session.charge;
            }
            if (session.pending > 0 && --session.pending == 0) {
                ProjectileLauncher.shoot(player, side, session);
            }
            if (session.remaining <= 0 || --session.remaining != 0) continue;
            player.setData(ProjectileLauncher.firingKey(side), (Object)false);
        }
        for (HumanoidArm side : HumanoidArm.values()) {
            Session s = ProjectileLauncher.session(player, side);
            if (s == null || s.buffer <= 0 || s.remaining != 0 || ProjectileLauncher.cooldown(player, side) != 0 || ArmHardware.blocking(player, side.getOpposite()) != 0) continue;
            ProjectileLauncher.begin(player, side, s);
            break;
        }
        SESSIONS.tickBuffers(player);
    }

    private static final class Session
    extends ArmHardware.Session {
        int pending;
        int volley;
        int shots;

        Session(Player player) {
            super(player);
        }
    }

    public static final class LauncherAim {
        public static final double MUZZLE_UP = 3.7065235622;
        public static final double MUZZLE_FORWARD = 9.9198687778;

        public static Vec3 muzzle(Player player) {
            return LauncherAim.muzzle(player, HumanoidArm.LEFT);
        }

        public static Vec3 muzzle(Player player, HumanoidArm side) {
            return LauncherAim.muzzle(player.position(), player.getLookAngle(), player.getYRot(), player.getScale(), player.isCrouching(), BodyHitboxes.modelDrop(player), side == HumanoidArm.RIGHT);
        }

        public static Vec3 muzzle(Vec3 feet, Vec3 direction, float yaw, double scale, boolean crouching, double modelDrop) {
            return LauncherAim.muzzle(feet, direction, yaw, scale, crouching, modelDrop, false);
        }

        public static Vec3 muzzle(Vec3 feet, Vec3 direction, float yaw, double scale, boolean crouching, double modelDrop, boolean right) {
            double angle = Math.toRadians(yaw);
            Vec3 leftward = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
            Vec3 up = direction.cross(leftward).normalize();
            Vec3 left = right ? leftward.scale(-1.0) : leftward;
            double skin = 0.9375;
            double shoulder = (1.501 - (crouching ? 5.2 : 2.0) / 16.0) * skin - (crouching ? 0.125 : 0.0);
            return feet.add(0.0, shoulder * scale - modelDrop, 0.0).add(left.scale(5.0 * skin / 16.0 * scale)).add(up.scale(3.7065235622 * skin / 16.0 * scale)).add(direction.scale(9.9198687778 * skin / 16.0 * scale));
        }
    }
}

