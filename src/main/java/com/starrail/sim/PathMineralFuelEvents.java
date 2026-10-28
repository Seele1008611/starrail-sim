package com.starrail.sim;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

/** Supplies coal-equivalent fuel values for the Path's starcoal resources. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PathMineralFuelEvents {
    private PathMineralFuelEvents() {
    }

    @SubscribeEvent
    public static void onFuelTime(FurnaceFuelBurnTimeEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.is(PathMineralContent.STARCOAL.get())) {
            event.setBurnTime(1600);
        } else if (stack.is(PathMineralContent.STARCOAL_BLOCK.get().asItem())) {
            event.setBurnTime(16000);
        } else {
            int woodBurnTime = woodBurnTime(stack);
            if (woodBurnTime > 0) event.setBurnTime(woodBurnTime);
        }
    }

    private static int woodBurnTime(ItemStack stack) {
        for (PathEcologyContent.WoodSet set : new PathEcologyContent.WoodSet[]{
                PathEcologyContent.SUNMEADOW, PathEcologyContent.STARLIGHTWOOD}) {
            if (is(stack, set.log(), set.wood(), set.strippedLog(), set.strippedWood(), set.planks(),
                    set.stairs(), set.fence(), set.fenceGate(), set.trapdoor(), set.button())) return 300;
            if (stack.is(set.slab().get().asItem())) return 150;
            if (stack.is(set.door().get().asItem())) return 200;
            if (stack.is(set.pressurePlate().get().asItem())) return 300;
            if (stack.is(set.sapling().get().asItem())) return 100;
        }
        return 0;
    }

    @SafeVarargs
    private static boolean is(ItemStack stack, RegistryObject<Block>... blocks) {
        for (RegistryObject<Block> block : blocks) {
            if (stack.is(block.get().asItem())) return true;
        }
        return false;
    }
}
