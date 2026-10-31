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
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 *  net.neoforged.neoforge.network.registration.PayloadRegistrar
 */
package com.chromeware.surgery;

import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgerySeat;
import com.chromeware.surgery.SurgeryStock;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class SurgeryPackets {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(StockRequest.TYPE, StockRequest.CODEC, (packet, context) -> {
            SurgerySeat s;
            Player patt0$temp = context.player();
            if (!(patt0$temp instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player = (ServerPlayer)patt0$temp;
            Entity entity = player.getVehicle();
            SurgerySeat seat = entity instanceof SurgerySeat ? (s = (SurgerySeat)entity) : null;
            List<ItemStack> items = seat != null ? SurgeryStock.scan(seat).stream().map(SurgeryStock.Entry::item).toList() : List.of();
            int arms = seat != null ? SurgeryStock.arms(seat).size() : 0;
            PacketDistributor.sendToPlayer((ServerPlayer)player, (CustomPacketPayload)new Stock(items.size() > 256 ? items.subList(0, 256) : items, arms), (CustomPacketPayload[])new CustomPacketPayload[0]);
        });
        registrar.playToClient(Stock.TYPE, Stock.CODEC, (packet, context) -> {
            SurgeryStock.clientView = List.copyOf(packet.items());
            SurgeryStock.clientArms = packet.arms();
        });
        registrar.playToServer(Operate.TYPE, Operate.CODEC, (packet, context) -> Surgery.operate(context.player(), packet.item(), packet.category(), packet.index()));
    }

    public record StockRequest() implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<StockRequest> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"surgery_stock_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StockRequest> CODEC = StreamCodec.unit((Object)new StockRequest());

        public CustomPacketPayload.Type<StockRequest> type() {
            return TYPE;
        }
    }

    public record Stock(List<ItemStack> items, int arms) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Stock> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"surgery_stock"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Stock> CODEC = StreamCodec.composite((StreamCodec)ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list((int)256)), Stock::items, (StreamCodec)ByteBufCodecs.VAR_INT, Stock::arms, Stock::new);

        public CustomPacketPayload.Type<Stock> type() {
            return TYPE;
        }
    }

    public record Operate(ItemStack item, String category, int index) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Operate> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"surgery_operate"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Operate> CODEC = StreamCodec.composite((StreamCodec)ItemStack.STREAM_CODEC, Operate::item, (StreamCodec)ByteBufCodecs.stringUtf8((int)48), Operate::category, (StreamCodec)ByteBufCodecs.VAR_INT, Operate::index, Operate::new);

        public CustomPacketPayload.Type<Operate> type() {
            return TYPE;
        }
    }
}

