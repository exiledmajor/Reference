/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.GorillaBleeding;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LivingEntity.class})
public abstract class GorillaBleedingMixin {
    @Shadow
    protected float lastHurt;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"hurt"})
    private boolean chrome$bleed(DamageSource source, float amount, Operation<Boolean> original) {
        if (!source.is(GorillaBleeding.DAMAGE_TYPE)) {
            return (Boolean)original.call(new Object[]{source, Float.valueOf(amount)});
        }
        LivingEntity e = (LivingEntity)this;
        int invulnerable = e.invulnerableTime;
        int hurt = e.hurtTime;
        int duration = e.hurtDuration;
        float last = this.lastHurt;
        float health = e.getHealth();
        try {
            boolean bl = (Boolean)original.call(new Object[]{source, Float.valueOf(amount)});
            return bl;
        }
        finally {
            if (e.getHealth() <= health) {
                e.invulnerableTime = invulnerable;
                e.hurtTime = hurt;
                e.hurtDuration = duration;
                this.lastHurt = last;
            }
        }
    }

    @ModifyVariable(method={"setSprinting"}, at=@At(value="HEAD"), argsOnly=true)
    private boolean chrome$noBleedingSprint(boolean sprinting) {
        return sprinting && !GorillaBleeding.active((LivingEntity)this);
    }

    @ModifyVariable(method={"setJumping"}, at=@At(value="HEAD"), argsOnly=true)
    private boolean chrome$noBleedingAiJump(boolean jumping) {
        return jumping && !GorillaBleeding.active((LivingEntity)this);
    }

    @Inject(method={"jumpFromGround"}, at={@At(value="HEAD")}, cancellable=true)
    private void chrome$noBleedingJump(CallbackInfo ci) {
        if (GorillaBleeding.active((LivingEntity)this)) {
            ci.cancel();
        }
    }
}

