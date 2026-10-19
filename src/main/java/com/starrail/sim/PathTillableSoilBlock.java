package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/** Custom Path soil with an explicit hoe result. */
public final class PathTillableSoilBlock extends Block {
    private final Supplier<BlockState> hoeResult;
    private final Supplier<BlockState> shovelResult;

    public PathTillableSoilBlock(BlockBehaviour.Properties properties,
                                 Supplier<BlockState> hoeResult,
                                 Supplier<BlockState> shovelResult) {
        super(properties);
        this.hoeResult = hoeResult;
        this.shovelResult = shovelResult;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return PathSoilToolInteraction.interact(level, pos, player, hand,
                hoeResult, shovelResult);
    }
}
