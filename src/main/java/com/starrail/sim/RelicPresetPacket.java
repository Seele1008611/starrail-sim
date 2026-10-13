package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record RelicPresetPacket(int slot,StarRailRelicPresetService.Operation operation) {
    public static void encode(RelicPresetPacket p,FriendlyByteBuf b){b.writeVarInt(p.slot);b.writeEnum(p.operation);}
    public static RelicPresetPacket decode(FriendlyByteBuf b){return new RelicPresetPacket(b.readVarInt(),b.readEnum(StarRailRelicPresetService.Operation.class));}
    public static void handle(RelicPresetPacket p,Supplier<NetworkEvent.Context> supplier){
        var context=supplier.get();context.enqueueWork(()->{
            ServerPlayer player=context.getSender();if(player==null||!StarRailRelicPresetService.allow(player))return;
            var result=StarRailRelicPresetService.perform(player,p.slot,p.operation);
            StarRailNetwork.sendRelicSnapshot(player,"message.starrail_sim.relic."+result.key(),result.accepted());
        });context.setPacketHandled(true);
    }
}
