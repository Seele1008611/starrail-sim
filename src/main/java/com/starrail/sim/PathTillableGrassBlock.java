package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/** Grass block with the same hoe behavior as vanilla farmland sources. */
public final class PathTillableGrassBlock extends GrassBlock {
    private final Supplier<BlockState> hoeResult;
    private final Supplier<BlockState> shovelResult;

    public PathTillableGrassBlock(BlockBehaviour.Properties properties,
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
