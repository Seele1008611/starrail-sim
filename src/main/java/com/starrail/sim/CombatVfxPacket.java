package com.starrail.sim;

import com.starrail.sim.client.StarRailCombatMesh;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/** One server-confirmed visual event; client geometry does not alter combat calculations. */
public record CombatVfxPacket(Kind kind, StarRailPath path, boolean ranged,
                              Vec3 origin, Vec3 impact, double floorY, int targetId, int sourceId) {
    public enum Kind {
        HUNT_STRIKE, HUNT_REVERSE, TOUGHNESS_HIT, TOUGHNESS_BREAK,
        PRESERVATION_WALL, PRESERVATION_COUNTER,
        DESTRUCTION_RAGE, DESTRUCTION_BURN, DESTRUCTION_DESPERATE,
        ERUDITION_ECHO, ERUDITION_FINAL,
        NIHILITY_MARK, NIHILITY_SPREAD, NIHILITY_END,
        HARMONY_RESONANCE, HARMONY_AFTERGLOW, HARMONY_CONCERT, HARMONY_UNISON,
        ABUNDANCE_HEAL, ABUNDANCE_MANNA, ABUNDANCE_MERCY,
        REMEMBRANCE_RECORD, REMEMBRANCE_ECHO, REMEMBRANCE_ETERNAL,
        ELATION_BURST, ELATION_GRAND;

        public StarRailPath path() {
            String prefix = name().substring(0, name().indexOf('_')).toLowerCase(java.util.Locale.ROOT);
            return StarRailPath.byId(prefix);
        }
        public boolean strong() {
            return this == HUNT_REVERSE || this == DESTRUCTION_DESPERATE || this == ERUDITION_FINAL
                    || this == NIHILITY_END || this == HARMONY_UNISON || this == ABUNDANCE_MERCY
                    || this == REMEMBRANCE_ETERNAL || this == ELATION_GRAND;
        }
    }

    public static void encode(CombatVfxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.kind);
        buffer.writeEnum(packet.path);
        buffer.writeBoolean(packet.ranged);
        writeVector(buffer, packet.origin);
        writeVector(buffer, packet.impact);
        buffer.writeDouble(packet.floorY);
        buffer.writeVarInt(packet.targetId);
        buffer.writeVarInt(packet.sourceId);
    }

    public static CombatVfxPacket decode(FriendlyByteBuf buffer) {
        return new CombatVfxPacket(buffer.readEnum(Kind.class), buffer.readEnum(StarRailPath.class),
                buffer.readBoolean(), readVector(buffer), readVector(buffer), buffer.readDouble(),
                buffer.readVarInt(), buffer.readVarInt());
    }

    private static void writeVector(FriendlyByteBuf buffer, Vec3 v) {
        buffer.writeDouble(v.x); buffer.writeDouble(v.y); buffer.writeDouble(v.z);
    }

    private static Vec3 readVector(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void handle(CombatVfxPacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> StarRailCombatMesh.accept(packet)));
        context.setPacketHandled(true);
    }
}
