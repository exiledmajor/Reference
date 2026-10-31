/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.saveddata.SavedData
 *  net.minecraft.world.level.saveddata.SavedData$Factory
 */
package com.chromeware.netrunning;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;

public final class DevicePower
extends SavedData {
    private static final String NAME = "chromeware_device_power";
    private static final Map<BlockGetter, Set<BlockPos>> ACTIVE = Collections.synchronizedMap(new WeakHashMap());
    private final Set<BlockPos> powered = new HashSet<BlockPos>();

    public static DevicePower get(ServerLevel level) {
        DevicePower data = (DevicePower)level.getDataStorage().computeIfAbsent(new SavedData.Factory(DevicePower::new, DevicePower::load, null), NAME);
        if (!data.powered.isEmpty()) {
            ACTIVE.put((BlockGetter)level, data.powered);
        }
        return data;
    }

    private static DevicePower load(CompoundTag tag, HolderLookup.Provider registries) {
        DevicePower data = new DevicePower();
        for (long packed : tag.getLongArray("powered")) {
            data.powered.add(BlockPos.of((long)packed));
        }
        return data;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("powered", (Tag)new LongArrayTag(this.powered.stream().mapToLong(BlockPos::asLong).toArray()));
        return tag;
    }

    public static boolean powers(BlockGetter level, BlockPos pos) {
        if (ACTIVE.isEmpty()) {
            return false;
        }
        Set<BlockPos> set = ACTIVE.get(level);
        return set != null && set.contains(pos);
    }

    public static boolean powered(ServerLevel level, BlockPos pos) {
        return DevicePower.get((ServerLevel)level).powered.contains(pos);
    }

    public static void set(ServerLevel level, BlockPos pos, boolean on) {
        boolean changed;
        DevicePower data = DevicePower.get(level);
        boolean bl = changed = on ? data.powered.add(pos.immutable()) : data.powered.remove(pos);
        if (!changed) {
            return;
        }
        data.setDirty();
        if (data.powered.isEmpty()) {
            ACTIVE.remove(level);
        } else {
            ACTIVE.put((BlockGetter)level, data.powered);
        }
        Block block = level.getBlockState(pos).getBlock();
        level.neighborChanged(pos, block, pos);
        level.updateNeighborsAt(pos, block);
    }

    public static void forget(ServerLevel level, BlockPos pos) {
        if (DevicePower.powered(level, pos)) {
            DevicePower.set(level, pos, false);
        }
    }
}

