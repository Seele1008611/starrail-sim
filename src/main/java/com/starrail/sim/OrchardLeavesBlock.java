package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import java.util.function.Supplier;

/** Harvest fruit in place; normal leaf decay still prevents detached permanent fruit farms. */
final class OrchardLeavesBlock extends LeavesBlock {
    private final Supplier<Item> fruit;
    OrchardLeavesBlock(Properties p,Supplier<Item> f){super(p);fruit=f;registerDefaultState(defaultBlockState().setValue(BlockStateProperties.AGE_1,1));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(BlockStateProperties.AGE_1);}
    @Override public boolean isRandomlyTicking(BlockState s){return true;}
    @Override public void randomTick(BlockState s,ServerLevel l,BlockPos p,RandomSource r){
        super.randomTick(s,l,p,r);
        if(l.getBlockState(p).is(this)&&s.getValue(BlockStateProperties.AGE_1)==0&&l.getRawBrightness(p,0)>8&&r.nextInt(8)==0)
            l.setBlock(p,s.setValue(BlockStateProperties.AGE_1,1),UPDATE_CLIENTS);
    }
    @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand h,BlockHitResult hit){
        if(s.getValue(BlockStateProperties.AGE_1)==0)return InteractionResult.PASS;
        if(player.getItemInHand(h).is(Items.SHEARS)){
            if(!l.isClientSide){popResource(l,p,new ItemStack(StarRailSimMod.ITEMS.getEntries().stream()
                .filter(i->i.getId().getPath().equals("fruitwood_sprig")).findFirst().orElseThrow().get()));
                l.setBlock(p,s.setValue(BlockStateProperties.AGE_1,0),UPDATE_CLIENTS);
                if(!player.getAbilities().instabuild)player.getItemInHand(h).hurtAndBreak(1,player,e->e.broadcastBreakEvent(h));}
            return InteractionResult.sidedSuccess(l.isClientSide);
        }
        if(!l.isClientSide){popResource(l,p,new ItemStack(fruit.get(),2));l.setBlock(p,s.setValue(BlockStateProperties.AGE_1,0),UPDATE_CLIENTS);}
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
