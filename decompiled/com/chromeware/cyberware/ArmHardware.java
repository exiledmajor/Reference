/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.player.AttackEntityEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.neoforged.neoforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 */
package com.chromeware.cyberware;

import com.chromeware.body.BodyPart;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.chromeware.cyberware.ArmImplants;
import com.chromeware.cyberware.GorillaCombat;
import com.chromeware.cyberware.Installing;
import com.chromeware.launcher.ProjectileLauncher;
import com.chromeware.mantis.MantisImplant;
import com.chromeware.mantis.MantisState;
import com.chromeware.monowire.MonowireCombat;
import com.chromeware.monowire.MonowireImplant;
import com.chromeware.monowire.MonowireState;
import com.chromeware.netrunning.Netrunning;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid="chromeware")
public final class ArmHardware {
    public static boolean installHeld(Player player, Predicate<ItemStack> item, boolean needsArm) {
        if (!Installing.canOperate(player)) {
            return false;
        }
        InteractionHand hand = item.test(player.getMainHandItem()) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack held = player.getItemInHand(hand);
        if (!item.test(held)) {
            return false;
        }
        HumanoidArm side = ArmImplants.installSide(player, held, hand);
        LimbState body = LimbAttachments.get(player);
        if (needsArm && (body.isMissing(ArmImplants.part(side)) || body.isMissing(ArmImplants.handPart(side)))) {
            return false;
        }
        if (!ArmImplants.canPlace(player, held, side)) {
            return false;
        }
        ArmImplants.place(player, side, held);
        return true;
    }

    public static boolean fitted(Player player, HumanoidArm side, ItemLike item) {
        if (!ArmImplants.has(player, side, s -> s.is(item.asItem())) || !player.isAlive() || player.isSpectator()) {
            return false;
        }
        LimbState body = LimbAttachments.get(player);
        return !body.isMissing(BodyPart.BODY) && !body.isMissing(ArmImplants.part(side)) && !body.isMissing(ArmImplants.handPart(side));
    }

    public static boolean free(Player player, HumanoidArm side) {
        return (Netrunning.flags((Entity)player) & 4) == 0 && (!player.isUsingItem() || player.getUsedItemHand() != ArmImplants.hand(player, side)) && !player.isSwimming() && !player.isFallFlying() && !player.isPassenger();
    }

    public static int blocking(Player player, HumanoidArm side) {
        return Math.max(Math.max(GorillaCombat.blocking(player, side), ProjectileLauncher.blockingTicks(player, side)), Math.max(MantisImplant.blockingTicks(player, side), MonowireCombat.blockingTicks(player, side)));
    }

    public static void interrupt(Player player, HumanoidArm side) {
        GorillaCombat.interrupt(player, side);
        MantisImplant.interrupt(player, side);
        MonowireCombat.interrupt(player, side);
        ProjectileLauncher.interrupt(player, side);
    }

    public static boolean striking(Player player, HumanoidArm side) {
        MantisState mantis = MantisImplant.state(player, side);
        MonowireState wire = MonowireImplant.state(player, side);
        return GorillaCombat.blocking(player, side) > 0 || mantis.deployed() && MantisImplant.installed(player, side) && mantis.action() == 1 || wire.deployed() && MonowireImplant.installed(player, side) && wire.action() == 1;
    }

    public static int strike(Player player, HumanoidArm side) {
        return (MantisImplant.state(player, side).sequence() * 31 + MonowireImplant.state(player, side).stroke()) * 31 + (Integer)player.getData(ProjectileLauncher.shotKey(side)) + ProjectileLauncher.volleys(player, side) * 17 + GorillaCombat.state(player).sequence(side) * 29791;
    }

    private static boolean meleeOut(Player player) {
        HumanoidArm side = player.getMainArm();
        return MantisImplant.deployed(player, side) || MonowireImplant.deployed(player, side) || GorillaCombat.deployed(player, side);
    }

    @SubscribeEvent
    public static void attackVanilla(AttackEntityEvent event) {
        if (ArmHardware.meleeOut(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void mineVanilla(PlayerInteractEvent.LeftClickBlock event) {
        if (ArmHardware.meleeOut(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        ArmImplants.retractAll(event.getEntity());
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ArmImplants.retractAll(event.getEntity());
    }

    public static final class Cooldowns {
        private final Map<Player, int[]> cooldowns = new WeakHashMap<Player, int[]>();

        public int get(Player player, HumanoidArm side) {
            int[] c = this.cooldowns.get(player);
            return c == null ? 0 : c[side.ordinal()];
        }

        public int max(Player player) {
            return Math.max(this.get(player, HumanoidArm.LEFT), this.get(player, HumanoidArm.RIGHT));
        }

        public void set(Player player, HumanoidArm side, int ticks) {
            int[] c = this.cooldowns.computeIfAbsent(player, p -> new int[2]);
            c[side.ordinal()] = Math.max(0, ticks);
            if (c[0] == 0 && c[1] == 0) {
                this.cooldowns.remove(player);
            }
        }

        public void reduce(Player player, int ticks) {
            if (player.level().isClientSide()) {
                return;
            }
            for (HumanoidArm side : HumanoidArm.values()) {
                this.set(player, side, this.get(player, side) - Math.max(0, ticks));
            }
        }

        public void tick(Player player) {
            int[] c = this.cooldowns.get(player);
            if (c == null) {
                return;
            }
            for (int i = 0; i < 2; ++i) {
                if (c[i] <= 0) continue;
                int n = i;
                c[n] = c[n] - 1;
            }
            if (c[0] == 0 && c[1] == 0) {
                this.cooldowns.remove(player);
            }
        }
    }

    public static final class Sessions<S extends Session> {
        private final Map<Player, Session[]> sessions = new WeakHashMap<Player, Session[]>();

        public S get(Player player, HumanoidArm side) {
            Session[] s = this.sessions.get(player);
            return (S)(s == null ? null : s[side.ordinal()]);
        }

        public void set(Player player, HumanoidArm side, S session) {
            Session[] s = this.sessions.computeIfAbsent(player, p -> new Session[2]);
            s[side.ordinal()] = session;
            if (s[0] == null && s[1] == null) {
                this.sessions.remove(player);
            }
        }

        public boolean charging(Player player) {
            S s = this.get(player, HumanoidArm.RIGHT);
            return s != null && ((Session)s).charge >= 0;
        }

        public void clearBuffers(Player player) {
            Session[] s = this.sessions.get(player);
            if (s != null) {
                for (Session session : s) {
                    if (session == null) continue;
                    session.buffer = 0;
                }
            }
        }

        public void queue(Player player, S session, int bufferTicks) {
            this.clearBuffers(player);
            ((Session)session).buffer = bufferTicks + 1;
        }

        public void tickBuffers(Player player) {
            Session[] s = this.sessions.get(player);
            if (s != null) {
                for (Session session : s) {
                    if (session == null || session.buffer <= 0) continue;
                    --session.buffer;
                }
            }
        }
    }

    public static class Session {
        public final Level level;
        public int age;
        public int remaining;
        public int buffer;
        public int charge = -1;

        public Session(Player player) {
            this.level = player.level();
        }
    }
}

