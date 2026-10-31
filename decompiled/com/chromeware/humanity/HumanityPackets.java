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
 *  net.minecraft.server.level.ServerPlayer
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.chromeware.humanity;

import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class HumanityPackets {
    public static Consumer<Episode> clientReceiver = episode -> {};
    public static Consumer<CustomPacketPayload> syringeReceiver = payload -> {};

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(Episode.TYPE, Episode.CODEC, (packet, context) -> clientReceiver.accept((Episode)packet));
        registrar.playToClient(SyringeStart.TYPE, SyringeStart.CODEC, (packet, context) -> syringeReceiver.accept(packet));
        registrar.playToClient(SyringeStop.TYPE, SyringeStop.CODEC, (packet, context) -> syringeReceiver.accept(packet));
        registrar.playToClient(Notice.TYPE, Notice.CODEC, (packet, context) -> syringeReceiver.accept(packet));
    }

    static void send(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)payload, (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public record Episode(int kind, int stage, float intensity, boolean berserk) implements CustomPacketPayload
    {
        public static final int THRESHOLD = 0;
        public static final int EPISODE = 1;
        public static final int CLEAR = 2;
        public static final CustomPacketPayload.Type<Episode> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"psychosis_episode"));
        public static final StreamCodec<ByteBuf, Episode> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Episode::kind, (StreamCodec)ByteBufCodecs.VAR_INT, Episode::stage, (StreamCodec)ByteBufCodecs.FLOAT, Episode::intensity, (StreamCodec)ByteBufCodecs.BOOL, Episode::berserk, Episode::new);

        public CustomPacketPayload.Type<Episode> type() {
            return TYPE;
        }
    }

    public record SyringeStart(int actor, boolean other, boolean accelerated, int target, double actorX, double actorZ, double targetX, double targetZ) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<SyringeStart> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"syringe_animation"));
        public static final StreamCodec<ByteBuf, SyringeStart> CODEC = new StreamCodec<ByteBuf, SyringeStart>(){

            public SyringeStart decode(ByteBuf b) {
                return new SyringeStart((Integer)ByteBufCodecs.VAR_INT.decode((Object)b), b.readBoolean(), b.readBoolean(), (Integer)ByteBufCodecs.VAR_INT.decode((Object)b), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble());
            }

            public void encode(ByteBuf b, SyringeStart s) {
                ByteBufCodecs.VAR_INT.encode((Object)b, (Object)s.actor);
                b.writeBoolean(s.other);
                b.writeBoolean(s.accelerated);
                ByteBufCodecs.VAR_INT.encode((Object)b, (Object)s.target);
                b.writeDouble(s.actorX);
                b.writeDouble(s.actorZ);
                b.writeDouble(s.targetX);
                b.writeDouble(s.targetZ);
            }
        };

        public CustomPacketPayload.Type<SyringeStart> type() {
            return TYPE;
        }
    }

    public record SyringeStop(int actor) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<SyringeStop> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"syringe_animation_stop"));
        public static final StreamCodec<ByteBuf, SyringeStop> CODEC = ByteBufCodecs.VAR_INT.map(SyringeStop::new, SyringeStop::actor);

        public CustomPacketPayload.Type<SyringeStop> type() {
            return TYPE;
        }
    }

    public record Notice(int kind, double amount) implements CustomPacketPayload
    {
        public static final int APPLIED = 0;
        public static final int EXPIRED = 1;
        public static final CustomPacketPayload.Type<Notice> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"immunosuppressant_notice"));
        public static final StreamCodec<ByteBuf, Notice> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Notice::kind, (StreamCodec)ByteBufCodecs.DOUBLE, Notice::amount, Notice::new);

        public CustomPacketPayload.Type<Notice> type() {
            return TYPE;
        }
    }
}

