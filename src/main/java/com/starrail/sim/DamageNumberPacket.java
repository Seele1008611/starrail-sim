package com.starrail.sim;

import com.starrail.sim.client.StarRailDamageNumbers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server-to-client combat number packet. */
public final class DamageNumberPacket {
    private final int targetId;
    private final float amount;
    private final boolean critical;
    private final boolean toughness;

    public DamageNumberPacket(int targetId, float amount, boolean critical) {
        this(targetId, amount, critical, false);
    }

    public DamageNumberPacket(int targetId, float amount, boolean critical,
                              boolean toughness) {
        this.targetId = targetId;
        this.amount = amount;
        this.critical = critical;
        this.toughness = toughness;
    }

    public static void encode(DamageNumberPacket message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.targetId);
        buffer.writeFloat(message.amount);
        buffer.writeBoolean(message.critical);
        buffer.writeBoolean(message.toughness);
    }

    public static DamageNumberPacket decode(FriendlyByteBuf buffer) {
        return new DamageNumberPacket(
                buffer.readInt(), buffer.readFloat(), buffer.readBoolean(),
                buffer.readBoolean());
    }

    public static void handle(DamageNumberPacket message,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> StarRailDamageNumbers.add(
                message.targetId, message.amount, message.critical, message.toughness));
        context.setPacketHandled(true);
    }
}
