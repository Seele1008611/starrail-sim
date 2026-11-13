package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

final class KitchenBlock extends Block implements EntityBlock {
    KitchenBlock(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.starrail_sim.kitchen_station"));
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KitchenBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != PathKitchen.ENTITY.get()) return null;
        return (world, pos, blockState, entity) -> ((KitchenBlockEntity) entity).tick();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer server) || !(level.getBlockEntity(pos) instanceof KitchenBlockEntity kitchen)) {
            return InteractionResult.PASS;
        }
        NetworkHooks.openScreen(server, new SimpleMenuProvider(
                (id, inventory, ignored) -> new KitchenMenu(id, inventory, pos, kitchen),
                Component.translatable("block.starrail_sim.kitchen_station")), buffer -> {
            buffer.writeBlockPos(pos);
            buffer.writeNullable(kitchen.rememberedRecipe(), (out, recipe) -> out.writeResourceLocation(recipe));
        });
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!level.isClientSide && !state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof KitchenBlockEntity kitchen) {
            Containers.dropContents(level, pos, kitchen.inventory);
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
