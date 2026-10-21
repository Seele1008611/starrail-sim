package com.starrail.sim;

import com.starrail.sim.client.StarRailPassPickerClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.function.Supplier;

/** Opens the path-specific light-cone selector after the server validates a Special Pass. */
public record OpenLightConePassPickerPacket(StarRailPath path, InteractionHand hand) {
    public static void encode(OpenLightConePassPickerPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.path());
        buffer.writeEnum(packet.hand());
    }

    public static OpenLightConePassPickerPacket decode(FriendlyByteBuf buffer) {
        return new OpenLightConePassPickerPacket(buffer.readEnum(StarRailPath.class),
                buffer.readEnum(InteractionHand.class));
    }

    public static void handle(OpenLightConePassPickerPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                StarRailPassPickerClient.open(packet.path(), packet.hand())));
        context.setPacketHandled(true);
    }
}
