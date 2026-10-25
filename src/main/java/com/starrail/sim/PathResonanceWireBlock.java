package com.starrail.sim;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** Resonance crystal wire with vanilla redstone wire behavior and connection rules. */
public final class PathResonanceWireBlock extends RedStoneWireBlock {
    private static final ParticleOptions RESONANCE_DUST = new DustParticleOptions(
            new Vector3f(0.20F, 0.82F, 0.86F), 1.0F);

    public PathResonanceWireBlock() {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .instabreak()
                .pushReaction(PushReaction.DESTROY));
    }

    /** Match vanilla redstone wire, which also connects where the side is unspecified. */
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
                                      @Nullable Direction direction) {
        return true;
    }

    /** Keep vanilla's powered-wire particle cadence while using resonance crystal colors. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWER) != 0 && random.nextInt(10) == 0) {
            level.addParticle(RESONANCE_DUST,
                    pos.getX() + random.nextDouble(), pos.getY() + 0.0625D,
                    pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
        }
    }
}
