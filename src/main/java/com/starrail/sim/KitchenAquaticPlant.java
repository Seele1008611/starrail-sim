package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;
import java.util.function.Supplier;

/** Source-water plants: harvesting leaves the root; collected divisions can be replanted. */
final class KitchenAquaticPlant extends BushBlock implements SimpleWaterloggedBlock, BonemealableBlock {
    private final Supplier<Item> produce;
    KitchenAquaticPlant(Properties p,Supplier<Item> produce){super(p);this.produce=produce;
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.AGE_3,0).setValue(BlockStateProperties.WATERLOGGED,true));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(BlockStateProperties.AGE_3,BlockStateProperties.WATERLOGGED);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return c.getLevel().getFluidState(c.getClickedPos()).is(Fluids.WATER)
            ? defaultBlockState():null;}
    @Override protected boolean mayPlaceOn(BlockState s,BlockGetter l,BlockPos p){
        if(!s.isFaceSturdy(l,p,Direction.UP))return false;
        if(this==PathKitchenExpansion.block("edible_anemone_patch").get())return s.is(PathKitchenExpansion.block("tidal_stripe_rock").get())||s.is(net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE);
        return s.is(PathKitchenExpansion.block("jadebranch_mud").get())||s.is(net.minecraft.tags.BlockTags.DIRT)||s.is(Blocks.CLAY)||s.is(Blocks.MUD);
    }
    @Override public boolean canSurvive(BlockState s,LevelReader l,BlockPos p){return s.getValue(BlockStateProperties.WATERLOGGED)&&mayPlaceOn(l.getBlockState(p.below()),l,p.below());}
    @Override public FluidState getFluidState(BlockState s){return s.getValue(BlockStateProperties.WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(s);}
    @Override public BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor l,BlockPos p,BlockPos q){
        l.scheduleTick(p,Fluids.WATER,Fluids.WATER.getTickDelay(l));
        return canSurvive(s,l,p)?s:Blocks.WATER.defaultBlockState();
    }
    @Override public void randomTick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){if(r.nextInt(5)==0&&s.getValue(BlockStateProperties.AGE_3)<3)l.setBlock(p,s.cycle(BlockStateProperties.AGE_3),UPDATE_CLIENTS);}
    @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
        if(s.getValue(BlockStateProperties.AGE_3)<3)return InteractionResult.PASS;
        if(!l.isClientSide){popResource(l,p,new ItemStack(produce.get(),2));l.setBlock(p,s.setValue(BlockStateProperties.AGE_3,1),UPDATE_CLIENTS);}
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public boolean isValidBonemealTarget(LevelReader l,BlockPos p,BlockState s,boolean client){return s.getValue(BlockStateProperties.AGE_3)<3;}
    @Override public boolean isBonemealSuccess(Level l,RandomSource r,BlockPos p,BlockState s){return true;}
    @Override public void performBonemeal(ServerLevel l,RandomSource r,BlockPos p,BlockState s){l.setBlock(p,s.setValue(BlockStateProperties.AGE_3,Math.min(3,s.getValue(BlockStateProperties.AGE_3)+1)),UPDATE_CLIENTS);}
}
