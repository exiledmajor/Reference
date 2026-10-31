/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.UUIDUtil
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.chromeware.netrunning;

import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.Installing;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.cyberware.SandevistanImplant;
import com.chromeware.cyberware.ZetatechBerserk;
import com.chromeware.netrunning.DeviceHacks;
import com.chromeware.netrunning.DevicePower;
import com.chromeware.netrunning.HackFx;
import com.chromeware.netrunning.Netrunning;
import com.chromeware.netrunning.Quickhack;
import com.chromeware.sandevistan.Sandevistan;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NetrunningPackets {
    public static Consumer<CompoundTag> clientReceiver = tag -> {};
    public static Consumer<Marker> markerReceiver = marker -> {};
    public static Consumer<Veil> veilReceiver = veil -> {};
    public static final int CAST = 0;
    public static final int REMOVE = 1;
    public static final int STATUS = 3;
    public static final int INSTALL = 4;
    public static final int DECK = 5;

    public static void marker(ServerPlayer p, UUID key, Vec3 at, int entity, int ticks) {
        PacketDistributor.sendToPlayer((ServerPlayer)p, (CustomPacketPayload)new Marker(key, at.x, at.y, at.z, entity, ticks), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void veil(ServerPlayer p, int entity, int ticks) {
        PacketDistributor.sendToPlayer((ServerPlayer)p, (CustomPacketPayload)new Veil(entity, ticks), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void activateOperatingSystem(Player p) {
        if (DisabledNotice.blocked(p)) {
            return;
        }
        if (!Installing.canOperate(p) || (Netrunning.flags((Entity)p) & 4) != 0) {
            return;
        }
        if (Implant.ZETATECH_BERSERK.active(p)) {
            ZetatechBerserk.toggle(p);
        } else if (SandevistanImplant.active(p) && p instanceof ServerPlayer) {
            ServerPlayer server = (ServerPlayer)p;
            Sandevistan.toggle(server);
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(OperatingSystem.TYPE, OperatingSystem.CODEC, (packet, context) -> NetrunningPackets.activateOperatingSystem(context.player()));
        registrar.playToServer(Action.TYPE, Action.CODEC, (packet, context) -> {
            Player patt0$temp = context.player();
            if (!(patt0$temp instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer p = (ServerPlayer)patt0$temp;
            Entity target = p.level().getEntity(packet.target());
            switch (packet.action()) {
                case 0: {
                    Netrunning.cast((Player)p, target, Quickhack.byIndex(packet.hack()));
                    break;
                }
                case 1: {
                    Quickhack hack = Quickhack.byIndex(packet.hack());
                    if (hack == null) break;
                    Netrunning.removeProgram((Player)p, Netrunning.state((Player)p).programs().indexOf(hack.id));
                    break;
                }
                case 3: {
                    break;
                }
                case 4: {
                    if (packet.hack() < 0 || packet.hack() >= p.getInventory().getContainerSize()) break;
                    Netrunning.installProgram((Player)p, p.getInventory().getItem(packet.hack()));
                    break;
                }
                case 5: {
                    p.setData(PlayerData.DECK_OPEN, (Object)(packet.hack() == 1 && Netrunning.usable((Player)p) ? 1 : 0));
                    return;
                }
                default: {
                    return;
                }
            }
            if (Netrunning.installed((Player)p)) {
                NetrunningPackets.status(p, target);
            }
        });
        registrar.playToServer(DeviceAction.TYPE, DeviceAction.CODEC, (packet, context) -> {
            ServerPlayer p;
            Player patt0$temp = context.player();
            if (!(patt0$temp instanceof ServerPlayer) || !(p = (ServerPlayer)patt0$temp).level().isLoaded(packet.pos())) {
                return;
            }
            if (packet.action() == 0) {
                Netrunning.castBlock((Player)p, packet.pos(), Quickhack.byIndex(packet.hack()));
            } else if (packet.action() != 3) {
                return;
            }
            if (Netrunning.installed((Player)p)) {
                NetrunningPackets.deviceStatus(p, packet.pos());
            }
        });
        registrar.playToClient(Status.TYPE, Status.CODEC, (packet, context) -> clientReceiver.accept(packet.data()));
        registrar.playToClient(Marker.TYPE, Marker.CODEC, (packet, context) -> markerReceiver.accept((Marker)packet));
        registrar.playToClient(Veil.TYPE, Veil.CODEC, (packet, context) -> veilReceiver.accept((Veil)packet));
        registrar.playToClient(HackFx.Fx.TYPE, HackFx.Fx.CODEC, (packet, context) -> HackFx.receiver.accept((HackFx.Fx)packet));
    }

    private static CompoundTag costs(ServerPlayer p) {
        CompoundTag costs = new CompoundTag();
        for (String id : Netrunning.state((Player)p).programs()) {
            Quickhack h = Quickhack.byId(id);
            if (h == null) continue;
            costs.putInt(id, Netrunning.cost((Player)p, h));
        }
        return costs;
    }

    public static void deviceStatus(ServerPlayer p, BlockPos pos) {
        CompoundTag data = new CompoundTag();
        data.putInt("target", -1);
        data.putLong("device", pos.asLong());
        data.putBoolean("reachable", DeviceHacks.reachable((Player)p, pos));
        data.putBoolean("powered", DevicePower.powered(p.serverLevel(), pos));
        ListTag queue = new ListTag();
        for (Netrunning.DeviceUpload u : Netrunning.deviceUploads((Player)p, pos)) {
            CompoundTag row = new CompoundTag();
            row.putString("hack", u.hack.id);
            row.putInt("remaining", u.remaining);
            row.putInt("total", u.total);
            queue.add((Object)row);
        }
        data.put("queue", (Tag)queue);
        data.put("costs", (Tag)NetrunningPackets.costs(p));
        PacketDistributor.sendToPlayer((ServerPlayer)p, (CustomPacketPayload)new Status(data), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    public static void status(ServerPlayer p, Entity target) {
        CompoundTag data = new CompoundTag();
        data.putInt("target", target == null ? -1 : target.getId());
        data.putBoolean("reachable", Netrunning.targetAllowed((Player)p, target));
        ListTag queue = new ListTag();
        for (Netrunning.Upload u : Netrunning.uploads((Player)p, target)) {
            if (queue.size() >= 32) break;
            CompoundTag row = new CompoundTag();
            row.putString("hack", u.hack.id);
            row.putInt("remaining", u.remaining);
            row.putInt("total", u.total);
            queue.add((Object)row);
        }
        data.put("queue", (Tag)queue);
        data.put("costs", (Tag)NetrunningPackets.costs(p));
        PacketDistributor.sendToPlayer((ServerPlayer)p, (CustomPacketPayload)new Status(data), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)path);
    }

    public record Marker(UUID key, double x, double y, double z, int entity, int ticks) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Marker> TYPE = new CustomPacketPayload.Type(NetrunningPackets.id("netrunning_marker"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Marker> CODEC = StreamCodec.composite((StreamCodec)UUIDUtil.STREAM_CODEC, Marker::key, (StreamCodec)ByteBufCodecs.DOUBLE, Marker::x, (StreamCodec)ByteBufCodecs.DOUBLE, Marker::y, (StreamCodec)ByteBufCodecs.DOUBLE, Marker::z, (StreamCodec)ByteBufCodecs.VAR_INT, Marker::entity, (StreamCodec)ByteBufCodecs.VAR_INT, Marker::ticks, Marker::new);

        public CustomPacketPayload.Type<Marker> type() {
            return TYPE;
        }
    }

    public record Veil(int entity, int ticks) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Veil> TYPE = new CustomPacketPayload.Type(NetrunningPackets.id("netrunning_veil"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Veil> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Veil::entity, (StreamCodec)ByteBufCodecs.VAR_INT, Veil::ticks, Veil::new);

        public CustomPacketPayload.Type<Veil> type() {
            return TYPE;
        }
    }

    public record OperatingSystem() implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<OperatingSystem> TYPE = new CustomPacketPayload.Type(NetrunningPackets.id("operating_system"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OperatingSystem> CODEC = StreamCodec.unit((Object)new OperatingSystem());

        public CustomPacketPayload.Type<OperatingSystem> type() {
            return TYPE;
        }
    }

    public record Action(int action, int target, int hack) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Action> TYPE = new CustomPacketPayload.Type(NetrunningPackets.id("netrunning_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Action> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Action::action, (StreamCodec)ByteBufCodecs.VAR_INT, Action::target, (StreamCodec)ByteBufCodecs.VAR_INT, Action::hack, Action::new);

        public CustomPacketPayload.Type<Action> type() {
            return TYPE;
        }
    }

    public record DeviceAction(int action, BlockPos pos, int hack) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<DeviceAction> TYPE = new CustomPacketPayload.Type(NetrunningPackets.id("netrunning_device_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DeviceAction> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, DeviceAction::action, (StreamCodec)BlockPos.STREAM_CODEC, DeviceAction::pos, (StreamCodec)ByteBufCodecs.VAR_INT, DeviceAction::hack, DeviceAction::new);

        public CustomPacketPayload.Type<DeviceAction> type() {
            return TYPE;
        }
    }

    public record Status(CompoundTag data) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Status> TYPE = new CustomPacketPayload.Type(NetrunningPackets.id("netrunning_status"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Status> CODEC = ByteBufCodecs.COMPOUND_TAG.map(Status::new, Status::data).cast();

        public CustomPacketPayload.Type<Status> type() {
            return TYPE;
        }
    }
}

