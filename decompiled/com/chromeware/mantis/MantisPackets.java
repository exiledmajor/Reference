/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 */
package com.chromeware.mantis;

import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.mantis.MantisImplant;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class MantisPackets {
    public static final int TOGGLE = 0;
    public static final int SLASH = 1;
    public static final int CHARGE = 2;
    public static final int RELEASE = 3;
    public static final int CANCEL = 4;

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("3").playToServer(Action.TYPE, Action.CODEC, (packet, context) -> {
            Player p = context.player();
            if (packet.action() != 4 && DisabledNotice.blocked(p)) {
                return;
            }
            switch (packet.action()) {
                case 0: {
                    MantisImplant.toggle(p);
                    break;
                }
                case 1: {
                    MantisImplant.slash(p, p.getMainArm());
                    break;
                }
                case 2: {
                    MantisImplant.charge(p);
                    break;
                }
                case 3: {
                    MantisImplant.release(p);
                    break;
                }
                case 4: {
                    MantisImplant.cancel(p);
                    break;
                }
            }
        });
    }

    public record Action(int action) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Action> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"mantis_action"));
        public static final StreamCodec<ByteBuf, Action> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Action::action, Action::new);

        public CustomPacketPayload.Type<Action> type() {
            return TYPE;
        }
    }
}

