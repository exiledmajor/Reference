/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.PacketDistributor
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.cyberware.FortifiedJumpState;
import com.chromeware.cyberware.GorillaBleeding;
import com.chromeware.cyberware.Implant;
import com.chromeware.cyberware.ImplantPackets;
import com.chromeware.netrunning.Netrunning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LocalPlayer.class})
public abstract class JumpMixin {
    @Unique
    private boolean chrome$jumpHeld;
    @Unique
    private final FortifiedJumpState chrome$ankles = new FortifiedJumpState();
    @Unique
    private boolean chrome$doubleUsed = true;

    @Inject(method={"aiStep"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/Input;tick(ZF)V", shift=At.Shift.AFTER)})
    private void chrome$implantJump(CallbackInfo ci) {
        boolean usable;
        LocalPlayer p = (LocalPlayer)this;
        Minecraft mc = Minecraft.getInstance();
        boolean down = mc.screen == null && mc.options.keyJump.isDown();
        boolean bl = usable = p.isAlive() && !GorillaBleeding.active((LivingEntity)p) && !p.isPassenger() && !p.getAbilities().flying && !p.isInWaterOrBubble() && (Netrunning.flags((Entity)p) & 1) == 0 && !p.isInLava() && !p.onClimbable() && !p.isFallFlying();
        if (p.onGround()) {
            this.chrome$doubleUsed = false;
        }
        if (Implant.FORTIFIED_ANKLES.active((Player)p) && usable && mc.screen == null) {
            if (down && !this.chrome$jumpHeld) {
                this.chrome$ankles.press(p.onGround());
                JumpMixin.send(1);
            }
            this.chrome$ankles.tick(p.onGround());
            if (this.chrome$ankles.charging()) {
                p.input.jumping = false;
            }
            if (!down && this.chrome$jumpHeld) {
                double velocity = this.chrome$ankles.release(p.onGround());
                if (velocity > 0.0) {
                    JumpMixin.send(2);
                    p.setDeltaMovement(p.getDeltaMovement().x, velocity, p.getDeltaMovement().z);
                    p.setOnGround(false);
                    p.fallDistance = 0.0f;
                } else {
                    JumpMixin.send(3);
                }
            }
        } else {
            if (this.chrome$ankles.held()) {
                this.chrome$ankles.cancel();
                JumpMixin.send(3);
            }
            if (down && !this.chrome$jumpHeld && usable && !p.onGround() && !this.chrome$doubleUsed && Implant.REINFORCED_TENDONS.active((Player)p)) {
                this.chrome$doubleUsed = true;
                JumpMixin.send(0);
                p.setDeltaMovement(p.getDeltaMovement().x, 0.42, p.getDeltaMovement().z);
                p.fallDistance = 0.0f;
            } else if (this.chrome$jumpHeld && !down) {
                JumpMixin.send(3);
            }
        }
        this.chrome$jumpHeld = down;
    }

    @Unique
    private static void send(int action) {
        PacketDistributor.sendToServer((CustomPacketPayload)new ImplantPackets.JumpInput(action), (CustomPacketPayload[])new CustomPacketPayload[0]);
    }
}

