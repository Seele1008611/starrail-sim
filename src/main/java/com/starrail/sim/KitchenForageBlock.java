package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Supplier;

/** Regrowing kitchen ingredients with terrain specific to their native patches. */
final class KitchenForageBlock extends SweetBerryBushBlock {
    private final Supplier<Item> produce;
    private final List<Block> terrain;

    KitchenForageBlock(Properties properties, Supplier<Item> produce, List<Block> terrain) {
        super(properties);
        this.produce = produce;
        this.terrain = terrain;
    }

    @Override protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return terrain.contains(state.getBlock()) || state.is(PathTerrainBlocks.PATH_GRASS_BLOCK.get())
                || state.is(PathTerrainBlocks.PATH_DIRT.get())
                || state.is(PathKitchen.GRAIN_SOIL.get())
                || (terrain.contains(net.minecraft.world.level.block.Blocks.BASALT)
                    && (state.is(PathKitchen.EMBER_ROCK.get()) || state.is(PathKitchen.TRAVERTINE.get())))
                || (terrain.contains(net.minecraft.world.level.block.Blocks.SNOW_BLOCK)
                    && (state.is(PathKitchen.FROZEN_ROCK.get()) || state.is(PathKitchen.HARD_SNOW.get()) || state.is(PathKitchen.MIRROR_ICE.get())))
                || super.mayPlaceOn(state, level, pos);
    }

    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {}

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                           InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) < 3) return InteractionResult.PASS;
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(produce.get(), 2));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_CLIENTS);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(this);
    }
}
