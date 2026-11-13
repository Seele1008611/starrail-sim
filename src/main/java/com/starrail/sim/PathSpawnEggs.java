package com.starrail.sim;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

final class PathSpawnEggs {
    private static final List<RegistryObject<Item>> EGGS = List.of(
            egg(VoidrangerEntities.VOIDRANGER_REAVER, 0x403547, 0xdba15f),
            egg(VoidrangerEntities.VOIDRANGER_DISTORTER, 0x342a50, 0xbb70de),
            egg(VoidrangerEntities.VOIDRANGER_TRAMPLER, 0x534342, 0xd89a69),
            egg(VoidrangerEntities.VOIDRANGER_ELIMINATOR, 0x2d3b4d, 0x7faaf0),
            egg(StarveinConstructEntities.STARVEIN_CONSTRUCT_GUARDIAN, 0x3c515b, 0x73e4dc));

    private PathSpawnEggs() {}

    static void register() {}

    static void addCreativeItems(CreativeModeTab.Output output) {
        for (RegistryObject<Item> egg : EGGS) output.accept(egg.get());
    }

    private static <T extends Mob> RegistryObject<Item> egg(RegistryObject<EntityType<T>> type, int shell, int spots) {
        return StarRailSimMod.ITEMS.register(type.getId().getPath() + "_spawn_egg",
                () -> new ForgeSpawnEggItem(type, shell, spots, new Item.Properties()));
    }
}
