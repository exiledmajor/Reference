/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.mantis;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MantisState(boolean deployed, int action, int sequence) {
    public static final int IDLE = 0;
    public static final int SLASH = 1;
    public static final int CHARGE = 2;
    public static final int CROSS = 3;
    public static final int LEAP = 4;
    public static final MantisState RETRACTED = new MantisState(false, 0, 0);
    public static final StreamCodec<ByteBuf, MantisState> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.BOOL, MantisState::deployed, (StreamCodec)ByteBufCodecs.VAR_INT, MantisState::action, (StreamCodec)ByteBufCodecs.VAR_INT, MantisState::sequence, MantisState::new);
}

