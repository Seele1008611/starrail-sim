package com.starrail.sim;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
/** Contains no client-chosen path, target, damage, or cooldown. */
public record ActiveSkillRequestPacket() {
    public static void encode(ActiveSkillRequestPacket p,FriendlyByteBuf b) {}
    public static ActiveSkillRequestPacket decode(FriendlyByteBuf b) {return new ActiveSkillRequestPacket();}
    public static void handle(ActiveSkillRequestPacket p,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();context.enqueueWork(()->{if(context.getSender()!=null)ActivePathSkills.cast(context.getSender());});context.setPacketHandled(true);
    }
}
