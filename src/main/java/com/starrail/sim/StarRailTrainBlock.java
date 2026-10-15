package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/** Large reference-based locomotive; the original compact segment states remain loadable. */
public final class StarRailTrainBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty LARGE = BooleanProperty.create("large");
    public static final EnumProperty<Segment> SEGMENT = EnumProperty.create("segment", Segment.class);

    private static final Segment ANCHOR = Segment.X0_Y0_Z0;
    private static final ThreadLocal<Boolean> REMOVING_STRUCTURE =
            ThreadLocal.withInitial(() -> false);

    public StarRailTrainBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LARGE, false)
                .setValue(SEGMENT, ANCHOR));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, SEGMENT, LARGE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos anchor = context.getClickedPos();
        Level level = context.getLevel();

        // Validate the whole visible envelope; only solid hull cells are placed below.
        for (int id = 0; id < StarRailTrainLayout.CELL_COUNT; id++) {
            if (StarRailTrainLayout.x(id) >= StarRailTrainLayout.WIDTH
                    || StarRailTrainLayout.y(id) >= StarRailTrainLayout.HEIGHT
                    || StarRailTrainLayout.z(id) >= StarRailTrainLayout.LENGTH) continue;
            BlockPos target = StarRailTrainLayout.position(anchor, facing, id);
            if (!isReplaceable(level, target, context)) return null;
        }
        return defaultBlockState().setValue(FACING, facing).setValue(LARGE, true).setValue(SEGMENT, ANCHOR);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(LARGE) && state.getValue(SEGMENT) == ANCHOR
                ? new StarRailTrainBlockEntity(pos, state) : null;
    }

    @Override public RenderShape getRenderShape(BlockState state) {
        return state.getValue(LARGE) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            StarRailDimensionService.activateTrain(serverPlayer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isReplaceable(Level level, BlockPos pos, BlockPlaceContext context) {
        return !level.isOutsideBuildHeight(pos)
                && level.hasChunkAt(pos)
                && level.getWorldBorder().isWithinBounds(pos)
                && level.getBlockState(pos).canBeReplaced(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide || state.getValue(SEGMENT) != ANCHOR) return;

        Direction facing = state.getValue(FACING);
        if (state.getValue(LARGE)) {
            for (int id : StarRailTrainLayout.OCCUPIED_CELLS) {
                if (id == 0) continue;
                level.setBlock(StarRailTrainLayout.position(pos, facing, id),
                        StarRailWorldBlocks.STAR_RAIL_TRAIN_PART.get().defaultBlockState()
                                .setValue(FACING, facing).setValue(StarRailTrainPartBlock.CELL, id), Block.UPDATE_ALL);
            }
            return;
        }
        for (Segment segment : Segment.values()) {
            if (segment == ANCHOR) continue;
            BlockPos target = getSegmentPos(pos, facing, segment);
            BlockState cellState = defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(SEGMENT, segment);
            level.setBlock(target, cellState, Block.UPDATE_ALL);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (state.getValue(LARGE) && !state.is(newState.getBlock())) {
            if (!level.isClientSide) removeLargeStructure(level, pos, state.getValue(FACING), pos);
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }
        if (!state.is(newState.getBlock()) && !REMOVING_STRUCTURE.get()) {
            REMOVING_STRUCTURE.set(true);
            try {
                Direction facing = state.getValue(FACING);
                BlockPos anchor = getFrontPos(pos, state);
                for (Segment segment : Segment.values()) {
                    BlockPos target = getSegmentPos(anchor, facing, segment);
                    if (target.equals(pos)) continue;

                    BlockState other = level.getBlockState(target);
                    if (other.is(this)
                            && !other.getValue(LARGE)
                            && other.getValue(FACING) == facing
                            && other.getValue(SEGMENT) == segment) {
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            } finally {
                REMOVING_STRUCTURE.remove();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                               CollisionContext context) {
        if (state.getValue(LARGE)) return StarRailTrainLayout.shape(0, state.getValue(FACING));
        return Shapes.block();
    }

    static void removeLargeStructure(Level level, BlockPos anchor, Direction facing, BlockPos except) {
        if (REMOVING_STRUCTURE.get()) return;
        REMOVING_STRUCTURE.set(true);
        try {
            for (int id = 0; id < StarRailTrainLayout.CELL_COUNT; id++) {
                BlockPos target = StarRailTrainLayout.position(anchor, facing, id);
                if (target.equals(except)) continue;
                BlockState state = level.getBlockState(target);
                boolean matches = id == 0
                        ? state.is(StarRailWorldBlocks.STAR_RAIL_TRAIN.get()) && state.getValue(LARGE)
                        : state.is(StarRailWorldBlocks.STAR_RAIL_TRAIN_PART.get())
                                && state.getValue(StarRailTrainPartBlock.CELL) == id;
                if (matches && state.getValue(FACING) == facing) {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        } finally { REMOVING_STRUCTURE.remove(); }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    private static BlockPos getSegmentPos(BlockPos anchor, Direction facing, Segment segment) {
        return anchor.relative(facing.getClockWise(), segment.x)
                .relative(facing.getOpposite(), segment.z)
                .above(segment.y);
    }

    /** Resolves any cell in the train back to the front-left-bottom anchor. */
    public static BlockPos getFrontPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Segment segment = state.getValue(SEGMENT);
        return pos.relative(facing.getCounterClockWise(), segment.x)
                .relative(facing, segment.z)
                .below(segment.y);
    }

    public enum Segment implements StringRepresentable {
        X0_Y0_Z0(0, 0, 0), X1_Y0_Z0(1, 0, 0),
        X0_Y1_Z0(0, 1, 0), X1_Y1_Z0(1, 1, 0),
        X0_Y2_Z0(0, 2, 0), X1_Y2_Z0(1, 2, 0),

        X0_Y0_Z1(0, 0, 1), X1_Y0_Z1(1, 0, 1),
        X0_Y1_Z1(0, 1, 1), X1_Y1_Z1(1, 1, 1),
        X0_Y2_Z1(0, 2, 1), X1_Y2_Z1(1, 2, 1),

        X0_Y0_Z2(0, 0, 2), X1_Y0_Z2(1, 0, 2),
        X0_Y1_Z2(0, 1, 2), X1_Y1_Z2(1, 1, 2),
        X0_Y2_Z2(0, 2, 2), X1_Y2_Z2(1, 2, 2),

        X0_Y0_Z3(0, 0, 3), X1_Y0_Z3(1, 0, 3),
        X0_Y1_Z3(0, 1, 3), X1_Y1_Z3(1, 1, 3),
        X0_Y2_Z3(0, 2, 3), X1_Y2_Z3(1, 2, 3);

        private final int x;
        private final int y;
        private final int z;

        Segment(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String getSerializedName() {
            return "x" + x + "_y" + y + "_z" + z;
        }
    }
}
