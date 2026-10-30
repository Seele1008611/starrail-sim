package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registry for the four stage-2 voidrangers and the eliminator's bolt. */
public final class VoidrangerEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StarRailSimMod.MOD_ID);

    public static final RegistryObject<EntityType<ReaverVoidranger>> VOIDRANGER_REAVER = ENTITY_TYPES.register(
            "voidranger_reaver", () -> EntityType.Builder.of(ReaverVoidranger::new, MobCategory.MONSTER)
                    .sized(1.25F, 2.5F).clientTrackingRange(12).updateInterval(2)
                    .build("voidranger_reaver"));
    public static final RegistryObject<EntityType<DistorterVoidranger>> VOIDRANGER_DISTORTER = ENTITY_TYPES.register(
            "voidranger_distorter", () -> EntityType.Builder.of(DistorterVoidranger::new, MobCategory.MONSTER)
                    .sized(1.125F, 2.5F).clientTrackingRange(12).updateInterval(2)
                    .build("voidranger_distorter"));
    public static final RegistryObject<EntityType<TramplerVoidranger>> VOIDRANGER_TRAMPLER = ENTITY_TYPES.register(
            "voidranger_trampler", () -> EntityType.Builder.of(TramplerVoidranger::new, MobCategory.MONSTER)
                    .sized(1.5F, 3.2F).clientTrackingRange(16).updateInterval(2)
                    .build("voidranger_trampler"));
    public static final RegistryObject<EntityType<EliminatorVoidranger>> VOIDRANGER_ELIMINATOR = ENTITY_TYPES.register(
            "voidranger_eliminator", () -> EntityType.Builder.of(EliminatorVoidranger::new, MobCategory.MONSTER)
                    .sized(2.0F, 3.2F).clientTrackingRange(16).updateInterval(2)
                    .build("voidranger_eliminator"));
    public static final RegistryObject<EntityType<VoidrangerBoltEntity>> VOIDRANGER_BOLT = ENTITY_TYPES.register(
            "voidranger_bolt", () -> EntityType.Builder.of(VoidrangerBoltEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1)
                    .build("voidranger_bolt"));

    private VoidrangerEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(VoidrangerEntities::registerAttributes);
        modEventBus.addListener(VoidrangerEntities::registerSpawnPlacements);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(VOIDRANGER_REAVER.get(), ReaverVoidranger.createAttributes().build());
        event.put(VOIDRANGER_DISTORTER.get(), DistorterVoidranger.createAttributes().build());
        event.put(VOIDRANGER_TRAMPLER.get(), TramplerVoidranger.createAttributes().build());
        event.put(VOIDRANGER_ELIMINATOR.get(), EliminatorVoidranger.createAttributes().build());
    }

    /** Keep land spawns on solid ground and allow safe water-surface spawns in any Path biome. */
    private static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        registerNaturalSpawn(event, VOIDRANGER_REAVER.get());
        registerNaturalSpawn(event, VOIDRANGER_DISTORTER.get());
        registerNaturalSpawn(event, VOIDRANGER_TRAMPLER.get());
        registerNaturalSpawn(event, VOIDRANGER_ELIMINATOR.get());
    }

    private static <T extends Monster> void registerNaturalSpawn(
            SpawnPlacementRegisterEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacements.Type.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (entityType, level, spawnType, pos, random) ->
                        hasValidNaturalSpawnPosition(entityType, level, spawnType, pos, random),
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static <T extends Monster> boolean hasValidNaturalSpawnPosition(
            EntityType<T> entityType, ServerLevelAccessor level, MobSpawnType spawnType,
            BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()) return false;

        BlockPos floorPos = pos.below();
        var floorFluid = level.getFluidState(floorPos);
        if (floorFluid.isEmpty()) {
            return Monster.checkMonsterSpawnRules(entityType, level, spawnType, pos, random)
                    && level.getBlockState(floorPos).isFaceSturdy(level, floorPos, Direction.UP);
        }

        // Monster.checkMonsterSpawnRules also requires a sturdy block below the spawn point,
        // which rejects every water block. For this exposed-water branch, keep its difficulty
        // and light checks while replacing only the solid-floor condition with a water surface.
        boolean exposedWaterSurface = floorFluid.is(FluidTags.WATER)
                && level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                pos.getX(), pos.getZ()) == pos.getY();
        return exposedWaterSurface
                && level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(level, pos, random);
    }
}
