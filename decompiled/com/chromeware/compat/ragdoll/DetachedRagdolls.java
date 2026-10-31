/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.leo.sableplayerragdoll.block.RagdollBlocks
 *  dev.leo.sableplayerragdoll.block.RagdollPartBlock
 *  dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity
 *  dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity$BodyPart
 *  dev.ryanhcode.sable.api.SubLevelAssemblyHelper
 *  dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle
 *  dev.ryanhcode.sable.api.sublevel.SubLevelContainer
 *  dev.ryanhcode.sable.companion.math.BoundingBox3i
 *  dev.ryanhcode.sable.companion.math.BoundingBox3ic
 *  dev.ryanhcode.sable.companion.math.Pose3d
 *  dev.ryanhcode.sable.sublevel.ServerSubLevel
 *  dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaterniondc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package com.chromeware.compat.ragdoll;

import com.chromeware.Chromeware;
import com.chromeware.body.BodyPart;
import com.chromeware.body.DetachedLimb;
import com.chromeware.surgery.SurgerySeat;
import dev.leo.sableplayerragdoll.block.RagdollBlocks;
import dev.leo.sableplayerragdoll.block.RagdollPartBlock;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class DetachedRagdolls {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean spawn(Player player, BodyPart part, boolean cyber, ItemStack item, Vec3 position) {
        boolean bl;
        ServerSubLevel piece;
        BlockPos staging;
        ServerLevel level;
        block20: {
            RagdollPartBlockEntity block;
            RagdollPartBlockEntity.BodyPart nativePart;
            block19: {
                Level level2 = player.level();
                if (!(level2 instanceof ServerLevel)) {
                    return false;
                }
                level = (ServerLevel)level2;
                staging = null;
                for (int up = 2; up <= 8; ++up) {
                    BlockPos candidate = player.blockPosition().above(up);
                    if (!level.isInWorldBounds(candidate) || !level.hasChunkAt(candidate) || !level.isEmptyBlock(candidate) || level.getBlockEntity(candidate) != null) continue;
                    staging = candidate;
                    break;
                }
                if (staging == null) {
                    return false;
                }
                piece = null;
                nativePart = RagdollPartBlockEntity.BodyPart.valueOf((String)(part == BodyPart.BODY ? "TORSO" : part.name()));
                level.setBlock(staging, (BlockState)RagdollBlocks.ragdollPartDefaultState().setValue((Property)RagdollPartBlock.BODY_PART, (Comparable)nativePart), 2);
                BlockEntity blockEntity = level.getBlockEntity(staging);
                if (blockEntity instanceof RagdollPartBlockEntity) {
                    block = (RagdollPartBlockEntity)blockEntity;
                    break block19;
                }
                boolean bl2 = false;
                if (level.getBlockState(staging).is(RagdollBlocks.ragdollPart())) {
                    level.setBlock(staging, Blocks.AIR.defaultBlockState(), 2);
                }
                return bl2;
            }
            block.configure(nativePart, player.getGameProfile());
            block.getPersistentData().putBoolean("ChromeDetached", true);
            block.getPersistentData().putBoolean("ChromeFrame", cyber);
            block.getPersistentData().putInt("ChromeMissing", 0);
            block.setChanged();
            Set<BlockPos> blocks = Set.of(staging);
            piece = SubLevelAssemblyHelper.assembleBlocks((ServerLevel)level, (BlockPos)staging, blocks, (BoundingBox3ic)BoundingBox3i.from(blocks));
            if (piece != null && !piece.isRemoved()) break block20;
            boolean bl3 = false;
            if (level.getBlockState(staging).is(RagdollBlocks.ragdollPart())) {
                level.setBlock(staging, Blocks.AIR.defaultBlockState(), 2);
            }
            return bl3;
        }
        try {
            piece.getPlot().updateBoundingBox();
            Pose3d pose = piece.logicalPose();
            pose.orientation().rotationY(Math.toRadians(-player.yBodyRot));
            Entity entity = player.getVehicle();
            if (entity instanceof SurgerySeat) {
                SurgerySeat seat = (SurgerySeat)entity;
                pose.orientation().rotateX(Math.toRadians(seat.recline(1.0f)));
            }
            Vec3 center = pose.transformPosition(Vec3.atCenterOf((Vec3i)piece.getPlot().getCenterBlock()));
            Vec3 offset = position.subtract(center);
            pose.position().add(offset.x, offset.y, offset.z);
            RigidBodyHandle.of((ServerSubLevel)piece).teleport((Vector3dc)pose.position(), (Quaterniondc)pose.orientation());
            piece.updateLastPose();
            piece.forceUpdateGlobalBounds();
            RigidBodyHandle.of((ServerSubLevel)piece).addLinearAndAngularVelocity((Vector3dc)new Vector3d(player.getDeltaMovement().x, 0.12, player.getDeltaMovement().z), (Vector3dc)new Vector3d(0.8, 0.3, part.name().startsWith("LEFT") ? 0.6 : -0.6));
            DetachedLimb pending = (DetachedLimb)((EntityType)DetachedLimb.TYPE.get()).create((Level)level);
            if (pending == null) {
                throw new IllegalStateException("No detached-limb entity");
            }
            pending.configure(piece.getUniqueId(), item, position);
            if (!level.addFreshEntity((Entity)pending)) {
                throw new IllegalStateException("Detached-limb entity rejected");
            }
            bl = true;
        }
        catch (LinkageError | RuntimeException error) {
            boolean bl4;
            try {
                if (piece != null && !piece.isRemoved()) {
                    SubLevelContainer.getContainer((ServerLevel)level).removeSubLevel(piece, SubLevelRemovalReason.REMOVED);
                }
                Chromeware.LOGGER.warn("Could not assemble detached {}: {}", (Object)part, (Object)error.toString());
                bl4 = false;
            }
            catch (Throwable throwable) {
                if (level.getBlockState(staging).is(RagdollBlocks.ragdollPart())) {
                    level.setBlock(staging, Blocks.AIR.defaultBlockState(), 2);
                }
                throw throwable;
            }
            if (level.getBlockState(staging).is(RagdollBlocks.ragdollPart())) {
                level.setBlock(staging, Blocks.AIR.defaultBlockState(), 2);
            }
            return bl4;
        }
        if (level.getBlockState(staging).is(RagdollBlocks.ragdollPart())) {
            level.setBlock(staging, Blocks.AIR.defaultBlockState(), 2);
        }
        return bl;
    }
}

