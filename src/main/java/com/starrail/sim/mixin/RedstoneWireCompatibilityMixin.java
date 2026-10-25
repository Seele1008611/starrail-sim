package com.starrail.sim.mixin;

import com.starrail.sim.PathResonanceWireBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets resonance wire share vanilla redstone wire networks and signal attenuation. */
@Mixin(RedStoneWireBlock.class)
public abstract class RedstoneWireCompatibilityMixin {
    @Inject(
            method = "shouldConnectTo(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void starrail$connectToResonanceWire(BlockState state, @Nullable Direction direction,
                                                        CallbackInfoReturnable<Boolean> callbackInfo) {
        if (state.getBlock() instanceof PathResonanceWireBlock) {
            callbackInfo.setReturnValue(true);
        }
    }

    @Inject(
            method = "getWireSignal(Lnet/minecraft/world/level/block/state/BlockState;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void starrail$readSignalFromEitherWire(BlockState state,
                                                   CallbackInfoReturnable<Integer> callbackInfo) {
        if (state.getBlock() instanceof RedStoneWireBlock) {
            callbackInfo.setReturnValue(state.getValue(RedStoneWireBlock.POWER));
        }
    }
}
