/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.Sable
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.ButtonBlock
 *  net.minecraft.world.level.block.LeverBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.level.BlockEvent$BreakEvent
 *  net.neoforged.neoforge.event.level.LevelEvent$Load
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package com.chromeware.netrunning;

import com.chromeware.compat.SableCompatibility;
import com.chromeware.cyberware.KiroshiOptics;
import com.chromeware.netrunning.Detonation;
import com.chromeware.netrunning.DevicePower;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.Quickhack;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.joml.Vector3d;
import org.joml.Vector3dc;

@EventBusSubscriber(modid="chromeware")
public final class DeviceHacks {
    public static final TagKey<Block> DEVICES = TagKey.create((ResourceKey)Registries.BLOCK, (ResourceLocation)ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"hackable_devices"));

    public static boolean device(BlockState state) {
        return state.is(DEVICES);
    }

    public static boolean applicable(Level level, BlockPos pos, Quickhack hack) {
        BlockState state = level.getBlockState(pos);
        return hack.device() ? DeviceHacks.device(state) : hack == Quickhack.DETONATE_GRENADE && Detonation.explosive(level, state);
    }

    public static boolean target(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return DeviceHacks.device(state) || Detonation.explosive(level, state);
    }

    public static Vec3 center(Level level, BlockPos pos) {
        SubLevel body;
        SubLevel subLevel = body = SableCompatibility.loaded() ? Sable.HELPER.getContaining(level, (Vec3i)pos) : null;
        if (body == null) {
            return Vec3.atCenterOf((Vec3i)pos);
        }
        Vector3d point = body.logicalPose().transformPosition((Vector3dc)new Vector3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5), new Vector3d());
        return new Vec3(point.x, point.y, point.z);
    }

    public static boolean reachable(Player p, BlockPos pos) {
        BlockHitResult block;
        if (!DeviceHacks.target(p.level(), pos)) {
            return false;
        }
        Vec3 center = DeviceHacks.center(p.level(), pos);
        if (p.getEyePosition().distanceToSqr(center) > 1024.0) {
            return false;
        }
        if (KiroshiOptics.reveals(p, p.level(), pos, center)) {
            return true;
        }
        BlockHitResult hit = p.level().clip(new ClipContext(p.getEyePosition(), center, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)p));
        return hit.getType() == HitResult.Type.MISS || hit instanceof BlockHitResult && (block = hit).getBlockPos().equals((Object)pos);
    }

    public static void apply(Player p, BlockPos pos, Quickhack hack) {
        Level level = p.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        BlockState state = level2.getBlockState(pos);
        if (!DeviceHacks.applicable((Level)level2, pos, hack)) {
            return;
        }
        Vec3 center = DeviceHacks.center((Level)level2, pos);
        level2.sendParticles((ParticleOptions)ParticleTypes.ELECTRIC_SPARK, center.x, center.y, center.z, 10, 0.3, 0.3, 0.3, 0.05);
        switch (hack) {
            case REMOTE_ACTIVATION: {
                Block block = state.getBlock();
                if (block instanceof LeverBlock) {
                    LeverBlock lever = (LeverBlock)block;
                    lever.pull(state, (Level)level2, pos, p);
                    break;
                }
                block = state.getBlock();
                if (block instanceof ButtonBlock) {
                    ButtonBlock button = (ButtonBlock)block;
                    button.press(state, (Level)level2, pos, p);
                    break;
                }
                DevicePower.set(level2, pos, !DevicePower.powered(level2, pos));
                break;
            }
            case DETONATE_GRENADE: {
                if (!Detonation.block(p, pos)) break;
                Netrunning.detonated(p);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void loaded(LevelEvent.Load event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)levelAccessor;
            DevicePower.get(level);
        }
    }

    @SubscribeEvent
    public static void broken(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (levelAccessor instanceof ServerLevel) {
            ServerLevel level = (ServerLevel)levelAccessor;
            DevicePower.forget(level, event.getPos());
        }
    }
}

