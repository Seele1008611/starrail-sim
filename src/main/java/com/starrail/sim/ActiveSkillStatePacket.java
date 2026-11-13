package com.starrail.sim;
import com.starrail.sim.client.ActiveSkillClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;
public record ActiveSkillStatePacket(UUID player,ResourceLocation dimension,StarRailPath path,int rank,int cooldown,float shield,int duration,int activeTicks,int charges,float recordedDamage) {
    public ActiveSkillStatePacket(UUID player,ResourceLocation dimension,StarRailPath path,int rank,int cooldown,float shield,int duration) {this(player,dimension,path,rank,cooldown,shield,duration,0,0,0);}
    public static void encode(ActiveSkillStatePacket p,FriendlyByteBuf b) {b.writeUUID(p.player);b.writeResourceLocation(p.dimension);b.writeEnum(p.path);b.writeVarInt(p.rank);b.writeVarInt(p.cooldown);b.writeFloat(p.shield);b.writeVarInt(p.duration);b.writeVarInt(p.activeTicks);b.writeVarInt(p.charges);b.writeFloat(p.recordedDamage);}
    public static ActiveSkillStatePacket decode(FriendlyByteBuf b) {return new ActiveSkillStatePacket(b.readUUID(),b.readResourceLocation(),b.readEnum(StarRailPath.class),b.readVarInt(),b.readVarInt(),b.readFloat(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readFloat());}
    public static void handle(ActiveSkillStatePacket p,Supplier<NetworkEvent.Context> supplier) {var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ActiveSkillClient.state(p)));c.setPacketHandled(true);}
}
