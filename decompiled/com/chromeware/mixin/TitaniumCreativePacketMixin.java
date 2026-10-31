/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.server.network.ServerGamePacketListenerImpl
 *  net.minecraft.world.entity.player.Player
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.TitaniumBones;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ServerGamePacketListenerImpl.class})
public abstract class TitaniumCreativePacketMixin {
    @Shadow
    public ServerPlayer player;

    @ModifyExpressionValue(method={"handleSetCreativeModeSlot"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;getMaxStackSize()I")})
    private int chrome$personalCreativeLimit(int original, ServerboundSetCreativeModeSlotPacket packet) {
        short slot = packet.slotNum();
        return slot >= 9 && slot <= 45 || slot < 0 ? TitaniumBones.limit((Player)this.player, packet.itemStack()) : original;
    }
}

