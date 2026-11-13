package com.starrail.sim;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.fml.ModList;

final class RuinLootContainers {
    private record LootrApi(Class<?> inventory, Class<?> blockEntity, Method position,
                            Method table, Method id) {
        static LootrApi load() {
            if (!ModList.get().isLoaded("lootr")) return null;
            try {
                Class<?> inventory = Class.forName("noobanidus.mods.lootr.api.inventory.ILootrInventory");
                Class<?> block = Class.forName("noobanidus.mods.lootr.api.blockentity.ILootBlockEntity");
                return new LootrApi(inventory, block, inventory.getMethod("getPos"),
                        block.getMethod("getTable"), block.getMethod("getTileId"));
            } catch (ReflectiveOperationException | LinkageError error) {
                LogUtils.getLogger().warn("Could not load the Lootr 1.20.1 compatibility API", error);
                return null;
            }
        }
    }

    private static final class OptionalLootr {
        private static final LootrApi API = LootrApi.load();
    }

    static void forEachOpened(ServerPlayer player, AbstractContainerMenu menu,
                              Consumer<RandomizableContainerBlockEntity> action) {
        if (!(menu instanceof ChestMenu chestMenu)) return;
        var inventory = chestMenu.getContainer();
        var level = player.serverLevel();
        if (inventory instanceof RandomizableContainerBlockEntity chest) {
            action.accept(chest);
        } else if (inventory instanceof CompoundContainer doubleChest) {
            BlockPos origin = player.blockPosition();
            BlockPos.betweenClosedStream(origin.offset(-8, -4, -8), origin.offset(8, 4, 8))
                    .filter(level::hasChunkAt)
                    .map(level::getBlockEntity)
                    .filter(RandomizableContainerBlockEntity.class::isInstance)
                    .map(RandomizableContainerBlockEntity.class::cast)
                    .filter(doubleChest::contains)
                    .forEach(action);
        } else {
            LootrApi api = OptionalLootr.API;
            if (api == null || !api.inventory().isInstance(inventory)) return;
            BlockPos pos = invoke(api.position(), inventory, BlockPos.class);
            if (pos != null && level.hasChunkAt(pos)
                    && level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest
                    && api.blockEntity().isInstance(chest)) action.accept(chest);
        }
    }

    static ResourceLocation lootTable(RandomizableContainerBlockEntity chest) {
        LootrApi api = OptionalLootr.API;
        if (api != null && api.blockEntity().isInstance(chest)) {
            ResourceLocation table = invoke(api.table(), chest, ResourceLocation.class);
            if (table != null) return table;
        }
        String table = chest.saveWithFullMetadata().getString("LootTable");
        return table.isEmpty() ? null : ResourceLocation.tryParse(table);
    }

    static UUID personalLootId(RandomizableContainerBlockEntity chest) {
        LootrApi api = OptionalLootr.API;
        return api != null && api.blockEntity().isInstance(chest)
                ? invoke(api.id(), chest, UUID.class) : null;
    }

    private static <T> T invoke(Method method, Object receiver, Class<T> resultType) {
        try {
            return resultType.cast(method.invoke(receiver));
        } catch (ReflectiveOperationException error) {
            LogUtils.getLogger().warn("Could not read Lootr container data using {}", method.getName(), error);
            return null;
        }
    }
}
