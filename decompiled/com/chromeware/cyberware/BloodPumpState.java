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

public record BloodPumpState(int charges, int rechargeTicks, int healingTicks) {
    public static final int MAX_CHARGES = 2;
    public static final int RECHARGE_TICKS = 600;
    public static final int HEALING_TICKS = 120;
    public static final BloodPumpState FULL = new BloodPumpState(BloodPumpState.maxCharges(), 0, 0);
    public static final Codec<BloodPumpState> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)128).fieldOf("charges").forGetter(BloodPumpState::charges), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).fieldOf("recharge_ticks").forGetter(BloodPumpState::rechargeTicks)).apply((Applicative)instance, (charges, recharge) -> new BloodPumpState((int)charges, (int)recharge, 0)));
    public static final StreamCodec<ByteBuf, BloodPumpState> STREAM_CODEC = new StreamCodec<ByteBuf, BloodPumpState>(){

        public BloodPumpState decode(ByteBuf buffer) {
            return new BloodPumpState(buffer.readInt(), buffer.readInt(), buffer.readInt());
        }

        public void encode(ByteBuf buffer, BloodPumpState state) {
            buffer.writeInt(state.charges).writeInt(state.rechargeTicks).writeInt(state.healingTicks);
        }
    };

    public BloodPumpState {
        charges = Math.clamp((long)charges, (int)0, (int)BloodPumpState.maxCharges());
        rechargeTicks = charges == BloodPumpState.maxCharges() ? 0 : Math.max(1, rechargeTicks);
        healingTicks = Math.max(0, healingTicks);
    }

    public static int maxCharges() {
        return ImplantSettings.istat("blood_pump", "charges", 2);
    }

    public BloodPumpState activate() {
        return this.activate(ImplantSettings.cooldown("blood_pump", 600));
    }

    public BloodPumpState activate(int period) {
        if (this.charges == 0) {
            return this;
        }
        return new BloodPumpState(this.charges - 1, this.charges == BloodPumpState.maxCharges() ? Math.max(1, period) : this.rechargeTicks, ImplantSettings.duration("blood_pump", 120));
    }

    public BloodPumpState tick(boolean healing) {
        return this.tick(healing, ImplantSettings.cooldown("blood_pump", 600));
    }

    public BloodPumpState tick(boolean healing, int period) {
        return this.advanceRecharge(1, healing ? Math.max(0, this.healingTicks - 1) : 0, period);
    }

    public BloodPumpState reduceRecharge(int ticks) {
        return this.reduceRecharge(ticks, ImplantSettings.cooldown("blood_pump", 600));
    }

    public BloodPumpState reduceRecharge(int ticks, int period) {
        return this.advanceRecharge(Math.max(0, ticks), this.healingTicks, period);
    }

    private BloodPumpState advanceRecharge(int ticks, int healing, int period) {
        int available = this.charges;
        int remaining = this.rechargeTicks;
        if (available < BloodPumpState.maxCharges()) {
            remaining -= ticks;
            while (remaining <= 0 && available < BloodPumpState.maxCharges()) {
                if (++available >= BloodPumpState.maxCharges()) continue;
                remaining += Math.max(1, period);
            }
        }
        return new BloodPumpState(available, remaining, healing);
    }

    public BloodPumpState stopHealing() {
        return new BloodPumpState(this.charges, this.rechargeTicks, 0);
    }

    public boolean healOnNextTick() {
        return this.healingTicks > 0 && (ImplantSettings.duration("blood_pump", 120) - this.healingTicks + 1) % 20 == 0;
    }
}

