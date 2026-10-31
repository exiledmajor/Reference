/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LimbState(int missingMask) {
    public static final int ALL_MASK = (1 << BodyPart.values().length) - 1;
    public static final LimbState INTACT = new LimbState(0);
    public static final LimbState CORE = new LimbState(255);
    public static final Codec<LimbState> CODEC = Codec.INT.xmap(LimbState::new, LimbState::missingMask);
    public static final StreamCodec<ByteBuf, LimbState> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(LimbState::new, LimbState::missingMask);

    public LimbState {
        missingMask &= ALL_MASK;
        for (BodyPart part : BodyPart.values()) {
            if ((missingMask & part.bit()) == 0) continue;
            missingMask |= part.subtreeMask();
        }
    }

    public boolean isMissing(BodyPart part) {
        return (this.missingMask & part.bit()) != 0;
    }

    public LimbState remove(BodyPart part) {
        return new LimbState(this.missingMask | part.subtreeMask());
    }

    public LimbState restore(BodyPart part) {
        if (part.parent() != null && this.isMissing(part.parent())) {
            throw new IllegalStateException("Restore " + part.parent().key() + " first");
        }
        return new LimbState(this.missingMask & ~part.subtreeMask());
    }

    public LimbState restoreTorso() {
        return new LimbState(this.missingMask & ~BodyPart.BODY.bit());
    }
}

