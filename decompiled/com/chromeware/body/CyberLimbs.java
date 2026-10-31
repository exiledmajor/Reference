/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.core.Registry
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.attachment.AttachmentType
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 *  net.neoforged.neoforge.registries.DeferredRegister
 *  net.neoforged.neoforge.registries.NeoForgeRegistries
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.BodyRestrictions;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.cyberware.CyberwareCapacity;
import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.cyberware.CyberwareStats;
import com.chromeware.cyberware.Installing;
import com.chromeware.humanity.Humanity;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber(modid="chromeware")
public record CyberLimbs(int installed, int synthskin) {
    public static final CyberLimbs NONE = new CyberLimbs(0, 0);
    public static final List<BodyPart> PARTS = List.of(BodyPart.RIGHT_ARM, BodyPart.BODY, BodyPart.RIGHT_LEG, BodyPart.LEFT_ARM, BodyPart.LEFT_LEG);
    public static final Codec<CyberLimbs> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.INT.fieldOf("installed").forGetter(CyberLimbs::installed), (App)Codec.INT.optionalFieldOf("synthskin", (Object)0).forGetter(CyberLimbs::synthskin)).apply((Applicative)i, CyberLimbs::new));
    public static final StreamCodec<ByteBuf, CyberLimbs> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, CyberLimbs::installed, (StreamCodec)ByteBufCodecs.VAR_INT, CyberLimbs::synthskin, CyberLimbs::new);
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create((Registry)NeoForgeRegistries.ATTACHMENT_TYPES, (String)"chromeware");
    public static final Supplier<AttachmentType<CyberLimbs>> FRAMES = TYPES.register("cyber_limbs", () -> AttachmentType.builder(() -> NONE).serialize(CODEC).copyOnDeath().sync(STREAM_CODEC).build());
    private static final Map<Player, BodyPart> REQUESTED = new WeakHashMap<Player, BodyPart>();

    public CyberLimbs(int installed, int synthskin) {
        int framed = BodyPart.RIGHT_ARM.bit() | BodyPart.LEFT_ARM.bit() | BodyPart.RIGHT_LEG.bit() | BodyPart.LEFT_LEG.bit() | BodyPart.BODY.bit();
        this.installed = installed;
        this.synthskin = synthskin &= (installed &= framed);
    }

    public boolean has(BodyPart part) {
        return (this.installed & part.bit()) != 0;
    }

    public boolean skinned(BodyPart part) {
        return (this.synthskin & part.bit()) != 0;
    }

    public CyberLimbs with(BodyPart part, boolean on) {
        return new CyberLimbs(on ? this.installed | part.bit() : this.installed & ~part.bit(), on ? this.synthskin : this.synthskin & ~part.bit());
    }

    public CyberLimbs skin(BodyPart part, boolean on) {
        return new CyberLimbs(this.installed, on ? this.synthskin | part.bit() : this.synthskin & ~part.bit());
    }

    public static CyberLimbs get(Player player) {
        return (CyberLimbs)player.getData(FRAMES);
    }

    public static boolean has(Player player, BodyPart part) {
        return CyberLimbs.get(player).has(part);
    }

    public static boolean chrome(Player player, BodyPart part) {
        CyberLimbs f = CyberLimbs.get(player);
        return f.has(part) && !f.skinned(part);
    }

    public static BodyPart part(HumanoidArm side) {
        return side == HumanoidArm.LEFT ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM;
    }

    public static boolean frame(ItemStack stack) {
        return stack.is(CyberwareItems.CYBER_ARM) || stack.is(CyberwareItems.CYBER_LEG) || stack.is(CyberwareItems.CYBER_TORSO);
    }

    public static ItemStack item(BodyPart part) {
        return switch (part) {
            case BodyPart.LEFT_ARM, BodyPart.RIGHT_ARM -> CyberwareItems.CYBER_ARM.toStack();
            case BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG -> CyberwareItems.CYBER_LEG.toStack();
            case BodyPart.BODY -> CyberwareItems.CYBER_TORSO.toStack();
            default -> ItemStack.EMPTY;
        };
    }

    public static boolean fits(ItemStack stack, BodyPart part) {
        return CyberLimbs.frame(stack) && ItemStack.isSameItem((ItemStack)CyberLimbs.item(part), (ItemStack)stack);
    }

    public static void request(Player player, BodyPart part) {
        if (part == null) {
            REQUESTED.remove(player);
        } else {
            REQUESTED.put(player, part);
        }
    }

    public static BodyPart target(Player player, ItemStack stack) {
        BodyPart first;
        BodyPart requested = REQUESTED.get(player);
        if (requested != null && CyberLimbs.fits(stack, requested)) {
            return requested;
        }
        if (stack.is(CyberwareItems.CYBER_TORSO)) {
            return BodyPart.BODY;
        }
        boolean arm = stack.is(CyberwareItems.CYBER_ARM);
        HumanoidArm main = player.getMainArm();
        BodyPart bodyPart = arm ? CyberLimbs.part(main) : (first = main == HumanoidArm.LEFT ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG);
        BodyPart second = arm ? CyberLimbs.part(main.getOpposite()) : (first == BodyPart.LEFT_LEG ? BodyPart.RIGHT_LEG : BodyPart.LEFT_LEG);
        LimbState body = LimbAttachments.get(player);
        for (BodyPart part : List.of(first, second)) {
            if (!body.isMissing(part) || CyberLimbs.has(player, part)) continue;
            return part;
        }
        for (BodyPart part : List.of(first, second)) {
            if (CyberLimbs.has(player, part)) continue;
            return part;
        }
        return first;
    }

    public static Component refusal(Player player, ItemStack stack, BodyPart part) {
        if (!CyberLimbs.fits(stack, part)) {
            return Component.translatable((String)"message.chromeware.frame_wrong_limb", (Object[])new Object[]{stack.getHoverName()});
        }
        if (CyberLimbs.has(player, part)) {
            return Component.translatable((String)"message.chromeware.frame_present");
        }
        if (!CyberwareCapacity.fits(player, stack, ItemStack.EMPTY)) {
            return Component.translatable((String)"message.chromeware.capacity_full", (Object[])new Object[]{CyberwareCapacity.used(player) + CyberwareStats.get(stack).capacity(), CyberwareCapacity.limit(player)});
        }
        return null;
    }

    public static boolean installHeld(Player player) {
        if (!Installing.canOperate(player)) {
            return false;
        }
        InteractionHand hand = CyberLimbs.frame(player.getMainHandItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack held = player.getItemInHand(hand);
        if (!CyberLimbs.frame(held)) {
            return false;
        }
        BodyPart part = CyberLimbs.target(player, held);
        Component refusal = CyberLimbs.refusal(player, held, part);
        if (refusal != null) {
            player.displayClientMessage(refusal, true);
            return false;
        }
        CyberLimbs.install(player, part);
        held.shrink(1);
        player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.frame_installed", (Object[])new Object[]{CyberLimbs.item(part).getHoverName(), Component.translatable((String)("screen.chromeware.limb." + CyberLimbs.key(part)))}), true);
        return true;
    }

    static void install(Player player, BodyPart part) {
        LimbState body = LimbAttachments.get(player);
        if (body.isMissing(part)) {
            LimbAttachments.set(player, part == BodyPart.BODY ? body.restoreTorso() : body.restore(part));
        }
        player.setData(FRAMES, (Object)CyberLimbs.get(player).with(part, true));
        BodyRestrictions.apply(player);
    }

    public static void remove(Player player, BodyPart part) {
        if (player.level().isClientSide() || !CyberLimbs.has(player, part)) {
            return;
        }
        player.setData(FRAMES, (Object)CyberLimbs.get(player).with(part, false));
        LimbAttachments.set(player, LimbAttachments.get(player).remove(part));
        Humanity.removed(player);
        Installing.returnItem(player, CyberLimbs.item(part));
        BodyRestrictions.apply(player);
    }

    public static void synthskin(Player player, BodyPart part, boolean on) {
        if (player.level().isClientSide() || !CyberLimbs.has(player, part)) {
            return;
        }
        player.setData(FRAMES, (Object)CyberLimbs.get(player).skin(part, on));
    }

    public static String key(BodyPart part) {
        return part == BodyPart.BODY ? "torso" : part.key();
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || player.tickCount % 5 != 0) {
            return;
        }
        CyberLimbs frames = CyberLimbs.get(player);
        if (frames.installed() == 0) {
            return;
        }
        LimbState body = LimbAttachments.get(player);
        for (BodyPart part : PARTS) {
            if (part == BodyPart.BODY || !frames.has(part) || !body.isMissing(part)) continue;
            player.setData(FRAMES, (Object)CyberLimbs.get(player).with(part, false));
            Humanity.removed(player);
            Installing.returnItem(player, CyberLimbs.item(part));
        }
    }
}

