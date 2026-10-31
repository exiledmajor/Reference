/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.tick.PlayerTickEvent$Post
 */
package com.chromeware.cyberware;

import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.PlayerData;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.monowire.MonowireCombat;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="chromeware")
public final class ArmRecharge {
    private static final Map<Player, int[]> SENT = new WeakHashMap<Player, int[]>();

    public static int remaining(Player player, HumanoidArm side) {
        return Math.max(GorillaCombat.cooldown(player, side), Math.max(MantisImplant.cooldown(player, side), Math.max(MonowireCombat.cooldown(player, side), ProjectileLauncher.cooldown(player, side))));
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        int[] sent = SENT.computeIfAbsent(player, p -> new int[4]);
        boolean changed = false;
        for (HumanoidArm side : HumanoidArm.values()) {
            int i = side == HumanoidArm.LEFT ? 2 : 0;
            int left = ArmRecharge.remaining(player, side);
            sent[i] = Math.max(0, sent[i] - 1);
            if (left > sent[i] + 1) {
                int n = left;
                sent[i + 1] = n;
                sent[i] = n;
                changed = true;
                continue;
            }
            if (left >= sent[i] - 1) continue;
            sent[i] = left;
            changed = true;
        }
        if (changed) {
            player.setData(PlayerData.ARM_RECHARGE, (Object)new State(sent[0], sent[1], sent[2], sent[3]));
        }
    }

    public record State(int right, int rightLength, int left, int leftLength) {
        public static final State NONE = new State(0, 0, 0, 0);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.VAR_INT, State::right, (StreamCodec)ByteBufCodecs.VAR_INT, State::rightLength, (StreamCodec)ByteBufCodecs.VAR_INT, State::left, (StreamCodec)ByteBufCodecs.VAR_INT, State::leftLength, State::new);

        public int remaining(HumanoidArm side) {
            return side == HumanoidArm.LEFT ? this.left : this.right;
        }

        public int length(HumanoidArm side) {
            return side == HumanoidArm.LEFT ? this.leftLength : this.rightLength;
        }

        public float progress(HumanoidArm side, float elapsed) {
            int length = this.length(side);
            return length <= 0 ? 1.0f : Math.clamp((float)(1.0f - ((float)this.remaining(side) - elapsed) / (float)length), (float)0.0f, (float)1.0f);
        }
    }
}

