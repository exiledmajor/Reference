/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.humanity;

import com.chromeware.humanity.HumanityRules;
import com.chromeware.humanity.Suppression;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HumanityState(double permanent, double temporary, int stages, Suppression suppression) {
    public static final HumanityState EMPTY = new HumanityState(0.0, 0.0, -1, Suppression.NONE);
    public static final Codec<HumanityState> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.DOUBLE.optionalFieldOf("permanent", (Object)0.0).forGetter(HumanityState::permanent), (App)Codec.DOUBLE.optionalFieldOf("temporary", (Object)0.0).forGetter(HumanityState::temporary), (App)Codec.INT.optionalFieldOf("stages", (Object)-1).forGetter(HumanityState::stages), (App)Suppression.CODEC.optionalFieldOf("suppression", (Object)Suppression.NONE).forGetter(HumanityState::suppression)).apply((Applicative)i, HumanityState::new));

    public HumanityState {
        permanent = HumanityRules.clamp(permanent);
        temporary = HumanityRules.clamp(temporary);
        if (suppression == null) {
            suppression = Suppression.NONE;
        }
    }

    public double loss() {
        return HumanityRules.total(this.permanent, this.temporary);
    }

    public HumanityState initialized() {
        return this.stages >= 0 ? this : new HumanityState(this.permanent, this.temporary, HumanityRules.reachedStages(this.loss()), this.suppression);
    }

    public HumanityState with(double permanent, double temporary) {
        return new HumanityState(permanent, temporary, this.stages, this.suppression);
    }

    public HumanityState with(Suppression suppression) {
        return new HumanityState(this.permanent, this.temporary, this.stages, suppression);
    }

    public HumanityState stages(int stages) {
        return new HumanityState(this.permanent, this.temporary, stages, this.suppression);
    }

    public HumanityState relieveStrain(double amount) {
        double visible = Math.min(this.temporary, Math.max(0.0, amount));
        Suppression held = this.suppression.decayTemporary(Math.max(0.0, amount - visible)).next();
        return new HumanityState(this.permanent, this.temporary - visible, this.stages, held);
    }

    public record View(float permanent, float temporary, float chrome, float suppressed, int protectionTicks, boolean returning) {
        public static final View EMPTY = new View(0.0f, 0.0f, 0.0f, 0.0f, 0, false);
        public static final StreamCodec<ByteBuf, View> STREAM_CODEC = new StreamCodec<ByteBuf, View>(){

            public View decode(ByteBuf b) {
                return new View(b.readFloat(), b.readFloat(), b.readFloat(), b.readFloat(), (Integer)ByteBufCodecs.VAR_INT.decode((Object)b), b.readBoolean());
            }

            public void encode(ByteBuf b, View v) {
                b.writeFloat(v.permanent);
                b.writeFloat(v.temporary);
                b.writeFloat(v.chrome);
                b.writeFloat(v.suppressed);
                ByteBufCodecs.VAR_INT.encode((Object)b, (Object)v.protectionTicks);
                b.writeBoolean(v.returning);
            }
        };

        public double loss() {
            return HumanityRules.total(this.permanent, this.temporary);
        }

        public double humanity() {
            return HumanityRules.humanity(this.loss());
        }
    }
}

