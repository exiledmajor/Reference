/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer
 *  dev.ryanhcode.sable.api.sublevel.SubLevelContainer
 *  dev.ryanhcode.sable.sublevel.SubLevel
 *  dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason
 *  net.minecraft.core.HolderLookup$Provider
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.syncher.SynchedEntityData$Builder
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Entity$RemovalReason
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.neoforged.neoforge.registries.DeferredHolder
 *  net.neoforged.neoforge.registries.DeferredRegister
 */
package com.chromeware.body;

import com.chromeware.compat.SableCompatibility;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DetachedLimb
extends Entity {
    public static final int LIFETIME = 60;
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create((ResourceKey)Registries.ENTITY_TYPE, (String)"chromeware");
    public static final DeferredHolder<EntityType<?>, EntityType<DetachedLimb>> TYPE = ENTITIES.register("detached_limb", () -> EntityType.Builder.of(DetachedLimb::new, (MobCategory)MobCategory.MISC).sized(0.1f, 0.1f).clientTrackingRange(4).updateInterval(20).build("detached_limb"));
    private UUID physics;
    private ItemStack item = ItemStack.EMPTY;
    private int remaining = 60;

    public DetachedLimb(EntityType<? extends DetachedLimb> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void configure(UUID physics, ItemStack item, Vec3 position) {
        this.physics = physics;
        this.item = item.copy();
        this.setPos(position);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.physics != null) {
            tag.putUUID("Physics", this.physics);
        }
        if (!this.item.isEmpty()) {
            tag.put("Item", this.item.save((HolderLookup.Provider)this.registryAccess()));
        }
        tag.putInt("Remaining", this.remaining);
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
        this.physics = tag.hasUUID("Physics") ? tag.getUUID("Physics") : null;
        this.item = ItemStack.parseOptional((HolderLookup.Provider)this.registryAccess(), (CompoundTag)tag.getCompound("Item"));
        this.remaining = Math.clamp((long)tag.getInt("Remaining"), (int)0, (int)60);
    }

    public void tick() {
        SubLevel piece;
        Level level;
        super.tick();
        if (this.isRemoved() || !((level = this.level()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ServerSubLevelContainer container = SableCompatibility.loaded() ? SubLevelContainer.getContainer((ServerLevel)level2) : null;
        SubLevel subLevel = piece = this.physics == null || container == null ? null : container.getSubLevel(this.physics);
        if (piece != null && !piece.isRemoved()) {
            Vec3 center = piece.logicalPose().transformPosition(Vec3.atCenterOf((Vec3i)piece.getPlot().getCenterBlock()));
            this.setPos(center);
        }
        if (--this.remaining > 0 && piece != null && !piece.isRemoved()) {
            return;
        }
        this.finish(level2);
    }

    private void finish(ServerLevel level) {
        SubLevel piece;
        ServerSubLevelContainer container = SableCompatibility.loaded() ? SubLevelContainer.getContainer((ServerLevel)level) : null;
        SubLevel subLevel = piece = this.physics == null || container == null ? null : container.getSubLevel(this.physics);
        if (piece != null && !piece.isRemoved()) {
            container.removeSubLevel(piece, SubLevelRemovalReason.REMOVED);
        }
        this.physics = null;
        ItemStack result = this.item;
        this.item = ItemStack.EMPTY;
        level.sendParticles((ParticleOptions)ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 8, 0.15, 0.2, 0.15, 0.02);
        if (!result.isEmpty()) {
            ItemEntity dropped = new ItemEntity((Level)level, this.getX(), this.getY(), this.getZ(), result);
            dropped.setDefaultPickUpDelay();
            level.addFreshEntity((Entity)dropped);
        }
        super.remove(Entity.RemovalReason.DISCARDED);
    }

    public void remove(Entity.RemovalReason reason) {
        Level level;
        if (reason.shouldDestroy() && !this.isRemoved() && (level = this.level()) instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            this.finish(level2);
        } else {
            super.remove(reason);
        }
    }
}

