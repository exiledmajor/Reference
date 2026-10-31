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

public record BerserkState(int activeTicks, int cooldown, int kills, boolean slamming) {
    public static final int DURATION = 220;
    public static final int COOLDOWN = 500;
    public static final BerserkState READY = new BerserkState(0, 0, 0, false);
    private static final Codec<BerserkState> SAVED = RecordCodecBuilder.create(i -> i.group((App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).optionalFieldOf("active_ticks", (Object)0).forGetter(BerserkState::activeTicks), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).optionalFieldOf("cooldown", (Object)0).forGetter(BerserkState::cooldown)).apply((Applicative)i, (active, cooldown) -> new BerserkState((int)active, (int)cooldown, 0, false)));
    public static final Codec<BerserkState> CODEC = SAVED.xmap(s -> s.activeTicks > 0 ? new BerserkState(0, Math.max(s.cooldown, ImplantSettings.cooldown("zetatech_berserk", 500)), 0, false) : s, s -> s);
    public static final StreamCodec<ByteBuf, BerserkState> STREAM_CODEC = new StreamCodec<ByteBuf, BerserkState>(){

        public BerserkState decode(ByteBuf b) {
            return new BerserkState(b.readInt(), b.readInt(), b.readInt(), b.readBoolean());
        }

        public void encode(ByteBuf b, BerserkState s) {
            b.writeInt(s.activeTicks);
            b.writeInt(s.cooldown);
            b.writeInt(s.kills);
            b.writeBoolean(s.slamming);
        }
    };

    public BerserkState {
        activeTicks = Math.max(0, activeTicks);
        cooldown = Math.max(0, cooldown);
        kills = Math.max(0, kills);
        if (activeTicks == 0) {
            kills = 0;
            slamming = false;
        }
    }

    public BerserkState tick() {
        return new BerserkState(Math.max(0, this.activeTicks - 1), Math.max(0, this.cooldown - 1), this.kills, this.slamming);
    }

    public BerserkState reduce(int ticks) {
        return new BerserkState(this.activeTicks, Math.max(0, this.cooldown - Math.max(0, ticks)), this.kills, this.slamming);
    }
}

