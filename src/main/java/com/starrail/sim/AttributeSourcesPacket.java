package com.starrail.sim;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** A bounded, owner-only snapshot. Coefficients retain their vanilla operations. */
public record AttributeSourcesPacket(UUID owner, List<Row> rows) {
    public static final int COUNT=13, SOURCES=8;
    public record Group(double addition,double basePercent,double totalFactor) { }
    public record Row(double base,double total,double raw,List<Group> groups) { }
    public static void encode(AttributeSourcesPacket p,FriendlyByteBuf b) {
        b.writeUUID(p.owner);
        for(Row row:p.rows) {
            b.writeDouble(row.base);b.writeDouble(row.total);b.writeDouble(row.raw);
            for(Group g:row.groups){b.writeDouble(g.addition);b.writeDouble(g.basePercent);b.writeDouble(g.totalFactor);}
        }
    }
    public static AttributeSourcesPacket decode(FriendlyByteBuf b) {
        UUID owner=b.readUUID();var rows=new java.util.ArrayList<Row>();
        for(int i=0;i<COUNT;i++) {
            double base=b.readDouble(),total=b.readDouble(),raw=b.readDouble();
            var groups=new java.util.ArrayList<Group>();
            for(int j=0;j<SOURCES;j++)groups.add(new Group(b.readDouble(),b.readDouble(),b.readDouble()));
            rows.add(new Row(base,total,raw,List.copyOf(groups)));
        }
        return new AttributeSourcesPacket(owner,List.copyOf(rows));
    }
    public static void handle(AttributeSourcesPacket p,Supplier<NetworkEvent.Context> supplier) {
        var context=supplier.get();
        context.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                ()->()->com.starrail.sim.client.StarRailAttributeScreen.accept(p)));
        context.setPacketHandled(true);
    }
}
