package com.starrail.sim;

import com.starrail.sim.client.MonsterCombatEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** One completed server action. Windups instead read the existing synchronized attack state. */
public record MonsterCombatPacket(Kind kind, ResourceLocation dimension, long tick, Vec3 point, Vec3 normal, double radius, int color, int sourceId) {
    public enum Kind { DISTORTER_PULSE, CONFIRMED_HIT, STOMP, BOLT_RELEASE, BOLT_IMPACT, GUARDIAN_SWEEP, GUARDIAN_WAVE }
    public MonsterCombatPacket(Kind kind,ResourceLocation dimension,long tick,Vec3 point) {
        this(kind,dimension,tick,point,new Vec3(0,1,0),0,0x9857f5,-1);
    }
    public static void encode(MonsterCombatPacket p, FriendlyByteBuf b) {
        b.writeEnum(p.kind); b.writeResourceLocation(p.dimension); b.writeLong(p.tick);
        b.writeDouble(p.point.x); b.writeDouble(p.point.y); b.writeDouble(p.point.z);
        b.writeDouble(p.normal.x); b.writeDouble(p.normal.y); b.writeDouble(p.normal.z);
        b.writeDouble(p.radius);b.writeInt(p.color);b.writeVarInt(p.sourceId);
    }
    public static MonsterCombatPacket decode(FriendlyByteBuf b) {
        return new MonsterCombatPacket(b.readEnum(Kind.class),b.readResourceLocation(),b.readLong(),
                new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),
                new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readDouble(),b.readInt(),b.readVarInt());
    }
    public static void handle(MonsterCombatPacket p, Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();
        context.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->MonsterCombatEffects.accept(p)));
        context.setPacketHandled(true);
    }
}
