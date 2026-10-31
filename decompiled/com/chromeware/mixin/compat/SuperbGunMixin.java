/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.atsuishio.superbwarfare.data.gun.GunData
 *  com.atsuishio.superbwarfare.data.gun.ShootParameters
 *  com.atsuishio.superbwarfare.world.phys.EntityResult
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.compat;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.ShootParameters;
import com.atsuishio.superbwarfare.world.phys.EntityResult;
import com.chromeware.compat.guns.GunCyberware;
import com.chromeware.compat.guns.SuperbIntegration;
import com.chromeware.cyberware.ZetatechBerserk;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"com.atsuishio.superbwarfare.item.gun.GunItem"}, remap=false)
public abstract class SuperbGunMixin {
    @Inject(method={"onRayHitEntity"}, at={@At(value="TAIL")})
    private void chrome$rayEntity(Entity shooter, ServerLevel level, GunData data, EntityResult hit, Vec3 position, Vec3 direction, CallbackInfo ci) {
        GunCyberware.impactCurrent(hit.getHitPos());
    }

    @Inject(method={"onRayHitBlock"}, at={@At(value="TAIL")})
    private void chrome$rayBlock(Entity shooter, ServerLevel level, Entity target, GunData data, Vec3 direction, BlockHitResult hit, Vec3 position, CallbackInfo ci) {
        GunCyberware.impactCurrent(position.add((double)hit.getDirection().getStepX() * 0.02, (double)hit.getDirection().getStepY() * 0.02, (double)hit.getDirection().getStepZ() * 0.02));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"shoot(Lcom/atsuishio/superbwarfare/data/gun/ShootParameters;)V"})
    private void chrome$round(ShootParameters args, Operation<Void> original) {
        Player p;
        Entity entity = args.shooter;
        if (entity instanceof Player && !ZetatechBerserk.rangedAllowed(p = (Player)entity)) {
            return;
        }
        GunCyberware.Shot previous = GunCyberware.current();
        try {
            Entity entity2 = args.shooter;
            if (entity2 instanceof Player) {
                Player p2 = (Player)entity2;
                if (args.data.canShoot(args.ammoSupplier)) {
                    boolean chargedTech = SuperbIntegration.chargedTech(args.data);
                    GunCyberware.current(GunCyberware.fired(p2, args.data.stack(), chargedTech));
                    args = new ShootParameters(args.ammoSupplier, args.shooter, args.level, args.shootPosition, args.shootDirection, args.data, args.spread * GunCyberware.spread(p2), args.zoom, args.targetEntityUUID, args.targetPos);
                }
            }
            original.call(new Object[]{args});
        }
        finally {
            GunCyberware.current(previous);
        }
    }
}

