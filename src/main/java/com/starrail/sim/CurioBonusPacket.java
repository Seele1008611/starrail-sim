package com.starrail.sim;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record CurioBonusPacket(CompoundTag bonuses) {
    public static void encode(CurioBonusPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.bonuses());
    }

    public static CurioBonusPacket decode(FriendlyByteBuf buffer) {
        return new CurioBonusPacket(buffer.readNbt());
    }

    public static void handle(CurioBonusPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.starrail.sim.client.CurioBonusTooltips.update(packet.bonuses())));
        context.setPacketHandled(true);
    }
}
