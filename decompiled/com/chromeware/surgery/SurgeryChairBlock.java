/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BedPart
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.EnumProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.pathfinder.PathComputationType
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 */
package com.chromeware.surgery;

import com.chromeware.surgery.Surgery;
import com.chromeware.surgery.SurgeryChairBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SurgeryChairBlock
extends HorizontalDirectionalBlock
implements EntityBlock {
    public static final MapCodec<SurgeryChairBlock> CODEC = SurgeryChairBlock.simpleCodec(SurgeryChairBlock::new);
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    public static final double SEAT = 0.625;
    private static final VoxelShape SHAPE = Shapes.or((VoxelShape)Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)4.0, (double)16.0), (VoxelShape)Block.box((double)1.0, (double)4.0, (double)1.0, (double)15.0, (double)10.0, (double)15.0));

    public SurgeryChairBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)FACING, (Comparable)Direction.NORTH)).setValue(PART, (Comparable)BedPart.FOOT));
    }

    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, PART});
    }

    public static BlockPos foot(BlockPos pos, BlockState state) {
        return state.getValue(PART) == BedPart.FOOT ? pos : pos.relative(((Direction)state.getValue((Property)FACING)).getOpposite());
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos head = context.getClickedPos().relative(facing);
        Level level = context.getLevel();
        return level.getBlockState(head).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(head) ? (BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)facing) : null;
    }

    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            level.setBlock(pos.relative((Direction)state.getValue((Property)FACING)), (BlockState)state.setValue(PART, (Comparable)BedPart.HEAD), 3);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes((LevelAccessor)level, pos, 3);
        }
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction toOther;
        Direction direction2 = toOther = state.getValue(PART) == BedPart.FOOT ? (Direction)state.getValue((Property)FACING) : ((Direction)state.getValue((Property)FACING)).getOpposite();
        if (!(direction != toOther || neighbor.is((Block)this) && neighbor.getValue(PART) != state.getValue(PART))) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return Surgery.seat(level, SurgeryChairBlock.foot(pos, state), player) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART) == BedPart.FOOT ? new SurgeryChairBlockEntity(pos, state) : null;
    }
}

