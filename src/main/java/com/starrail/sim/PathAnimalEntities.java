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
    public static final RegistryObject<EntityType<GemLizard>> GEM_LIZARD = ENTITY_TYPES.register(
            "gem_lizard", () -> EntityType.Builder.of(GemLizard::new, MobCategory.CREATURE)
                    .sized(.72F, .53F).clientTrackingRange(8).build("gem_lizard"));
    public static final RegistryObject<EntityType<KitchenWildlife>> LOTUS_OTTER = wildlife("lotus_otter",1.05F,.6F);
    public static final RegistryObject<EntityType<KitchenWildlife>> ROCK_CRAB = wildlife("rockdwelling_crab",.9F,.4F);
    public static final RegistryObject<EntityType<KitchenWildlife>> ORANGE_FINCH = wildlife("duskorange_finch",.4F,.5F);
    public static final RegistryObject<EntityType<DuskredFish>> DUSKRED_FISH = ENTITY_TYPES.register("duskred_fish",()->
            EntityType.Builder.of(DuskredFish::new,MobCategory.WATER_AMBIENT).sized(.45F,.36F).clientTrackingRange(8).build("duskred_fish"));
    private static RegistryObject<EntityType<KitchenWildlife>> wildlife(String id,float w,float h){
        return ENTITY_TYPES.register(id,()->EntityType.Builder.<KitchenWildlife>of((type,level)->switch(id){
            case "lotus_otter" -> new KitchenWildlife.Otter(type,level);
            case "rockdwelling_crab" -> new KitchenWildlife.Crab(type,level);
            default -> new KitchenWildlife.Finch(type,level);
        },MobCategory.CREATURE).sized(w,h).clientTrackingRange(8).build(id));
    }
    private static final java.util.List<RegistryObject<Item>> KITCHEN_EGGS=java.util.List.of(
            spawnEgg("lotus_otter_spawn_egg",LOTUS_OTTER,0x68432C,0xC9AE7E),
            spawnEgg("rockdwelling_crab_spawn_egg",ROCK_CRAB,0x65666C,0xA2A09A),
            spawnEgg("duskorange_finch_spawn_egg",ORANGE_FINCH,0xB57B3E,0xE28B2D),
            spawnEgg("duskred_fish_spawn_egg",DUSKRED_FISH,0xAD3F3D,0xD5D2C9));
    public static final RegistryObject<Item> GEM_LIZARD_SPAWN_EGG = spawnEgg(
            "gem_lizard_spawn_egg", GEM_LIZARD, 0x69443E, 0x66C8AA);

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
        event.put(GEM_LIZARD.get(), GemLizard.createAttributes().build());
        event.put(LOTUS_OTTER.get(),KitchenWildlife.attributes(12).build());
        event.put(ROCK_CRAB.get(),KitchenWildlife.attributes(10).build());
        event.put(ORANGE_FINCH.get(),KitchenWildlife.attributes(6).build());
        event.put(DUSKRED_FISH.get(),DuskredFish.attributes().build());
    }

    /** Registers ground-only natural spawning so the land animals cannot appear in deep water. */
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        registerGroundSpawn(event, SUNNYHARVEST_DEER.get());
        registerGroundSpawn(event, STARVELVET_RAM.get());
        registerGroundSpawn(event, WOODLAND_FORAGER.get());
        registerGroundSpawn(event, STILLTIDE_FOWL.get());
        registerGroundSpawn(event, STAR_SCREE_PACKBEAST.get());
        event.register(GEM_LIZARD.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                GemLizard::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(LOTUS_OTTER.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,KitchenWildlife::spawn,SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ROCK_CRAB.get(),SpawnPlacements.Type.NO_RESTRICTIONS,Heightmap.Types.OCEAN_FLOOR,KitchenWildlife::spawn,SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(ORANGE_FINCH.get(),SpawnPlacements.Type.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,KitchenWildlife::spawn,SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(DUSKRED_FISH.get(),SpawnPlacements.Type.IN_WATER,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,DuskredFish::spawn,SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static <T extends Animal> void registerGroundSpawn(
            SpawnPlacementRegisterEvent event, EntityType<T> type) {
        event.register(type, SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (entity, level, reason, pos, random) -> {
                    if (Animal.checkAnimalSpawnRules(entity, level, reason, pos, random)) return true;
                    if (!level.getLevel().dimension().equals(StarRailDimensionIds.THE_PATH)) return false;
                    if (!level.getFluidState(pos).isEmpty() || level.getRawBrightness(pos, 0) <= 8) return false;
                    var ground = level.getBlockState(pos.below());
                    if (!ground.isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP)) return false;
                    return level.getBiome(pos).unwrapKey().map(key -> {
                        var id = key.location();
                        if (!id.getNamespace().equals(StarRailSimMod.MOD_ID)) return false;
                        return switch (id.getPath()) {
                            case "frostveil_taiga" -> ground.is(net.minecraft.world.level.block.Blocks.PODZOL)
                                    || ground.is(PathTerrainBlocks.PATH_COARSE_SOIL.get())
                                    || ground.is(PathEcologyContent.FROST_MOSS.get());
                            case "ochrecrystal_hills" -> ground.is(PathOchreContent.OCHRE_SAND.get())
                                    || ground.is(PathOchreContent.OCHRE_STONE.base().get());
                            default -> false;
                        };
                    }).orElse(false);
                },
                SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private static <T extends net.minecraft.world.entity.Mob> RegistryObject<Item> spawnEgg(
            String id, RegistryObject<EntityType<T>> type, int baseColor, int spotColor) {
        return StarRailSimMod.ITEMS.register(id,
                () -> new ForgeSpawnEggItem(type, baseColor, spotColor, new Item.Properties()));
    }

    public static void addCreativeItems(CreativeModeTab.Output output) {
        KITCHEN_EGGS.forEach(i->output.accept(i.get()));
        output.accept(GEM_LIZARD_SPAWN_EGG.get());
        output.accept(SUNNYHARVEST_DEER_SPAWN_EGG.get());
        output.accept(STARVELVET_RAM_SPAWN_EGG.get());
        output.accept(WOODLAND_FORAGER_SPAWN_EGG.get());
        output.accept(STILLTIDE_FOWL_SPAWN_EGG.get());
        output.accept(STAR_SCREE_PACKBEAST_SPAWN_EGG.get());
    }
}
