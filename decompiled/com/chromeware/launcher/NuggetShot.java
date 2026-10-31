/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.simibubi.create.AllItems
 *  net.minecraft.core.particles.ItemParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 */
package com.chromeware.launcher;

import com.chromeware.config.ImplantSettings;
import com.chromeware.launcher.LauncherEntities;
import com.simibubi.create.AllItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class NuggetShot
extends ThrowableItemProjectile {
    public static final int EXPERIENCE = 3;

    public NuggetShot(EntityType<? extends NuggetShot> type, Level level) {
        super(type, level);
    }

    public NuggetShot(Level level, LivingEntity owner, ItemStack nugget) {
        super((EntityType)LauncherEntities.NUGGET.get(), owner, level);
        this.setItem(nugget.copyWithCount(1));
    }

    public static boolean nugget(ItemStack stack) {
        return NuggetShot.damage(stack) > 0.0f;
    }

    public static float damage(ItemStack stack) {
        if (stack.is(Items.IRON_NUGGET)) {
            return ImplantSettings.fstat("projectile_launch_system", "iron_nugget_damage", 10.0);
        }
        if (stack.is(Items.GOLD_NUGGET)) {
            return ImplantSettings.fstat("projectile_launch_system", "gold_nugget_damage", 7.0);
        }
        if (stack.is((Item)AllItems.BRASS_NUGGET.get())) {
            return ImplantSettings.fstat("projectile_launch_system", "brass_nugget_damage", 12.0);
        }
        if (stack.is((Item)AllItems.COPPER_NUGGET.get())) {
            return ImplantSettings.fstat("projectile_launch_system", "copper_nugget_damage", 8.0);
        }
        if (stack.is((Item)AllItems.ZINC_NUGGET.get())) {
            return ImplantSettings.fstat("projectile_launch_system", "zinc_nugget_damage", 6.0);
        }
        if (stack.is((Item)AllItems.EXP_NUGGET.get())) {
            return ImplantSettings.fstat("projectile_launch_system", "experience_nugget_damage", 3.0);
        }
        return 0.0f;
    }

    public float damage() {
        return NuggetShot.damage(this.getItem());
    }

    protected Item getDefaultItem() {
        return Items.IRON_NUGGET;
    }

    protected double getDefaultGravity() {
        return 0.0075;
    }

    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        hit.getEntity().hurt(this.damageSources().thrown((Entity)this, this.getOwner()), this.damage());
    }

    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (this.level().isClientSide()) {
            return;
        }
        if (this.getItem().is((Item)AllItems.EXP_NUGGET.get())) {
            this.level().addFreshEntity((Entity)new ExperienceOrb(this.level(), this.getX(), this.getY(), this.getZ(), 3));
        }
        this.level().broadcastEntityEvent((Entity)this, (byte)3);
        this.discard();
    }

    public void handleEntityEvent(byte id) {
        if (id != 3) {
            super.handleEntityEvent(id);
            return;
        }
        for (int i = 0; i < 6; ++i) {
            this.level().addParticle((ParticleOptions)new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), (this.random.nextDouble() - 0.5) * 0.12, this.random.nextDouble() * 0.1, (this.random.nextDouble() - 0.5) * 0.12);
        }
    }
}

