/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.AreaEffectCloud
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ThrownPotion
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package com.chromeware.mixin;

import com.chromeware.cyberware.ImplantEffects;
import com.chromeware.cyberware.UniversalBooster;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ThrownPotion.class, AreaEffectCloud.class})
public abstract class UniversalBoosterPotionMixin {
    @Unique
    private final Set<Player> chrome$boosted = new HashSet<Player>();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"applySplash"}, require=0)
    private void chrome$splash(Iterable<MobEffectInstance> effects, Entity hit, Operation<Void> original) {
        this.chrome$boosted.clear();
        try {
            original.call(new Object[]{effects, hit});
        }
        finally {
            this.chrome$boosted.clear();
        }
    }

    @WrapMethod(method={"tick"}, require=0)
    private void chrome$cloudTick(Operation<Void> original) {
        this.chrome$boosted.clear();
        try {
            original.call(new Object[0]);
        }
        finally {
            this.chrome$boosted.clear();
        }
    }

    @Unique
    private void chrome$health(LivingEntity target, MobEffect effect) {
        Player p;
        if (target instanceof Player && !(p = (Player)target).level().isClientSide() && UniversalBooster.healing((Holder<MobEffect>)BuiltInRegistries.MOB_EFFECT.wrapAsHolder((Object)effect)) && this.chrome$boosted.add(p)) {
            ImplantEffects.healthItem(p);
        }
    }

    @WrapOperation(method={"applySplash", "tick"}, require=1, at={@At(value="INVOKE", target="Lnet/minecraft/world/effect/MobEffect;applyInstantenousEffect(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V")})
    private void chrome$instant(MobEffect effect, Entity source, Entity owner, LivingEntity target, int amplifier, double strength, Operation<Void> original) {
        original.call(new Object[]{effect, source, owner, target, amplifier, strength});
        this.chrome$health(target, effect);
    }

    @WrapOperation(method={"applySplash", "tick"}, require=1, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z")})
    private boolean chrome$regeneration(LivingEntity target, MobEffectInstance effect, Entity source, Operation<Boolean> original) {
        boolean added = (Boolean)original.call(new Object[]{target, effect, source});
        if (added) {
            this.chrome$health(target, (MobEffect)effect.getEffect().value());
        }
        return added;
    }
}

