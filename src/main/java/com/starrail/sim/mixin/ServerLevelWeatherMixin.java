package com.starrail.sim.mixin;

import com.starrail.sim.StarRailDimensionIds;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The Path uses DerivedLevelData, whose weather setters intentionally do nothing.
 * Forward weather commands to the shared primary weather state so vanilla's normal
 * per-level weather tick can synchronize the change to players in this dimension.
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelWeatherMixin {
    @Inject(method = "setWeatherParameters(IIZZ)V", at = @At("HEAD"), cancellable = true)
    private void starrail$forwardPathWeatherToPrimaryLevel(int clearWeatherTime, int weatherTime,
                                                            boolean raining, boolean thundering,
                                                            CallbackInfo callbackInfo) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (!self.dimension().equals(StarRailDimensionIds.THE_PATH)) return;

        self.getServer().overworld().setWeatherParameters(
                clearWeatherTime, weatherTime, raining, thundering);
        callbackInfo.cancel();
    }
}
