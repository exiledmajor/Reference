/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponentType
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 *  net.neoforged.neoforge.network.PacketDistributor
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.chromeware.humanity;

import com.chromeware.cyberware.CyberwareItems;
import com.chromeware.humanity.Humanity;
import com.chromeware.humanity.HumanityPackets;
import com.chromeware.humanity.ImmunosuppressantItem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid="chromeware")
public final class SyringeInjection {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create((ResourceKey)Registries.DATA_COMPONENT_TYPE, (String)"chromeware");
    public static final Supplier<DataComponentType<Boolean>> ACTIVE = COMPONENTS.register("active_injection", () -> DataComponentType.builder().networkSynchronized(ByteBufCodecs.BOOL).build());
    static final int SELF_APPLY_TICK = 80;
    static final int SELF_END_TICK = 100;
    static final int OTHER_APPLY_TICK = 80;
    static final int OTHER_END_TICK = 100;
    private static final double MAX_TARGET_GAP = 1.0;
    static final float INJECTION_PRICK_DAMAGE = 0.01f;
    private static final int COMBAT_ACCELERATION_TICKS = 100;
    private static final Map<UUID, Session> SESSIONS = new HashMap<UUID, Session>();
    private static final Map<UUID, Long> RECENT_DAMAGE_UNTIL = new HashMap<UUID, Long>();
    private static final Set<UUID> INTERNAL_INJECTION_DAMAGE = new HashSet<UUID>();

    public static boolean beginSelf(ServerPlayer player, InteractionHand hand) {
        return SyringeInjection.begin(player, hand, null, false);
    }

    public static boolean beginOther(ServerPlayer player, LivingEntity target, InteractionHand hand) {
        if (SyringeInjection.isOtherInjectionBlockedByCombat(player, target)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.syringe.combat_blocked"), true);
            return false;
        }
        if (!SyringeInjection.isPointBlank(player, target)) {
            return false;
        }
        return SyringeInjection.begin(player, hand, target, true);
    }

