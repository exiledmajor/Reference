/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.cyberware;

import com.chromeware.config.ImplantSettings;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SandevistanState(int activeTicks, int cooldown) {
    public static final int DURATION = 200;
    public static final int COOLDOWN = 600;
    public static final int KILL_EXTENSION = 20;
    public static final SandevistanState READY = new SandevistanState(0, 0);
    private static final Codec<SandevistanState> SAVED = RecordCodecBuilder.create(i -> i.group((App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).optionalFieldOf("active_ticks", (Object)0).forGetter(SandevistanState::activeTicks), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).optionalFieldOf("cooldown", (Object)0).forGetter(SandevistanState::cooldown)).apply((Applicative)i, SandevistanState::new));
    public static final Codec<SandevistanState> CODEC = SAVED.xmap(s -> s.activeTicks > 0 ? new SandevistanState(0, Math.max(ImplantSettings.cooldown("sandevistan", 600), s.cooldown)) : s, s -> s);
    public static final StreamCodec<ByteBuf, SandevistanState> STREAM_CODEC = new StreamCodec<ByteBuf, SandevistanState>(){

        public SandevistanState decode(ByteBuf b) {
            return new SandevistanState(b.readInt(), b.readInt());
        }

        public void encode(ByteBuf b, SandevistanState s) {
            b.writeInt(s.activeTicks);
            b.writeInt(s.cooldown);
        }
    };

    public SandevistanState {
        activeTicks = Math.max(0, activeTicks);
        cooldown = Math.max(0, cooldown);
    }

    public SandevistanState tick() {
        return new SandevistanState(Math.max(0, this.activeTicks - 1), Math.max(0, this.cooldown - 1));
    }

    public SandevistanState extend() {
        return this.activeTicks > 0 ? new SandevistanState((int)Math.min(Integer.MAX_VALUE, (long)this.activeTicks + (long)ImplantSettings.ticks("sandevistan", "kill_extension_seconds", 20)), this.cooldown) : this;
    }

    public SandevistanState reduce(int ticks) {
        return new SandevistanState(this.activeTicks, Math.max(0, this.cooldown - Math.max(0, ticks)));
    }
}

