/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.zigythebird.playeranim.animation.PlayerAnimManager
 *  com.zigythebird.playeranim.api.PlayerAnimationAccess
 *  it.unimi.dsi.fastutil.Pair
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.RemotePlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 */
package com.chromeware.client.sandevistan;

import com.chromeware.client.sandevistan.AfterimageSnapshot;
import com.chromeware.client.sandevistan.FrozenPlayerAnimation;
import com.chromeware.mixin.client.LimbAnimatorAccessor;
import com.chromeware.mixin.client.LivingEntityAfterimageAccessor;
import com.chromeware.mixin.client.SandevistanLivingAccessor;
import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import it.unimi.dsi.fastutil.Pair;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class AfterimagePlayerEntity
extends RemotePlayer {
    List<FrozenPlayerAnimation.Layer> animations = List.of();
    public float mantisHeadFollow;
    public final Map<HumanoidArm, Vec3> wireSockets = new EnumMap<HumanoidArm, Vec3>(HumanoidArm.class);
    public final Map<HumanoidArm, Vec3> wireGrips = new EnumMap<HumanoidArm, Vec3>(HumanoidArm.class);
    public Vec3 captureOrigin;

    AfterimagePlayerEntity(AbstractClientPlayer source) {
        this(source, source.getGameProfile());
    }

    AfterimagePlayerEntity(AbstractClientPlayer source, GameProfile profile) {
        this((ClientLevel)source.level(), profile, source.getId());
    }

    AfterimagePlayerEntity(ClientLevel world, GameProfile profile, int entityId) {
        super(world, profile);
        this.setId(entityId);
    }

    void apply(AfterimageSnapshot snapshot) {
        this.captureOrigin = null;
        snapshot.hardware().apply((AbstractClientPlayer)this);
        this.animations = snapshot.animations();
        PlayerAnimManager manager = PlayerAnimationAccess.getPlayerAnimManager((AbstractClientPlayer)this);
        manager.getLayers().clear();
        for (FrozenPlayerAnimation.Layer layer : this.animations) {
            manager.getLayers().add(Pair.of((Object)layer.priority(), (Object)layer));
        }
        this.mantisHeadFollow = snapshot.mantisHeadFollow();
        this.wireSockets.clear();
        this.wireGrips.clear();
        this.setPos(snapshot.position());
        this.xo = snapshot.position().x;
        this.yo = snapshot.position().y;
        this.zo = snapshot.position().z;
        this.setDeltaMovement(snapshot.velocity());
        this.setYRot(snapshot.yaw());
        this.yRotO = snapshot.yaw();
        this.setXRot(snapshot.pitch());
        this.xRotO = snapshot.pitch();
        this.yBodyRot = snapshot.yBodyRot();
        this.yBodyRotO = snapshot.yBodyRot();
        this.yHeadRot = snapshot.yHeadRot();
        this.yHeadRotO = snapshot.yHeadRot();
        this.setPose(snapshot.pose());
        this.setShiftKeyDown(snapshot.sneaking());
        this.setSprinting(snapshot.sprinting());
        this.setSwimming(snapshot.swimming());
        ((SandevistanLivingAccessor)((Object)this)).chrome$setFlag(7, snapshot.fallFlying());
        this.setOnGround(snapshot.onGround());
        this.clearFire();
        this.setInvisible(false);
        this.tickCount = Mth.floor((float)snapshot.animationAge());
        this.swinging = snapshot.swinging();
        this.swingingArm = snapshot.swingingArm();
        this.attackAnim = snapshot.attackAnim();
        this.oAttackAnim = snapshot.attackAnim();
        LimbAnimatorAccessor limb = (LimbAnimatorAccessor)this.walkAnimation;
        limb.chrome$setPreviousSpeed(snapshot.limbSpeed());
        limb.chrome$setSpeed(snapshot.limbSpeed());
        limb.chrome$setPos(snapshot.limbPosition());
        LivingEntityAfterimageAccessor living = (LivingEntityAfterimageAccessor)((Object)this);
        living.chrome$setPreviousLeaningPitch(snapshot.swimAmount());
        living.chrome$setLeaningPitch(snapshot.swimAmount());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = snapshot.equipment().getOrDefault(slot, ItemStack.EMPTY);
            this.setItemSlot(slot, stack.copy());
        }
        this.stopUsingItem();
        if (snapshot.usingItem()) {
            this.startUsingItem(snapshot.activeHand());
            living.chrome$setItemUseTimeLeft(snapshot.useItemRemaining());
        }
        this.setMainArm(snapshot.mainArm());
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (Object)snapshot.visibleModelParts());
        this.xCloakO = snapshot.xCloakO();
        this.yCloakO = snapshot.yCloakO();
        this.zCloakO = snapshot.zCloakO();
        this.xCloak = snapshot.xCloak();
        this.yCloak = snapshot.yCloak();
        this.zCloak = snapshot.zCloak();
        this.elytraRotX = snapshot.elytraRotX();
        this.elytraRotY = snapshot.elytraRotY();
        this.elytraRotZ = snapshot.elytraRotZ();
    }

    void setShifterModelParts(byte modelParts) {
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (Object)modelParts);
    }

    float renderTickDelta(AfterimageSnapshot snapshot) {
        return snapshot.animationAge() - (float)Mth.floor((float)snapshot.animationAge());
    }

    public boolean shouldRenderName() {
        return false;
    }
}

