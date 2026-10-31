/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.game.ServerboundInteractPacket
 *  net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
 *  net.minecraft.network.protocol.game.ServerboundPlayerActionPacket
 *  net.minecraft.network.protocol.game.ServerboundSwingPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemOnPacket
 *  net.minecraft.network.protocol.game.ServerboundUseItemPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.server.network.ServerGamePacketListenerImpl
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.sandevistan.Sandevistan;
import com.chromeware.sandevistan.SandevistanMovementAccess;
import com.chromeware.sandevistan.SandevistanPacketQueue;
import java.util.Set;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerGamePacketListenerImpl.class})
public abstract class SandevistanConnectionMixin
implements SandevistanMovementAccess {
    @Shadow
    public ServerPlayer player;
    @Unique
    private ServerboundMovePlayerPacket chrome$pending;
    @Unique
    private ResourceKey<Level> chrome$dimension;
    @Unique
    private boolean chrome$replaying;
    @Unique
    private static final String CHROME_THREAD = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V";

    @Inject(method={"handleMovePlayer"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void chrome$movement(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (this.chrome$replaying) {
            return;
        }
        if (!Sandevistan.shouldAdvance((Entity)this.player)) {
            this.chrome$pending = packet;
            this.chrome$dimension = this.player.level().dimension();
            if (packet.hasRotation()) {
                this.player.setYRot(packet.getYRot(this.player.getYRot()));
                this.player.setYHeadRot(this.player.getYRot());
                this.player.setXRot(packet.getXRot(this.player.getXRot()));
            }
            ci.cancel();
        } else {
            this.chrome$flushMovement();
        }
    }

    @Override
    public void chrome$flushMovement() {
        if (this.chrome$replaying || this.chrome$pending == null || !Sandevistan.shouldAdvance((Entity)this.player)) {
            return;
        }
        ServerboundMovePlayerPacket packet = this.chrome$pending;
        this.chrome$pending = null;
        if (!this.player.isAlive() || !this.player.level().dimension().equals(this.chrome$dimension)) {
            return;
        }
        this.chrome$replaying = true;
        try {
            ((ServerGamePacketListenerImpl)this).handleMovePlayer(packet);
        }
        finally {
            this.chrome$replaying = false;
        }
    }

    @Inject(method={"teleport(DDDFFLjava/util/Set;)V"}, at={@At(value="HEAD")})
    private void chrome$clearTeleport(double x, double y, double z, float yaw, float pitch, Set<?> flags, CallbackInfo ci) {
        this.chrome$pending = null;
    }

    @Unique
    private boolean chrome$queue() {
        return !SandevistanPacketQueue.isReplaying() && !Sandevistan.shouldAdvance((Entity)this.player);
    }

    @Inject(method={"handlePlayerAction"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void chrome$action(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (this.chrome$queue()) {
            SandevistanPacketQueue.queueBlockAction((ServerGamePacketListenerImpl)this, packet);
            ci.cancel();
        }
    }

    @Inject(method={"handleInteract"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void chrome$interact(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.chrome$queue()) {
            SandevistanPacketQueue.queueEntityAction((ServerGamePacketListenerImpl)this, packet);
            ci.cancel();
        }
    }

    @Inject(method={"handleUseItemOn"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void chrome$block(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (this.chrome$queue()) {
            SandevistanPacketQueue.queueBlockUse((ServerGamePacketListenerImpl)this, packet);
            ci.cancel();
        }
    }

    @Inject(method={"handleUseItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void chrome$item(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (this.chrome$queue()) {
            SandevistanPacketQueue.queueItemUse((ServerGamePacketListenerImpl)this, packet);
            ci.cancel();
        }
    }

    @Inject(method={"handleAnimate"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void chrome$swing(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (this.chrome$queue()) {
            SandevistanPacketQueue.queueSwing((ServerGamePacketListenerImpl)this, packet);
            ci.cancel();
        }
    }
}

