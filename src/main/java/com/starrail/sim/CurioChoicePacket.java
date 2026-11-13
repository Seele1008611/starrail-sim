package com.starrail.sim;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record CurioChoicePacket(UUID id, ItemStack first, ItemStack second) {
    public static void encode(CurioChoicePacket p, FriendlyByteBuf b) {
        b.writeUUID(p.id);
        b.writeItem(p.first);
        b.writeItem(p.second);
    }

    public static CurioChoicePacket decode(FriendlyByteBuf b) {
        return new CurioChoicePacket(b.readUUID(), b.readItem(), b.readItem());
    }

    public static void handle(CurioChoicePacket p, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.starrail.sim.client.CurioChoiceScreen.open(p)));
        context.setPacketHandled(true);
    }
}
