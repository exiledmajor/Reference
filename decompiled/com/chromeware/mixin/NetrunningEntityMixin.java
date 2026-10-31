/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package com.chromeware.mixin;

import com.chromeware.netrunning.Netrunning;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={Entity.class})
public abstract class NetrunningEntityMixin {
    @ModifyVariable(method={"move"}, at=@At(value="HEAD"), argsOnly=true)
    private Vec3 chrome$movementLock(Vec3 movement) {
        int flags = Netrunning.flags((Entity)this);
        return (flags & 1) != 0 ? new Vec3(0.0, movement.y, 0.0) : movement;
    }
}

