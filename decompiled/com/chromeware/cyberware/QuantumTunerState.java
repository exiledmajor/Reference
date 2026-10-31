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

public record QuantumTunerState(int reserve, int recovery, int combatTicks) {
    public static final int RESERVE = 1000;
    public static final int RECOVERY = 1200;
    public static final int COMBAT_GRACE = 100;
    public static final QuantumTunerState FULL = new QuantumTunerState(QuantumTunerState.reserveCapacity(), 0, 0);
    public static final Codec<QuantumTunerState> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).fieldOf("reserve").forGetter(QuantumTunerState::reserve), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).fieldOf("recovery").forGetter(QuantumTunerState::recovery), (App)Codec.intRange((int)0, (int)100).fieldOf("combat_ticks").forGetter(QuantumTunerState::combatTicks)).apply((Applicative)i, QuantumTunerState::new));
    public static final StreamCodec<ByteBuf, QuantumTunerState> STREAM_CODEC = new StreamCodec<ByteBuf, QuantumTunerState>(){

        public QuantumTunerState decode(ByteBuf b) {
            return new QuantumTunerState(b.readInt(), b.readInt(), b.readInt());
        }

        public void encode(ByteBuf b, QuantumTunerState s) {
            b.writeInt(s.reserve).writeInt(s.recovery).writeInt(s.combatTicks);
        }
    };

    public QuantumTunerState {
        reserve = Math.max(0, reserve);
        recovery = reserve == QuantumTunerState.reserveCapacity() ? 0 : Math.max(1, recovery);
        combatTicks = Math.clamp((long)combatTicks, (int)0, (int)100);
    }

    public static int reserveCapacity() {
        return ImplantSettings.ticks("quantum_tuner", "reserve_seconds", 1000);
    }

    public QuantumTunerState spend(int ticks) {
        int spent = Math.min(this.reserve, Math.max(0, ticks));
        return spent == 0 ? this : new QuantumTunerState(this.reserve - spent, ImplantSettings.cooldown("quantum_tuner", 1200), this.combatTicks);
    }

    public QuantumTunerState tick(boolean fighting) {
        int grace;
        int n = grace = fighting ? 100 : Math.max(0, this.combatTicks - 1);
        if (this.reserve == QuantumTunerState.reserveCapacity() || fighting || this.combatTicks > 0) {
            return new QuantumTunerState(this.reserve, this.recovery, grace);
        }
        return this.recovery <= 1 ? new QuantumTunerState(QuantumTunerState.reserveCapacity(), 0, grace) : new QuantumTunerState(this.reserve, this.recovery - 1, grace);
    }

    public QuantumTunerState reduceRecovery(int ticks) {
        if (this.recovery == 0) {
            return this;
        }
        return ticks >= this.recovery ? new QuantumTunerState(QuantumTunerState.reserveCapacity(), 0, this.combatTicks) : new QuantumTunerState(this.reserve, this.recovery - Math.max(0, ticks), this.combatTicks);
    }

    public QuantumTunerState tickSharedCombat(boolean fighting) {
        QuantumTunerState next = new QuantumTunerState(this.reserve, this.recovery, 0).tick(fighting);
        return new QuantumTunerState(next.reserve, next.recovery, 0);
    }
}

