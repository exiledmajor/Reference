/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity$BodyPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat;

import com.chromeware.body.BodyPart;
import com.chromeware.body.CyberLimbs;
import com.chromeware.body.LimbAttachments;
import com.chromeware.body.LimbState;
import com.mojang.authlib.GameProfile;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity"}, remap=false)
public abstract class RagdollPartMixin {
    @Inject(method={"configure(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lnet/minecraft/world/entity/player/Player;)V"}, at={@At(value="TAIL")})
    private void chrome$player(RagdollPartBlockEntity.BodyPart part, Player player, CallbackInfo ci) {
        this.chrome$snapshot(part, player);
    }

    @Inject(method={"configure(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lcom/mojang/authlib/GameProfile;)V"}, at={@At(value="TAIL")})
    private void chrome$profile(RagdollPartBlockEntity.BodyPart part, GameProfile profile, CallbackInfo ci) {
        Player player;
        BlockEntity block = (BlockEntity)this;
        if (block.getLevel() != null && (player = block.getLevel().getPlayerByUUID(profile.getId())) != null) {
            this.chrome$snapshot(part, player);
        }
    }

    private void chrome$snapshot(RagdollPartBlockEntity.BodyPart part, Player player) {
        BlockEntity block = (BlockEntity)this;
        LimbState state = LimbAttachments.functional(player);
        BodyPart limb = part == RagdollPartBlockEntity.BodyPart.TORSO ? BodyPart.BODY : BodyPart.valueOf(part.name());
        CompoundTag tag = block.getPersistentData();
        tag.putInt("ChromeMissing", state.missingMask());
        tag.putBoolean("ChromeFrame", CyberLimbs.chrome(player, limb));
        block.setChanged();
    }
}

