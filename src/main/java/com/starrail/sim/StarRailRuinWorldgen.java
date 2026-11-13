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

    public static final RegistryObject<StructureType<DoomsdayArenaStructure>> DOOMSDAY_ARENA =
            TYPES.register("doomsday_arena", () -> () -> DoomsdayArenaStructure.CODEC);
    public static final RegistryObject<StructurePieceType> DOOMSDAY_ARENA_PIECE =
            PIECES.register("doomsday_arena_piece", () -> DoomsdayArenaPiece::new);

    public static final RegistryObject<StructureType<DaylessArchiveStructure>> DAYLESS_ARCHIVE =
            TYPES.register("dayless_archive", () -> () -> DaylessArchiveStructure.CODEC);
    public static final RegistryObject<StructurePieceType> DAYLESS_ARCHIVE_PIECE =
            PIECES.register("dayless_archive_piece", () -> DaylessArchivePiece::new);

    public static final RegistryObject<StructureType<TidewardDockStructure>> TIDEWARD_DOCK =
            TYPES.register("tideward_dock", () -> () -> TidewardDockStructure.CODEC);
    public static final RegistryObject<StructurePieceType> TIDEWARD_DOCK_PIECE =
            PIECES.register("tideward_dock_piece", () -> TidewardDockPiece::new);

    public static final RegistryObject<StructureType<BrokenRingObservatoryStructure>> BROKEN_RING_OBSERVATORY =
            TYPES.register("broken_ring_observatory", () -> () -> BrokenRingObservatoryStructure.CODEC);
    public static final RegistryObject<StructurePieceType> BROKEN_RING_OBSERVATORY_PIECE =
            PIECES.register("broken_ring_observatory_piece", () -> BrokenRingObservatoryPiece::new);

    private StarRailRuinWorldgen() { }
    public static void register(IEventBus bus) {
        TYPES.register(bus);
        PIECES.register(bus);
    }
}
