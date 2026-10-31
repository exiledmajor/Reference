/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.AllEntityTypes
 *  com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType
 *  com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Position
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.LargeFireball
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.windcharge.WindCharge
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.ProjectileImpactEvent
 *  net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package com.chromeware.launcher;

import com.chromeware.compat.SableCompatibility;
import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.launcher.NuggetShot;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.content.equipment.potatoCannon.PotatoProjectileEntity;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;

@EventBusSubscriber(modid="chromeware")
public final class LauncherShots {
    public static final String TAG = "chromeware_launcher_shot";
    public static final String VOLLEY_TAG = "chromeware_launcher_volley";
    public static final float DAMAGE_BONUS = 1.5f;
    public static final double POTATO_SPEED = 2.0;
    public static final double NUGGET_SPEED = 3.2;
    public static final double WIND_SPEED = 1.5;

    public static boolean ammo(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.FIRE_CHARGE) || stack.is(Items.WIND_CHARGE) || NuggetShot.nugget(stack)) {
            return true;
        }
        return LauncherShots.potato(level, stack) != null;
    }

    private static PotatoCannonProjectileType potato(Level level, ItemStack stack) {
        return PotatoCannonProjectileType.getTypeForItem((RegistryAccess)level.registryAccess(), (Item)stack.getItem()).map(Holder::value).orElse(null);
    }

    public static ItemStack pick(Player player, HumanoidArm side) {
        ItemStack held = player.getItemInHand(ArmImplants.hand(player, side));
        if (LauncherShots.ammo(player.level(), held)) {
            return held;
        }
        Inventory inventory = player.getInventory();
        ArrayList<ItemStack> found = new ArrayList<ItemStack>();
        for (int i = 0; i < inventory.getContainerSize(); ++i) {
            ItemStack stack = inventory.getItem(i);
            if (!LauncherShots.ammo(player.level(), stack)) continue;
            found.add(stack);
        }
        return found.isEmpty() ? ItemStack.EMPTY : (ItemStack)found.get(player.getRandom().nextInt(found.size()));
    }

    public static boolean fire(ServerLevel level, Player player, ItemStack source, Vec3 muzzle, Vec3 direction, boolean volley) {
        if (!LauncherShots.ammo((Level)level, source)) {
            return false;
        }
        ItemStack shot = source.copyWithCount(1);
        BlockPos at = BlockPos.containing((Position)muzzle);
        if (shot.is(Items.FIRE_CHARGE)) {
            LargeFireball fireball = new LargeFireball((Level)level, (LivingEntity)player, direction, 1);
            fireball.setPos(muzzle);
            LauncherShots.launch(level, (Projectile)fireball, volley);
            level.playSound(null, at, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.65f, 0.85f);
            level.sendParticles((ParticleOptions)ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 6, 0.03, 0.03, 0.03, 0.025);
        } else if (shot.is(Items.WIND_CHARGE)) {
            WindCharge charge = new WindCharge(player, (Level)level, muzzle.x, muzzle.y, muzzle.z);
            charge.shoot(direction.x, direction.y, direction.z, 1.5f, 0.0f);
            LauncherShots.launch(level, (Projectile)charge, volley);
            level.playSound(null, at, SoundEvents.WIND_CHARGE_THROW, SoundSource.PLAYERS, 0.6f, 0.8f);
        } else if (NuggetShot.nugget(shot)) {
            NuggetShot nugget = new NuggetShot((Level)level, (LivingEntity)player, shot);
            nugget.setPos(muzzle);
            nugget.shoot(direction.x, direction.y, direction.z, 3.2f, 0.0f);
            LauncherShots.launch(level, (Projectile)nugget, volley);
            level.playSound(null, at, SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8f, 1.5f);
            level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, 0.02, 0.02, 0.02, 0.01);
        } else {
            PotatoCannonProjectileType type = LauncherShots.potato((Level)level, shot);
            int split = Math.max(1, type.split());
            for (int i = 0; i < split; ++i) {
                Vec3 path = split == 1 ? direction : LauncherShots.spread(direction, player.getRandom().nextDouble() * Math.PI * 2.0, Math.toRadians(4.0 + player.getRandom().nextDouble() * 6.0));
                PotatoProjectileEntity potato = (PotatoProjectileEntity)AllEntityTypes.POTATO_PROJECTILE.create((Level)level);
                if (potato == null) {
                    return false;
                }
                potato.setItem(shot);
                potato.setOwner((Entity)player);
                potato.setPos(muzzle);
                potato.setDeltaMovement(path.scale(2.0 * (double)type.velocityMultiplier()));
                LauncherShots.launch(level, (Projectile)potato, volley);
            }
            PotatoProjectileEntity.playLaunchSound((Level)level, (Vec3)muzzle, (float)type.soundPitch());
        }
        if (!player.getAbilities().instabuild) {
            source.shrink(1);
        }
        return true;
    }

    private static void launch(ServerLevel level, Projectile projectile, boolean volley) {
        projectile.getPersistentData().putBoolean(TAG, true);
        if (volley) {
            projectile.getPersistentData().putBoolean(VOLLEY_TAG, true);
        }
        level.addFreshEntity((Entity)projectile);
    }

    static Vec3 spread(Vec3 direction, double around, double angle) {
        Vec3 side = Math.abs(direction.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : direction.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 up = side.cross(direction).normalize();
        Vec3 off = side.scale(Math.cos(around)).add(up.scale(Math.sin(around)));
        return direction.scale(Math.cos(angle)).add(off.scale(Math.sin(angle))).normalize();
    }

    public static boolean launcherShot(Entity entity) {
        return entity != null && entity.getPersistentData().getBoolean(TAG);
    }

    public static Vec3 worldLocation(Level level, HitResult hit) {
        SubLevel body;
        if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult)) {
            return hit.getLocation();
        }
        BlockHitResult block = (BlockHitResult)hit;
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(level, (Vec3i)block.getBlockPos()) : null;
        if (body == null) {
            return hit.getLocation();
        }
        Vector3d p = body.logicalPose().transformPosition((Vector3dc)new Vector3d(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z), new Vector3d());
        return new Vec3(p.x, p.y, p.z);
    }

    @SubscribeEvent
    public static void bonus(LivingIncomingDamageEvent event) {
        Entity shot = event.getSource().getDirectEntity();
        if (LauncherShots.launcherShot(shot) && !shot.getPersistentData().getBoolean(VOLLEY_TAG)) {
            event.setAmount(event.getAmount() * ImplantSettings.fstat("projectile_launch_system", "damage_multiplier", 1.5));
        }
    }

    @SubscribeEvent
    public static void impact(ProjectileImpactEvent event) {
        Projectile other;
        HitResult hitResult = event.getRayTraceResult();
        if (!(hitResult instanceof EntityHitResult)) {
            return;
        }
        EntityHitResult hit = (EntityHitResult)hitResult;
        Projectile projectile = event.getProjectile();
        Entity target = hit.getEntity();
        if (!LauncherShots.launcherShot((Entity)projectile)) {
            return;
        }
        if (target == projectile.getOwner()) {
            event.setCanceled(true);
            return;
        }
        if (target instanceof Projectile && LauncherShots.launcherShot((Entity)(other = (Projectile)target)) && other.getOwner() != null && other.getOwner() == projectile.getOwner()) {
            event.setCanceled(true);
            return;
        }
        if (target instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)target;
            living.invulnerableTime = 0;
        }
    }
}

