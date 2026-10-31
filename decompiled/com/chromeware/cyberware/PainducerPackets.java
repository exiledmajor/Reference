/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 */
package com.chromeware.cyberware;

import java.util.function.Consumer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class PainducerPackets {
    public static Consumer<Payment> clientReceiver = payment -> {};

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Payment.TYPE, Payment.CODEC, (payment, context) -> clientReceiver.accept((Payment)payment));
    }

    public static void send(ServerPlayer player) {
        if (player.connection != null) {
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new Payment(player.getHealth()), (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public record Payment(float health) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Payment> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"painducer_payment"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Payment> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.FLOAT, Payment::health, Payment::new);

        public CustomPacketPayload.Type<Payment> type() {
            return TYPE;
        }
    }
}

