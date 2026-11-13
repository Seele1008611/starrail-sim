package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.registries.RegistryObject;

/** starlightwood signs retain vanilla editing behaviour and use their own persistent block entity types. */
public final class PathStarlightSigns {
    private PathStarlightSigns() {}
    public static final WoodType FROST_WOOD = WoodType.register(new WoodType("starrail_sim:starlightwood", net.minecraft.world.level.block.state.properties.BlockSetType.OAK));
    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.copy(Blocks.SPRUCE_SIGN);
    }
    public static final RegistryObject<Block> SIGN = StarRailSimMod.BLOCKS.register("starlightwood_sign", () -> new FrostStanding(properties()));
    public static final RegistryObject<Block> WALL_SIGN = StarRailSimMod.BLOCKS.register("starlightwood_wall_sign", () -> new FrostWall(properties()));
    public static final RegistryObject<Block> HANGING_SIGN = StarRailSimMod.BLOCKS.register("starlightwood_hanging_sign", () -> new FrostHanging(properties()));
    public static final RegistryObject<Block> WALL_HANGING_SIGN = StarRailSimMod.BLOCKS.register("starlightwood_wall_hanging_sign", () -> new FrostWallHanging(properties()));
    public static final RegistryObject<Item> SIGN_ITEM = StarRailSimMod.ITEMS.register("starlightwood_sign", () -> new SignItem(new Item.Properties().stacksTo(16), SIGN.get(), WALL_SIGN.get()));
    public static final RegistryObject<Item> HANGING_SIGN_ITEM = StarRailSimMod.ITEMS.register("starlightwood_hanging_sign", () -> new HangingSignItem(HANGING_SIGN.get(), WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final RegistryObject<BlockEntityType<FrostSignEntity>> SIGN_ENTITY = StarRailSimMod.BLOCK_ENTITY_TYPES.register("starlightwood_sign", () -> BlockEntityType.Builder.of(FrostSignEntity::new, SIGN.get(), WALL_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<FrostHangingEntity>> HANGING_ENTITY = StarRailSimMod.BLOCK_ENTITY_TYPES.register("starlightwood_hanging_sign", () -> BlockEntityType.Builder.of(FrostHangingEntity::new, HANGING_SIGN.get(), WALL_HANGING_SIGN.get()).build(null));
    static void register() {}
    public static final class FrostSignEntity extends SignBlockEntity { public FrostSignEntity(BlockPos p, BlockState s) { super(SIGN_ENTITY.get(), p, s); } }
    public static final class FrostHangingEntity extends HangingSignBlockEntity { public FrostHangingEntity(BlockPos p, BlockState s) { super(HANGING_ENTITY.get(), p, s); } }
    private static final class FrostStanding extends StandingSignBlock {
        FrostStanding(BlockBehaviour.Properties p) { super(p, FROST_WOOD); }
        @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new FrostSignEntity(p,s); }
        @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) { return createTickerHelper(t, SIGN_ENTITY.get(), SignBlockEntity::tick); }
    }
    private static final class FrostWall extends WallSignBlock {
        FrostWall(BlockBehaviour.Properties p) { super(p, FROST_WOOD); }
        @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new FrostSignEntity(p,s); }
        @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) { return createTickerHelper(t, SIGN_ENTITY.get(), SignBlockEntity::tick); }
    }
    private static final class FrostHanging extends CeilingHangingSignBlock {
        FrostHanging(BlockBehaviour.Properties p) { super(p, FROST_WOOD); }
        @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new FrostHangingEntity(p,s); }
        @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) { return createTickerHelper(t, HANGING_ENTITY.get(), SignBlockEntity::tick); }
    }
    private static final class FrostWallHanging extends WallHangingSignBlock {
        FrostWallHanging(BlockBehaviour.Properties p) { super(p, FROST_WOOD); }
        @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new FrostHangingEntity(p,s); }
        @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) { return createTickerHelper(t, HANGING_ENTITY.get(), SignBlockEntity::tick); }
    }
}
