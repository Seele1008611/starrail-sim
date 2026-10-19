package com.starrail.sim;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.function.Supplier;

/** A log or wood block that declares its stripped variant through Forge's tool action API. */
public final class PathStrippablePillarBlock extends RotatedPillarBlock {
    private final Supplier<? extends Block> strippedVariant;

    public PathStrippablePillarBlock(BlockBehaviour.Properties properties,
                                     Supplier<? extends Block> strippedVariant) {
        super(properties);
        this.strippedVariant = strippedVariant;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context,
                                          ToolAction toolAction, boolean simulate) {
        if (toolAction == ToolActions.AXE_STRIP) {
            return strippedVariant.get().defaultBlockState()
                    .setValue(AXIS, state.getValue(AXIS));
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }
}
