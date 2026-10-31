/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin.client;

import com.chromeware.body.hitbox.BodyBox;
import com.chromeware.body.hitbox.BodyHitboxes;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererBodyMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method={"pick"}, at={@At(value="TAIL")})
    private void chrome$bodyPicking(float tick, CallbackInfo ci) {
        EntityHitResult hit;
        EntityHitResult hit2;
        double best;
        if (this.minecraft.level == null || this.minecraft.player == null || this.minecraft.getCameraEntity() != this.minecraft.player) {
            return;
        }
        double reach = this.minecraft.player.entityInteractionRange();
        Vec3 start = this.minecraft.player.getEyePosition(tick);
        Vec3 end = start.add(this.minecraft.player.getViewVector(tick).scale(reach));
        HitResult vanilla = this.minecraft.hitResult;
        HitResult block = this.minecraft.player.pick(reach, tick, false);
        double d = best = block.getType() == HitResult.Type.BLOCK ? start.distanceTo(block.getLocation()) : Double.POSITIVE_INFINITY;
        if (vanilla instanceof EntityHitResult && !((hit2 = (EntityHitResult)vanilla).getEntity() instanceof Player)) {
            best = Math.min(best, start.distanceTo(hit2.getLocation()));
        }
        EntityHitResult selected = null;
        for (Player target : this.minecraft.level.getEntitiesOfClass(Player.class, new AABB(start, end).inflate(1.25), candidate -> candidate != this.minecraft.player && !candidate.isSpectator() && candidate.isPickable())) {
            Optional<BodyBox.Hit> hit3 = BodyHitboxes.raycast(target, start, end, 0.0);
            if (!hit3.isPresent() || !(hit3.get().distance() <= best + 1.0E-5)) continue;
            best = hit3.get().distance();
            selected = new EntityHitResult((Entity)target, hit3.get().point());
        }
        if (selected != null) {
            this.minecraft.hitResult = selected;
            this.minecraft.crosshairPickEntity = selected.getEntity();
        } else if (vanilla instanceof EntityHitResult && (hit = (EntityHitResult)vanilla).getEntity() instanceof Player) {
            this.minecraft.hitResult = block;
            this.minecraft.crosshairPickEntity = null;
        }
    }
}

