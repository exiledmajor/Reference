/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.monowire;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record MonowireState(boolean deployed, int stroke, int action) {
    public static final int IDLE = 0;
    public static final int LASH = 1;
    public static final int CHARGE = 2;
    public static final int CROSS = 3;
    public static final MonowireState RETRACTED = new MonowireState(false, 0, 0);
    public static final StreamCodec<ByteBuf, MonowireState> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.BOOL, MonowireState::deployed, (StreamCodec)ByteBufCodecs.VAR_INT, MonowireState::stroke, (StreamCodec)ByteBufCodecs.VAR_INT, MonowireState::action, MonowireState::new);

    public MonowireState with(int stroke, int action) {
        return new MonowireState(this.deployed, stroke, action);
    }
}

