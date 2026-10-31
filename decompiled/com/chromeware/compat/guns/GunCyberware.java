/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.EventPriority
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.common.NeoForge
 *  net.neoforged.neoforge.event.entity.EntityJoinLevelEvent
 *  top.ribs.scguns.entity.projectile.ProjectileEntity
 *  top.ribs.scguns.event.GunFireEvent$Pre
 *  top.ribs.scguns.event.GunProjectileHitEvent
 */
package com.chromeware.compat.guns;

import com.chromeware.body.OneHanded;
import com.chromeware.compat.guns.GunComponents;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.ZetatechBerserk;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import top.ribs.scguns.entity.projectile.ProjectileEntity;
import top.ribs.scguns.event.GunFireEvent;
import top.ribs.scguns.event.GunProjectileHitEvent;

@EventBusSubscriber(modid="chromeware")
public final class GunCyberware {
    public static final String CHARGE = "chromeware_microgenerator";
    public static final String TECH = "chromeware_charged_tech";
    private static final ThreadLocal<Shot> CURRENT = new ThreadLocal();
    private static final ThreadLocal<Integer> HIT_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final Map<ItemStack, Reload> RELOADS = new WeakHashMap<ItemStack, Reload>();
    private static final Set<Player> DISCHARGING = Collections.newSetFromMap(new WeakHashMap());

    public static boolean available() {
        ModList mods = ModList.get();
        return mods.isLoaded("pointblank") || mods.isLoaded("tacz") || mods.isLoaded("superbwarfare") || mods.isLoaded("scguns");
    }

    public static double recoil(Player p) {
        return (Implant.SHOCK_ABSORBER.active(p) ? ImplantSettings.stat("shock_absorber", "recoil_multiplier", 0.75) : 1.0) * (Implant.IMMOVABLE_FORCE.active(p) ? ImplantSettings.stat("immovable_force", "recoil_multiplier", 0.65) : 1.0);
    }

    public static double spread(Player p) {
        return Implant.IMMOVABLE_FORCE.active(p) ? ImplantSettings.stat("immovable_force", "spread_multiplier", 0.75) : 1.0;
    }

    public static float shotExhaustion(Player p) {
        return ImplantEffects.attackExhaustion(p, 0.1f);
    }

    public static void reloadStarted(Player p, ItemStack gun, int count) {
        if (!p.level().isClientSide()) {
            RELOADS.put(gun, new Reload(p, count));
        }
    }

    public static void reloadFinished(Player p, ItemStack gun, int count) {
        Reload attempt = RELOADS.remove(gun);
        if (attempt != null && attempt.player == p && attempt.before == 0 && count > 0 && Implant.MICROGENERATOR.active(p)) {
            gun.set(GunComponents.MICROGENERATOR.get(), (Object)Math.min(ImplantSettings.istat("microgenerator", "maximum_charges", 5), count));
        }
    }

    public static int charge(ItemStack gun) {
        return Math.clamp((long)((Integer)gun.getOrDefault(GunComponents.MICROGENERATOR.get(), (Object)0)).intValue(), (int)0, (int)5);
    }

    public static Shot fired(Player p, ItemStack gun) {
        if (p.level().isClientSide()) {
            return new Shot(p, 0.0f);
        }
        int bullets = GunCyberware.charge(gun);
        if (bullets > 0) {
            gun.remove(GunComponents.MICROGENERATOR.get());
        }
        p.causeFoodExhaustion(GunCyberware.shotExhaustion(p));
        return new Shot(p, Implant.MICROGENERATOR.active(p) ? (float)bullets : 0.0f);
    }

    public static Shot fired(Player p, ItemStack gun, boolean chargedTech) {
        Shot shot = GunCyberware.fired(p, gun);
        shot.tech = chargedTech;
        return shot;
    }

    public static Shot current() {
        return CURRENT.get();
    }

    public static void current(Shot shot) {
        if (shot == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(shot);
        }
    }

    public static void enterHit() {
        HIT_DEPTH.set(HIT_DEPTH.get() + 1);
    }

    public static void leaveHit() {
        int n = HIT_DEPTH.get() - 1;
        if (n <= 0) {
            HIT_DEPTH.remove();
        } else {
            HIT_DEPTH.set(n);
        }
    }

