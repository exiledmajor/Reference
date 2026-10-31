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
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.cyberware.ArmControls;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.DisabledNotice;
import com.chromeware.cyberware.ImplantAbility;
import com.chromeware.cyberware.ImplantColors;
import com.chromeware.cyberware.MovementImplants;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.surgery.Surgery;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ImplantPackets {
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SetColor.TYPE, SetColor.CODEC, (packet, context) -> {
            if (!ImplantColors.valid(packet.key())) {
                return;
            }
            Player player = context.player();
            player.setData(PlayerData.IMPLANT_COLORS, (Object)((ImplantColors)player.getData(PlayerData.IMPLANT_COLORS)).with(packet.key(), packet.rgb()));
        });
        event.registrar("1").playToServer(JumpInput.TYPE, JumpInput.CODEC, (packet, context) -> MovementImplants.request(context.player(), packet.action()));
        event.registrar("1").playToServer(Limb.TYPE, Limb.CODEC, (packet, context) -> {
            Player player = context.player();
            BodyPart part = packet.limb();
            if (part == null) {
                return;
            }
            switch (packet.action()) {
                case 0: {
                    CyberLimbs.request(player, part);
                    break;
                }
                case 1: {
                    Surgery.requestRemoval(player, new Surgery.Removal(-3, part.ordinal()));
                    break;
                }
                case 2: 
                case 3: {
                    CyberLimbs.synthskin(player, part, packet.action() == 2);
                    break;
                }
            }
        });
        event.registrar("2").playToServer(Arm.TYPE, Arm.CODEC, (packet, context) -> {
            Player player = context.player();
            if ((packet.action() == 0 || packet.action() == 1 || packet.action() == 4 || packet.action() == 5) && DisabledNotice.blocked(player)) {
                return;
            }
            switch (packet.action()) {
                case 0: {
                    ArmControls.toggle(player);
                    break;
                }
                case 1: {
                    ArmControls.act(player, packet.arm());
                    break;
                }
                case 4: {
                    ArmControls.charge(player, packet.arm());
                    break;
                }
                case 5: {
                    ArmControls.release(player, packet.arm());
                    break;
                }
                case 6: {
                    ArmControls.cancel(player, packet.arm());
                    break;
                }
                case 2: {
                    Surgery.requestRemoval(player, new Surgery.Removal(-1, packet.side()));
                    break;
                }
                case 3: {
                    ArmImplants.request(player, packet.arm());
                    break;
                }
            }
        });
        event.registrar("1").playToServer(RemoveOperatingSystem.TYPE, RemoveOperatingSystem.CODEC, (packet, context) -> Surgery.requestRemoval(context.player(), new Surgery.Removal(-2, 0)));
        event.registrar("1").playToServer(RemoveInternal.TYPE, RemoveInternal.CODEC, (packet, context) -> Surgery.requestRemoval(context.player(), new Surgery.Removal(packet.category(), packet.index())));
        event.registrar("1").playToServer(Activate.TYPE, Activate.CODEC, (packet, context) -> {
            if (packet.ability() >= 0 && packet.ability() < ImplantAbility.values().length) {
                ImplantAbility.values()[packet.ability()].activate(context.player());
            }
        });
    }

    public record SetColor(String key, int rgb) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<SetColor> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"implant_color"));
        public static final StreamCodec<ByteBuf, SetColor> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.stringUtf8((int)64), SetColor::key, (StreamCodec)ByteBufCodecs.INT, SetColor::rgb, SetColor::new);

        public CustomPacketPayload.Type<SetColor> type() {
            return TYPE;
        }
    }

    public record JumpInput(int action) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<JumpInput> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"implant_jump"));
        public static final StreamCodec<ByteBuf, JumpInput> CODEC = ByteBufCodecs.VAR_INT.map(JumpInput::new, JumpInput::action);

        public CustomPacketPayload.Type<JumpInput> type() {
            return TYPE;
        }
    }

    public record Limb(int action, int part) implements CustomPacketPayload
    {
        public static final int TARGET = 0;
        public static final int REMOVE = 1;
        public static final int SYNTHSKIN = 2;
        public static final int CHROME = 3;
        public static final CustomPacketPayload.Type<Limb> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"limb_frame"));
        public static final StreamCodec<ByteBuf, Limb> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Limb::action, (StreamCodec)ByteBufCodecs.VAR_INT, Limb::part, Limb::new);

        public static Limb of(int action, BodyPart part) {
            return new Limb(action, part.ordinal());
        }

        public BodyPart limb() {
            BodyPart[] parts = BodyPart.values();
            return this.part >= 0 && this.part < parts.length && CyberLimbs.PARTS.contains((Object)parts[this.part]) ? parts[this.part] : null;
        }

        public CustomPacketPayload.Type<Limb> type() {
            return TYPE;
        }
    }

    public record Arm(int action, int side) implements CustomPacketPayload
    {
        public static final int TOGGLE = 0;
        public static final int ACT = 1;
        public static final int REMOVE = 2;
        public static final int TARGET = 3;
        public static final int CHARGE = 4;
        public static final int RELEASE = 5;
        public static final int CANCEL = 6;
        public static final CustomPacketPayload.Type<Arm> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"arm_control"));
        public static final StreamCodec<ByteBuf, Arm> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, Arm::action, (StreamCodec)ByteBufCodecs.VAR_INT, Arm::side, Arm::new);

        public static Arm of(int action, HumanoidArm side) {
            return new Arm(action, side == HumanoidArm.LEFT ? 1 : 0);
        }

        public HumanoidArm arm() {
            return this.side == 1 ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        }

        public CustomPacketPayload.Type<Arm> type() {
            return TYPE;
        }
    }

    public record RemoveOperatingSystem() implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RemoveOperatingSystem> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"remove_operating_system"));
        public static final StreamCodec<ByteBuf, RemoveOperatingSystem> CODEC = StreamCodec.unit((Object)new RemoveOperatingSystem());

        public CustomPacketPayload.Type<RemoveOperatingSystem> type() {
            return TYPE;
        }
    }

    public record RemoveInternal(int category, int index) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<RemoveInternal> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"remove_internal"));
        public static final StreamCodec<ByteBuf, RemoveInternal> CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, RemoveInternal::category, (StreamCodec)ByteBufCodecs.VAR_INT, RemoveInternal::index, RemoveInternal::new);

        public CustomPacketPayload.Type<RemoveInternal> type() {
            return TYPE;
        }
    }

    public record Activate(int ability) implements CustomPacketPayload
    {
        public static final CustomPacketPayload.Type<Activate> TYPE = new CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath((String)"chromeware", (String)"activate_implant"));
        public static final StreamCodec<ByteBuf, Activate> CODEC = ByteBufCodecs.VAR_INT.map(Activate::new, Activate::ability);

        public CustomPacketPayload.Type<Activate> type() {
            return TYPE;
        }
    }
}

