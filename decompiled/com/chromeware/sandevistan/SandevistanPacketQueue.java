/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ServerboundInteractPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.network.ServerGamePacketListenerImpl
 *  net.minecraft.world.entity.Entity
 */
package com.chromeware.sandevistan;

import com.chromeware.Chromeware;
import com.chromeware.sandevistan.Sandevistan;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;

public final class SandevistanPacketQueue {
    private static final int MAX_ACTIONS = 64;
    private static final Map<UUID, Pending> PENDING = new HashMap<UUID, Pending>();
    private static final ThreadLocal<Boolean> REPLAYING = ThreadLocal.withInitial(() -> false);

    private SandevistanPacketQueue() {
    }

    public static boolean isReplaying() {
        return REPLAYING.get();
    }

    public static void queueBlockAction(ServerGamePacketListenerImpl handler, ServerboundPlayerActionPacket packet) {
        SandevistanPacketQueue.queue(handler, packet);
    }

    public static void queueEntityAction(ServerGamePacketListenerImpl handler, ServerboundInteractPacket packet) {
        SandevistanPacketQueue.queue(handler, packet);
    }

    public static void queueBlockUse(ServerGamePacketListenerImpl handler, ServerboundUseItemOnPacket packet) {
        SandevistanPacketQueue.queue(handler, packet);
    }

    public static void queueItemUse(ServerGamePacketListenerImpl handler, ServerboundUseItemPacket packet) {
        SandevistanPacketQueue.queue(handler, packet);
    }

    public static void queueSwing(ServerGamePacketListenerImpl handler, ServerboundSwingPacket packet) {
        SandevistanPacketQueue.queue(handler, packet);
    }

    private static void queue(ServerGamePacketListenerImpl handler, Packet<?> packet) {
        Pending pending = PENDING.computeIfAbsent(handler.player.getUUID(), ignored -> new Pending(handler));
        if (pending.actions.size() >= 64) {
            pending.actions.removeFirst();
            Chromeware.LOGGER.debug("Discarded oldest queued action for slowed player {} after reaching {} actions.", (Object)handler.player.getUUID(), (Object)64);
        }
        pending.actions.addLast(packet);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Pending>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Pending> entry = iterator.next();
            Pending pending = entry.getValue();
            if (server.getPlayerList().getPlayer(entry.getKey()) != pending.handler.player || !pending.handler.player.isAlive()) {
                iterator.remove();
                continue;
            }
            if (!Sandevistan.shouldAdvance((Entity)pending.handler.player)) continue;
            if (pending.actions.isEmpty()) {
                iterator.remove();
                continue;
            }
            REPLAYING.set(true);
            try {
                while (!pending.actions.isEmpty()) {
                    SandevistanPacketQueue.dispatch(pending.handler, pending.actions.removeFirst());
                }
            }
            finally {
                REPLAYING.set(false);
            }
            iterator.remove();
        }
    }

    private static void dispatch(ServerGamePacketListenerImpl handler, Packet<?> packet) {
        if (packet instanceof ServerboundPlayerActionPacket) {
            ServerboundPlayerActionPacket action = (ServerboundPlayerActionPacket)packet;
            handler.handlePlayerAction(action);
        } else if (packet instanceof ServerboundInteractPacket) {
            ServerboundInteractPacket entity = (ServerboundInteractPacket)packet;
            handler.handleInteract(entity);
        } else if (packet instanceof ServerboundUseItemOnPacket) {
            ServerboundUseItemOnPacket block = (ServerboundUseItemOnPacket)packet;
            handler.handleUseItemOn(block);
        } else if (packet instanceof ServerboundUseItemPacket) {
            ServerboundUseItemPacket item = (ServerboundUseItemPacket)packet;
            handler.handleUseItem(item);
        } else if (packet instanceof ServerboundSwingPacket) {
            ServerboundSwingPacket swing = (ServerboundSwingPacket)packet;
            handler.handleAnimate(swing);
        }
    }

    public static void clearAll() {
        PENDING.clear();
    }

    public static void clear(UUID playerId) {
        PENDING.remove(playerId);
    }

    private static final class Pending {
        private final ServerGamePacketListenerImpl handler;
        private final ArrayDeque<Packet<?>> actions = new ArrayDeque();

        private Pending(ServerGamePacketListenerImpl handler) {
            this.handler = handler;
        }
    }
}