    private static boolean begin(ServerPlayer player, InteractionHand hand, LivingEntity target, boolean other) {
        if (SESSIONS.containsKey(player.getUUID())) {
            return false;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ImmunosuppressantItem)) {
            return false;
        }
        SyringeInjection.clearActiveMarkers(player);
        stack.set(ACTIVE.get(), (Object)true);
        player.getInventory().setChanged();
        double actorX = player.getX();
        double actorZ = player.getZ();
        if (target != null) {
            Vec3 away = new Vec3(player.getX() - target.getX(), 0.0, player.getZ() - target.getZ());
            if (away.lengthSqr() < 1.0E-4) {
                away = Vec3.directionFromRotation((float)0.0f, (float)player.getYRot()).scale(-1.0);
            }
            away = away.normalize();
            double separation = (double)player.getBbWidth() * 0.5 + (double)target.getBbWidth() * 0.5 + 0.22;
            double candidateX = target.getX() + away.x * separation;
            double candidateZ = target.getZ() + away.z * separation;
            if (player.level().noCollision((Entity)player, player.getBoundingBox().move(candidateX - player.getX(), 0.0, candidateZ - player.getZ()))) {
                actorX = candidateX;
                actorZ = candidateZ;
            }
        }
        boolean accelerated = SyringeInjection.shouldAccelerate(player);
        SESSIONS.put(player.getUUID(), new Session(player, hand, stack.getItem(), target == null ? null : target.getUUID(), other, accelerated, actorX, actorZ, target == null ? 0.0 : target.getX(), target == null ? 0.0 : target.getZ()));
        PacketDistributor.sendToPlayersInDimension((ServerLevel)player.serverLevel(), (CustomPacketPayload)new HumanityPackets.SyringeStart(player.getId(), other, accelerated, target == null ? -1 : target.getId(), actorX, actorZ, target == null ? 0.0 : target.getX(), target == null ? 0.0 : target.getZ()), (CustomPacketPayload[])new CustomPacketPayload[0]);
        return true;
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        RECENT_DAMAGE_UNTIL.entrySet().removeIf(entry -> {
            ServerPlayer player = event.getServer().getPlayerList().getPlayer((UUID)entry.getKey());
            return player == null || player.serverLevel().getGameTime() >= (Long)entry.getValue();
        });
        Iterator<Map.Entry<UUID, Session>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            int endTick;
            LivingEntity target;
            Entity entity;
            Session session = iterator.next().getValue();
            ServerPlayer player = session.player;
            if (!player.isAlive() || player.isRemoved()) {
                SyringeInjection.clearActiveMarkers(player);
                SyringeInjection.stop(player);
                iterator.remove();
                continue;
            }
            ItemStack activeStack = player.getItemInHand(session.hand);
            if (!(session.applied || activeStack.getItem() == session.item && SyringeInjection.hasActiveVisual(activeStack))) {
                SyringeInjection.clearActiveMarkers(player);
                SyringeInjection.stop(player);
                iterator.remove();
                continue;
            }
            if (session.other && (!((entity = player.serverLevel().getEntity(session.targetUuid)) instanceof LivingEntity) || SyringeInjection.isOtherInjectionBlockedByCombat(player, target = (LivingEntity)entity))) {
                if (entity instanceof LivingEntity) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.chromeware.syringe.combat_blocked"), true);
                }
                SyringeInjection.clearActiveMarkers(player);
                SyringeInjection.stop(player);
                iterator.remove();
                continue;
            }
            if (session.other && !SyringeInjection.alignParticipants(player, session)) {
                SyringeInjection.clearActiveMarkers(player);
                SyringeInjection.stop(player);
                iterator.remove();
                continue;
            }
            session.ticks = session.ticks + (session.accelerated ? 2 : 1);
            int applyTick = session.other ? 80 : 80;
            int n = endTick = session.other ? 100 : 100;
            if (!session.applied && session.ticks >= applyTick) {
                session.applied = true;
                SyringeInjection.apply(player, session);
            }
            if (session.ticks < endTick) continue;
            SyringeInjection.clearActiveMarkers(player);
            SyringeInjection.stop(player);
            iterator.remove();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void apply(ServerPlayer player, Session session) {
        UUID ignoredPlayer;
        Item item;
        ItemStack stack = player.getItemInHand(session.hand);
        if (stack.isEmpty() || stack.getItem() != session.item || !((item = session.item) instanceof ImmunosuppressantItem)) {
            return;
        }
        ImmunosuppressantItem injectable = (ImmunosuppressantItem)item;
        ServerPlayer target = player;
        if (session.other) {
            LivingEntity living;
            Entity entity = player.serverLevel().getEntity(session.targetUuid);
            if (!(entity instanceof LivingEntity) || !SyringeInjection.isPointBlank(player, living = (LivingEntity)entity)) {
                return;
            }
            target = living;
        }
        UUID uUID = ignoredPlayer = target instanceof ServerPlayer ? target.getUUID() : null;
        if (ignoredPlayer != null) {
            INTERNAL_INJECTION_DAMAGE.add(ignoredPlayer);
        }
        ItemStack consumedStack = stack.copy();
        ItemStack emptySyringe = new ItemStack((ItemLike)CyberwareItems.EMPTY_SYRINGE.get());
        player.setItemInHand(session.hand, emptySyringe);
        boolean applied = false;
        try {
            applied = injectable.inject(player, (LivingEntity)target, consumedStack);
            if (!applied) {
                return;
            }
            target.invulnerableTime = 0;
            target.hurt(target.damageSources().generic(), 0.01f);
        }
        finally {
            if (!applied && player.isAlive() && player.getItemInHand(session.hand) == emptySyringe) {
                player.setItemInHand(session.hand, stack);
            }
            if (ignoredPlayer != null) {
                INTERNAL_INJECTION_DAMAGE.remove(ignoredPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)livingEntity;
            if ((double)event.getNewDamage() > 1.0E-6 && !INTERNAL_INJECTION_DAMAGE.contains(player.getUUID())) {
                RECENT_DAMAGE_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + 100L);
            }
        }
    }

    private static boolean shouldAccelerate(ServerPlayer player) {
        return SyringeInjection.isRecentlyDamaged(player) || Humanity.state((Player)player).suppression().returning();
    }

    private static boolean isOtherInjectionBlockedByCombat(ServerPlayer actor, LivingEntity target) {
        ServerPlayer targetPlayer;
        return SyringeInjection.isRecentlyDamaged(actor) || target instanceof ServerPlayer && SyringeInjection.isRecentlyDamaged(targetPlayer = (ServerPlayer)target);
    }

    public static boolean isRecentlyDamaged(ServerPlayer player) {
        Long combatUntil = RECENT_DAMAGE_UNTIL.get(player.getUUID());
        return combatUntil != null && player.serverLevel().getGameTime() < combatUntil;
    }

    private static boolean isPointBlank(ServerPlayer player, LivingEntity target) {
        return target.isAlive() && target.level() == player.level() && player.hasLineOfSight((Entity)target) && player.getBoundingBox().inflate(1.0).intersects(target.getBoundingBox());
    }

    public static boolean hasActiveVisual(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(ACTIVE.get()));
    }

    private static boolean alignParticipants(ServerPlayer player, Session session) {
        LivingEntity target;
        Entity entity = player.serverLevel().getEntity(session.targetUuid);
        if (!(entity instanceof LivingEntity) || !(target = (LivingEntity)entity).isAlive() || target.level() != player.level()) {
            return false;
        }
        SyringeInjection.freezeHorizontal((LivingEntity)player, session.actorX, session.actorZ);
        SyringeInjection.freezeHorizontal(target, session.targetX, session.targetZ);
        SyringeInjection.face((LivingEntity)player, target);
        SyringeInjection.face(target, (LivingEntity)player);
        return true;
    }

    private static void freezeHorizontal(LivingEntity entity, double x, double z) {
        entity.setPos(x, entity.getY(), z);
        entity.setDeltaMovement(0.0, Math.min(0.0, entity.getDeltaMovement().y), 0.0);
        entity.hurtMarked = true;
    }

    private static void face(LivingEntity actor, LivingEntity target) {
        Vec3 delta = target.getBoundingBox().getCenter().subtract(actor.getEyePosition());
        float yaw = (float)(Mth.atan2((double)delta.z, (double)delta.x) * 57.2957763671875) - 90.0f;
        actor.setYRot(yaw);
        actor.setYHeadRot(yaw);
        actor.yBodyRot = yaw;
    }

    private static void clearActiveMarkers(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); ++slot) {
            player.getInventory().getItem(slot).remove(ACTIVE.get());
        }
        player.getInventory().setChanged();
    }

    private static void stop(ServerPlayer player) {
        PacketDistributor.sendToPlayersInDimension((ServerLevel)player.serverLevel(), (CustomPacketPayload)new HumanityPackets.SyringeStop(player.getId()), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }

    private static final class Session {
        private final ServerPlayer player;
        private final InteractionHand hand;
        private final Item item;
        private final UUID targetUuid;
        private final boolean other;
        private final boolean accelerated;
        private final double actorX;
        private final double actorZ;
        private final double targetX;
        private final double targetZ;
        private int ticks;
        private boolean applied;

        private Session(ServerPlayer player, InteractionHand hand, Item item, UUID targetUuid, boolean other, boolean accelerated, double actorX, double actorZ, double targetX, double targetZ) {
            this.player = player;
            this.hand = hand;
            this.item = item;
            this.targetUuid = targetUuid;
            this.other = other;
            this.accelerated = accelerated;
            this.actorX = actorX;
            this.actorZ = actorZ;
            this.targetX = targetX;
            this.targetZ = targetZ;
        }
    }
}

