/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.entity.HumanoidArm
 */
package com.chromeware.cyberware;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.HumanoidArm;

public record GorillaState(int deployed, int leftTicks, int rightTicks, int leftSequence, int rightSequence, int heavy) {
    public static final GorillaState NONE = new GorillaState(0, 0, 0, 0, 0, 0);
    public static final StreamCodec<ByteBuf, GorillaState> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, GorillaState::deployed, (StreamCodec)ByteBufCodecs.VAR_INT, GorillaState::leftTicks, (StreamCodec)ByteBufCodecs.VAR_INT, GorillaState::rightTicks, (StreamCodec)ByteBufCodecs.VAR_INT, GorillaState::leftSequence, (StreamCodec)ByteBufCodecs.VAR_INT, GorillaState::rightSequence, (StreamCodec)ByteBufCodecs.VAR_INT, GorillaState::heavy, GorillaState::new);

    public static int bit(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? 1 : 2;
    }

    public boolean deployed(HumanoidArm side) {
        return (this.deployed & GorillaState.bit(side)) != 0;
    }

    public boolean charging(HumanoidArm side) {
        return (this.heavy & GorillaState.bit(side)) != 0;
    }

    public boolean smash(HumanoidArm side) {
        return (this.heavy & GorillaState.bit(side) << 2) != 0;
    }

    public int ticks(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.leftTicks : this.rightTicks;
    }

    public int sequence(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? this.leftSequence : this.rightSequence;
    }

    public GorillaState tick() {
        return new GorillaState(this.deployed, Math.max(0, this.leftTicks - 1), Math.max(0, this.rightTicks - 1), this.leftSequence, this.rightSequence, this.heavy);
    }

    public GorillaState deploy(int mask) {
        return new GorillaState(mask, this.leftTicks, this.rightTicks, this.leftSequence, this.rightSequence, this.heavy & (mask | mask << 2));
    }

    public GorillaState charge(HumanoidArm side, boolean on) {
        return new GorillaState(this.deployed, this.leftTicks, this.rightTicks, this.leftSequence, this.rightSequence, on ? this.heavy | GorillaState.bit(side) : this.heavy & ~GorillaState.bit(side));
    }

    public GorillaState punch(HumanoidArm side, int ticks, boolean smash) {
        int mask = this.heavy & ~(GorillaState.bit(side) | GorillaState.bit(side) << 2) | (smash ? GorillaState.bit(side) << 2 : 0);
        return side == HumanoidArm.LEFT ? new GorillaState(this.deployed, ticks, this.rightTicks, this.leftSequence + 1, this.rightSequence, mask) : new GorillaState(this.deployed, this.leftTicks, ticks, this.leftSequence, this.rightSequence + 1, mask);
    }

    public GorillaState interrupt(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? new GorillaState(this.deployed, 0, this.rightTicks, this.leftSequence, this.rightSequence, this.heavy) : new GorillaState(this.deployed, this.leftTicks, 0, this.leftSequence, this.rightSequence, this.heavy);
    }
}

