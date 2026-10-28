package com.starrail.sim;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Entity types for the original wildlife that replaces overworld livestock in the Path dimension. */
public final class PathAnimalEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StarRailSimMod.MOD_ID);

    public static final RegistryObject<EntityType<SunnyHarvestDeer>> SUNNYHARVEST_DEER = ENTITY_TYPES.register(
            "sunnyharvest_deer", () -> EntityType.Builder.of(SunnyHarvestDeer::new, MobCategory.CREATURE)
                    .sized(1.0F, 1.6F).clientTrackingRange(10).build("sunnyharvest_deer"));
    public static final RegistryObject<EntityType<StarvelvetRam>> STARVELVET_RAM = ENTITY_TYPES.register(
            "starvelvet_ram", () -> EntityType.Builder.of(StarvelvetRam::new, MobCategory.CREATURE)
                    .sized(1.35F, 1.3F).clientTrackingRange(10).build("starvelvet_ram"));
    public static final RegistryObject<EntityType<WoodlandForager>> WOODLAND_FORAGER = ENTITY_TYPES.register(
            "woodland_forager", () -> EntityType.Builder.of(WoodlandForager::new, MobCategory.CREATURE)
                    .sized(1.75F, 1.1F).clientTrackingRange(10).build("woodland_forager"));
    public static final RegistryObject<EntityType<StilltideFowl>> STILLTIDE_FOWL = ENTITY_TYPES.register(
            "stilltide_fowl", () -> EntityType.Builder.of(StilltideFowl::new, MobCategory.CREATURE)
                    .sized(0.5F, 0.95F).clientTrackingRange(8).build("stilltide_fowl"));
    public static final RegistryObject<EntityType<StarScreePackbeast>> STAR_SCREE_PACKBEAST = ENTITY_TYPES.register(
            "star_scree_packbeast", () -> EntityType.Builder.of(StarScreePackbeast::new, MobCategory.CREATURE)
                    .sized(2.1F, 1.85F).clientTrackingRange(10).build("star_scree_packbeast"));

    public static final RegistryObject<Item> SUNNYHARVEST_DEER_SPAWN_EGG = spawnEgg(
            "sunnyharvest_deer_spawn_egg", SUNNYHARVEST_DEER, 0xC89A46, 0xE9D79C);
    public static final RegistryObject<Item> STARVELVET_RAM_SPAWN_EGG = spawnEgg(
            "starvelvet_ram_spawn_egg", STARVELVET_RAM, 0x8C809E, 0xD8CDB8);
    public static final RegistryObject<Item> WOODLAND_FORAGER_SPAWN_EGG = spawnEgg(
            "woodland_forager_spawn_egg", WOODLAND_FORAGER, 0x52694D, 0x9E7850);
    public static final RegistryObject<Item> STILLTIDE_FOWL_SPAWN_EGG = spawnEgg(
            "stilltide_fowl_spawn_egg", STILLTIDE_FOWL, 0x4E7779, 0xD7C58C);
    public static final RegistryObject<Item> STAR_SCREE_PACKBEAST_SPAWN_EGG = spawnEgg(
            "star_scree_packbeast_spawn_egg", STAR_SCREE_PACKBEAST, 0x494A4C, 0x49AEB5);

    private PathAnimalEntities() {
    }

    /** Called by the mod's common setup wiring once ENTITY_TYPES is attached to the mod event bus. */
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }

    /** Must be wired to EntityAttributeCreationEvent by the main mod. */
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SUNNYHARVEST_DEER.get(), SunnyHarvestDeer.createAttributes().build());
        event.put(STARVELVET_RAM.get(), StarvelvetRam.createAttributes().build());
        event.put(WOODLAND_FORAGER.get(), WoodlandForager.createAttributes().build());
        event.put(STILLTIDE_FOWL.get(), StilltideFowl.createAttributes().build());
        event.put(STAR_SCREE_PACKBEAST.get(), StarScreePackbeast.createAttributes().build());
    }

    /** Registers ground-only natural spawning so the land animals cannot appear in deep water. */
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        registerGroundSpawn(event, SUNNYHARVEST_DEER.get());
        registerGroundSpawn(event, STARVELVET_RAM.get());
        registerGroundSpawn(event, WOODLAND_FORAGER.get());
        registerGroundSpawn(event, STILLTIDE_FOWL.get());
        registerGroundSpawn(event, STAR_SCREE_PACKBEAST.get());
    }

    private static <T extends Animal> void registerGroundSpawn(
            SpawnPlacementRegisterEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules,
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static <T extends net.minecraft.world.entity.Mob> RegistryObject<Item> spawnEgg(
            String id, RegistryObject<EntityType<T>> type, int baseColor, int spotColor) {
        return StarRailSimMod.ITEMS.register(id,
                () -> new ForgeSpawnEggItem(type, baseColor, spotColor, new Item.Properties()));
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        output.accept(SUNNYHARVEST_DEER_SPAWN_EGG.get());
        output.accept(STARVELVET_RAM_SPAWN_EGG.get());
        output.accept(WOODLAND_FORAGER_SPAWN_EGG.get());
        output.accept(STILLTIDE_FOWL_SPAWN_EGG.get());
        output.accept(STAR_SCREE_PACKBEAST_SPAWN_EGG.get());
    }
}
