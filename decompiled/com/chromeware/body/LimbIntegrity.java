/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbState;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;

public record LimbIntegrity(List<Float> ratios) {
    public static final LimbIntegrity INTACT = new LimbIntegrity(List.of());
    public static final Codec<LimbIntegrity> CODEC = Codec.FLOAT.listOf().xmap(LimbIntegrity::new, LimbIntegrity::ratios);
    public static final StreamCodec<ByteBuf, LimbIntegrity> STREAM_CODEC = StreamCodec.of((buf, value) -> {
        for (float ratio : value.ratios) {
            buf.writeFloat(ratio);
        }
    }, buf -> {
        ArrayList<Float> ratios = new ArrayList<Float>();
        for (BodyPart ignored : BodyPart.values()) {
            ratios.add(Float.valueOf(buf.readFloat()));
        }
        return new LimbIntegrity(ratios);
    });

    public LimbIntegrity {
        ArrayList<Float> normalized = new ArrayList<Float>();
        for (BodyPart part : BodyPart.values()) {
            float value = part.ordinal() < ratios.size() ? ratios.get(part.ordinal()).floatValue() : 1.0f;
            normalized.add(Float.valueOf(Float.isFinite(value) ? Math.clamp((float)value, (float)0.0f, (float)1.0f) : 1.0f));
        }
        ratios = List.copyOf(normalized);
    }

    public float ratio(BodyPart part) {
        return this.ratios.get(part.ordinal()).floatValue();
    }

    public LimbIntegrity withRatio(BodyPart part, float ratio) {
        ArrayList<Float> next = new ArrayList<Float>(this.ratios);
        next.set(part.ordinal(), Float.valueOf(ratio));
        return new LimbIntegrity(next);
    }

    public LimbIntegrity damage(BodyPart part, float amount, float maxHealth) {
        if (!Float.isFinite(amount) || amount <= 0.0f) {
            return this;
        }
        return this.withRatio(part, this.ratio(part) - amount / part.maxIntegrity(maxHealth));
    }

    public LimbIntegrity reconcile(LimbState previous, LimbState next) {
        LimbIntegrity result = this;
        for (BodyPart part : BodyPart.values()) {
            if (next.isMissing(part)) {
                result = result.withRatio(part, 0.0f);
                continue;
            }
            if (!previous.isMissing(part)) continue;
            result = result.withRatio(part, 1.0f);
        }
        return result;
    }
}

