/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 */
package com.chromeware.sandevistan;

import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.SandevistanContraptions;
import com.chromeware.sandevistan.TimeField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SandevistanPackets {
    public static Consumer<Snapshot> clientReceiver = snapshot -> {};

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("3").playToServer(Toggle.TYPE, Toggle.CODEC, (p, c) -> {
            Player patt0$temp = c.player();
            if (patt0$temp instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)patt0$temp;
                Sandevistan.toggle(player);
            }
        });
        event.registrar("3").playToClient(Snapshot.TYPE, Snapshot.CODEC, (p, c) -> clientReceiver.accept((Snapshot)p));
    }

    private static Snapshot snapshot(Level level) {
        return new Snapshot(Sandevistan.serverTicks(), List.copyOf(Sandevistan.fields(level)), SandevistanContraptions.owners(level), SandevistanContraptions.clocks(level), SandevistanContraptions.controllers(level));
    }

    public static void broadcast(ServerLevel level) {
        if (level.players().isEmpty()) {
            return;
        }
        Snapshot packet = SandevistanPackets.snapshot((Level)level);
        for (ServerPlayer player : level.players()) {
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)packet, (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    public static void send(ServerPlayer player) {
        PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)SandevistanPackets.snapshot(player.level()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public record Toggle() implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Toggle> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_toggle"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Toggle> CODEC = StreamCodec.unit((Object)new Toggle());

        public CustomPacketPayload.Type<Toggle> type() {
            return TYPE;
        }
    }

    public record Snapshot(long tick, List<TimeField> fields, Map<UUID, UUID> rigs, Map<UUID, UUID> bodyClocks, Set<BlockPos> controllers) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Snapshot> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"sandevistan_snapshot"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC = new StreamCodec<RegistryFriendlyByteBuf, Snapshot>(){

            public Snapshot decode(RegistryFriendlyByteBuf b) {
                long tick = b.readLong();
                int count = b.readVarInt();
                if (count < 0 || count > 4096) {
                    throw new IllegalArgumentException("Invalid time-field count");
                }
                ArrayList<TimeField> fields = new ArrayList<TimeField>(count);
                for (int i = 0; i < count; ++i) {
                    fields.add(new TimeField(b.readUUID(), (ResourceKey<Level>)b.readResourceKey(Registries.DIMENSION), b.readDouble(), b.readDouble(), b.readDouble(), b.readDouble(), b.readUnsignedByte(), b.readLong(), b.readUnsignedByte()));
                }
                int rigCount = b.readVarInt();
                if (rigCount < 0 || rigCount > 65536) {
                    throw new IllegalArgumentException("Invalid rig count");
                }
                HashMap<UUID, UUID> rigs = new HashMap<UUID, UUID>();
                for (int i = 0; i < rigCount; ++i) {
                    rigs.put(b.readUUID(), b.readUUID());
                }
                int clockCount = b.readVarInt();
                if (clockCount < 0 || clockCount > 65536) {
                    throw new IllegalArgumentException("Invalid body clock count");
                }
                HashMap<UUID, UUID> clocks = new HashMap<UUID, UUID>();
                for (int i = 0; i < clockCount; ++i) {
                    clocks.put(b.readUUID(), b.readUUID());
                }
                int controllerCount = b.readVarInt();
                if (controllerCount < 0 || controllerCount > 65536) {
                    throw new IllegalArgumentException("Invalid controller count");
                }
                HashSet<BlockPos> controllers = new HashSet<BlockPos>();
                for (int i = 0; i < controllerCount; ++i) {
                    controllers.add(b.readBlockPos());
                }
                return new Snapshot(tick, List.copyOf(fields), Map.copyOf(rigs), Map.copyOf(clocks), Set.copyOf(controllers));
            }

            public void encode(RegistryFriendlyByteBuf b, Snapshot p) {
                b.writeLong(p.tick);
                b.writeVarInt(p.fields.size());
                for (TimeField f : p.fields) {
                    b.writeUUID(f.owner());
                    b.writeResourceKey(f.dimension());
                    b.writeDouble(f.x());
                    b.writeDouble(f.y());
                    b.writeDouble(f.z());
                    b.writeDouble(f.radius());
                    b.writeByte(f.slowdown());
                    b.writeLong(f.origin());
                    b.writeByte(f.mode());
                }
                b.writeVarInt(p.rigs.size());
                p.rigs.forEach((body, owner) -> {
                    b.writeUUID(body);
                    b.writeUUID(owner);
                });
                b.writeVarInt(p.bodyClocks.size());
                p.bodyClocks.forEach((body, owner) -> {
                    b.writeUUID(body);
                    b.writeUUID(owner);
                });
                b.writeVarInt(p.controllers.size());
                p.controllers.forEach(arg_0 -> ((RegistryFriendlyByteBuf)b).writeBlockPos(arg_0));
            }
        };

        public Snapshot(long tick, List<TimeField> fields) {
            this(tick, fields, Map.of(), Map.of(), Set.of());
        }

        public CustomPacketPayload.Type<Snapshot> type() {
            return TYPE;
        }
    }
}

