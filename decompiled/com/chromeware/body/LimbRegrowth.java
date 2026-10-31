/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.body;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.cyberware.ArmImplants;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public record LimbRegrowth(int part, float progress) {
    public static final LimbRegrowth NONE = new LimbRegrowth(-1, 0.0f);
    public static final int TICKS = 200;
    public static final float START = 0.15f;
    public static final Codec<LimbRegrowth> CODEC = RecordCodecBuilder.create(i -> i.group((App)Codec.INT.fieldOf("part").forGetter(LimbRegrowth::part), (App)Codec.FLOAT.fieldOf("progress").forGetter(LimbRegrowth::progress)).apply((Applicative)i, LimbRegrowth::new));
    public static final StreamCodec<ByteBuf, LimbRegrowth> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, LimbRegrowth::part, (StreamCodec)ByteBufCodecs.FLOAT, LimbRegrowth::progress, LimbRegrowth::new);

    public LimbRegrowth {
        if (part < 0 || part >= BodyPart.values().length) {
            part = -1;
            progress = 0.0f;
        }
        progress = Float.isFinite(progress) ? Math.clamp((float)progress, (float)0.0f, (float)1.0f) : 0.0f;
    }

    public BodyPart growing() {
        return this.part < 0 ? null : BodyPart.values()[this.part];
    }

    public static LimbRegrowth get(Player player) {
        return (LimbRegrowth)player.getData(LimbAttachments.REGROWTH);
    }

    public static boolean growing(Player player, BodyPart limb) {
        BodyPart growing = LimbRegrowth.get(player).growing();
        return growing != null && (growing == limb || growing.parent() == limb);
    }

    public static float length(Player player, BodyPart limb) {
        LimbRegrowth growth = LimbRegrowth.get(player);
        BodyPart growing = growth.growing();
        if (!LimbRegrowth.growing(player, limb)) {
            return 1.0f;
        }
        float from = growing == limb ? 0.15f : 0.75f;
        return from + (1.0f - from) * growth.progress;
    }

    static BodyPart next(Player player, LimbState body) {
        if (body.isMissing(BodyPart.BODY)) {
            return body.isMissing(BodyPart.HEAD) ? null : BodyPart.BODY;
        }
        boolean main = player.getMainArm() == HumanoidArm.LEFT;
        List<BodyPart> order = List.of(main ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM, main ? BodyPart.RIGHT_ARM : BodyPart.LEFT_ARM, BodyPart.LEFT_LEG, BodyPart.RIGHT_LEG, main ? BodyPart.LEFT_HAND : BodyPart.RIGHT_HAND, main ? BodyPart.RIGHT_HAND : BodyPart.LEFT_HAND, BodyPart.LEFT_FOOT, BodyPart.RIGHT_FOOT);
        for (BodyPart part : order) {
            if (!LimbRegrowth.regrows(player, body, part)) continue;
            return part;
        }
        return null;
    }

    static boolean regrows(Player player, LimbState body, BodyPart part) {
        if (part.isVital() || !body.isMissing(part) || body.isMissing(BodyPart.HEAD)) {
            return false;
        }
        if (part == BodyPart.BODY) {
            return true;
        }
        if (body.isMissing(BodyPart.BODY)) {
            return false;
        }
        if (part.parent() != null && body.isMissing(part.parent())) {
            return false;
        }
        HumanoidArm arm = part == BodyPart.LEFT_ARM || part == BodyPart.LEFT_HAND ? HumanoidArm.LEFT : (part == BodyPart.RIGHT_ARM || part == BodyPart.RIGHT_HAND ? HumanoidArm.RIGHT : null);
        return arm == null || !ArmImplants.get(player, arm).replacesLimb();
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        float progress;
        MobEffectInstance regeneration;
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        LimbState body = LimbAttachments.get(player);
        LimbRegrowth growth = LimbRegrowth.get(player);
        BodyPart part = growth.growing();
        if (part != null && !LimbRegrowth.regrows(player, body, part)) {
            player.setData(LimbAttachments.REGROWTH, (Object)NONE);
            part = null;
            growth = NONE;
        }
        if ((regeneration = player.getEffect(MobEffects.REGENERATION)) == null || !player.isAlive()) {
            return;
        }
        if (part == null) {
            part = LimbRegrowth.next(player, body);
            if (part == null) {
                return;
            }
            growth = new LimbRegrowth(part.ordinal(), 0.0f);
        }
        if ((progress = growth.progress + (float)(regeneration.getAmplifier() + 1) / 200.0f) < 1.0f) {
            player.setData(LimbAttachments.REGROWTH, (Object)new LimbRegrowth(part.ordinal(), progress));
            player.refreshDimensions();
            return;
        }
        player.setData(LimbAttachments.REGROWTH, (Object)NONE);
        LimbAttachments.set(player, part == BodyPart.BODY ? body.restoreTorso() : body.restore(part));
        player.level().playSound(null, player.getX(), player.getY() + 1.0, player.getZ(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.PLAYERS, 0.6f, 1.4f);
    }
}

