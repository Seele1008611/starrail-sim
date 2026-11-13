package com.starrail.sim;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

/** Small shallow habitat patches placed inside the feature's own chunk, never under structures. */
final class KitchenRegionFeature extends Feature<NoneFeatureConfiguration> {
    private static final DeferredRegister<Feature<?>> FEATURES=DeferredRegister.create(Registries.FEATURE,StarRailSimMod.MOD_ID);
    static {FEATURES.register("kitchen_region_habitat",()->new KitchenRegionFeature(NoneFeatureConfiguration.CODEC));}
    static void register(IEventBus bus){FEATURES.register(bus);}
    KitchenRegionFeature(Codec<NoneFeatureConfiguration> c){super(c);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){
        var l=c.level();var r=c.random();var origin=c.origin();
        String biome=l.getBiome(origin.atY(l.getHeight(Heightmap.Types.WORLD_SURFACE_WG,origin.getX(),origin.getZ())))
                .unwrapKey().map(k->k.location().getPath()).orElse("");
        boolean coast=biome.equals("echo_tide_cliffs"),lotus=biome.equals("jadebranch_lotus_flats");
        if(!coast&&!lotus)return false;
        int x=(origin.getX()&~15)+4+r.nextInt(8),z=(origin.getZ()&~15)+4+r.nextInt(8);
        int y=l.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z)-1;
        if(y<l.getMinBuildHeight()+4||y>l.getMaxBuildHeight()-6)return false;
        var center=new BlockPos(x,y,z);
        // Create a two-deep water pocket with a sturdy native bed. This also gives fish connected source water.
        for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++){
            if(dx*dx+dz*dz>10)continue;BlockPos top=center.offset(dx,0,dz);
            if(Math.abs(l.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,top.getX(),top.getZ())-y)>3)continue;
            if(!l.getBlockState(top.below(2)).getFluidState().isEmpty())continue;
            l.setBlock(top.below(2),PathKitchenExpansion.block(coast?"tidal_stripe_rock":"jadebranch_mud").get().defaultBlockState(),2);
            l.setBlock(top.below(),Blocks.WATER.defaultBlockState(),2);l.setBlock(top,Blocks.WATER.defaultBlockState(),2);
            if(r.nextInt(4)==0)l.setBlock(top.below(),PathKitchenExpansion.block(coast?"edible_anemone_patch":"singing_lotus").get()
                    .defaultBlockState().setValue(BlockStateProperties.AGE_3,3),2);
            if(!coast&&r.nextInt(5)==0&&l.isEmptyBlock(top.above()))l.setBlock(top.above(),PathKitchenExpansion.block("lotus_lily_pad").get().defaultBlockState(),2);
        }
        for(int i=0;i<10;i++){
            int dx=r.nextInt(7)-3,dz=r.nextInt(7)-3;if(dx*dx+dz*dz<8)continue;
            BlockPos p=center.offset(dx,0,dz);
            if(!l.getBlockState(p).isAir()&&l.getBlockState(p.above()).getFluidState().isEmpty())
                l.setBlock(p,PathKitchenExpansion.block(coast?"renewable_tidepool":"lotus_gravel").get().defaultBlockState(),2);
        }
        return true;
    }
}
