package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import java.util.function.Supplier;

public record AttributeSourcesRequestPacket() {
    public static void encode(AttributeSourcesRequestPacket p,FriendlyByteBuf b) { }
    public static AttributeSourcesRequestPacket decode(FriendlyByteBuf b) {return new AttributeSourcesRequestPacket();}
    public static void handle(AttributeSourcesRequestPacket p,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();
        context.enqueueWork(()->{
            var player=context.getSender();
            if(player!=null && StarRailAttributeSources.allowRequest(player))
                StarRailNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(()->player),StarRailAttributeSources.snapshot(player));
        });context.setPacketHandled(true);
    }
}
