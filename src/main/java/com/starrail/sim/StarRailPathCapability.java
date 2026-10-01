package com.starrail.sim;

/**
 * 模组代码说明：把命途数据能力附加到玩家，并负责创建、保存和恢复该能力的数据。
 */

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Capability registration, attachment, and player clone handling. */
@Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class StarRailPathCapability {
    public static final Capability<IStarRailPathData> PATH_DATA =
            CapabilityManager.get(new net.minecraftforge.common.capabilities.CapabilityToken<>() {
            });

    private StarRailPathCapability() {
    }

    @SubscribeEvent
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IStarRailPathData.class);
    }

    /** Forge event subscribers for entity capabilities live on the FORGE bus. */
    @Mod.EventBusSubscriber(modid = StarRailSimMod.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void attach(AttachCapabilitiesEvent<Entity> event) {
            if (event.getObject() instanceof Player) {
                event.addCapability(
                        new ResourceLocation(StarRailSimMod.MOD_ID, "path_data"),
                        new Provider());
            }
        }

        @SubscribeEvent
        public static void clone(PlayerEvent.Clone event) {
            event.getOriginal().reviveCaps();
            event.getOriginal().getCapability(PATH_DATA).ifPresent(oldData ->
                    event.getEntity().getCapability(PATH_DATA).ifPresent(newData ->
                            newData.copyFrom(oldData)));
            event.getOriginal().invalidateCaps();
        }
    }

    private static final class Provider implements ICapabilityProvider,
            net.minecraftforge.common.capabilities.ICapabilitySerializable<CompoundTag> {
        private final IStarRailPathData data = new StarRailPathData();
        private final LazyOptional<IStarRailPathData> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
            return capability == PATH_DATA ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.deserializeNBT(tag);
        }

    }
}
