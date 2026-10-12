package com.starrail.sim;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** 注册自然生成遗迹与遗器试炼场的结构类型和结构片读取器。 */
public final class StarRailRuinWorldgen {
    private static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, StarRailSimMod.MOD_ID);
    private static final DeferredRegister<StructurePieceType> PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, StarRailSimMod.MOD_ID);
    public static final RegistryObject<StructureType<FloatingRuinStructure>> ORDINARY =
            TYPES.register("floating_ordinary_ruin", () -> () -> FloatingRuinStructure.CODEC);
    public static final RegistryObject<StructureType<RelicTrialArenaStructure>> RELIC_TRIAL_ARENA =
            TYPES.register("relic_trial_arena", () -> () -> RelicTrialArenaStructure.CODEC);
    public static final RegistryObject<StructurePieceType> PIECE =
            PIECES.register("floating_ruin_piece", () -> FloatingRuinPiece::new);
    public static final RegistryObject<StructurePieceType> ARENA_PIECE =
            PIECES.register("relic_trial_arena_piece", () -> RelicTrialArenaPiece::new);

    private StarRailRuinWorldgen() { }
    public static void register(IEventBus bus) {
        TYPES.register(bus);
        PIECES.register(bus);
    }
}