    public static boolean firearmDamage(DamageSource source) {
        if (HIT_DEPTH.get() > 0) {
            return true;
        }
        Optional key = source.typeHolder().unwrapKey();
        if (key.isEmpty()) {
            return false;
        }
        String namespace = ((ResourceKey)key.get()).location().getNamespace();
        String path = ((ResourceKey)key.get()).location().getPath();
        return namespace.equals("tacz") && path.contains("bullet") || namespace.equals("scguns") && path.equals("bullet") || namespace.equals("superbwarfare") && (path.startsWith("gunfire") || path.equals("laser") || path.equals("laser_headshot"));
    }

    public static void attach(Shot shot, Entity bullet) {
        if (shot != null && shot.charge > 0.0f) {
            bullet.getPersistentData().putFloat(CHARGE, shot.charge);
            shot.charge = 0.0f;
        }
        if (shot != null && shot.tech) {
            bullet.getPersistentData().putBoolean(TECH, true);
            shot.tech = false;
        }
    }

    @SubscribeEvent
    public static void spawned(EntityJoinLevelEvent event) {
        Entity entity;
        if (!event.getLevel().isClientSide() && (entity = event.getEntity()) instanceof Projectile) {
            Projectile bullet = (Projectile)entity;
            if (GunCyberware.current() != null && bullet.getOwner() == GunCyberware.current().player) {
                GunCyberware.attach(GunCyberware.current(), (Entity)bullet);
            }
        }
    }

    public static void impact(Entity projectile, Player owner, Vec3 pos) {
        float charge = projectile.getPersistentData().getFloat(CHARGE);
        projectile.getPersistentData().remove(CHARGE);
        GunCyberware.shock(owner, pos, charge);
    }

    public static void impactCurrent(Vec3 pos) {
        Shot shot = GunCyberware.current();
        if (shot == null) {
            return;
        }
        float charge = shot.charge;
        shot.charge = 0.0f;
        GunCyberware.shock(shot.player, pos, charge);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void shock(Player owner, Vec3 pos, float charge) {
        ServerLevel level;
        block10: {
            block9: {
                Level level2;
                if (charge <= 0.0f || !((level2 = owner.level()) instanceof ServerLevel)) break block9;
                level = (ServerLevel)level2;
                if (Implant.MICROGENERATOR.active(owner) && DISCHARGING.add(owner)) break block10;
            }
            return;
        }
        try {
            for (int i = 0; i < Math.clamp((long)((int)charge), (int)1, (int)ImplantSettings.istat("microgenerator", "maximum_charges", 5)); ++i) {
                LightningBolt bolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create((Level)level);
                if (bolt == null) continue;
                bolt.moveTo(pos);
                if (owner instanceof ServerPlayer) {
                    ServerPlayer server = (ServerPlayer)owner;
                    bolt.setCause(server);
                }
                level.addFreshEntity((Entity)bolt);
            }
        }
        finally {
            DISCHARGING.remove(owner);
        }
    }

    private record Reload(Player player, int before) {
    }

    public static final class Shot {
        public final Player player;
        private float charge;
        private boolean tech;

        private Shot(Player player, float charge) {
            this.player = player;
            this.charge = charge;
        }
    }

    public static final class Scorched {
        public static void register() {
            IEventBus bus = NeoForge.EVENT_BUS;
            bus.addListener(EventPriority.HIGH, Scorched::fire);
            bus.addListener(Scorched::spawn);
            bus.addListener(EventPriority.LOWEST, Scorched::hit);
        }

        private static void fire(GunFireEvent.Pre event) {
            Player player = event.getEntity();
            HumanoidArm arm = OneHanded.resolve(player, InteractionHand.MAIN_HAND);
            if (arm == null || ArmControls.deployedOn(player, arm) || !ZetatechBerserk.rangedAllowed(player)) {
                event.setCanceled(true);
            }
        }

        private static void spawn(EntityJoinLevelEvent event) {
            ProjectileEntity bullet;
            Entity entity;
            Shot shot = GunCyberware.current();
            if (!event.getLevel().isClientSide() && shot != null && (entity = event.getEntity()) instanceof ProjectileEntity && (bullet = (ProjectileEntity)entity).getShooter() == shot.player) {
                GunCyberware.attach(shot, (Entity)bullet);
            }
        }

        private static void hit(GunProjectileHitEvent event) {
            LivingEntity livingEntity;
            ProjectileEntity bullet = event.getProjectile();
            HitResult hit = event.getRayTrace();
            if (!bullet.level().isClientSide() && hit.getType() != HitResult.Type.MISS && (livingEntity = bullet.getShooter()) instanceof Player) {
                Player player = (Player)livingEntity;
                GunCyberware.impact((Entity)bullet, player, hit.getLocation());
            }
        }
    }
}

