package com.starrail.sim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

final class PathFishingLoot extends LootModifier {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, StarRailSimMod.MOD_ID);
    private static final Codec<PathFishingLoot> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, PathFishingLoot::new));
    private static final ResourceLocation SNOWFIELD = new ResourceLocation(StarRailSimMod.MOD_ID, "mirrorfrost_snowfield");

    static { SERIALIZERS.register("snowfield_fishing", () -> CODEC); }

    static void register(IEventBus bus) { SERIALIZERS.register(bus); }

    private PathFishingLoot(LootItemCondition[] conditions) { super(conditions); }

    @Override public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof FishingHook hook)
                || !context.getLevel().dimension().equals(StarRailDimensionIds.THE_PATH)
                ) return loot;
        boolean snow=context.getLevel().getBiome(hook.blockPosition()).is(SNOWFIELD);
        boolean coast=context.getLevel().getBiome(hook.blockPosition()).is(new ResourceLocation(StarRailSimMod.MOD_ID,"echo_tide_cliffs"));
        if(!snow&&!coast)return loot;
        loot.replaceAll(stack -> stack.is(ItemTags.FISHES)
                ? new ItemStack(snow?PathKitchen.FROST_SALMON.get():PathKitchenExpansion.food("duskred_fish").get(), stack.getCount()) : stack);
        return loot;
    }
}
