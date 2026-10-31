/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package com.chromeware.mixin;

import com.chromeware.body.BodyDamage;
import com.chromeware.body.hitbox.BodyBox;
import com.chromeware.body.hitbox.BodyHitboxes;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ProjectileUtil.class})
public abstract class ProjectileUtilBodyMixin {
    @Inject(method={"getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;"}, at={@At(value="RETURN")}, cancellable=true)
    private static void chrome$partCollision(Level level, Entity projectile, Vec3 start, Vec3 end, AABB search, Predicate<Entity> filter, float padding, CallbackInfoReturnable<EntityHitResult> cir) {
        Player bestPlayer = null;
        BodyBox.Hit best = null;
        for (Player player : level.getEntitiesOfClass(Player.class, search.inflate(1.25), candidate -> candidate != projectile && candidate.isAlive() && filter.test((Entity)candidate))) {
            Optional<BodyBox.Hit> hit = BodyHitboxes.raycast(player, start, end, 0.0);
            if (!hit.isPresent() || best != null && !(hit.get().distance() < best.distance())) continue;
            best = hit.get();
            bestPlayer = player;
        }
        EntityHitResult vanilla = (EntityHitResult)cir.getReturnValue();
        if (vanilla != null && !(vanilla.getEntity() instanceof Player) && (best == null || start.distanceTo(vanilla.getLocation()) <= best.distance())) {
            return;
        }
        if (best != null) {
            BodyDamage.remember(bestPlayer, projectile, best);
            cir.setReturnValue((Object)new EntityHitResult(bestPlayer, best.point()));
        } else if (vanilla != null && vanilla.getEntity() instanceof Player) {
            cir.setReturnValue(null);
        }
    }
}

