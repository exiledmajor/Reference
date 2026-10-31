/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.chromeware.cyberware.EnemyKills;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.netrunning.Detonation;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class KiroshiOptics {
    public static final List<Implant> ALL = List.of(Implant.THE_ORACLE, Implant.STALKER, Implant.SENTRY, Implant.DOOMSAYER, Implant.COCKATRICE, Implant.CLAIRVOYANT, Implant.KIROSHI_OPTICS);
    public static final TagKey<Block> TRAPS = TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.parse((String)"chromeware:optics/traps"));
    public static final TagKey<Block> DEVICES = TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.parse((String)"chromeware:optics/devices"));
    public static final int AFTER_SCAN = 1200;
    public static final double CLAIRVOYANT_RANGE = 20.0;
    public static final double TRAP_RANGE = 30.0;
    public static final double DEVICE_RANGE = 40.0;
    public static final double STALKER_RANGE = 20.0;
    public static final double STALKER_DEPTH = 50.0;

    public static boolean optic(Implant implant) {
        return implant != null && ALL.contains((Object)implant);
    }

    public static Implant installed(Player p) {
        for (Implant optic : ALL) {
            if (!optic.active(p)) continue;
            return optic;
        }
        return null;
    }

    public static boolean enemies(Player p) {
        return Implant.CLAIRVOYANT.active(p);
    }

    public static boolean stalker(Player p) {
        return Implant.STALKER.active(p) || Implant.THE_ORACLE.active(p);
    }

    public static boolean traps(Player p) {
        return Implant.DOOMSAYER.active(p) || Implant.THE_ORACLE.active(p);
    }

    public static boolean devices(Player p) {
        return Implant.SENTRY.active(p) || Implant.THE_ORACLE.active(p);
    }

    public static boolean scanned(Player p) {
        return ImplantEffects.scanning(p) || ImplantEffects.state(p).clock("scan_after") > 0 && KiroshiOptics.installed(p) != null;
    }

    public static boolean trap(Level level, BlockState state) {
        return state.is(TRAPS) || Detonation.explosive(level, state);
    }

    public static boolean device(BlockState state) {
        return state.is(DEVICES);
    }

    public static boolean nearCrosshair(Vec3 eye, Vec3 direction, Vec3 target) {
        Vec3 offset = target.subtract(eye);
        return offset.lengthSqr() <= ImplantSettings.stat("doomsayer", "trap_range", 30.0) * ImplantSettings.stat("doomsayer", "trap_range", 30.0) && direction.dot(offset.normalize()) >= 0.95;
    }

    public static boolean clairvoyant(Player p, LivingEntity e) {
        return e.isAlive() && e.distanceToSqr((Entity)p) <= ImplantSettings.stat("clairvoyant", "enemy_range", 20.0) * ImplantSettings.stat("clairvoyant", "enemy_range", 20.0) && EnemyKills.eligible(p, e) && p.hasLineOfSight((Entity)e);
    }

    public static boolean stalked(Player p, LivingEntity e) {
        double dx = e.getX() - p.getX();
        double dz = e.getZ() - p.getZ();
        return e.isAlive() && dx * dx + dz * dz <= ImplantSettings.stat("stalker", "horizontal_range", 20.0) * ImplantSettings.stat("stalker", "horizontal_range", 20.0) && Math.abs(e.getY() - p.getY()) <= ImplantSettings.stat("stalker", "vertical_range", 50.0) && EnemyKills.eligible(p, e);
    }

    public static boolean dangerous(Entity e, Vec3 eye, Vec3 direction) {
        return e.isAlive() && (e.getType().is(ScannerTargets.EXPLOSIVES) || e.getType().is(ScannerTargets.SECURITY)) && KiroshiOptics.nearCrosshair(eye, direction, e.getBoundingBox().getCenter());
    }

    public static boolean reveals(Player p, Level level, BlockPos pos, Vec3 center) {
        if (!KiroshiOptics.scanned(p)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        double distance = p.getEyePosition().distanceToSqr(center);
        return KiroshiOptics.devices(p) && KiroshiOptics.device(state) && distance <= ImplantSettings.stat("sentry", "device_range", 40.0) * ImplantSettings.stat("sentry", "device_range", 40.0) || KiroshiOptics.traps(p) && KiroshiOptics.trap(level, state) && distance <= ImplantSettings.stat("doomsayer", "trap_range", 30.0) * ImplantSettings.stat("doomsayer", "trap_range", 30.0);
    }

    public static final class ScannerTargets {
        public static final TagKey<EntityType<?>> SECURITY = TagKey.create((ResourceKey)Registries.ENTITY_TYPE, (ResourceLocation)ResourceLocation.parse((String)"chromeware:scanner_security_devices"));
        public static final TagKey<EntityType<?>> EXPLOSIVES = TagKey.create((ResourceKey)Registries.ENTITY_TYPE, (ResourceLocation)ResourceLocation.parse((String)"chromeware:scanner_explosives"));
    }
}

