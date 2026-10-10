package com.starrail.sim;

import com.starrail.sim.client.StarRailRuinBattleHud;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/** Server-owned battle display. A negative entity ID explicitly hides the panel. */
public record RuinBattlePacket(ResourceLocation dimension, int entityId, UUID entityUuid,
                               Component name, StarRailPath path, float health, float maxHealth,
                               float toughness, float maxToughness,
                               StarRailToughnessService.Phase phase, int remainingTicks) {
    public static RuinBattlePacket hidden(ResourceLocation dimension) {
        return new RuinBattlePacket(dimension, -1, new UUID(0, 0), Component.empty(),
                StarRailPath.NONE, 0, 0, 0, 0, StarRailToughnessService.Phase.NORMAL, 0);
    }

    public static void encode(RuinBattlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.entityId);
        if (packet.entityId < 0) return;
        buffer.writeUUID(packet.entityUuid);
        buffer.writeComponent(packet.name);
        buffer.writeEnum(packet.path);
        buffer.writeFloat(packet.health);
        buffer.writeFloat(packet.maxHealth);
        buffer.writeFloat(packet.toughness);
        buffer.writeFloat(packet.maxToughness);
        buffer.writeEnum(packet.phase);
        buffer.writeVarInt(packet.remainingTicks);
    }

    public static RuinBattlePacket decode(FriendlyByteBuf buffer) {
        ResourceLocation dimension = buffer.readResourceLocation();
        int id = buffer.readVarInt();
        if (id < 0) return hidden(dimension);
        return new RuinBattlePacket(dimension, id, buffer.readUUID(), buffer.readComponent(),
                buffer.readEnum(StarRailPath.class), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat(), buffer.readFloat(), buffer.readEnum(StarRailToughnessService.Phase.class),
                buffer.readVarInt());
    }

    public static void handle(RuinBattlePacket packet, Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> StarRailRuinBattleHud.accept(packet)));
        context.setPacketHandled(true);
    }
}
