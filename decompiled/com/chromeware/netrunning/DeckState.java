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
package com.chromeware.netrunning;

import com.chromeware.config.ImplantSettings;
import com.chromeware.netrunning.Quickhack;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;

public record DeckState(double debt, List<String> programs, int overclock, int cooldown, int camillo, int reallocator, int ice) {
    public static final int SLOTS = 8;
    public static final int OVERCLOCK = 280;
    public static final int COOLDOWN = 600;
    public static final DeckState EMPTY = new DeckState(0.0, List.of(), 0, 0, 0, 0, 0);
    private static final Codec<DeckState> SAVED = RecordCodecBuilder.create(i -> i.group((App)Codec.DOUBLE.optionalFieldOf("debt", (Object)0.0).forGetter(DeckState::debt), (App)Codec.STRING.listOf().optionalFieldOf("programs", List.of()).forGetter(DeckState::programs), (App)Codec.INT.optionalFieldOf("overclock", (Object)0).forGetter(DeckState::overclock), (App)Codec.INT.optionalFieldOf("cooldown", (Object)0).forGetter(DeckState::cooldown), (App)Codec.INT.optionalFieldOf("camillo", (Object)0).forGetter(DeckState::camillo), (App)Codec.INT.optionalFieldOf("reallocator", (Object)0).forGetter(DeckState::reallocator), (App)Codec.INT.optionalFieldOf("ice", (Object)0).forGetter(DeckState::ice)).apply((Applicative)i, DeckState::new));
    public static final Codec<DeckState> CODEC = SAVED.xmap(s -> new DeckState(s.debt, s.programs, 0, Math.max(s.cooldown, s.overclock > 0 ? ImplantSettings.cooldown("cyberdeck", 600) : 0), s.camillo, s.reallocator, s.ice), s -> s);
    public static final StreamCodec<ByteBuf, DeckState> STREAM_CODEC = new StreamCodec<ByteBuf, DeckState>(){

        public DeckState decode(ByteBuf b) {
            double debt = b.readDouble();
            int count = b.readInt();
            if (count < 0 || count > 8) {
                throw new IllegalArgumentException("Invalid program count");
            }
            ArrayList<String> ids = new ArrayList<String>();
            for (int j = 0; j < count; ++j) {
                Quickhack hack = Quickhack.byIndex(b.readInt());
                if (hack == null) {
                    throw new IllegalArgumentException("Unknown program");
                }
                ids.add(hack.id);
            }
            return new DeckState(debt, ids, b.readInt(), b.readInt(), b.readInt(), b.readInt(), b.readInt());
        }

        public void encode(ByteBuf b, DeckState s) {
            b.writeDouble(s.debt);
            b.writeInt(s.programs.size());
            s.programs.forEach(id -> b.writeInt(Quickhack.byId(id).ordinal()));
            b.writeInt(s.overclock);
            b.writeInt(s.cooldown);
            b.writeInt(s.camillo);
            b.writeInt(s.reallocator);
            b.writeInt(s.ice);
        }
    };

    public DeckState {
        debt = Double.isFinite(debt) ? Math.clamp((double)debt, (double)0.0, (double)10000.0) : 0.0;
        programs = programs.stream().filter(id -> Quickhack.byId(id) != null).distinct().limit(8L).toList();
        overclock = Math.max(0, overclock);
        cooldown = Math.max(0, cooldown);
        camillo = Math.max(0, camillo);
        reallocator = Math.max(0, reallocator);
        ice = Math.max(0, ice);
    }

    public double available(int maximum) {
        return Math.max(0.0, (double)maximum - this.debt);
    }

    public DeckState debt(double value) {
        return new DeckState(value, this.programs, this.overclock, this.cooldown, this.camillo, this.reallocator, this.ice);
    }

    public DeckState programs(List<String> value) {
        return new DeckState(this.debt, value, this.overclock, this.cooldown, this.camillo, this.reallocator, this.ice);
    }

    public DeckState stopped() {
        return new DeckState(this.debt, this.programs, 0, Math.max(this.cooldown, this.overclock > 0 ? ImplantSettings.cooldown("cyberdeck", 600) : 0), this.camillo, this.reallocator, this.ice);
    }

    public DeckState tick(double recovery) {
        return new DeckState(Math.max(0.0, this.debt - Math.max(0.0, recovery)), this.programs, Math.max(0, this.overclock - 1), this.overclock == 1 ? ImplantSettings.cooldown("cyberdeck", 600) : Math.max(0, this.cooldown - 1), Math.max(0, this.camillo - 1), Math.max(0, this.reallocator - 1), Math.max(0, this.ice - 1));
    }
}

