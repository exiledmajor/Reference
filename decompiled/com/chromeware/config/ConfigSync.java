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
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.common.ModConfigSpec$ConfigValue
 *  net.neoforged.neoforge.event.server.ServerStoppedEvent
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 */
package com.chromeware.config;

import com.chromeware.config.ChromewareConfig;
import com.chromeware.config.ImplantSettings;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid="chromeware")
public final class ConfigSync {
    private static Snapshot previous;

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Snapshot.TYPE, Snapshot.CODEC, (packet, context) -> packet.apply());
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 20 != 0 || !ChromewareConfig.SERVER.isLoaded()) {
            return;
        }
        Snapshot current = Snapshot.capture();
        if (!current.equals(previous)) {
            previous = current;
            PacketDistributor.sendToAllPlayers((CustomPacketPayload)current, (CustomPacketPayload[])new CustomPacketPayload[0]);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        previous = null;
    }

    public record Snapshot(boolean humanity, boolean capacity, int startingCapacity, Map<String, Double> stats) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Snapshot> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"gameplay_config"));
        public static final StreamCodec<ByteBuf, Snapshot> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.BOOL, Snapshot::humanity, (StreamCodec)ByteBufCodecs.BOOL, Snapshot::capacity, (StreamCodec)ByteBufCodecs.VAR_INT, Snapshot::startingCapacity, (StreamCodec)ByteBufCodecs.map(HashMap::new, (StreamCodec)ByteBufCodecs.STRING_UTF8, (StreamCodec)ByteBufCodecs.DOUBLE), Snapshot::stats, Snapshot::new);

        public Snapshot {
            stats = Map.copyOf(stats);
        }

        public CustomPacketPayload.Type<Snapshot> type() {
            return TYPE;
        }

        public static Snapshot capture() {
            HashMap<String, Double> values = new HashMap<String, Double>();
            ChromewareConfig.DEFINITIONS.forEach((path, stat) -> values.put((String)path, ImplantSettings.stat(stat.implant(), stat.key(), stat.defaultValue())));
            return new Snapshot(ChromewareConfig.humanityEnabled(), ChromewareConfig.capacityEnabled(), ChromewareConfig.startingCapacity(), values);
        }

        public void apply() {
            if (!ChromewareConfig.SERVER.isLoaded()) {
                return;
            }
            ChromewareConfig.HUMANITY_ENABLED.set((Object)this.humanity);
            ChromewareConfig.CAPACITY_ENABLED.set((Object)this.capacity);
            ChromewareConfig.STARTING_CAPACITY.set((Object)this.startingCapacity);
            this.stats.forEach((path, number) -> {
                ChromewareConfig.Stat definition = ChromewareConfig.DEFINITIONS.get(path);
                if (definition == null) {
                    return;
                }
                ModConfigSpec.ConfigValue<? extends Number> value = ChromewareConfig.VALUES.get(path);
                if (definition.unit().equals("integer")) {
                    value.set((Object)number.intValue());
                } else {
                    value.set(number);
                }
            });
        }
    }
}

