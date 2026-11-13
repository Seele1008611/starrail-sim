package com.starrail.sim;
import com.starrail.sim.client.ActiveSkillClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public record ActiveSkillFxPacket(StarRailPath path,ResourceLocation dimension,long tick,Vec3 from,Vec3 to,int sourceId,boolean hit,int stage,int targetId) {
    public ActiveSkillFxPacket(StarRailPath path,ResourceLocation dimension,long tick,Vec3 from,Vec3 to,int sourceId,boolean hit) {this(path,dimension,tick,from,to,sourceId,hit,0,-1);}
    public ActiveSkillFxPacket(StarRailPath path,ResourceLocation dimension,long tick,Vec3 from,Vec3 to,int sourceId,boolean hit,int stage) {this(path,dimension,tick,from,to,sourceId,hit,stage,-1);}
    public static void encode(ActiveSkillFxPacket p,FriendlyByteBuf b) {b.writeEnum(p.path);b.writeResourceLocation(p.dimension);b.writeLong(p.tick);b.writeDouble(p.from.x);b.writeDouble(p.from.y);b.writeDouble(p.from.z);b.writeDouble(p.to.x);b.writeDouble(p.to.y);b.writeDouble(p.to.z);b.writeVarInt(p.sourceId);b.writeBoolean(p.hit);b.writeVarInt(p.stage);b.writeVarInt(p.targetId);}
    public static ActiveSkillFxPacket decode(FriendlyByteBuf b) {return new ActiveSkillFxPacket(b.readEnum(StarRailPath.class),b.readResourceLocation(),b.readLong(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readVarInt(),b.readBoolean(),b.readVarInt(),b.readVarInt());}
    public static void handle(ActiveSkillFxPacket p,Supplier<NetworkEvent.Context> supplier) {var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ActiveSkillClient.effect(p)));c.setPacketHandled(true);}
}
